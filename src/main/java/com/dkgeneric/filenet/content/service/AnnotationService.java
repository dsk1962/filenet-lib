package com.dkgeneric.filenet.content.service;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import com.dkgeneric.filenet.content.common.ServiceException;
import com.dkgeneric.filenet.content.common.Utilities;
import com.dkgeneric.filenet.content.config.ApplicationConfig;
import com.dkgeneric.filenet.content.exceptioncodes.DocumentServiceExceptionCodes;
import com.dkgeneric.filenet.content.provider.P8ProviderImpl;
import com.dkgeneric.filenet.content.request.CreateAnnotationRequest;
import com.dkgeneric.filenet.content.response.CreateAnnotationResponse;
import com.filenet.api.core.Annotation;

import lombok.extern.slf4j.Slf4j;

/**
 * The Document Service class. This class contains methods to manipulate FileNet objects
 */
@Component("p8contentlibAnnotationService")
@ConditionalOnProperty(name = "com.davita.ecm.p8.content.service.enabled", matchIfMissing = true)
@Slf4j
public class AnnotationService extends AuthorizationBasedService {

	public AnnotationService(@Qualifier("p8contentlibAuthService") AuthService authService,
			@Qualifier("p8ContentLibConfig") ApplicationConfig clientConfig,
			@Qualifier("p8contentlibUtilities") Utilities utilities, ValidationService validationService) {
		super(authService, clientConfig, utilities, validationService);
	}

	public CreateAnnotationResponse createAnnotation(CreateAnnotationRequest request) throws ServiceException {
		try (P8ProviderImpl p8ProviderImpl = authService.createConnection(request)) {
			return createAnnotation(request, p8ProviderImpl);
		}
	}
	
	public CreateAnnotationResponse createAnnotation(CreateAnnotationRequest request, P8ProviderImpl p8ProviderImpl) {
		log.debug("createAnnotation.Entry {}", request);
		CreateAnnotationResponse result = new CreateAnnotationResponse();
		try {
			if (request.getP8AnnotationObject() == null)
				throw new ServiceException(DocumentServiceExceptionCodes.MISSING_PARAMETERS.getExceptionCode());
			if (request.getP8AnnotationObject().getResource() == null)
				throw new ServiceException(DocumentServiceExceptionCodes.MISSING_RESOURCE.getExceptionCode());
			if (!StringUtils.hasText(request.getP8AnnotationObject().getP8AnnotationClassName()))
				throw new ServiceException(DocumentServiceExceptionCodes.MISSING_DOCUMENT_CLASS.getExceptionCode());

			Annotation annotation = p8ProviderImpl.createAnnotation(request.getP8AnnotationObject());
			result.setP8AnnotationId(annotation.get_Id().toString());
			
		} catch (Exception e) {
			utilities.setResponseErrors(result, e);
			if (!(e instanceof ServiceException))
				log.error("createAnnotation method catched exception. Request: {}, Response: {}", request, result, e);
		}
		log.debug("createAnnotation.Exit {}", result);
		return result;
	}
}
