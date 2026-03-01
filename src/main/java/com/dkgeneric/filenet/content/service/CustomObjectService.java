package com.dkgeneric.filenet.content.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import com.dkgeneric.filenet.content.common.ServiceException;
import com.dkgeneric.filenet.content.common.Utilities;
import com.dkgeneric.filenet.content.config.ApplicationConfig;
import com.dkgeneric.filenet.content.exceptioncodes.DocumentServiceExceptionCodes;
import com.dkgeneric.filenet.content.exceptioncodes.GeneralExceptionCodes;
import com.dkgeneric.filenet.content.model.P8ContentObjectAuditInfo;
import com.dkgeneric.filenet.content.model.P8Object;
import com.dkgeneric.filenet.content.model.P8ObjectAuditInfo;
import com.dkgeneric.filenet.content.provider.P8ProviderImpl;
import com.dkgeneric.filenet.content.request.CreateCustomObjectRequest;
import com.dkgeneric.filenet.content.request.DeleteObjectByIdRequest;
import com.dkgeneric.filenet.content.request.GetCustomObjectByIdRequest;
import com.dkgeneric.filenet.content.request.UpdateDocumentMetadataRequest;
import com.dkgeneric.filenet.content.response.CreateCustomObjectResponse;
import com.dkgeneric.filenet.content.response.DeleteObjectByIdResponse;
import com.dkgeneric.filenet.content.response.GetCustomObjectByIdResponse;
import com.dkgeneric.filenet.content.response.UpdateDocumentMetadataResponse;
import com.filenet.api.core.CustomObject;
import com.filenet.api.core.IndependentlyPersistableObject;
import com.filenet.api.core.UpdatingBatch;

import lombok.extern.slf4j.Slf4j;

/**
 * The Document Service class. This class contains methods to manipulate FileNet objects
 */
@Component("p8contentlibCustomObjectService")
@ConditionalOnProperty(name = "com.davita.ecm.p8.content.service.enabled", matchIfMissing = true)
@Slf4j
public class CustomObjectService extends AuthorizationBasedService {

	public CustomObjectService(@Qualifier("p8contentlibAuthService") AuthService authService,
			@Qualifier("p8ContentLibConfig") ApplicationConfig clientConfig,
			@Qualifier("p8contentlibUtilities") Utilities utilities, ValidationService validationService) {
		super(authService, clientConfig, utilities, validationService);
	}

	/**
	 * Creates the document.
	 *
	 * @param request the create document request
	 * @return the operation results. Content information will not be populated
	 * @throws ServiceException Signals that Filenet access info is not configured correctly
	 */
	public CreateCustomObjectResponse createCustomObject(CreateCustomObjectRequest request) throws ServiceException {
		try (P8ProviderImpl p8ProviderImpl = authService.createConnection(request)) {
			return createCustomObject(request, p8ProviderImpl);
		}
	}

	/**
	 * Creates the document.
	 *
	 * @param request the create document request
	 * @param p8ProviderImpl FileNet provider
	 * @return the operation results. Content information will not be populated 
	 */
	public CreateCustomObjectResponse createCustomObject(CreateCustomObjectRequest request, P8ProviderImpl p8ProviderImpl) {
		log.debug("createDocument.Entry {}", request);
		CreateCustomObjectResponse result = new CreateCustomObjectResponse();
		try {
			if (request.getP8Object() == null)
				throw new ServiceException(DocumentServiceExceptionCodes.MISSING_PARAMETERS.getExceptionCode());
			if (!StringUtils.hasText(request.getP8Object().getDocumentClass()))
				throw new ServiceException(DocumentServiceExceptionCodes.MISSING_DOCUMENT_CLASS.getExceptionCode());
			P8ContentObjectAuditInfo auditInfo = new P8ContentObjectAuditInfo();
			CustomObject document = p8ProviderImpl.createCustomObject(request.getP8Object(), auditInfo);
			result.setP8Object(p8ProviderImpl.createP8Object(document, false));
			result.setP8CustomObjectId(document.get_Id().toString());
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
	 * Updates custom object metadata.
	 *
	 * @param request the update custom object request
	 * @return the operation results
	 * @throws ServiceException  Signals that Filenet access info is not configured correctly
	 */
	public DeleteObjectByIdResponse deleteCustomObject(DeleteObjectByIdRequest request)
			throws ServiceException {
		return deleteCustomObject(request, false);
	}

	/**
	 * Updates custom object metadata.
	 *
	 * @param request the update custom object request
	 * @return the operation results
	 * @throws ServiceException  Signals that Filenet access info is not configured correctly
	 */
	public DeleteObjectByIdResponse deleteCustomObject(DeleteObjectByIdRequest request,boolean useBatchUpdate)
			throws ServiceException {
		try (P8ProviderImpl p8ProviderImpl = authService.createConnection(request)) {
			if (useBatchUpdate)
				p8ProviderImpl.createUpdatingBatch();
			DeleteObjectByIdResponse result = deleteCustomObject(request, p8ProviderImpl);
			if (useBatchUpdate)
				p8ProviderImpl.commitUpdateBatch();
			return result;
		}
	}

	/**
	 * Updates custom object metadata.
	 *
	 * @param request the update custom object request
	 * @return the operation results
	 * @throws ServiceException  Signals that Filenet access info is not configured correctly
	 */
	public DeleteObjectByIdResponse deleteCustomObject(DeleteObjectByIdRequest request,P8ProviderImpl p8ProviderImpl)
			throws ServiceException {
		DeleteObjectByIdResponse result = new DeleteObjectByIdResponse();
		try {
			p8ProviderImpl.deleteCustomObjectById(request.getObjectId());
		} catch (Exception e) {
			utilities.setResponseErrors(result, e, request.getObjectId());
			if (!(e instanceof ServiceException))
				log.error("updateDocumentMetadata method catched exception. Request: {}, Response: {}", request, result,
						e);
		}
		log.debug("deleteCustomObject.Exit {}", result);
		return result;
	}

	/**
	 * Gets the document metadata by id.
	 *
	 * @param request the get document data request
	 * @return the operation results
	 */
	public GetCustomObjectByIdResponse getCustomObjectMetadataById(GetCustomObjectByIdRequest request) {
		log.debug("GetCustomObjectByIdResponse.Entry {}", request);
		GetCustomObjectByIdResponse result = new GetCustomObjectByIdResponse();
		try (P8ProviderImpl p8ProviderImpl = authService.createConnection(request)) {
			CustomObject document = p8ProviderImpl.getCustomObjectById(request.getCustomObjectId(),
					utilities.getPropertiesToInclude(request.getPropertiesListName()));
			if (document == null) {
				utilities.setResponseErrors(result,
						GeneralExceptionCodes.DOCUMENT_NOT_FOUND_BY_ID_EXCEPTION.getExceptionCode(),
						new Object[] { request.getCustomObjectId() });
			} else {
				P8Object p8Object = p8ProviderImpl.createP8Object(document);
				result.setP8Object(p8Object);
			}
		} catch (Exception e) {
			utilities.setResponseErrors(result, e, request.getCustomObjectId());
			if (!(e instanceof ServiceException))
				log.error("getDocumentMetadataById method catched exception. Request: {}, Response: {}", request,
						result, e);
		}
		log.debug("GetCustomObjectByIdResponse.Exit {}", result);
		return result;
	}

	/**
	 * Updates custom object metadata.
	 *
	 * @param request the update custom object request
	 * @return the operation results
	 * @throws ServiceException  Signals that Filenet access info is not configured correctly
	 */
	public UpdateDocumentMetadataResponse updateCustomObjectMetadata(UpdateDocumentMetadataRequest request)
			throws ServiceException {
		return updateCustomObjectMetadata(request, true);
	}

	/**
	 * Updates custom object metadata.
	 *
	 * @param request the update custom object request
	 * @param useBatchUpdate if set to true {@link UpdatingBatch} will be used to save data in FileNet
	 * @return the operation results
	 * @throws ServiceException  Signals that Filenet access info is not configured correctly
	 */
	public UpdateDocumentMetadataResponse updateCustomObjectMetadata(UpdateDocumentMetadataRequest request,
			boolean useBatchUpdate) throws ServiceException {
		try (P8ProviderImpl p8ProviderImpl = authService.createConnection(request)) {
			if (useBatchUpdate)
				p8ProviderImpl.createUpdatingBatch();
			UpdateDocumentMetadataResponse result = updateCustomObjectMetadata(request, p8ProviderImpl);
			if (useBatchUpdate)
				p8ProviderImpl.commitUpdateBatch();
			return result;
		}
	}

	/**
	 * Updates custom object metadata.
	 *
	 * @param request the update custom object request
	 * @param p8ProviderImpl FileNet provider
	 * @return the operation results
	 */
	public UpdateDocumentMetadataResponse updateCustomObjectMetadata(UpdateDocumentMetadataRequest request,
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
			List<CustomObject> customObjects = new ArrayList<>(10);

			if (StringUtils.hasText(request.getId())) {
				customObjects.add(p8ProviderImpl.getCustomObjectById(request.getId()));
			} else {
				String query = utilities.prepareQuery(request.getSearchData(), p8ProviderImpl);
				for (IndependentlyPersistableObject ipo : p8ProviderImpl.searchDocuments(query).getSearchResults())
					customObjects.add((CustomObject) ipo);
			}
			List<P8ObjectAuditInfo> auditInfos = new ArrayList<>();
			int count = p8ProviderImpl.updateCustomObjectMetadata(customObjects,  props, auditInfos);

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
