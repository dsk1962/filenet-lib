package com.dkgeneric.filenet.content.exceptioncodes;

/**
 * The Enum ContentServiceExceptionCodes. Conetnt service specific exception
 * codes
 */
public enum ContentServiceExceptionCodes {

	/** Input parameters needed to retrieve document are missing. */
	MISSING_INPUT_PARAMETERS("p8contentlib.contentservice.exception.missingparameters"),

	/** Requested document was not found. */
	DOCUMENT_NOT_FOUND("p8contentlib.contentservice.exception.documentnotfound");

	/** The exception code. */
	/* for getting the value of enums */
	private String exceptionCode;

	/**
	 * Instantiates a new content service exception codes.
	 *
	 * @param exceptionCode the exception code
	 */
	private ContentServiceExceptionCodes(String exceptionCode) {
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
}
