package com.dkgeneric.filenet.content.service;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import com.dkgeneric.filenet.content.common.Utilities;
import com.dkgeneric.filenet.content.config.ApplicationConfig;

/**
 * Base class for services that require authorization
 */
@Component
@ConditionalOnProperty(name = "com.davita.ecm.p8.content.service.enabled", matchIfMissing = true)
public class AuthorizationBasedService extends BaseService {

	/** The auth service. */
	protected final AuthService authService;

	public AuthorizationBasedService(@Qualifier("p8contentlibAuthService") AuthService authService,
			@Qualifier("p8ContentLibConfig") ApplicationConfig clientConfig,
			@Qualifier("p8contentlibUtilities") Utilities utilities, ValidationService validationService) {
		super(clientConfig, utilities, validationService);
		this.authService = authService;
	}
}
