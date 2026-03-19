package com.dkgeneric.filenet.content.provider;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import com.dkgeneric.commons.exceptions.InvalidRequestException;
import com.dkgeneric.filenet.content.common.ECMConstants;
import com.dkgeneric.filenet.content.common.ServiceException;
import com.dkgeneric.filenet.content.exceptioncodes.DocumentServiceExceptionCodes;
import com.dkgeneric.filenet.content.exceptioncodes.P8ExceptionCodes;
import com.dkgeneric.filenet.content.model.ClassDefinition;
import com.dkgeneric.filenet.content.model.IResultSetProcessor;
import com.dkgeneric.filenet.content.model.KeyValuePair;
import com.dkgeneric.filenet.content.model.P8AnnotationObject;
import com.dkgeneric.filenet.content.model.P8ContentObject;
import com.dkgeneric.filenet.content.model.P8ContentObjectAuditInfo;
import com.dkgeneric.filenet.content.model.P8Object;
import com.dkgeneric.filenet.content.model.P8ObjectAuditInfo;
import com.dkgeneric.filenet.content.model.P8Properties;
import com.dkgeneric.filenet.content.model.P8ResultSet;
import com.dkgeneric.filenet.content.model.P8RowResultSet;
import com.dkgeneric.filenet.content.model.PropertyAuditInfo;
import com.dkgeneric.filenet.content.model.SearchParameters;
import com.dkgeneric.filenet.content.request.CopyDocumentRequest;
import com.dkgeneric.filenet.content.resources.P8ContentResource;
import com.dkgeneric.filenet.content.response.CopyDocumentResponse;
import com.dkgeneric.filenet.content.service.AuthService;
import com.filenet.api.collection.ContentElementList;
import com.filenet.api.collection.IndependentObjectSet;
import com.filenet.api.collection.PageIterator;
import com.filenet.api.collection.RepositoryRowSet;
import com.filenet.api.constants.AutoClassify;
import com.filenet.api.constants.CheckinType;
import com.filenet.api.constants.RefreshMode;
import com.filenet.api.constants.ReservationType;
import com.filenet.api.core.Annotation;
import com.filenet.api.core.Containable;
import com.filenet.api.core.ContentTransfer;
import com.filenet.api.core.CustomObject;
import com.filenet.api.core.Document;
import com.filenet.api.core.Factory;
import com.filenet.api.core.IndependentObject;
import com.filenet.api.core.IndependentlyPersistableObject;
import com.filenet.api.core.VersionSeries;
import com.filenet.api.exception.EngineRuntimeException;
import com.filenet.api.exception.ExceptionCode;
import com.filenet.api.property.FilterElement;
import com.filenet.api.property.Properties;
import com.filenet.api.property.Property;
import com.filenet.api.property.PropertyEngineObject;
import com.filenet.api.property.PropertyFilter;
import com.filenet.api.property.PropertyIndependentObjectSet;
import com.filenet.api.query.RepositoryRow;
import com.filenet.api.query.SearchSQL;
import com.filenet.api.query.SearchScope;
import com.filenet.api.util.Id;

import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

/**
 * This class provides set of methods to communicate with FileNet
 */
@Slf4j
@Getter
@Setter
public class P8ProviderImpl extends P8ProviderBase {

	@SuppressWarnings({ "unchecked", "rawtypes" })
	public static KeyValuePair convert(Property property) {
		KeyValuePair result = new KeyValuePair();
		result.setKey(property.getPropertyName());
		Object value = property.getObjectValue();
		if (value instanceof List aList) {
			ArrayList list = new ArrayList();
			for (Object o : aList)
				list.add(o);
			value = list;
		}
		result.setValue(value);
		return result;
	}

	public static P8Properties createP8Properties(RepositoryRow repositoryRow) {
		P8Properties p8Properties = new P8Properties();
		if (repositoryRow != null) {
			Properties properties = repositoryRow.getProperties();
			for (@SuppressWarnings("unchecked")
			Iterator<Property> propertyIterator = properties.iterator(); propertyIterator.hasNext();) {
				Property p = propertyIterator.next();
				// do not populate p8 object property types
				if (!(p instanceof PropertyEngineObject || p instanceof PropertyIndependentObjectSet)) {
					KeyValuePair kvp = convert(p);
					p8Properties.addProperty(kvp.getKey(), kvp.getValue());
				}
			}
		}
		return p8Properties;

	}

	/**
	 * Populate properties from P8 API object to {@link P8Object}
	 *
	 * @param independentObject the independent object
	 * @param p8Object          this library {@link P8Object}
	 */
	public static void populateProperties(IndependentObject independentObject, P8Object p8Object) {
		p8Object.setDocumentClass(independentObject.getClassName());
		Properties properties = independentObject.getProperties();
		if (properties.isPropertyPresent(ECMConstants.P8_ID_PROPERTYNAME))
			p8Object.setId(((Containable) independentObject).get_Id().toString());
		for (@SuppressWarnings("unchecked")
		Iterator<Property> propertyIterator = properties.iterator(); propertyIterator.hasNext();) {
			Property p = propertyIterator.next();
			// do not populate p8 object property types
			if (!(p instanceof PropertyEngineObject || p instanceof PropertyIndependentObjectSet)) {
				KeyValuePair kvp = convert(p);
				p8Object.getProperties().addProperty(kvp.getKey(), kvp.getValue());
			}
		}
	}

	/**
	 * Creates a new instance
	 */
	protected P8ProviderImpl() {
	}

	/**
	 * Creates a new instance
	 *
	 * @param authService the AuthService. This parameter contains information about
	 *                    FileNet connection. Proxy user will be used for
	 *                    authorization
	 * @throws ServiceException Signals that Filenet access info is not configured correctly
	 */
	public P8ProviderImpl(AuthService authService) throws ServiceException {
		this(authService, null, null);
	}

	/**
	 * Instantiates a new p 8 provider impl.
	 *
	 * @param authService the AuthService. This parameter contains information about
	 *                    FileNet connection.
	 * @param userName    the user name to be used for FileNet authorization
	 * @param password    the password to be used for FileNet authorization
	 * @throws ServiceException Signals that Filenet access info is not configured correctly
	 */
	public P8ProviderImpl(AuthService authService, String userName, String password) throws ServiceException {
		this.authService = authService;
		maxResultSetSize = authService.getClientConfig().getMaxResultSetSize();
		searchTimeLimit = authService.getClientConfig().getSearchTimeLimit();
		connection = this.authService.getConnection(userName, password);
	}

	public void addContent(Document document, P8ContentObject p8ContentObject, P8ContentObjectAuditInfo auditInfo)
			throws ServiceException {
		P8ContentResource resource = p8ContentObject.getResource();
		if (resource != null) {
			InputStream stream = resource.getInputStream();
			populateContentElement(document, resource, stream);
		}
		if (auditInfo != null) {
			auditInfo.setContentResource(resource);
		}
	}

	/**
	 * Copy document.
	 *
	 * @param copyDocumentRequest the copy document request
	 * @param auditInfo the audit info
	 * @return the copy document response
	 * @throws ServiceException the service exception
	 */
	public CopyDocumentResponse copyDocument(CopyDocumentRequest copyDocumentRequest,
			P8ContentObjectAuditInfo auditInfo) throws ServiceException {
		CopyDocumentResponse response = new CopyDocumentResponse();
		// retrieve source document
		createUpdatingBatch();
		Document sourceDocument = getDocumentById(copyDocumentRequest.getSourceDocumentId());
		for (P8ContentObject p8ContentObject : copyDocumentRequest.getP8ContentObjectList()) {
			// calculate class name
			String docClass = StringUtils.hasText(p8ContentObject.getDocumentClass())
					? p8ContentObject.getDocumentClass()
					: sourceDocument.getClassName();
			p8ContentObject.setDocumentClass(docClass);
			preprocessProperties(p8ContentObject);
			// create new instance
			Id id = Id.createId();
			Document document = createDocumentInMemory(p8ContentObject, id, auditInfo);
			// copy requested source document properties except already added
			Properties sourceProperties = sourceDocument.getProperties();
			for (String propertyName : copyDocumentRequest.getPropertyNamesToCopy())
				if (sourceProperties.isPropertyPresent(propertyName)
						&& !p8ContentObject.getProperties().containsKey(propertyName))
					document.getProperties().putObjectValue(propertyName,
							sourceProperties.getObjectValue(propertyName));

			if (copyDocumentRequest.getCopyPostProcessor() != null)
				copyDocumentRequest.getCopyPostProcessor().postProcess(sourceDocument, document, copyDocumentRequest);
			document.checkin(AutoClassify.DO_NOT_AUTO_CLASSIFY, CheckinType.MAJOR_VERSION);
			addToUpdateBatch(document);
			log.debug("copyDocument.Exit new document id = {}", id);
			response.getP8DocumentIdList().add(id.toString());
		}
		commitUpdateBatch();
		return response;
	}

	/**
	 * Creates the FileNet document.
	 *
	 * @param p8ContentObject data to create FileNet document
	 * @return the new FileNet document
	 * @throws IOException Signals that an I/O exception has occurred.
	 * @throws ServiceException Signals that Filenet access info is not configured correctly or p8ContentObject has invalid resource
	 */
	public Annotation createAnnotation(P8AnnotationObject p8AnnotationObject) throws IOException, ServiceException {
		Annotation annotation = createAnnotationInMemory(p8AnnotationObject, null);
		annotation.save(RefreshMode.REFRESH);
		return annotation;
	}

	private Annotation createAnnotationInMemory(P8AnnotationObject p8AnnotationObject,
			P8ContentObjectAuditInfo auditInfo) throws ServiceException {
		Annotation annotation = Factory.Annotation.createInstance(getConnectionObjectStore(),
				p8AnnotationObject.getP8AnnotationClassName());
		P8ContentResource resource = p8AnnotationObject.getResource();
		List<PropertyAuditInfo> pAuditInfos = populateProperties(annotation.getProperties(),
				p8AnnotationObject.getProperties(), true);
		if (resource != null) {
			InputStream stream = resource.getInputStream();
			populateContentElement(annotation, resource, stream);
			annotation.set_AnnotatedObject(Factory.Document.fetchInstance(getConnectionObjectStore(),
					new Id(p8AnnotationObject.getP8AnnotedObject().getId()), null));
		}
		if (auditInfo != null) {
			auditInfo.setObjectId(p8AnnotationObject.getP8AnnotedObject().getId());
			auditInfo.setObjectClass(p8AnnotationObject.getDocumentClass());
			auditInfo.setModifiedProperties(pAuditInfos);
			auditInfo.setContentResource(resource);
		}
		return annotation;
	}

	/**
	 * Creates the FileNet custom object.
	 *
	 * @param p8Object data to create FileNet  custom object.
	 * @return the new FileNet  custom object.
	 * @throws IOException Signals that an I/O exception has occurred.
	 * @throws ServiceException Signals that Filenet access info is not configured correctly
	 */
	public CustomObject createCustomObject(P8Object p8Object) throws IOException, ServiceException {
		return createCustomObject(p8Object, null);
	}

	/**
	 * Creates the FileNet custom object.
	 *
	 * @param p8Object data to create FileNet  custom object.
	 * @param auditInfo audit data (changed properties, etc.) can be null
	 * @return the new FileNet  custom object.
	 * @throws IOException Signals that an I/O exception has occurred.
	 * @throws ServiceException Signals that Filenet access info is not configured correctly
	 */
	public CustomObject createCustomObject(P8Object p8Object, P8ContentObjectAuditInfo auditInfo)
			throws IOException, ServiceException {
		Id id = Id.createId();
		log.debug("createCustomObject.Entry {}", p8Object);
		preprocessProperties(p8Object);
		CustomObject customObject = createCustomObjectInMemory(p8Object, id, auditInfo);
		if (updatingBatch != null)
			addToUpdateBatch(customObject);
		else
			customObject.save(RefreshMode.REFRESH);
		log.debug("createCustomObject.Exit new customObject id = {}", id);
		return customObject;
	}

	public CustomObject createCustomObjectInMemory(P8Object p8Object, Id id, P8ContentObjectAuditInfo auditInfo)
			throws ServiceException {
		CustomObject customObject = Factory.CustomObject.createInstance(getConnectionObjectStore(),
				p8Object.getDocumentClass(), id);
		List<PropertyAuditInfo> pAuditInfos = populateProperties(customObject.getProperties(), p8Object.getProperties(),
				true);
		if (auditInfo != null) {
			auditInfo.setObjectId(id.toString());
			auditInfo.setObjectClass(customObject.getClassName());
			auditInfo.setModifiedProperties(pAuditInfos);
		}
		return customObject;
	}

	/**
	 * Creates the FileNet document.
	 *
	 * @param p8ContentObject data to create FileNet document
	 * @return the new FileNet document
	 * @throws IOException Signals that an I/O exception has occurred.
	 * @throws ServiceException Signals that Filenet access info is not configured correctly or p8ContentObject has invalid resource
	 */
	public Document createDocument(P8ContentObject p8ContentObject) throws IOException, ServiceException {
		return createDocument(p8ContentObject, null);
	}

	/**
	 * Creates the FileNet document.
	 *
	 * @param p8ContentObject data to create FileNet document
	 * @param auditInfo audit data (changed properties, content info, etc.) can be null
	 * @return the new FileNet document
	 * @throws IOException Signals that an I/O exception has occurred.
	 * @throws ServiceException Signals that Filenet access info is not configured correctly or p8ContentObject has invalid resource
	 */
	public Document createDocument(P8ContentObject p8ContentObject, P8ContentObjectAuditInfo auditInfo)
			throws IOException, ServiceException {
		Id id = Id.createId();
		log.debug("createDocument.Entry {}", p8ContentObject);
		preprocessProperties(p8ContentObject);
		Document document = createDocumentInMemory(p8ContentObject, id, auditInfo);
		document.checkin(AutoClassify.DO_NOT_AUTO_CLASSIFY, CheckinType.MAJOR_VERSION);
		if (updatingBatch != null)
			addToUpdateBatch(document);
		else
			document.save(RefreshMode.REFRESH);
		log.debug("createDocument.Exit new document id = {}", id);
		return document;
	}

	public Document createDocumentInMemory(P8ContentObject p8ContentObject, Id id, P8ContentObjectAuditInfo auditInfo)
			throws ServiceException {
		Document document = Factory.Document.createInstance(getConnectionObjectStore(),
				p8ContentObject.getDocumentClass(), id);
		List<PropertyAuditInfo> pAuditInfos = populateProperties(document.getProperties(),
				p8ContentObject.getProperties(), true);
		addContent(document, p8ContentObject, auditInfo);
		if (auditInfo != null) {
			auditInfo.setObjectId(id.toString());
			auditInfo.setObjectClass(document.getClassName());
			auditInfo.setModifiedProperties(pAuditInfos);
		}
		return document;
	}

	/**
	 * Creates the new FileNet document version.
	 *
	 * @param p8ContentObject data to create FileNet document
	 * @param p8ObjectId filenet source object id (Document or VesrsionSeries id)
	 * @param saveAsMajorVersion flag to control version type. If true Major version will be created otherwise Minor
	 * @param auditInfo audit data (changed properties, content info, etc.) can be null
	 * @return the new FileNet document
	 * @throws IOException Signals that an I/O exception has occurred.
	 * @throws ServiceException Signals that Filenet access info is not configured correctly or p8ContentObject has invalid resource
	 */
	public Document createDocumentVersion(P8ContentObject p8ContentObject, String p8ObjectId,
			boolean saveAsMajorVersion, P8ContentObjectAuditInfo auditInfo) throws IOException, ServiceException {
		VersionSeries vs = null;
		log.debug("createDocumentVersion.Entry p8ContentObject: {}, p8ObjectId: {}, saveAsMAjorVersion: {}",
				p8ContentObject, p8ObjectId, saveAsMajorVersion);
		try {
			vs = getDocumentById(p8ObjectId, "ID,VersionSeries").get_VersionSeries();
		} catch (EngineRuntimeException e) {
			ExceptionCode exceptionCode = e.getExceptionCode();
			if (exceptionCode == ExceptionCode.E_OBJECT_NOT_FOUND)
				vs = Factory.VersionSeries.fetchInstance(objectStore, new Id(p8ObjectId), null);
			else
				throw e;
		}
		if (vs.get_IsReserved().booleanValue()) {
			Document reservation = (Document) vs.get_Reservation();
			throw new ServiceException(DocumentServiceExceptionCodes.DOCUMENT_ALREADY_CHECKED_OUT.getExceptionCode(),
					new Object[] { p8ObjectId, reservation.get_Creator(), reservation.get_DateCreated() });
		}
		Document document = (Document) vs.get_CurrentVersion();
		if (StringUtils.hasText(p8ContentObject.getDocumentClass())
				&& !p8ContentObject.getDocumentClass().equalsIgnoreCase(document.getClassName()))
			throw new InvalidRequestException("Requested document class name: " + p8ContentObject.getDocumentClass()
					+ " doesn't match base document class name " + document.getClassName());
		else
			p8ContentObject.setDocumentClass(document.getClassName());
		preprocessProperties(p8ContentObject);
		document.checkout(ReservationType.EXCLUSIVE, null, null, null);
		document.save(RefreshMode.NO_REFRESH);
		vs.refresh();

		document = (Document) vs.get_Reservation();
		try {
			List<PropertyAuditInfo> pAuditInfos = populateProperties(document.getProperties(),
					p8ContentObject.getProperties(), false);
			addContent(document, p8ContentObject, auditInfo);
			document.checkin(AutoClassify.DO_NOT_AUTO_CLASSIFY, CheckinType.MAJOR_VERSION);
			document.save(RefreshMode.REFRESH);
			if (auditInfo != null) {
				auditInfo.setModifiedProperties(pAuditInfos);
				auditInfo.setObjectId(document.get_Id().toString());
				auditInfo.setObjectClass(document.getClassName());
			}
		} catch (EngineRuntimeException ere) {
			document.refresh();
			document.delete();
			document.save(RefreshMode.NO_REFRESH);
			throw ere;
		}
		log.debug("createDocumentVersion.Exit new document id = {}", document.get_Id().toString());
		return document;
	}

	public P8Object createP8Object(IndependentObject independentObject) {
		return createP8Object(independentObject, true);
	}

	public P8Object createP8Object(IndependentObject independentObject, boolean populateResource) {
		if (independentObject instanceof Document document) {
			P8ContentObject p8ContentObject = new P8ContentObject();
			if (populateResource)
				p8ContentObject.setResource(getContentResource(document));
			populateProperties(independentObject, p8ContentObject);
			return p8ContentObject;
		}
		P8Object p8Object = new P8Object();
		populateProperties(independentObject, p8Object);
		return p8Object;

	}

	/**
	 * Deletes custom object in FileNet.
	 *
	 * @param customObject the custom object to delete
	 * @throws ServiceException 
	 */
	public void deleteCustomObject(CustomObject customObject) throws ServiceException {
		customObject.delete();
		if (updatingBatch != null)
			addToUpdateBatch(customObject);
		else
			customObject.save(RefreshMode.NO_REFRESH);
	}

	/**
	 * Deletes custom object in FileNet.
	 *
	 * @param id the custom object Id to delete
	 * @throws ServiceException Signals that Filenet access info is not configured correctly
	 */
	public void deleteCustomObjectById(String id) throws ServiceException {
		log.debug("deleteCustomObjectById.id {}", id);
		deleteCustomObject(Factory.CustomObject.fetchInstance(getConnectionObjectStore(), new Id(id), null));
	}

	/**
	 * Deletes document in FileNet.
	 *
	 * @param document the document to delete
	 * @throws ServiceException 
	 */
	public void deleteDocument(Document document) throws ServiceException {
		document.delete();
		if (updatingBatch != null)
			addToUpdateBatch(document);
		else
			document.save(RefreshMode.NO_REFRESH);
	}

	/**
	 * Deletes document in FileNet.
	 *
	 * @param docId the document Id to delete
	 * @throws ServiceException Signals that Filenet access info is not configured correctly
	 */
	public void deleteDocumentById(String docId) throws ServiceException {
		log.debug("deleteDocumentById.id {}", docId);
		deleteDocument(Factory.Document.fetchInstance(getConnectionObjectStore(), new Id(docId), null));
	}

	/**
	 * Gets the document content resource information.
	 *
	 * @param filenetObject the FileNet IndependentlyPersistableObject
	 * @return the content resource
	 */
	public P8ContentResource getContentResource(IndependentlyPersistableObject filenetObject) {
		return getContentResourceExtended(filenetObject, false);
	}

	/**
	 * Gets the document content resource information.
	 *
	 * @param filenetObject the FileNet IndependentlyPersistableObject
	 * @param isLinkObject the flag. If true content is retrieved from linked object
	 * @return the content resource
	 */
	public P8ContentResource getContentResourceExtended(IndependentlyPersistableObject filenetObject,
			boolean isLinkObject) {
		if (!filenetObject.getProperties().isPropertyPresent(ECMConstants.P8_CONTENT_ELEMENTS_PROPERTYNAME))
			return null;
		ContentElementList list = null;
		if (filenetObject instanceof Document g)
			list = g.get_ContentElements();
		else if (filenetObject instanceof Annotation a)
			list = a.get_ContentElements();
		if (CollectionUtils.isEmpty(list))
			return null;
		for (Object o : list) {
			if (o instanceof ContentTransfer contentTransfer) {
				P8ContentResource result = new P8ContentResource();
				result.setFileName(contentTransfer.get_RetrievalName());
				result.setContentType(contentTransfer.get_ContentType());
				Double size = contentTransfer.get_ContentSize();
				result.setSize(size == null ? -1 : size.longValue());
				try {
					result.setResourceObject(contentTransfer.accessContentStream());
				} catch (EngineRuntimeException e) {
					return processResourceAccessException(result, filenetObject, e);
				}
				return result;
			}
		}
		return null;
	}

	/**
	 * Retrieves the FileNet document by id.
	 *
	 * @param customObjectId the FileNet custom object id
	 * @return the FileNet custom object
	 * @throws ServiceException Signals that Filenet access info is not configured correctly
	 */
	public CustomObject getCustomObjectById(String customObjectId) throws ServiceException {
		return getCustomObjectById(customObjectId, null);
	}

	/**
	 * Retrieves the FileNet custom object by id.
	 *
	 * @param customObjectId           the FileNet document id
	 * @param propertyCSVList the comma separated list of property names to populate
	 * @return the FileNet custom object
	 * @throws ServiceException Signals that Filenet access info is not configured correctly
	 */
	public CustomObject getCustomObjectById(String customObjectId, String propertyCSVList) throws ServiceException {
		PropertyFilter propertyFilter = null;
		if (StringUtils.hasText(propertyCSVList)) {
			propertyFilter = new PropertyFilter();
			propertyFilter.addIncludeProperty(
					new FilterElement(null, null, null, StringUtils.replace(propertyCSVList, ",", " "), null));
		}
		return Factory.CustomObject.fetchInstance(getConnectionObjectStore(), new Id(customObjectId), propertyFilter);
	}

	/**
	 * Retrieves the FileNet document by id.
	 *
	 * @param docId the FileNet document id
	 * @return the FileNet document
	 * @throws ServiceException Signals that Filenet access info is not configured correctly
	 */
	public Document getDocumentById(String docId) throws ServiceException {
		return getDocumentById(docId, null);
	}

	/**
	 * Retrieves the FileNet document by id.
	 *
	 * @param docId           the FileNet document id
	 * @param propertyCSVList the comma separated list of property names to populate
	 * @return the FileNet document
	 * @throws ServiceException Signals that Filenet access info is not configured correctly
	 */
	public Document getDocumentById(String docId, String propertyCSVList) throws ServiceException {
		PropertyFilter propertyFilter = null;
		if (StringUtils.hasText(propertyCSVList)) {
			propertyFilter = new PropertyFilter();
			propertyFilter.addIncludeProperty(
					new FilterElement(null, null, null, StringUtils.replace(propertyCSVList, ",", " "), null));
		}
		return Factory.Document.fetchInstance(getConnectionObjectStore(), new Id(docId), propertyFilter);
	}

	/**
	 * Gets the non pagination results.
	 *
	 * @param searchScope the search scope
	 * @param searchSQL   the search SQL
	 * @return the result set 
	 */
	private P8ResultSet getNonPaginationResults(SearchScope searchScope, SearchSQL searchSQL) {
		P8ResultSet results = new P8ResultSet();
		IndependentObjectSet rrs = searchScope.fetchObjects(searchSQL, null, null, false);
		if (!rrs.isEmpty()) {
			@SuppressWarnings("unchecked")
			Iterator<IndependentlyPersistableObject> it = rrs.iterator();
			while (it.hasNext())
				results.addIndependentlyPersistableObject(it.next());
		}
		results.setTotalPageNumber(1);
		results.setTotalDocumentNumber(results.getSearchResults().size());
		return results;
	}

	/**
	 * Gets the non pagination results.
	 *
	 * @param searchScope the search scope
	 * @param searchSQL   the search SQL
	 * @return the result set 
	 */
	private P8RowResultSet getNonPaginationRowResults(SearchScope searchScope, SearchSQL searchSQL) {
		P8RowResultSet results = new P8RowResultSet();
		RepositoryRowSet rrs = searchScope.fetchRows(searchSQL, null, null, false);
		if (!rrs.isEmpty()) {
			@SuppressWarnings("unchecked")
			Iterator<RepositoryRow> it = rrs.iterator();
			while (it.hasNext())
				results.addRepositoryRow(it.next());
		}
		results.setTotalPageNumber(1);
		results.setTotalRows(results.getSearchResults().size());
		return results;
	}

	/**
	 * Gets the pagination results.
	 *
	 * @param searchScope the search scope
	 * @param searchSQL   the search SQL
	 * @param pageSize    the page size
	 * @param pageNum     the page num
	 * @return the result set 
	 */
	private P8ResultSet getPaginationResults(SearchScope searchScope, SearchSQL searchSQL, int pageSize, int pageNum) {
		P8ResultSet results = new P8ResultSet();
		int pageCount = 1;
		IndependentObjectSet ioSet = searchScope.fetchObjects(searchSQL, pageSize, null, true);
		PageIterator pageIt = ioSet.pageIterator();
		int totalPages = 0;
		int totalDocuments = 0;
		while (pageIt.nextPage()) {
			if (pageCount == pageNum || pageNum < 0) {
				Object[] ioArr = pageIt.getCurrentPage();
				for (Object io : ioArr)
					results.addIndependentlyPersistableObject((IndependentlyPersistableObject) io);
			}
			totalPages++;
			totalDocuments += pageIt.getElementCount();
			pageCount++;
		}
		results.setTotalPageNumber(totalPages == 0 ? 1 : totalPages);
		results.setTotalDocumentNumber(totalDocuments);
		return results;
	}

	/**
	 * Gets the pagination results.
	 *
	 * @param searchScope the search scope
	 * @param searchSQL   the search SQL
	 * @param pageSize    the page size
	 * @param pageNum     the page num
	 * @return the result set 
	 */
	private P8RowResultSet getPaginationRowResults(SearchScope searchScope, SearchSQL searchSQL, int pageSize,
			int pageNum) {
		P8RowResultSet results = new P8RowResultSet();
		int pageCount = 1;
		RepositoryRowSet rrs = searchScope.fetchRows(searchSQL, pageSize, null, true);
		PageIterator pageIt = rrs.pageIterator();
		int totalPages = 0;
		int totalRows = 0;
		while (pageIt.nextPage()) {
			if (pageCount == pageNum || pageNum < 0) {
				Object[] ioArr = pageIt.getCurrentPage();
				for (Object io : ioArr)
					results.addRepositoryRow((RepositoryRow) io);
			}
			totalPages++;
			totalRows += pageIt.getElementCount();
			pageCount++;
		}
		results.setTotalPageNumber(totalPages == 0 ? 1 : totalPages);
		results.setTotalRows(totalRows);
		return results;
	}

	private void preprocessProperties(P8Object p8Object) throws ServiceException {
		ClassDefinition classDefinition = authService.getClassDefinition(baseRequest, p8Object.getDocumentClass());
		if (classDefinition == null)
			throw new InvalidRequestException("Invalid class name: " + p8Object.getDocumentClass());
		authService.getValidationService().preprocessPropertyValues(p8Object, classDefinition);
	}

	/**
	 * Process documents.
	 *
	 * @param query            the query
	 * @param searchParameters the search parameters
	 * @param processor result set processor
	 * @throws Exception 
	 */
	public void processDocuments(String query, SearchParameters searchParameters, IResultSetProcessor processor)
			throws Exception {
		log.debug("processDocuments.Entry Query: {}", query);
		SearchSQL searchSQL = createSearchSQL(query, searchParameters);
		int pageSize = searchParameters == null ? -1 : searchParameters.getPageSize();
		searchSQL.setQueryString(query);
		SearchScope searchScope = new SearchScope(objectStore);
		long start = System.currentTimeMillis();
		IndependentObjectSet ioSet = searchScope.fetchObjects(searchSQL, pageSize <= 0 ? null : pageSize, null, true);
		PageIterator pageIt = ioSet.pageIterator();
		int totalDocuments = 0;
		boolean processNext = true;
		while (pageIt.nextPage()) {
			if (!processNext)
				break;
			Object[] ioArr = pageIt.getCurrentPage();
			for (Object io : ioArr) {
				processNext = processor.processNextObject(createP8Object((IndependentlyPersistableObject) io));
				totalDocuments++;
			}
		}
		log.debug("searchDocuments.Exit Search completed in {} ms. Processed document count: {}",
				System.currentTimeMillis() - start, totalDocuments);
	}

	private P8ContentResource processResourceAccessException(P8ContentResource contentResource,
			IndependentlyPersistableObject filenetObject, EngineRuntimeException ert) throws EngineRuntimeException {
		String errorCode = ert.getExceptionCode().getErrorId();
		if (authService.getClientConfig().getBrokenResourceErrorCodes().contains(errorCode)) {
			String id = null;
			if (filenetObject instanceof Document g)
				id = g.getProperties().isPropertyPresent(ECMConstants.P8_ID_PROPERTYNAME) ? g.get_Id().toString()
						: null;
			else if (filenetObject instanceof Annotation a)
				id = a.getProperties().isPropertyPresent(ECMConstants.P8_ID_PROPERTYNAME) ? a.get_Id().toString()
						: null;
			log.error("Broken Resource.Document id: {}. Exception code: {}.", id, errorCode);
			contentResource.setBrokenResource(true);
			return contentResource;
		}
		throw ert;
	}

	/**
	 * Search document.
	 *
	 * @param query the query. 
	 * @return the document
	 * @throws ServiceException Signals that query returns more than 1 document
	 */
	public IndependentlyPersistableObject searchDocument(String query) throws ServiceException {
		return searchDocument(query, true);
	}

	/**
	 * Search document.
	 *
	 * @param query      the query
	 * @param singleOnly the single only. If set to true code will throw exception
	 *                   if query will return more than 1 document
	 * @return the retrieved document
	 * @throws ServiceException Signals that query returns more than 1 document if singleOnly is true
	 */
	public IndependentlyPersistableObject searchDocument(String query, boolean singleOnly) throws ServiceException {
		log.debug("searchDocument.Entry Query: {}", query);
		int maxSize = singleOnly ? 2 : maxResultSetSize;
		if (maxSize > 0) {
			query = query.trim();
			int index = query.toLowerCase().indexOf(SELECT_CLAUSE);
			if (index >= 0)
				query = query.substring(0, index + SELECT_CLAUSE.length()) + "TOP " + maxSize + " "
						+ query.substring(index + SELECT_CLAUSE.length());
		}
		if (searchTimeLimit > 0)
			query += " OPTIONS (TIMELIMIT " + searchTimeLimit + ")";
		log.debug("searchDocument. Query: {}", query);
		SearchSQL searchSQL = new SearchSQL();
		searchSQL.setQueryString(query);
		long start = System.currentTimeMillis();
		SearchScope searchScope = new SearchScope(objectStore);
		IndependentObjectSet rrs = null;
		rrs = searchScope.fetchObjects(searchSQL, null, null, false);
		if (!rrs.isEmpty()) {
			@SuppressWarnings("unchecked")
			Iterator<IndependentlyPersistableObject> it = rrs.iterator();
			IndependentlyPersistableObject document = it.next();
			if (singleOnly && it.hasNext())
				throw new ServiceException(P8ExceptionCodes.SEARCH_MULTIPLE_RETURN.getExceptionCode());
			return document;
		}
		log.debug("searchDocument.Exit Search completed in {} ms.", System.currentTimeMillis() - start);
		return null;
	}

	/**
	 * Search documents.
	 *
	 * @param query the query
	 * @return the result set 
	 */
	public P8ResultSet searchDocuments(String query) {
		return searchDocuments(query, new SearchParameters());
	}

	/**
	 * Search documents.
	 *
	 * @param query            the query
	 * @param searchParameters the search parameters
	 * @return the result set 
	 */
	public P8ResultSet searchDocuments(String query, SearchParameters searchParameters) {
		log.debug("searchDocuments.Entry Query: {}", query);
		int pageSize = searchParameters == null ? -1 : searchParameters.getPageSize();
		int pageNum = searchParameters == null ? 0 : searchParameters.getStartPage();
		SearchSQL searchSQL = createSearchSQL(query, searchParameters);
		SearchScope searchScope = new SearchScope(objectStore);
		long start = System.currentTimeMillis();
		P8ResultSet results = pageSize <= 0 ? getNonPaginationResults(searchScope, searchSQL)
				: getPaginationResults(searchScope, searchSQL, pageSize, pageNum);
		log.debug("searchDocuments.Exit Search completed in {} ms. Document count: {}",
				System.currentTimeMillis() - start, results.getTotalDocumentNumber());
		return results;
	}

	/**
	 * Search row set.
	 *
	 * @param query the query
	 * @return the result set 
	 */
	public P8RowResultSet searchRows(String query) {
		return searchRows(query, new SearchParameters());
	}

	/**
	 * Search row set.
	 *
	 * @param query            the query
	 * @param searchParameters the search parameters
	 * @return the result set 
	 */
	public P8RowResultSet searchRows(String query, SearchParameters searchParameters) {
		log.debug("searchRows.Entry Query: {}", query);
		int pageSize = searchParameters == null ? -1 : searchParameters.getPageSize();
		int pageNum = searchParameters == null ? 0 : searchParameters.getStartPage();
		SearchSQL searchSQL = createSearchSQL(query, searchParameters);
		SearchScope searchScope = new SearchScope(objectStore);
		long start = System.currentTimeMillis();
		P8RowResultSet results = pageSize <= 0 ? getNonPaginationRowResults(searchScope, searchSQL)
				: getPaginationRowResults(searchScope, searchSQL, pageSize, pageNum);
		log.debug("searchRows.Exit Search completed in {} ms. Rows count: {}", System.currentTimeMillis() - start,
				results.getTotalRows());
		return results;
	}

	/**
	 * Update document metadata.
	 *
	 * @param document   the document
	 * @param properties the document properties
	 * @return the information about document changes for audit purposes
	 * @throws ServiceException 
	 */
	public P8ObjectAuditInfo updateCustomObjectMetadata(CustomObject customObject, Map<String, Object> properties)
			throws ServiceException {
		ArrayList<CustomObject> customObjects = new ArrayList<>(1);
		customObjects.add(customObject);
		ArrayList<P8ObjectAuditInfo> auditInfos = new ArrayList<>();
		updateCustomObjectMetadata(customObjects, properties, auditInfos);
		return auditInfos.get(0);
	}

	/**
	 * Update documents metadata.
	 *
	 * @param documents  the documents			return p8ProviderImpl.copyDocument(copyDocumentRequest, auditInfo);
	
	 * @param properties document the properties
	 * @param auditInfos audit data (changed properties, content info, etc.) can be null
	 * @return the number of update documents
	 * @throws ServiceException 
	 */
	public int updateCustomObjectMetadata(List<CustomObject> customObjects, Map<String, Object> properties,
			List<P8ObjectAuditInfo> auditInfos) throws ServiceException {
		if (customObjects == null)
			customObjects = Collections.emptyList();
		log.debug("updateCustomObjectMetadata.Entry Custom Object count: {}, properties: {}", customObjects.size(),
				properties);

		long start = System.currentTimeMillis();
		for (CustomObject document : customObjects) {
			log.trace("updateCustomObjectMetadata.UpdateDocument: {}", document.get_Id());
			authService.getValidationService().preprocessPropertyValues(properties,
					authService.getClassDefinition(baseRequest, document.getClassName()));
			List<PropertyAuditInfo> pAuditInfos = populateProperties(document.getProperties(), properties, false);
			if (auditInfos != null) {
				P8ContentObjectAuditInfo auditInfo = new P8ContentObjectAuditInfo();
				auditInfo.setObjectId(document.get_Id().toString());
				auditInfo.setObjectClass(document.getClassName());
				auditInfo.setModifiedProperties(pAuditInfos);
				auditInfos.add(auditInfo);
			}
			if (updatingBatch != null)
				addToUpdateBatch(document);
			else
				document.save(RefreshMode.NO_REFRESH);
		}

		int res = customObjects.size();
		log.debug("updateCustomObjectMetadata.Exit Update completed in {} ms. Document count: {}",
				System.currentTimeMillis() - start, res);
		return res;
	}

	/**
	 * Update document metadata.
	 *
	 * @param document   the document
	 * @param properties the document properties
	 * @return the information about document changes for audit purposes
	 * @throws ServiceException 
	 */
	public P8ObjectAuditInfo updateDocumentMetadata(Document document, String reindexClass,
			Map<String, Object> properties) throws ServiceException {
		ArrayList<Document> documents = new ArrayList<>(1);
		documents.add(document);
		ArrayList<P8ObjectAuditInfo> auditInfos = new ArrayList<>();
		updateDocumentsMetadata(documents, reindexClass, properties, auditInfos);
		return auditInfos.get(0);
	}

	/**
	 * Update documents metadata.
	 *
	 * @param documents  the documents
	 * @param properties document the properties
	 * @return the number of update documents
	 * @throws ServiceException 
	 */
	public int updateDocumentsMetadata(List<CustomObject> customObjects, Map<String, Object> properties)
			throws ServiceException {
		return updateCustomObjectMetadata(customObjects, properties, null);
	}

	/**
	 * Update documents metadata.
	 *
	 * @param documents  the documents
	 * @param properties document the properties
	 * @return the number of update documents
	 * @throws ServiceException 
	 */
	public int updateDocumentsMetadata(List<Document> documents, String reindexClass, Map<String, Object> properties)
			throws ServiceException {
		return updateDocumentsMetadata(documents, reindexClass, properties, null);
	}

	/**
	 * Update documents metadata.
	 *
	 * @param documents  the documents			return p8ProviderImpl.copyDocument(copyDocumentRequest, auditInfo);
	
	 * @param properties document the properties
	 * @param auditInfos audit data (changed properties, content info, etc.) can be null
	 * @return the number of update documents
	 * @throws ServiceException 
	 */
	public int updateDocumentsMetadata(List<Document> documents, String reindexClass, Map<String, Object> properties,
			List<P8ObjectAuditInfo> auditInfos) throws ServiceException {
		if (documents == null)
			documents = Collections.emptyList();
		log.debug("updateDocumentsMetadata.Entry Document count: {}, properties: {}", documents.size(), properties);

		long start = System.currentTimeMillis();
		for (Document document : documents) {
			log.trace("updateDocumentsMetadata.UpdateDocument: {}", document.get_Id());
			authService.getValidationService().preprocessPropertyValues(properties,
					authService.getClassDefinition(baseRequest, document.getClassName()));
			List<PropertyAuditInfo> pAuditInfos = populateProperties(document.getProperties(), properties, false);
			if (StringUtils.hasText(reindexClass))
				document.changeClass(reindexClass);
			if (auditInfos != null) {
				P8ContentObjectAuditInfo auditInfo = new P8ContentObjectAuditInfo();
				auditInfo.setObjectId(document.get_Id().toString());
				auditInfo.setObjectClass(document.getClassName());
				auditInfo.setModifiedProperties(pAuditInfos);
				auditInfos.add(auditInfo);
			}
			if (updatingBatch != null)
				addToUpdateBatch(document);
			else
				document.save(RefreshMode.NO_REFRESH);
		}

		int res = documents.size();
		log.debug("updateDocumentsMetadata.Exit Update completed in {} ms. Document count: {}",
				System.currentTimeMillis() - start, res);
		return res;
	}
}
