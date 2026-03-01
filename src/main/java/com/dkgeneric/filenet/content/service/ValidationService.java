package com.dkgeneric.filenet.content.service;

import static com.dkgeneric.filenet.content.model.PropertyDefinition.*;

import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.time.temporal.ChronoField;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.PropertySource;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import com.dkgeneric.commons.exceptions.InvalidRequestException;
import com.dkgeneric.commons.service.AppJsonConfigurationService;
import com.dkgeneric.filenet.content.model.ClassDefinition;
import com.dkgeneric.filenet.content.model.P8Object;
import com.dkgeneric.filenet.content.model.PropertyDefinition;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.filenet.api.util.Id;

import jakarta.annotation.PostConstruct;

@Component
@ConditionalOnProperty(name = "com.davita.ecm.p8.content.service.enabled", matchIfMissing = true)
@PropertySource("classpath:/com/dkgeneric/filenet/content/resources/p8default.properties")
public class ValidationService {

	private final AppJsonConfigurationService configurationService;
	public static final String P8_VALIDATION_CONFIGURATION_KEY = "p8validationfiguration";
	public static final String P8_DATEFORMAT_KEY = "dateFormats";

	public ValidationService(AppJsonConfigurationService configurationService) {
		this.configurationService = configurationService;
	}

	@Value("#{'${p8default.dateformats}'.split(',')}")
	private List<String> defaultDateFormats;

	private List<DateTimeFormatter> dateTimeFormatters = new ArrayList<>(10);

	public Date parseDate(String sDate) {
		for (DateTimeFormatter dtf : dateTimeFormatters)
			try {
				return new Date(Instant.from(dtf.parse(sDate)).toEpochMilli());
			} catch (Exception e) {
				// ignore
			}
		throw new InvalidRequestException("Unknown date format: " + sDate);
	}

	public void preprocessPropertyValues(Map<String, Object> properties, ClassDefinition classDefinition) {
		for (Map.Entry<String, Object> entry : properties.entrySet()) {
			PropertyDefinition propertyDefinition = classDefinition.getPropertyDefinition(entry.getKey());
			if (propertyDefinition == null)
				throw new InvalidRequestException(
						"Unknown property name: " + entry.getKey() + " for class " + classDefinition.getSymbolicName());
			if (entry.getValue() != null && (entry.getValue() instanceof String s)) {
				if (StringUtils.hasText(s))
					switch (propertyDefinition.getType()) {
					case BOOLEAN:
						entry.setValue(Boolean.parseBoolean(s));
						break;
					case LONG:
						entry.setValue(Integer.parseInt(s));
						break;
					case DOUBLE:
						entry.setValue(Integer.parseInt(s));
						break;
					case GUID:
						entry.setValue(new Id(s));
						break;
					case DATE:
						entry.setValue(parseDate(s));
						break;
					case BINARY:
						entry.setValue(s.getBytes());
						break;
					default:
						// No action required
					}
				else if (propertyDefinition.getType() == STRING)
					entry.setValue(s);
				else
					entry.setValue(null);
			}
		}
	}

	public void preprocessPropertyValues(P8Object p8Object, ClassDefinition classDefinition) {
		preprocessPropertyValues(p8Object.getProperties(), classDefinition);
	}

	@PostConstruct
	public void postConstruct() throws JsonProcessingException {
		List<String> allFormats = new ArrayList<>();
		if (configurationService != null && configurationService.getAppConfiguration() != null
				&& configurationService.getAppConfiguration().has(P8_VALIDATION_CONFIGURATION_KEY)) {
			JsonNode validationCfg = configurationService.getAppConfiguration().get(P8_VALIDATION_CONFIGURATION_KEY);
			if (validationCfg.has(P8_DATEFORMAT_KEY)) {
				allFormats.addAll(Arrays.asList(validationCfg.get(P8_DATEFORMAT_KEY).asText().split(",")));
			}
		}
		allFormats.addAll(defaultDateFormats);
		for (String format : allFormats)
			dateTimeFormatters.add(new DateTimeFormatterBuilder().appendPattern(format)
					.parseDefaulting(ChronoField.HOUR_OF_DAY, 0).toFormatter().withZone(ZoneId.systemDefault()));
		dateTimeFormatters.add(DateTimeFormatter.ISO_INSTANT);
	}
}
