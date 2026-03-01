package com.dkgeneric.filenet.content.service;

import java.io.IOException;
import java.util.Base64;

import org.springframework.beans.factory.annotation.Qualifier;

import com.dkgeneric.filenet.content.common.ServiceException;
import com.dkgeneric.filenet.content.common.Utilities;
import com.dkgeneric.filenet.content.config.ApplicationConfig;
import com.dkgeneric.filenet.content.model.P8ContentObject;

import lombok.Getter;

/**
 * The Base Service class.
 */
public class BaseService {

	/** The client config. */
	@Getter
	protected final ApplicationConfig clientConfig;

	/** The utilities. */
	protected final Utilities utilities;

	@Getter
	protected final ValidationService validationService;

	public BaseService(@Qualifier("p8ContentLibConfig") ApplicationConfig clientConfig,
			@Qualifier("p8contentlibUtilities") Utilities utilities, ValidationService validationService) {
		this.clientConfig = clientConfig;
		this.utilities = utilities;
		this.validationService = validationService;
	}

	protected Base64.Encoder base64Encoder = Base64.getEncoder();
	protected Base64.Decoder base64Decoder = Base64.getDecoder();

	protected String getEncodedContent(P8ContentObject p8ContentObject) throws IOException, ServiceException {
		if (p8ContentObject != null && p8ContentObject.getResource() != null) {
			byte[] data = p8ContentObject.getResource().getBytes();
			if (data != null)
				return base64Encoder.encodeToString(data);
		}
		return null;
	}
}
