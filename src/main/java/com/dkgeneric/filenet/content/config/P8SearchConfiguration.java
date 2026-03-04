package com.dkgeneric.filenet.content.config;

import java.util.HashMap;
import java.util.Map;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.PropertySource;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import com.dkgeneric.commons.service.AppJsonConfigurationService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;

import jakarta.annotation.PostConstruct;
import lombok.Getter;
import lombok.Setter;

/**
 * This class allows access to FileNet search configuration. Configuration will
 * be loaded through spring boot configuration
 * as @PropertySource("${filenet.searchconfigurationpath:classpath:com/davita/ecm/p8/content/resources/dummy.properties}")
 */
@Component("p8contentlibP8SearchConfiguration")
@ConfigurationProperties(prefix = "p8contentlib")
@PropertySource("${filenet.searchconfigurationpath:classpath:com/dkgeneric/filenet/content/resources/dummy.properties}")
public class P8SearchConfiguration {

	public static final String P8_SEARCH_CONFIGURATION_KEY = "p8searchconfiguration";
	public static final String P8_SEARCH_TEMPLATES_KEY = "searchtemplates";
	public static final String P8_PROPERTIES_TO_INCLUDE_KEY = "propertiestoinclude";

	private final Environment env;
	private final AppJsonConfigurationService configurationService;

	public P8SearchConfiguration(Environment env, AppJsonConfigurationService configurationService) {
		this.env = env;
		this.configurationService = configurationService;
	}

	@Getter
	@Setter
	private Map<String, String> searchTemplate = new HashMap<>();
	@Getter
	@Setter
	private Map<String, String> propertiesToInclude = new HashMap<>();

	public String getDefaultDVADocSource() {
		String result = env.getProperty("p8contentlib.defaultDVADocSource");
		return StringUtils.hasText(result) ? result : "ECM_RS";
	}

	/**
	 * Gets the comma separated property list to include in query string. In
	 * property file this name should be prefixed with
	 * "p8contentlib.propertiestoinclude."
	 *
	 * @param propertyListName the property list name
	 * @return the properties to include
	 */
	public String getPropertiesToInclude(String propertyListName) {

		String result = null;
		if (StringUtils.hasText(propertyListName)) {
			result = propertiesToInclude.get(propertyListName);
			if (!StringUtils.hasText(result))
				result = env.getProperty("p8contentlib.propertiestoinclude." + propertyListName);
		}
		if (!StringUtils.hasText(result))
			result = env.getProperty("p8contentlib.propertiestoinclude.default");
		return result;
	}

	/**
	 * Gets query string from property file. In property file this name should be
	 * prefixed with "p8contentlib.searchtemplate."
	 *
	 * @param templateName the template name
	 * @return the query
	 */
	public String getQuery(String templateName) {
		String result = null;
		if (StringUtils.hasText(templateName)) {
			result = searchTemplate.get(templateName);
			if (!StringUtils.hasText(result))
				result = env.getProperty("p8contentlib.searchtemplate." + templateName);
		}
		return result;
	}

	@PostConstruct
	public void postConstruct() throws JsonProcessingException {
		if (configurationService != null && configurationService.getAppConfiguration() != null
				&& configurationService.getAppConfiguration().has(P8_SEARCH_CONFIGURATION_KEY)) {
			JsonNode searchCfg = configurationService.getAppConfiguration().get(P8_SEARCH_CONFIGURATION_KEY);
			if (searchCfg.has(P8_SEARCH_TEMPLATES_KEY))
				searchCfg.get(P8_SEARCH_TEMPLATES_KEY).fields().forEachRemaining(entry -> {
					if (searchTemplate.get(entry.getKey()) == null)
						searchTemplate.put(entry.getKey(), entry.getValue().asText());
				});
			if (searchCfg.has(P8_PROPERTIES_TO_INCLUDE_KEY))
				searchCfg.get(P8_PROPERTIES_TO_INCLUDE_KEY).fields().forEachRemaining(entry -> {
					if (propertiesToInclude.get(entry.getKey()) == null)
						propertiesToInclude.put(entry.getKey(), entry.getValue().asText());
				});
		}
	}
}
