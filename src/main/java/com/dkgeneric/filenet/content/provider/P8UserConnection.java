package com.dkgeneric.filenet.content.provider;

import com.dkgeneric.filenet.content.common.ServiceException;
import com.dkgeneric.filenet.content.service.AuthService;

/**
 * The Class P8UserConnection. Creates new connection to FileNet with currently
 * authenticated user
 */
public class P8UserConnection extends P8ProviderImpl {

	/**
	 * Instantiates a new FileNet connection with current user(authenticated by
	 * container).
	 *
	 * @param authService the authentication service
	 * @throws ServiceException propagates authenticationService exception  
	 */
	public P8UserConnection(AuthService authService) throws ServiceException {
		this.authService = authService;
		connection = this.authService.getUserConnection();
	}

}
