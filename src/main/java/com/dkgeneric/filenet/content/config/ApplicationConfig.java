package com.dkgeneric.filenet.content.config;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.util.StringUtils;

import com.dkgeneric.commons.common.ApplicationValue;
import com.dkgeneric.commons.config.LibConfig;
import com.dkgeneric.commons.service.AppJsonConfigurationService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import jakarta.annotation.PostConstruct;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * POJO class to keep FileNet access configurations <br>
 * {@link #toString()} implemented through Lombok @ToString. cpePassword will
 * not be included.
 */
@Configuration("p8ContentLibConfig")
@ToString(exclude = { "cpePassword" })
public class ApplicationConfig {

	public static final String P8_CONFIGURATION_ENTRY = "p8Cfg";
	private static final String P8_INSTANCE_CFG_JSON_KEY = "p8InstanceCfg";
	private final AppJsonConfigurationService jsonConfigurationService;
	private final LibConfig commonsLibConfig;
	private final ObjectMapper objectMapper;

	@Autowired
	public ApplicationConfig(AppJsonConfigurationService jsonConfigurationService, LibConfig commonsLibConfig,
			ObjectMapper objectMapper) {
		this.jsonConfigurationService = jsonConfigurationService;
		this.commonsLibConfig = commonsLibConfig;
		this.objectMapper = objectMapper;
	}

	public ApplicationConfig() {
		this.jsonConfigurationService = null;
		this.commonsLibConfig = null;
		this.objectMapper = null;
	}

	/**
	 * Indexed property name list. 
	 * configuration as @ApplicationValue(key = "/p8Cfg/p8InstanceCfg/indexedProperties")
	 * 
	 * @param indexedProperties the new indexed property name list
	 * @return Current indexed property name list
	 */
	@Getter
	@Setter
	@ApplicationValue(key = "/p8Cfg/p8InstanceCfg/indexedProperties")
	private List<String> indexedProperties;

	/**
	 * Resource type to document class mapping. 
	 * configuration as @ApplicationValue(key = "/p8Cfg/p8InstanceCfg/resourceTypeMapping")
	 * 
	 * @param resourceTypeMapping the new resource type to document class mapping
	 * @return Current resource type to document class mapping
	 */
	@Getter
	@Setter
	@ApplicationValue(key = "/p8Cfg/p8InstanceCfg/resourceTypeMapping")
	private Map<String, String> resourceTypeMapping = new HashMap<>();

	/**
	 * FileNet Content engine connection URL. Should be set through spring boot
	 * configuration as @Value("${dva.ecm.cpe.url}")
	 * 
	 * @param cpeUrl the new connection URL
	 * @return Current connection URL
	 */
	@Getter
	@Setter
	@Value("${dva.ecm.cpe.url:#{null}}")
	private String cpeUrl;

	/**
	 * Login module to create connection. Should be set through spring boot
	 * configuration as @Value("${dva.ecm.cpe.loginmodule:#{null}}")
	 * 
	 * @param cpeLoginModule the new login module name
	 * @return Current login module name
	 */
	@Getter
	@Setter
	@Value("${dva.ecm.cpe.loginmodule:#{null}}")
	private String cpeLoginModule;

	/**
	 * Proxy user name. Should be set through spring boot configuration
	 * as @Value("${dva.ecm.cpe.username:#{null}}")
	 * 
	 * @param cpeUsername the new proxy user name
	 * @return Current proxy user name
	 */
	@Getter
	@Setter
	@Value("${dva.ecm.cpe.username:#{null}}")
	private String cpeUsername;

	/**
	 * Proxy user password. Should be set through spring boot configuration
	 * as @Value("${dva.ecm.cpe.url}"). Should be set through spring boot
	 * configuration as @Value("${dva.ecm.cpe.password:#{null}}")
	 * 
	 * @param cpePassword the new property password
	 * @return Current property password
	 */
	@Getter
	@Setter
	@Value("${dva.ecm.cpe.password:#{null}}")
	private String cpePassword;

	/**
	 * Default FileNet domain name. Should be set through spring boot configuration
	 * as @Value("${dva.ecm.cpe.domainname:#{null}}"). If null default FileNet
	 * domain will be used.
	 * 
	 * @param domainName the new domain name
	 * @return Current domain name
	 */
	@Getter
	@Setter
	@Value("${dva.ecm.cpe.domainname:#{null}}")
	private String domainName;

	/**
	 * Default FileNet object store symbolic name. Should be set through spring boot
	 * configuration as @Value("${dva.ecm.cpe.objectstorename:#{null}}")
	 * 
	 * @param objectStoreName the new object store name
	 * @return Current object store name
	 */
	@Getter
	@Setter
	@Value("${dva.ecm.cpe.objectstorename:#{null}}")
	private String objectStoreName;

	/**
	 * If set to true class definitions in default object store will be populated as part of application startup.
	 * Default false.  
	 * configuration as @Value("${dva.ecm.cpe.populate.schema.onstart:#{false}}")
	 * 
	 * @param populateDefaultSchemaOnStart the populateDefaultSchemaOnStart value
	 * @return Current populateDefaultSchemaOnStart value
	 */
	@Getter
	@Setter
	@Value("${dva.ecm.cpe.populate.schema.onstart:#{false}}")
	private boolean populateDefaultSchemaOnStart;

	/**
	 * Default max result set size. Should be set through spring boot configuration
	 * as @Value("${dva.ecm.cpe.maxresultsetsize:#{-1}}").
	 * 
	 * @param maxResultSetSize the new max result set size
	 * @return Current max result set size
	 */
	@Getter
	@Setter
	@Value("${dva.ecm.cpe.maxresultsetsize:#{-1}}")
	private int maxResultSetSize;

	/**
	 * Default max result set size. Should be set through spring boot configuration
	 * as @Value("${dva.ecm.cpe.maxresultsetsize:#{-1}}").
	 * 
	 * @param maxResultSetSize the new max result set size
	 * @return Current max result set size
	 */
	@Getter
	@Setter
	private int searchTimeLimit=180;

	/**
	 * Default batch size. Should be set through spring boot configuration
	 * as @Value("${dva.ecm.cpe.batchsize:#{100}}").
	 * 
	 * @param batchSize the new batch size
	 * @return Current batch size
	 */
	@Getter
	@Setter
	@Value("${dva.ecm.cpe.batchsize:#{100}}")
	private int batchSize;

	@Getter
	@Setter
	private String jsonOutputDateFormat;
	@Getter
	@Setter
	private String jsonUTCOutputDateFormat = "yyyy-MM-dd'T'HH:mm:ssX";

	@ApplicationValue(key = "/p8Cfg/p8InstanceCfg/brokenResourceErrorCodes")
	@Getter
	@Setter
	private List<String> brokenResourceErrorCodes = Arrays.asList(new String[] { "FNRCC0019" });

	private void applyExternalizedValues(ApplicationConfig applicationConfig) {
		// initialize blank properties only
		if (!StringUtils.hasText(cpeLoginModule))
			cpeLoginModule = applicationConfig.getCpeLoginModule();
		if (!StringUtils.hasText(cpePassword))
			cpePassword = applicationConfig.getCpePassword();
		if (!StringUtils.hasText(cpeUrl))
			cpeUrl = applicationConfig.getCpeUrl();
		if (!StringUtils.hasText(cpeUsername))
			cpeUsername = applicationConfig.getCpeUsername();
		if (!StringUtils.hasText(domainName))
			domainName = applicationConfig.getDomainName();
		if (maxResultSetSize == -1)
			maxResultSetSize = applicationConfig.getMaxResultSetSize();
		searchTimeLimit = applicationConfig.getSearchTimeLimit();
		if (!StringUtils.hasText(objectStoreName))
			objectStoreName = applicationConfig.getObjectStoreName();
		if (!populateDefaultSchemaOnStart)
			populateDefaultSchemaOnStart = applicationConfig.isPopulateDefaultSchemaOnStart();
		if (StringUtils.hasText(applicationConfig.getJsonOutputDateFormat()))
			jsonOutputDateFormat = applicationConfig.getJsonOutputDateFormat();
		if (StringUtils.hasText(applicationConfig.getJsonUTCOutputDateFormat()))
			jsonUTCOutputDateFormat = applicationConfig.getJsonUTCOutputDateFormat();
	}

	@PostConstruct
	public void postConstruct() throws JsonProcessingException {
		JsonNode cfg = jsonConfigurationService.getAppConfiguration();
		if (cfg == null)
			return;
		if (!cfg.has(P8_CONFIGURATION_ENTRY))
			return;
		cfg = cfg.get(P8_CONFIGURATION_ENTRY);
		ApplicationConfig applicationConfig = cfg.has(P8_INSTANCE_CFG_JSON_KEY)
				? objectMapper.convertValue(cfg.get(P8_INSTANCE_CFG_JSON_KEY), ApplicationConfig.class)
				: null;
		if (applicationConfig != null)
			applyExternalizedValues(applicationConfig);
	}

}
