package com.dkgeneric.filenet.content.service;

import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.Date;

import org.springframework.stereotype.Component;

import com.dkgeneric.commons.service.MessageValueConverter;
import com.dkgeneric.filenet.content.config.ApplicationConfig;
import com.filenet.api.util.Id;

@Component("p8MessageValueConverter")
public class P8MessageValueConverter implements MessageValueConverter {

	private final DateTimeFormatter dtfISO861;

	public P8MessageValueConverter(ApplicationConfig applicationConfig) {
		dtfISO861 = DateTimeFormatter.ofPattern(applicationConfig.getJsonUTCOutputDateFormat());
	}

	@Override
	public Object convertValue(Object value) {
		if (value instanceof Id id)
			return id.toString();
		if (value instanceof Date dt)
			return dt.toInstant().atOffset(ZoneOffset.UTC).format(dtfISO861);
		return value;
	}

	@Override
	public int getOrder() {
		return 100;
	}

}
