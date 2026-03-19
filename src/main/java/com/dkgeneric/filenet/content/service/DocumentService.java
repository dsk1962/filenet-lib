package com.dkgeneric.filenet.content.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import com.dkgeneric.filenet.content.common.ECMConstants;
import com.dkgeneric.filenet.content.common.ServiceException;
import com.dkgeneric.filenet.content.common.Utilities;
import com.dkgeneric.filenet.content.config.ApplicationConfig;
import com.dkgeneric.filenet.content.exceptioncodes.DocumentServiceExceptionCodes;
import com.dkgeneric.filenet.content.exceptioncodes.GeneralExceptionCodes;
import com.dkgeneric.filenet.content.model.P8ContentObject;
import com.dkgeneric.filenet.content.model.P8ContentObjectAuditInfo;
import com.dkgeneric.filenet.content.model.P8ObjectAuditInfo;
import com.dkgeneric.filenet.content.provider.P8ProviderImpl;
import com.dkgeneric.filenet.content.request.CopyDocumentRequest;
import com.dkgeneric.filenet.content.request.CreateDocumentRequest;
import com.dkgeneric.filenet.content.request.CreateDocumentVersionRequest;
import com.dkgeneric.filenet.content.request.GetDocumentByIdRequest;
import com.dkgeneric.filenet.content.request.UpdateDocumentMetadataRequest;
import com.dkgeneric.filenet.content.response.CopyDocumentResponse;
import com.dkgeneric.filenet.content.response.CreateDocumentResponse;
import com.dkgeneric.filenet.content.response.CreateDocumentVersionResponse;
import com.dkgeneric.filenet.content.response.GetDocumentByIdResponse;
import com.dkgeneric.filenet.content.response.UpdateDocumentMetadataResponse;
import com.filenet.api.core.Document;
import com.filenet.api.core.IndependentlyPersistableObject;
import com.filenet.api.core.UpdatingBatch;

import lombok.extern.slf4j.Slf4j;

/**
 * The Document Service class. This class contains methods to manipulate FileNet objects
 */
@Component("p8contentlibDocumentService")
@ConditionalOnProperty(name = "com.davita.ecm.p8.content.service.enabled", matchIfMissing = true)
@Slf4j
public class DocumentService extends AuthorizationBasedService {

	public DocumentService(
			@Qualifier("p8contentlibAuthService") AuthService authService,
			@Qualifier("p8ContentLibConfig") ApplicationConfig clientConfig,
			@Qualifier("p8contentlibUtilities") Utilities utilities, ValidationService validationService) {
		super(authService, clientConfig, utilities, validationService);
	}

	public CopyDocumentResponse copyDavitaDocument(CopyDocumentRequest copyDocumentRequest,
			P8ContentObjectAuditInfo auditInfo) throws ServiceException {
		return copyDocument(copyDocumentRequest, auditInfo);
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
		try (P8ProviderImpl p8ProviderImpl = authService.createConnection(copyDocumentRequest)) {
			return p8ProviderImpl.copyDocument(copyDocumentRequest, auditInfo);
		} catch (Exception e) {
			CopyDocumentResponse response = new CopyDocumentResponse();
			utilities.setResponseErrors(response, e);
			if (!(e instanceof ServiceException))
				log.error("copyDocument method catched exception. Request: {}, Response: {}", copyDocumentRequest,
						response, e);
			return response;
		}
	}

	/**
	 * Creates the document.
	 *
	 * @param request the create document request
	 * @return the operation results. Content information will not be populated
	 * @throws ServiceException Signals that Filenet access info is not configured correctly
	 */
	public CreateDocumentResponse createDocument(CreateDocumentRequest request) throws ServiceException {
		try (P8ProviderImpl p8ProviderImpl = authService.createConnection(request)) {
			return createDocument(request, p8ProviderImpl);
		}
	}

	/**
	 * Creates the document.
	 *
	 * @param request the create document request
	 * @param p8ProviderImpl FileNet provider
	 * @return the operation results. Content information will not be populated 
	 */
	public CreateDocumentResponse createDocument(CreateDocumentRequest request, P8ProviderImpl p8ProviderImpl) {
		log.debug("createDocument.Entry {}", request);
		CreateDocumentResponse result = new CreateDocumentResponse();
		try {
			if (request.getP8ContentObject() == null)
				throw new ServiceException(DocumentServiceExceptionCodes.MISSING_PARAMETERS.getExceptionCode());
			if (request.getP8ContentObject().getResource() == null)
				throw new ServiceException(DocumentServiceExceptionCodes.MISSING_RESOURCE.getExceptionCode());
			if (!StringUtils.hasText(request.getP8ContentObject().getDocumentClass()))
				throw new ServiceException(DocumentServiceExceptionCodes.MISSING_DOCUMENT_CLASS.getExceptionCode());
			P8ContentObjectAuditInfo auditInfo = new P8ContentObjectAuditInfo();
			Document document = p8ProviderImpl.createDocument(request.getP8ContentObject(), auditInfo);
			result.setP8Object(p8ProviderImpl.createP8Object(document, false));
			result.setP8DocumentId(document.get_Id().toString());
			result.setAuditInfo(auditInfo);
		} catch (Exception e) {
			utilities.setResponseErrors(result, e);
			if (!(e instanceof ServiceException))
				log.error("createDocument method catched exception. Request: {}, Response: {}", request, result, e);
		}
		log.debug("createDocument.Exit {}", result);
		return result;
	}

	/**
	 * Creates the document.
	 *
	 * @param request the create document request
	 * @return the operation results. Content information will not be populated
	 * @throws ServiceException Signals that Filenet access info is not configured correctly
	 */
	public CreateDocumentVersionResponse createDocumentVersion(CreateDocumentVersionRequest request)
			throws ServiceException {
		try (P8ProviderImpl p8ProviderImpl = authService.createConnection(request)) {
			return createDocumentVersion(request, p8ProviderImpl);
		}
	}

	/**
	 * Creates the document.
	 *
	 * @param request the create document request
	 * @param p8ProviderImpl FileNet provider
	 * @return the operation results. Content information will not be populated
	 */
	public CreateDocumentVersionResponse createDocumentVersion(CreateDocumentVersionRequest request,
			P8ProviderImpl p8ProviderImpl) {
		log.debug("createDocumentVersion.Entry {}", request);
		CreateDocumentVersionResponse result = new CreateDocumentVersionResponse();
		try {
			if (request.getP8ContentObject() == null)
				throw new ServiceException(DocumentServiceExceptionCodes.MISSING_PARAMETERS.getExceptionCode());
			if (request.getP8ContentObject().getResource() == null)
				throw new ServiceException(DocumentServiceExceptionCodes.MISSING_RESOURCE.getExceptionCode());
			P8ContentObjectAuditInfo auditInfo = new P8ContentObjectAuditInfo();
			Document document = p8ProviderImpl.createDocumentVersion(request.getP8ContentObject(),
					request.getP8ObjectId(), request.isSaveAsMajorVersion(), auditInfo);
			result.setP8DocumentId(document.get_Id().toString());
			result.setP8Object(p8ProviderImpl.createP8Object(document, false));
			result.setAuditInfo(auditInfo);
		} catch (Exception e) {
			utilities.setResponseErrors(result, e);
			if (!(e instanceof ServiceException))
				log.error("createDocument method catched exception. Request: {}, Response: {}", request, result, e);
		}
		log.debug("createDocumentVersion.Exit {}", result);
		return result;
	}

	/**
	 * Gets the document metadata by id.
	 *
	 * @param request the get document data request
	 * @return the operation results
	 */
	public GetDocumentByIdResponse getDocumentMetadataById(GetDocumentByIdRequest request) {
		log.debug("getDocumentMetadataById.Entry {}", request);
		GetDocumentByIdResponse result = new GetDocumentByIdResponse();
		Document document = null;
		try (P8ProviderImpl p8ProviderImpl = authService.createConnection(request)) {
			String propertyList = utilities.getPropertiesToInclude(request.getPropertiesListName());
			if (StringUtils.hasText(propertyList) && request.isRetrieveLatestVersion())
				propertyList += "," + ECMConstants.P8_IS_CURRENT_VERSION + "," + ECMConstants.P8_VERSION_SERIES_PROPERTYNAME;
			document = p8ProviderImpl.getDocumentById(request.getDocumentId(), propertyList);
			if (document == null) {
				utilities.setResponseErrors(result,
						GeneralExceptionCodes.DOCUMENT_NOT_FOUND_BY_ID_EXCEPTION.getExceptionCode(),
						new Object[] { request.getDocumentId() });
			} else {
				if( request.isRetrieveLatestVersion() && !Boolean.TRUE.equals(document.get_IsCurrentVersion()))
					document = (Document)document.get_VersionSeries().get_CurrentVersion();
				P8ContentObject p8Object = (P8ContentObject) p8ProviderImpl.createP8Object(document);
				result.setP8ContentObject(p8Object);
			}
		} catch (Exception e) {
			utilities.setResponseErrors(result, e, request.getDocumentId());
			if (!(e instanceof ServiceException))
				log.error("getDocumentMetadataById method catched exception. Request: {}, Response: {}", request,
						result, e);
		}
		log.debug("getDocumentMetadataById.Exit {}", result);
		return result;
	}

	/**
	 * Updates document metadata.
	 *
	 * @param request the update document request
	 * @return the operation results
	 * @throws ServiceException  Signals that Filenet access info is not configured correctly
	 */
	public UpdateDocumentMetadataResponse updateDocumentMetadata(UpdateDocumentMetadataRequest request)
			throws ServiceException {
		return updateDocumentMetadata(request, true);
	}

	/**
	 * Updates document metadata.
	 *
	 * @param request the update document request
	 * @param useBatchUpdate if set to true {@link UpdatingBatch} will be used to save data in FileNet
	 * @return the operation results
	 * @throws ServiceException  Signals that Filenet access info is not configured correctly
	 */
	public UpdateDocumentMetadataResponse updateDocumentMetadata(UpdateDocumentMetadataRequest request,
			boolean useBatchUpdate) throws ServiceException {
		try (P8ProviderImpl p8ProviderImpl = authService.createConnection(request)) {
			if (useBatchUpdate)
				p8ProviderImpl.createUpdatingBatch();
			UpdateDocumentMetadataResponse result = updateDocumentMetadata(request, p8ProviderImpl);
			if (useBatchUpdate)
				p8ProviderImpl.commitUpdateBatch();
			return result;
		}
	}

	/**
	 * Updates document metadata.
	 *
	 * @param request the update document request
	 * @param p8ProviderImpl FileNet provider
	 * @return the operation results
	 */
	public UpdateDocumentMetadataResponse updateDocumentMetadata(UpdateDocumentMetadataRequest request,
			P8ProviderImpl p8ProviderImpl) {
		log.debug("updateDocumentMetadata.Entry {}", request);
		UpdateDocumentMetadataResponse result = new UpdateDocumentMetadataResponse();
		try {
			if (!StringUtils.hasText(request.getId()) && request.getSearchData() == null)
				throw new ServiceException(
						DocumentServiceExceptionCodes.MISSING_DOCUMENT_PARAMETERS.getExceptionCode());
			if (CollectionUtils.isEmpty(request.getProperties()))
				throw new ServiceException(
						DocumentServiceExceptionCodes.MISSING_PROPERTIES_PARAMETERS.getExceptionCode());

			Map<String, Object> props = request.getProperties();
			List<Document> documents = new ArrayList<>(10);

			if (StringUtils.hasText(request.getId())) {
				documents.add(p8ProviderImpl.getDocumentById(request.getId()));
			} else {
				String query = utilities.prepareQuery(request.getSearchData(), p8ProviderImpl);
				for (IndependentlyPersistableObject ipo : p8ProviderImpl.searchDocuments(query).getSearchResults())
					documents.add((Document) ipo);
			}
			List<P8ObjectAuditInfo> auditInfos = new ArrayList<>();
			int count = p8ProviderImpl.updateDocumentsMetadata(documents, request.getReindexClass(), props, auditInfos);

			result.setNumberOfUpdatedDocuments(count);
			result.setAuditInfos(auditInfos);
		} catch (Exception e) {
			utilities.setResponseErrors(result, e, request.getId());
			if (!(e instanceof ServiceException))
				log.error("updateDocumentMetadata method catched exception. Request: {}, Response: {}", request, result,
						e);
		}
		log.debug("updateDocumentMetadata.Exit {}", result);
		return result;
	}
}
