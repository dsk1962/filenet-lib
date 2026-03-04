package com.dkgeneric.filenet.content.provider;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import com.dkgeneric.filenet.content.common.ServiceException;
import com.dkgeneric.filenet.content.config.ApplicationConfig;
import com.dkgeneric.filenet.content.exceptioncodes.ConfigExceptionCodes;
import com.dkgeneric.filenet.content.exceptioncodes.P8ExceptionCodes;
import com.dkgeneric.filenet.content.model.PropertyAuditInfo;
import com.dkgeneric.filenet.content.model.SearchParameters;
import com.dkgeneric.filenet.content.request.BaseRequest;
import com.dkgeneric.filenet.content.resources.P8ContentResource;
import com.dkgeneric.filenet.content.service.AuthService;
import com.filenet.api.admin.ClassDefinition;
import com.filenet.api.admin.PropertyTemplate;
import com.filenet.api.collection.ContentElementList;
import com.filenet.api.collection.IndependentObjectSet;
import com.filenet.api.constants.RefreshMode;
import com.filenet.api.core.Annotation;
import com.filenet.api.core.Connection;
import com.filenet.api.core.ContentTransfer;
import com.filenet.api.core.Document;
import com.filenet.api.core.Domain;
import com.filenet.api.core.Factory;
import com.filenet.api.core.IndependentlyPersistableObject;
import com.filenet.api.core.ObjectStore;
import com.filenet.api.core.UpdatingBatch;
import com.filenet.api.exception.EngineRuntimeException;
import com.filenet.api.property.Properties;
import com.filenet.api.query.SearchSQL;
import com.filenet.api.query.SearchScope;

import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

/**
 * This class provides initial set  of methods to communicate with FileNet
 */
@Slf4j
@Getter
@Setter
public class P8ProviderBase implements AutoCloseable {

	public static final String SELECT_CLAUSE = "select ";

	protected AuthService authService;

	protected Connection connection;

	protected ObjectStore objectStore;

	protected Domain domain;

	protected UpdatingBatch updatingBatch;

	protected int updatingBatchItemCount = 0;

	protected int maxResultSetSize = -1;

	protected int searchTimeLimit = -1;

	protected BaseRequest baseRequest;

	/**
	 * Adds the modified FileNet object to update batch.
	 *
	 * @param ipo the modified FileNet object
	 * @throws ServiceException  Signals that update batch object was not created before this call
	 */
	public void addToUpdateBatch(IndependentlyPersistableObject ipo) throws ServiceException {
		if (updatingBatch == null)
			throw new ServiceException(P8ExceptionCodes.BATCH_NOT_CREATED.getExceptionCode());
		updatingBatch.add(ipo, null);
		updatingBatchItemCount++;
		if (updatingBatchItemCount > 0
				&& (updatingBatchItemCount % authService.getClientConfig().getBatchSize()) == 0) {
			updatingBatch.updateBatch();
			updatingBatch = UpdatingBatch.createUpdatingBatchInstance(domain, RefreshMode.NO_REFRESH);
		}
	}

	/**
	 * Closes connection. And nullifies updating batch
	 */
	@Override
	public void close() {
		updatingBatch = null;
		updatingBatchItemCount = 0;
		closeConnection();
	}

	/**
	 * Closes connection.
	 */
	public void closeConnection() {
		// remove authorization subject from usercontext for non-container authorization
		if (!(this instanceof P8UserConnection))
			authService.restoreSubject();
	}

	/**
	 * Commits changes added to update batch.
	 * @throws ServiceException Signals that update batch object was not created before this call
	 */
	public void commitUpdateBatch() throws ServiceException {
		if (updatingBatch == null)
			throw new ServiceException(P8ExceptionCodes.BATCH_NOT_CREATED.getExceptionCode());
		if (updatingBatch.hasPendingExecute())
			updatingBatch.updateBatch();
		updatingBatch = null;
		updatingBatchItemCount = 0;
	}

	/**
	 * Creates the multi value property list.
	 *
	 * @param coll the coll
	 * @return the list
	 */
	@SuppressWarnings("rawtypes")
	protected List createMultiValuePropertyList(Collection coll) {
		List p8propList = null;

		if (!coll.isEmpty()) {
			Object o = coll.iterator().next();
			if (o instanceof String) {
				p8propList = Factory.StringList.createList();
			} else if (o instanceof Date) {
				p8propList = Factory.DateTimeList.createList();
			} else if (o instanceof Integer) {
				p8propList = Factory.Integer32List.createList();
			} else if (o instanceof Double) {
				p8propList = Factory.Float64List.createList();
			} else if (o instanceof Boolean) {
				p8propList = Factory.BooleanList.createList();
			} else {
				throw new IllegalArgumentException("Unexpected type for multivalue property: '" + o.getClass().getName()
						+ "'. Only following types are supported: String|Date|Integer|Double|Boolean.");
			}
		}

		return p8propList;
	}

	protected SearchSQL createSearchSQL(String query, SearchParameters searchParameters) {
		int maxSize = searchParameters == null ? -1 : searchParameters.getMaxSize();
		if (maxSize <= 0)
			maxSize = maxResultSetSize;
		if (maxSize > 0) {
			query = query.trim();
			int index = query.toLowerCase().indexOf(SELECT_CLAUSE);
			if (index >= 0)
				query = query.substring(0, index + SELECT_CLAUSE.length()) + "TOP " + maxSize + " "
						+ query.substring(index + SELECT_CLAUSE.length());
		}
		int timeLimit = searchParameters == null ? -1 : searchParameters.getSearchTimeLimit();
		if (timeLimit <= 0)
			timeLimit = searchTimeLimit;
		if( timeLimit > 0 )
			query += " OPTIONS (TIMELIMIT " + timeLimit + ")";
		log.debug("createSearchSQL. Query: {}", query);
		SearchSQL searchSQL = new SearchSQL();
		searchSQL.setQueryString(query);
		return searchSQL;
	}

	/**
	 * Creates the new updating batch with RefreshMode = RefreshMode.REFRESH
	 */
	public void createUpdatingBatch() {
		createUpdatingBatch(true);
	}

	/**
	 * Creates the updating batch.
	 *
	 * @param refreshOnUpdate if set to true then batch mode RefreshMode =
	 *                        RefreshMode.REFRESH otherwise RefreshMode =
	 *                        RefreshMode.NO_REFRESH
	 */
	public void createUpdatingBatch(boolean refreshOnUpdate) {
		updatingBatch = UpdatingBatch.createUpdatingBatchInstance(domain,
				refreshOnUpdate ? RefreshMode.REFRESH : RefreshMode.NO_REFRESH);
	}

	/**
	 * Gets the class definition from connected object store.
	 *
	 * @param symbolicName object class symbolic name
	 * @return the ClassDefinition
	 */
	public ClassDefinition getClassDefinition(String symbolicName) {
		return Factory.ClassDefinition.fetchInstance(objectStore, symbolicName, null);
	}

	/**
	 * Gets the object store used in this instance.
	 *
	 * @return the object store
	 * @throws ServiceException  Signals that object store name is not configured or set as input parameter
	 */
	public ObjectStore getConnectionObjectStore() throws ServiceException {
		return objectStore == null ? selectObjectStore() : objectStore;
	}

	/**
	 * Gets the class definition from connected object store.
	 *
	 * @param symbolicName object class symbolic name
	 * @return the ClassDefinition
	 */
	public PropertyTemplate getPropertyTemplate(String symbolicName) {
		SearchSQL searchSQL = createSearchSQL(
				"SELECT * FROM PropertyTemplate WHERE SymbolicName = '" + symbolicName + "' ", null);
		SearchScope searchScope = new SearchScope(objectStore);
		IndependentObjectSet rrs = searchScope.fetchObjects(searchSQL, null, null, false);
		if (!rrs.isEmpty())
			return (PropertyTemplate) rrs.iterator().next();
		return null;
	}

	private Object getPropertyValue(Properties properties, String name) {
		return properties.isPropertyPresent(name) ? properties.getObjectValue(name) : null;
	}

	protected boolean isValueModified(Object oldValue, Object val) {
		if (oldValue != null && val != null) {
			if (!oldValue.equals(val))
				return true;
		} else if (oldValue != null)
			return true;
		else if (val != null && StringUtils.hasLength(val.toString()))
			return true;
		return false;
	}

	/**
	 * Populate content element.
	 *
	 * @param document the document
	 * @param resource the resource
	 * @param stream   the stream
	 */
	@SuppressWarnings("unchecked")
	protected void populateContentElement(Document document, P8ContentResource resource, InputStream stream) {
		String contentType = resource.getContentType();
		ContentTransfer ctObject = Factory.ContentTransfer.createInstance();
		ctObject.set_ContentType(contentType);
		ctObject.set_RetrievalName(resource.getFileName());
		ctObject.setCaptureSource(stream);

		ContentElementList contentList = Factory.ContentElement.createList();
		contentList.add(ctObject);
		document.set_ContentElements(contentList);
		document.set_MimeType(contentType);
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	protected List populateP8PropertyList(Collection coll) {
		List p8propList = createMultiValuePropertyList(coll);

		if (p8propList != null) {
			for (Object o : coll) {
				p8propList.add(o);
			}
		}
		return p8propList;
	}

	/**
	 * Populate properties.
	 *
	 * @param fnProps    the fn props
	 * @param inputProps the input props
	 */
	protected List<PropertyAuditInfo> populateProperties(Properties fnProps, Map<String, Object> inputProps,
			boolean createOp) {
		List<PropertyAuditInfo> result = new ArrayList<>();
		if (!CollectionUtils.isEmpty(inputProps))
			for (Entry<String, Object> entry : inputProps.entrySet()) {
				String key = entry.getKey();
				Object val = entry.getValue();
				if (val instanceof Collection coll) {
					val = populateP8PropertyList(coll);
				}
				boolean hasProperty = fnProps.isPropertyPresent(key);
				if (!createOp && !hasProperty)
					result.add(new PropertyAuditInfo(key, null, val));
				else {
					Object oldValue = getPropertyValue(fnProps, key);
					if (isValueModified(oldValue, val))
						result.add(new PropertyAuditInfo(key, oldValue, val));
				}
				fnProps.putObjectValue(key, val);
			}
		return result;
	}

	/**
	 * Select object store. Application configuration settings will be used.
	 * {@link ApplicationConfig}
	 *
	 * @return the object store
	 * @throws ServiceException  Signals that object store name is not configured or set as input parameter
	 */
	public ObjectStore selectObjectStore() throws ServiceException {
		return selectObjectStore(null, null);
	}

	/**
	 * Select object store.
	 *
	 * @param objectStoreName the object store name. If null a name from application
	 *                        configuration {@link ApplicationConfig} will be used.
	 *                        Object store will be searched in domain from
	 *                        application configuration or default FileNet domain.
	 * @return the object store
	 * @throws ServiceException  Signals that object store name is not configured or set as input parameter
	 */
	public ObjectStore selectObjectStore(String objectStoreName) throws ServiceException {
		return selectObjectStore(null, objectStoreName);
	}

	/**
	 * Select object store.
	 *
	 * @param domainName      the domain name. If null a name from application
	 *                        configuration {@link ApplicationConfig} will be used.
	 *                        If null and not configured, then defaul FileNet domain
	 *                        will be used
	 * @param objectStoreName the object store name. If null a name from application
	 *                        configuration will be used.
	 * @return the object store
	 * @throws ServiceException Signals that object store name is not configured or set as input parameter 
	 */
	public ObjectStore selectObjectStore(String domainName, String objectStoreName) throws ServiceException {
		try {
			log.debug("selectObjectStore.Entry input parameters. domainName: {}, objectStoreName: {}", domainName,
					objectStoreName);
			// get object store form cache
			if (domainName == null) {
				domainName = authService.getClientConfig().getDomainName();
				if (!StringUtils.hasText(domainName))
					domainName = null;
			}
			log.debug("selectObjectStore.  domainName: {}", domainName);
			domain = Factory.Domain.fetchInstance(connection, domainName, null);
			if (objectStoreName == null)
				objectStoreName = authService.getClientConfig().getObjectStoreName();
			if (!StringUtils.hasText(objectStoreName))
				throw new ServiceException(ConfigExceptionCodes.OBJECT_STORE_NAME_MISSING.getExceptionCode());
			log.debug("selectObjectStore. objectStoreName: {}", objectStoreName);
			objectStore = Factory.ObjectStore.fetchInstance(domain, objectStoreName, null);
			log.debug("selectObjectStore.Exit domainName{}, objectStoreName: {}", domain.get_Name(),
					objectStore.get_SymbolicName());
			return objectStore;
		} catch (EngineRuntimeException e) {
			throw new ServiceException(e);
		}
	}

	/**
	 * Populate content element.
	 *
	 * @param annotation the annotation
	 * @param resource the resource
	 * @param stream   the stream
	 */
	@SuppressWarnings("unchecked")
	protected void populateContentElement(Annotation annotation, P8ContentResource resource, InputStream stream) {
		String contentType = resource.getContentType();
		ContentTransfer ctObject = Factory.ContentTransfer.createInstance();
		ctObject.set_ContentType(contentType);
		ctObject.set_RetrievalName(resource.getFileName());
		ctObject.setCaptureSource(stream);

		ContentElementList contentList = Factory.ContentElement.createList();
		contentList.add(ctObject);
		annotation.set_ContentElements(contentList);
		annotation.set_MimeType(contentType);
	}
}
