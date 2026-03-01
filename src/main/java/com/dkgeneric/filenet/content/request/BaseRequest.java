package com.dkgeneric.filenet.content.request;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

/**
 * POJO base class for request objects <br>
 * {@link #toString()} implemented through Lombok @ToString.
 */
@Getter
@Setter
@NoArgsConstructor
@ToString(exclude = { "password" })
public class BaseRequest {

	/**
	 * This flag allows to control if proxy user will be used to create FileNet
	 * connection (false, default).
	 * 
	 * @param useContainerAuthentication if true than no proxy user will be used to
	 *                                   connect to FileNet
	 * @return Current flag value
	 */
	boolean useContainerAuthentication = false;

	/**
	 * User name to be used to connect to FileNet
	 * 
	 * @param userName the new user name
	 * @return Current user name
	 */
	private String userName;

	/**
	 * Password to be used to connect to FileNet
	 * 
	 * @param password the new password
	 * @return Current password value
	 */
	private String password;

	/**
	 * FileNet object store name
	 * 
	 * @param objectStoreName the new object store name
	 * @return Current object store name
	 */
	private String objectStoreName;

	/**
	 * FileNet domain name
	 * 
	 * @param domainName the new domain name
	 * @return Current domain name
	 */
	private String domainName;
}
