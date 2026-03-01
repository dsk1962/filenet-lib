package com.dkgeneric.filenet.content.exceptioncodes;

/**
 * The Enum GeneralExceptionCodes. General exception codes.
 */
public enum GeneralExceptionCodes {

	/** Document with specific id can't be found. */
	DOCUMENT_NOT_FOUND_BY_ID_EXCEPTION("p8contentlib.generalexception.documentnotfoundbyid"),

	/** General exception code. */
	UNCAUGHT_EXCEPTION("p8contentlib.generalexception.uncaughtexception");

	/** The exception code. */
	/* for getting the value of enums */
	private String exceptionCode;

	/**
	 * Instantiates a new general exception codes.
	 *
	 * @param exceptionCode the exception code
	 */
	private GeneralExceptionCodes(String exceptionCode) {
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
