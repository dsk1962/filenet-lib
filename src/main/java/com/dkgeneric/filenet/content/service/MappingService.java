package com.dkgeneric.filenet.content.service;

import java.io.IOException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import com.dkgeneric.commons.common.JsonOutputValueConverter;
import com.dkgeneric.commons.exceptions.FilenetPropMappingNotFoundException;
import com.dkgeneric.commons.model.json.JsonContent;
import com.dkgeneric.commons.model.json.JsonDocument;
import com.dkgeneric.commons.model.json.JsonObject;
import com.dkgeneric.commons.model.json.JsonProperty;
import com.dkgeneric.commons.model.json.JsonResource;
import com.dkgeneric.commons.model.json.JsonSearch;
import com.dkgeneric.commons.model.json.JsonSearchCondition;
import com.dkgeneric.commons.model.json.JsonSortByCondition;
import com.dkgeneric.commons.service.FilenetPropMappingService;
import com.dkgeneric.filenet.content.common.ECMConstants;
import com.dkgeneric.filenet.content.common.JsonOutputValueConverterImpl;
import com.dkgeneric.filenet.content.common.ServiceException;
import com.dkgeneric.filenet.content.common.Utilities;
import com.dkgeneric.filenet.content.config.ApplicationConfig;
import com.dkgeneric.filenet.content.exceptioncodes.DocumentServiceExceptionCodes;
import com.dkgeneric.filenet.content.exceptioncodes.MappingServiceExceptionCodes;
import com.dkgeneric.filenet.content.exceptioncodes.P8ExceptionCodes;
import com.dkgeneric.filenet.content.model.P8ContentObject;
import com.dkgeneric.filenet.content.model.P8Object;
import com.dkgeneric.filenet.content.model.PropertyDefinition;
import com.dkgeneric.filenet.content.model.SearchCondition;
import com.dkgeneric.filenet.content.model.SearchData;
import com.dkgeneric.filenet.content.provider.P8ProviderImpl;
import com.dkgeneric.filenet.content.request.BaseRequest;
import com.dkgeneric.filenet.content.request.CreateDocumentRequest;
import com.dkgeneric.filenet.content.request.UpdateDocumentMetadataRequest;
import com.dkgeneric.filenet.content.resources.P8ContentResource;
import com.dkgeneric.jpa.commons.filenet.model.FilenetPropMapping;

import lombok.Getter;

@Component
@ConditionalOnProperty(name = "com.davita.ecm.p8.content.service.enabled", matchIfMissing = true)
public class MappingService extends BaseService {

	public MappingService(FilenetPropMappingService p8PropMappingService,
			ObjectProvider<JsonOutputValueConverter> jsonOutputValueConverterProvider, AuthService authService,
			@Qualifier("p8ContentLibConfig") ApplicationConfig clientConfig,
			@Qualifier("p8contentlibUtilities") Utilities utilities, ValidationService validationService) {
		super(clientConfig, utilities, validationService);
		this.p8PropMappingService = p8PropMappingService;
		this.jsonOutputValueConverter = jsonOutputValueConverterProvider.getIfAvailable();
		this.authService = authService;
	}

	@Getter
	private final FilenetPropMappingService p8PropMappingService;
	@Getter
	private final JsonOutputValueConverter jsonOutputValueConverter;
	private final AuthService authService;

	public void addSearchConditions(String appId, List<JsonProperty> filter, SearchData searchData)
			throws FilenetPropMappingNotFoundException {
		if (filter != null)
			for (JsonProperty jsonProperty : filter) {
				String symbolicName = p8PropMappingService.getFilenetNameMappingFromDb(appId,
						jsonProperty.getPropertyName());
				String value = jsonProperty.getPropertyValue() == null ? null
						: jsonProperty.getPropertyValue().toString();
				searchData.addSearchCondition(new SearchCondition(
						symbolicName == null ? jsonProperty.getPropertyName() : symbolicName, value));
			}
	}

	public void convertJsonDocument(JsonDocument jsonDocument, String appId, CreateDocumentRequest request,
			boolean ignoreMissingMapping) throws ServiceException, FilenetPropMappingNotFoundException {
		if (!CollectionUtils.isEmpty(jsonDocument.getMetadata()))
			for (JsonProperty jsonProperty : jsonDocument.getMetadata()) {
				request.getP8ContentObject().getProperties().addProperty(p8PropMappingService
						.getFilenetNameMappingFromDb(appId, jsonProperty.getPropertyName(), ignoreMissingMapping),
						jsonProperty.getPropertyValue());
			}
		if (jsonDocument.getContent() != null) {
			request.getP8ContentObject().setResource(createP8ContentResource(jsonDocument.getContent()));
		}
	}

	public void convertJsonDocument(JsonDocument jsonDocument, String appId, UpdateDocumentMetadataRequest request,
			boolean ignoreMissingMapping) throws FilenetPropMappingNotFoundException {
		if (!CollectionUtils.isEmpty(jsonDocument.getMetadata()))
			for (JsonProperty jsonProperty : jsonDocument.getMetadata()) {
				request.getProperties().addProperty(p8PropMappingService.getFilenetNameMappingFromDb(appId,
						jsonProperty.getPropertyName(), ignoreMissingMapping), jsonProperty.getPropertyValue());
			}
	}

	public JsonDocument convertToJsonDocument(String appId, P8Object p8Object) throws IOException, ServiceException {
		return convertToJsonDocument(appId, p8Object, true);
	}

	public JsonDocument convertToJsonDocument(String appId, P8Object p8Object, boolean includeMappedPropertiesOnly)
			throws IOException, ServiceException {
		JsonDocument jsonDocument = new JsonDocument();
		jsonDocument.setMetadata(convertToJsonPropertyList(appId, p8Object, includeMappedPropertiesOnly));
		if (p8Object instanceof P8ContentObject p8ContentObject && p8ContentObject.getResource() != null) {
			jsonDocument.setContent(new JsonContent(p8ContentObject.getResource().getFileName(),
					p8ContentObject.getResource().getContentType(), getEncodedContent(p8ContentObject)));
		}
		return jsonDocument;
	}

	public List<JsonProperty> convertToJsonPropertyList(String appId, P8Object p8Object,
			boolean includeMappedPropertiesOnly) {
		if (p8Object == null || p8Object.getProperties() == null)
			return Collections.emptyList();
		List<JsonProperty> list = new ArrayList<>();
		JsonOutputValueConverterImpl converter = new JsonOutputValueConverterImpl(clientConfig);
		for (Map.Entry<String, Object> entry : p8Object.getProperties().entrySet())
			if (entry.getValue() != null)
				convertToJsonPropertyListEntry(appId, p8Object, entry, list, converter, includeMappedPropertiesOnly);
		return list;
	}

	private void convertToJsonPropertyListEntry(String appId, P8Object p8Object, Map.Entry<String, Object> entry,
			List<JsonProperty> list, JsonOutputValueConverterImpl converter, boolean includeMappedPropertiesOnly) {
		String name = null;
		if (appId != null)
			try {
				name = p8PropMappingService.getAppNameMappingFromDb(appId, entry.getKey(), false);
			} catch (FilenetPropMappingNotFoundException e) {
				if (!includeMappedPropertiesOnly
						&& !entry.getKey().equalsIgnoreCase(ECMConstants.P8_CONTENT_ELEMENTS_PROPERTYNAME))
					name = entry.getKey();
			}
		else
			name = entry.getKey();
		if (name != null)
			list.add(new JsonProperty(name, toJsonOutput(p8Object.getProperties().get(entry.getKey()), converter)));
	}

	private Object toJsonOutput(Object value, JsonOutputValueConverter converter) {
		if (jsonOutputValueConverter != null)
			return jsonOutputValueConverter.convertValue(value);
		if (converter != null)
			return converter.convertValue(value);
		if (value == null)
			return null;
		return value.toString();
	}

	public JsonContent createJsonAttachment(P8ContentResource p8ContentResource) throws IOException, ServiceException {
		if (p8ContentResource == null)
			return null;
		JsonContent jsonContent = new JsonContent();
		jsonContent.setTitle(p8ContentResource.getFileName());
		jsonContent.setContentType(p8ContentResource.getContentType());
		jsonContent.setData(base64Encoder.encodeToString(p8ContentResource.getBytes()));
		return jsonContent;
	}

	public P8ContentResource createP8ContentResource(JsonContent jsonContent) throws ServiceException {
		if (jsonContent == null)
			return null;
		P8ContentResource p8ContentResource = new P8ContentResource();
		p8ContentResource.setFileName(jsonContent.getTitle());
		p8ContentResource.setContentType(jsonContent.getContentType());
		if (jsonContent.getData() == null)
			throw new ServiceException(DocumentServiceExceptionCodes.ATTACHMENT_DATA_IS_REQUIRED.getExceptionCode());
		if (jsonContent.getData() instanceof String sData) {
			if (!StringUtils.hasText(sData))
				throw new ServiceException(
						DocumentServiceExceptionCodes.ATTACHMENT_DATA_IS_REQUIRED.getExceptionCode());
			else
				p8ContentResource.setResourceObject(base64Decoder.decode(sData));
		} else
			p8ContentResource.setResourceObject(jsonContent.getData());
		return p8ContentResource;
	}

	public String getRetrievalWhereCause(BaseRequest baseRequest, JsonResource<List<JsonObject>> jsonRetrieveResource)
			throws ServiceException, FilenetPropMappingNotFoundException, ParseException {
		try (P8ProviderImpl p8ProviderImpl = authService.createConnection(baseRequest)) {
			StringBuilder sb = new StringBuilder();
			sb.append("(");
			for (int j = 0; j < jsonRetrieveResource.getBody().size(); j++) {
				List<JsonProperty> lEntry = jsonRetrieveResource.getBody().get(j).getMetadata();
				if (j > 0)
					sb.append(" OR ");
				for (int i = 0; i < lEntry.size(); i++) {
					if (i > 0)
						sb.append(" AND ");
					getRetrievalWhereCauseEntry(
							p8PropMappingService.getFilenetNameMappingFromDb(jsonRetrieveResource.getAppId(),
									lEntry.get(i).getPropertyName()),
							sb, lEntry.get(i), p8ProviderImpl);
				}
			}
			sb.append(")");
			return sb.toString();
		}
	}

	private void getRetrievalWhereCauseEntry(String symbolicName, StringBuilder sb, JsonProperty jsonProperty,
			P8ProviderImpl p8ProviderImpl) throws ServiceException, ParseException {
		PropertyDefinition propertyDefinition = authService.getPropertyDefinition(p8ProviderImpl, symbolicName);
		if (propertyDefinition == null)
			throw new ServiceException(P8ExceptionCodes.UNKNOWN_PROPERTY_NAME.getExceptionCode(),
					new Object[] { symbolicName });
		if (propertyDefinition.getType() == PropertyDefinition.STRING)
			sb.append(symbolicName).append("='")
					.append(StringUtils.replace((String) jsonProperty.getPropertyValue(), "'", "''")).append("'");
		else if (propertyDefinition.getType() == PropertyDefinition.DATE) {
			String[] dateInterval = utilities.p8DateDayInterval(
					utilities.processDateValue(jsonProperty.getPropertyValue().toString(), p8ProviderImpl));
			sb.append('(').append(symbolicName).append(" >= ").append(dateInterval[0]).append(" AND ")
					.append(symbolicName).append(" < ").append(dateInterval[1]).append(')');
		} else
			sb.append(symbolicName).append("=").append(jsonProperty.getPropertyValue());

	}

	public void validateRequiredProperties(String appId, P8Object p8Object) throws ServiceException {
		List<FilenetPropMapping> mappingList = p8PropMappingService.getFilenetPropMapping(appId);
		if (mappingList != null) {
			for (FilenetPropMapping p8PropMapping : mappingList) {
				if (p8PropMapping.isRequiredOnCreate() && !StringUtils
						.hasText(p8Object.getProperties().getValueAsText(p8PropMapping.getFilenetName())))
					throw new ServiceException(
							DocumentServiceExceptionCodes.MAPPED_PROPERTY_REQUIRED.getExceptionCode(),
							new Object[] { p8PropMapping.getAppName(), p8PropMapping.getFilenetName(), appId });
			}
		}
	}

	public JsonSearch preprocessSearchResource(JsonResource<JsonSearch> jsonResource)
			throws ServiceException, FilenetPropMappingNotFoundException {
		JsonSearch jsonSearch = jsonResource.getBody();
		if (CollectionUtils.isEmpty(jsonSearch.getSearchFilter()))
			throw new ServiceException(MappingServiceExceptionCodes.EMPTY_SEARCH_FILTER_EXCEPTION.getExceptionCode());
		JsonSearch result = new JsonSearch();
		result.setActiveDocumentsOnly(jsonSearch.isActiveDocumentsOnly());
		result.setIncludeContent(jsonSearch.isIncludeContent());
		result.setIncludeMappedPropertiesOnly(jsonSearch.isIncludeMappedPropertiesOnly());
		result.setCurrentVersionOnly(jsonSearch.isCurrentVersionOnly());
		ApplicationConfig applicationConfig = authService.getClientConfig();
		String className = applicationConfig.getResourceTypeMapping().get(jsonResource.getResourceType());
		if (!StringUtils.hasText(className))
			className = applicationConfig.getResourceTypeMapping().get("default");
		if (!StringUtils.hasText(className))
			throw new ServiceException(MappingServiceExceptionCodes.NO_DOCCLASS_MAPPING_EXCEPTION.getExceptionCode(),
					(Object) jsonResource.getResourceType());
		result.setDocumentClass(className);
		if (!CollectionUtils.isEmpty(jsonSearch.getPropertiesToRetrieve())) {
			List<String> propertiesToRetrieve = new ArrayList<>(jsonSearch.getPropertiesToRetrieve().size());
			for (String propertyName : jsonSearch.getPropertiesToRetrieve())
				propertiesToRetrieve
						.add(p8PropMappingService.getFilenetNameMappingFromDb(jsonResource.getAppId(), propertyName));
			result.setPropertiesToRetrieve(propertiesToRetrieve);
		}
		List<JsonSearchCondition> conditionList = new ArrayList<>();
		for (JsonSearchCondition jsc : jsonSearch.getSearchFilter()) {
			JsonSearchCondition jscCopy = new JsonSearchCondition(jsc);
			jscCopy.setPropertyName(
					p8PropMappingService.getFilenetNameMappingFromDb(jsonResource.getAppId(), jsc.getPropertyName()));
			conditionList.add(jscCopy);
		}
		result.setSearchFilter(conditionList);
		if (!CollectionUtils.isEmpty(jsonSearch.getSortBy())) {
			List<JsonSortByCondition> sortByList = new ArrayList<>();
			for (JsonSortByCondition jsb : jsonSearch.getSortBy())
				sortByList.add(new JsonSortByCondition(p8PropMappingService
						.getFilenetNameMappingFromDb(jsonResource.getAppId(), jsb.getPropertyName()), jsb.getOrder()));
			result.setSortBy(sortByList);
		}
		return result;
	}
}
