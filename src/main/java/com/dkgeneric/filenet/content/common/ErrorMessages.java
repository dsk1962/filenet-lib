package com.dkgeneric.filenet.content.common;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import org.springframework.stereotype.Component;

import com.dkgeneric.commons.config.CommonsErrorMessages;

import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;

/**
 * The Class ErrorMessages. This is a helper class that allows to get error
 * code, type or message based on error key. Error key is a string value. For
 * example see enums in this package
 * {@link com.dkgeneric.filenet.content.exceptioncodes}
 */

@Component("p8contentlibErrorMessages")
@Slf4j
public class ErrorMessages {

	private final CommonsErrorMessages errorMessages;

	public ErrorMessages(CommonsErrorMessages ecmErrorMessages) {
		this.errorMessages = ecmErrorMessages;
	}

	/**
	 * Instantiates a new error messages and loads initial error message
	 * codes/messages.
	 */
	@PostConstruct
	public void postConstruct() {
		try (InputStream is = ErrorMessages.class
				.getResourceAsStream("/com/dkgeneric/filenet/content/resources/errormessages.properties")) {

			if (is != null) {
				Properties temp = new Properties();
				temp.load(is);
				errorMessages.getProperties().putAll(temp);
			}
		} catch (IOException e) {
			log.error("Failed to load /com/dkgeneric/filenet/content/resources/errormessages.properties.", e);
		}
	}

}
