package com.dkgeneric.filenet.content.exceptioncodes;

/**
 * The Enum P8ExceptionCodes. FileNet specific exception codes
 */
public enum P8ExceptionCodes {

	/**
	 * Search operation returns more than one documents, but single document was
	 * expected..
	 */
	SEARCH_MULTIPLE_RETURN("p8contentlib.p8exception.search.multiple.return"),

	/** An attempt to commit batch changes, but batch was not created. */
	BATCH_NOT_CREATED("p8contentlib.p8exception.batchnotcreated"),

	/** General FileNet exception code. */
	ENGINE_RUNTIME_EXCEPTION_THROWN("p8contentlib.p8exception.engineruntimeexceptionthrown"),

	/** Unknown document resource. */
	UNKNOWN_RESOURCE_OBJECT("p8contentlib.p8exception.unknown.resourceobject"),

	/** Unknown document resource. */
	BROKEN_RESOURCE_OBJECT("p8contentlib.p8exception.broken.resourceobject"),

	/** Unknown document resource. */
	UNKNOWN_PROPERTY_NAME("p8contentlib.p8exception.unknown.propertyname");

	/** The exception code. */
	private String exceptionCode;

	/**
	 * Instantiates a new p 8 exception codes.
	 *
	 * @param exceptionCode the exception code
	 */
	private P8ExceptionCodes(String exceptionCode) {
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
