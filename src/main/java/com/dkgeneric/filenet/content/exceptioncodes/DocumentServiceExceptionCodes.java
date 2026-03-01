package com.dkgeneric.filenet.content.exceptioncodes;

/**
 * The Enum DocumentServiceExceptionCodes. Document specific exception codes
 */
public enum DocumentServiceExceptionCodes {

	/** Document class name is required, but missing in request. */
	MISSING_DOCUMENT_CLASS("p8contentlib.documentservice.exception.missingdocumentclass"),

	/** Required document parameters is missnf in request. */
	MISSING_DOCUMENT_PARAMETERS("p8contentlib.documentservice.exception.missingdocumentparameters"),

	/** Parameters required to process operation are missing. */
	MISSING_PARAMETERS("p8contentlib.documentservice.exception.missingparameters"),

	/** Document properties required to process operation are missing. */
	MISSING_PROPERTIES_PARAMETERS("p8contentlib.documentservice.exception.missingpropertiesparameters"),

	/** Document resource(attachment) is missing */
	MISSING_RESOURCE("p8contentlib.documentservice.exception.missingresource"),

	/** Document resource(attachment) is missing */
	MULTIPLE_ATTACHMENTS_NOT_SUPPORTED("p8contentlib.documentservice.exception.multipleattachmentsnotsupported"),

	/** Mapped property is required */
	MAPPED_PROPERTY_REQUIRED("p8contentlib.documentservice.exception.mappedpropertyrequired"),

	/** Mapped property is required */
	ATTACHMENT_DATA_IS_REQUIRED("p8contentlib.documentservice.exception.attachmentdatarequired"),
	
	/** Requested document is already checked out. */
	DOCUMENT_ALREADY_CHECKED_OUT("p8contentlib.documentservice.exception.documentalreadycheckedout");

	/** The exception code. */
	/* for getting the value of enums */
	private String exceptionCode;

	/**
	 * Instantiates a new document service exception codes.
	 *
	 * @param exceptionCode the exception code
	 */
	private DocumentServiceExceptionCodes(String exceptionCode) {
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
