package com.dkgeneric.filenet.content.service;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import com.dkgeneric.filenet.content.common.ServiceException;
import com.dkgeneric.filenet.content.common.Utilities;
import com.dkgeneric.filenet.content.config.ApplicationConfig;
import com.dkgeneric.filenet.content.request.GetClassDefinitionRequest;
import com.dkgeneric.filenet.content.response.GetClassDefinitionResponse;

import lombok.extern.slf4j.Slf4j;

/**
 * The Content Service class.
 */
@Component("p8contentlibSchemaService")
@ConditionalOnProperty(name = "com.davita.ecm.p8.content.service.enabled", matchIfMissing = true)

/** The Constant log. */
@Slf4j
public class SchemaService extends AuthorizationBasedService {

	public SchemaService(@Qualifier("p8contentlibAuthService") AuthService authService,
			@Qualifier("p8ContentLibConfig") ApplicationConfig clientConfig,
			@Qualifier("p8contentlibUtilities") Utilities utilities, ValidationService validationService) {
		super(authService, clientConfig, utilities, validationService);
	}

	/**
	 * Gets the class definitions. If object store is not specified in input parameter application configured object store will be used.
	 *
	 * @param request the request
	 * @return the class definitions
	 */
	public GetClassDefinitionResponse getClassDefinitions(GetClassDefinitionRequest request) {
		log.debug("getClassDefinitions.Entry {}", request);
		GetClassDefinitionResponse result = new GetClassDefinitionResponse();
		try {
			result.setClassDefinitions(authService.getClassDefinitions(request, request.getSymbolicNames()));
		} catch (Exception e) {
			utilities.setResponseErrors(result, e);
			if (!(e instanceof ServiceException))
				log.error("getClassDefinitions method catched exception. Request: {}, Response: {}", request, result,
						e);
		}
		log.debug("getClassDefinitions.Exit {}", result);
		return result;
	}

}
