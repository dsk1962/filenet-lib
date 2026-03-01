package com.dkgeneric.filenet.content.exceptioncodes;

/**
 * The Enum ConfigExceptionCodes. Contains application configuration specific
 * error code
 */
public enum ConfigExceptionCodes {

	/** FileNet content engine url is missing. */
	CE_URL_MISSING("p8contentlib.configexception.ceurl.missing"),

	/** The user name is required in operation, but is missing. */
	USER_NAME_MISSING("p8contentlib.configexception.username.missing"),

	/** The password is required in operation, but is missing. */
	PASSWORD_MISSING("p8contentlib.configexception.password.missing"),

	/** The object store name is required in operation, but is missing. */
	OBJECT_STORE_NAME_MISSING("p8contentlib.configexception.objectstorename.missing");

	/** The exception code. */
	private String exceptionCode;

	/**
	 * Disables new instance creation
	 *
	 * @param exceptionCode the exception code
	 */
	private ConfigExceptionCodes(String exceptionCode) {
		this.exceptionCode = exceptionCode;
	}

	/**
	 * Gets the exception code.
	 *
	 * @return the exception code
	 */
	public String getExceptionCode() {
		return exceptionCode;
	}

	/**
	 * To string.
	 *
	 * @return the string
	 */
	@Override
	public String toString() {
		return getExceptionCode();
	}
}
