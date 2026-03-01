package com.dkgeneric.filenet.content.exceptioncodes;

/**
 * The Enum GeneralExceptionCodes. General exception codes.
 */
public enum MappingServiceExceptionCodes {

	/** Document with specific id can't be found. */
	NO_DOCCLASS_MAPPING_EXCEPTION("p8contentlib.mappng.exception.nodocumentclassmapping"),

	/** General exception code. */
	EMPTY_SEARCH_FILTER_EXCEPTION("p8contentlib.mappng.exception.emptysearchfilter");

	/** The exception code. */
	/* for getting the value of enums */
	private String exceptionCode;

	/**
	 * Instantiates a new general exception codes.
	 *
	 * @param exceptionCode the exception code
	 */
	private MappingServiceExceptionCodes(String exceptionCode) {
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
