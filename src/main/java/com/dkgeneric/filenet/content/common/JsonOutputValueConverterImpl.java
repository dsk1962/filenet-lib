package com.dkgeneric.filenet.content.common;

import java.text.SimpleDateFormat;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.Date;

import org.springframework.util.StringUtils;

import com.dkgeneric.commons.common.JsonOutputValueConverter;
import com.dkgeneric.filenet.content.config.ApplicationConfig;

public class JsonOutputValueConverterImpl implements JsonOutputValueConverter {

	private SimpleDateFormat sdf = null;
	private DateTimeFormatter dtfISO861 = null;

	public JsonOutputValueConverterImpl(ApplicationConfig applicationConfig) {
		if (StringUtils.hasText(applicationConfig.getJsonUTCOutputDateFormat()))
			dtfISO861 = DateTimeFormatter.ofPattern(applicationConfig.getJsonUTCOutputDateFormat());
		else if (StringUtils.hasText(applicationConfig.getJsonOutputDateFormat()))
			sdf = new SimpleDateFormat(applicationConfig.getJsonOutputDateFormat());
	}

	@Override
	public Object convertValue(Object value) {
		if (value == null)
			return null;
		if (value instanceof Date dt) {
			if (dtfISO861 != null)
				return dt.toInstant().atOffset(ZoneOffset.UTC).format(dtfISO861);
			if (sdf != null)
				return sdf.format(dt);
		}
		return value.toString();
	}

}
