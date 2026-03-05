package com.dkgeneric.filenet.content.common;

import java.text.MessageFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Objects;

import org.slf4j.helpers.MessageFormatter;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;
import org.springframework.util.ObjectUtils;
import org.springframework.util.StringUtils;

import com.dkgeneric.commons.config.CommonsErrorMessages;
import com.dkgeneric.commons.exceptions.InvalidRequestException;
import com.dkgeneric.commons.model.json.JsonSearch;
import com.dkgeneric.commons.model.json.JsonSearchCondition;
import com.dkgeneric.commons.model.json.JsonSortByCondition;
import com.dkgeneric.filenet.content.config.ApplicationConfig;
import com.dkgeneric.filenet.content.config.P8SearchConfiguration;
import com.dkgeneric.filenet.content.exceptioncodes.GeneralExceptionCodes;
import com.dkgeneric.filenet.content.exceptioncodes.MappingServiceExceptionCodes;
import com.dkgeneric.filenet.content.exceptioncodes.P8ExceptionCodes;
import com.dkgeneric.filenet.content.exceptioncodes.SearchServiceExceptionCodes;
import com.dkgeneric.filenet.content.model.PropertyDefinition;
import com.dkgeneric.filenet.content.model.SearchCondition;
import com.dkgeneric.filenet.content.model.SearchData;
import com.dkgeneric.filenet.content.model.SortData;
import com.dkgeneric.filenet.content.model.SortOption;
import com.dkgeneric.filenet.content.provider.P8ProviderBase;
import com.dkgeneric.filenet.content.provider.P8ProviderImpl;
import com.dkgeneric.filenet.content.response.BaseResponse;
import com.filenet.api.exception.EngineRuntimeException;
import com.filenet.api.exception.ExceptionCode;

/**
 * The Class Utilities. Declares set of helper methods
 */
@Component("p8contentlibUtilities")
public class Utilities {

	/** The Constant SORT_ORDER_PLACEHOLDER. */
	public static final String SORT_ORDER_PLACEHOLDER = "$[sort_order_placeholder]";

	/** The Constant WHERE_FILTER_PLACEHOLDER. */
	public static final String WHERE_FILTER_PLACEHOLDER = "$[where_filter_placeholder]";

	/** The Constant PROPERTIES_TO_SELECT_PLACEHOLDER. */
	public static final String PROPERTIES_TO_SELECT_PLACEHOLDER = "$[properties_to_select_placeholder]";

	private static final String[] p8DateFormatRegex = { "\\d{8}T\\d{6}Z", "\\d{4}-\\d{2}-\\d{2}[+-]\\d{2}:\\d{2}",
			"\\d{4}-\\d{2}-\\d{2}T\\d{2}:\\d{2}:\\d{2}[+-]\\d{2}:\\d{2}" };

	private static final String P8_DATE_ONLY_FORMAT = "yyyy-MM-dd";

	private static final String DEFAULT_CLASS_NAME = "default";

	public static final String AND_JOIN = " AND ";

	public static final String OR_JOIN = " OR ";
	public static final String WHERE_CLAUSE = " WHERE ";

	/** This member is used to work with error messages */
	private final CommonsErrorMessages errorMessages;
	/**
	 * This member gives access to application specific P8 search configurations.
	 */
	private final P8SearchConfiguration p8SearchConfiguration;
	/**
	 * This member gives access to application specific P8 configurations.
	 */
	private final ApplicationConfig applicationConfig;

	public Utilities(CommonsErrorMessages errorMessages,
			@Qualifier("p8contentlibP8SearchConfiguration") P8SearchConfiguration p8SearchConfiguration,
			ApplicationConfig applicationConfig) {
		this.errorMessages = errorMessages;
		this.p8SearchConfiguration = p8SearchConfiguration;
		this.applicationConfig = applicationConfig;
	}

	/**
	 * Applies sort options to query string.
	 *
	 * @param query    the query
	 * @param sortData the sort data
	 *                 {@link com.dkgeneric.filenet.content.model.SortData}
	 * @return the query with applied sort order
	 * @throws ServiceException 
	 */
	private String applySortOptions(String query, SortData sortData) throws ServiceException {
		validateSortData(query, sortData);
		String orderBy = getOrderBy(sortData);
		if (StringUtils.hasText(orderBy)) {
			if (query.indexOf(SORT_ORDER_PLACEHOLDER) >= 0)
				return StringUtils.replace(query, SORT_ORDER_PLACEHOLDER, orderBy);
			else
				return query + orderBy;
		}
		return query;
	}

	/**
	 * Applies where conditions.
	 *
	 * @param query            the query
	 * @param searchConditions the list of search conditions {@link SearchCondition}
	 * @return the query with applied sort order
	 * @throws ServiceException if <b>searchConditions</b> has entries, but query
	 *                          contains no {@link #WHERE_FILTER_PLACEHOLDER} or
	 *                          <b>searchConditions</b> has no entries, but query
	 *                          contains {@link #WHERE_FILTER_PLACEHOLDER}
	 */
	private String applyWhereConditions(String query, List<SearchCondition> searchConditions) throws ServiceException {
		validateWhereConditions(query, searchConditions);
		if (!CollectionUtils.isEmpty(searchConditions)) {
			// throw exception if filter place holder is not present in query
			// generate filter string
			StringBuilder filter = new StringBuilder();
			for (int i = 0; i < searchConditions.size(); i++) {
				SearchCondition searchCondition = searchConditions.get(i);
				filter.append(searchCondition.getPropertyName()).append(' ').append(searchCondition.getOperation())
						.append(' ');
				// Check if operation is not IS NULL or NOT IS NULL
				if (searchCondition.getOperation().toUpperCase().indexOf("NULL") < 0) {
					if (searchCondition.isStringCondition() && !searchCondition.getOperation().toUpperCase().contains("IN"))
						filter.append("'").append(searchCondition.getSearchValue()).append("'");
					else
						filter.append(searchCondition.getSearchValue());
					filter.append(' ');
				}
				// don't add join condition for last entry
				if (i < searchConditions.size() - 1) {
					filter.append(searchCondition.getJoinCondition());
					filter.append(' ');
				}
			}
			query = StringUtils.replace(query, WHERE_FILTER_PLACEHOLDER, filter.toString());
		}
		return query;
	}

	/**
	 * Gets the order by string.
	 *
	 * @param sortData the sort data {@link SortData}
	 * @return the order by
	 */
	private String getOrderBy(SortData sortData) {
		if (sortData == null
				|| !StringUtils.hasText(sortData.getSortString()) && CollectionUtils.isEmpty(sortData.getSortOptions()))
			return null;
		if (StringUtils.hasText(sortData.getSortString()))
			return sortData.getSortString();
		boolean first = true;
		StringBuilder sb = new StringBuilder(" ORDER BY ");
		for (SortOption sortOption : sortData.getSortOptions()) {
			if (!first)
				sb.append(',');
			else
				first = false;
			sb.append(sortOption.getPropertyName());
			if (StringUtils.hasText(sortOption.getSortOrder()))
				sb.append(' ').append(sortOption.getSortOrder());
		}
		return sb.toString();
	}

	/**
	 * Gets the comma separated list of properties to include in search result.
	 *
	 * @param propertiesListName the properties list name see
	 *                           {@link P8SearchConfiguration}
	 * @return the properties to include
	 */
	public String getPropertiesToInclude(String propertiesListName) {
		return p8SearchConfiguration.getPropertiesToInclude(propertiesListName);
	}

	public String getQuery(JsonSearch jsonSearch, P8ProviderImpl p8ProviderImpl)
			throws ParseException, ServiceException {
		String className = jsonSearch.getDocumentClass();
		String propertiesToInclude = null;
		if (CollectionUtils.isEmpty(jsonSearch.getPropertiesToRetrieve())) {
			propertiesToInclude = p8SearchConfiguration.getPropertiesToInclude(className);
			if (!StringUtils.hasText(propertiesToInclude))
				propertiesToInclude = p8SearchConfiguration.getPropertiesToInclude(DEFAULT_CLASS_NAME);
			if (!StringUtils.hasText(propertiesToInclude))
				throw new ServiceException(SearchServiceExceptionCodes.NO_PROPERTIES_TO_INCLUDE.getExceptionCode(),
						(Object) className);
		} else
			propertiesToInclude = String.join(",", jsonSearch.getPropertiesToRetrieve());
		if (!propertiesToInclude.toLowerCase().contains("contentelements") && jsonSearch.isIncludeContent())
			propertiesToInclude += ",ContentElements";
		StringBuilder query = new StringBuilder();
		query.append(P8ProviderBase.SELECT_CLAUSE).append(propertiesToInclude).append(" FROM ").append(className);
		if (CollectionUtils.isEmpty(jsonSearch.getSearchFilter()))
			throw new ServiceException(MappingServiceExceptionCodes.EMPTY_SEARCH_FILTER_EXCEPTION.getExceptionCode());
		query.append(WHERE_CLAUSE);
		validatIndexedPropertyPresence(jsonSearch, query, p8ProviderImpl);
		if (jsonSearch.isCurrentVersionOnly())
			query.append(AND_JOIN).append(ECMConstants.P8_IS_CURRENT_VERSION).append(" = true ");
		if (jsonSearch.isActiveDocumentsOnly())
			query.append(AND_JOIN).append(ECMConstants.DVA_AVAILABILITYSTATUS_PROPERTYNAME).append(" = '")
					.append(ECMConstants.DVA_AVAILABILITY_STATUS_ACTIVE).append("'");
		processSortBy(jsonSearch, query);
		return query.toString();
	}

	private void validatIndexedPropertyPresence(JsonSearch jsonSearch, StringBuilder query,
			P8ProviderImpl p8ProviderImpl) throws ServiceException, ParseException {
		boolean hasIndexedProperty = false;
		for (int i = 0; i < jsonSearch.getSearchFilter().size(); i++) {
			JsonSearchCondition jsc = jsonSearch.getSearchFilter().get(i);
			processSearchCondition(jsc, query, p8ProviderImpl, i == jsonSearch.getSearchFilter().size() - 1);
			if (!hasIndexedProperty)
				hasIndexedProperty = !CollectionUtils.isEmpty(applicationConfig.getIndexedProperties())
						&& applicationConfig.getIndexedProperties().contains(jsc.getPropertyName());
		}
		if (!hasIndexedProperty)
			throw new ServiceException(SearchServiceExceptionCodes.MISSING_INDEXED_PROPERTY.getExceptionCode(),
					applicationConfig.getIndexedProperties());
	}

	/**
	 * Creates the query based on {@link SearchData}.
	 * 
	 * @param searchData the search data see {@link SearchData}
	 * @return the query
	 * @throws ServiceException Signals that searchData is null or searchData.getQuery() returns null or blank value 
	 * @throws ParseException 
	 */
	private String getQuery(SearchData searchData, P8ProviderImpl p8ProviderImpl)
			throws ServiceException, ParseException {
		// check if query is specified as query string or template name
		if (searchData != null && searchData.getJsonSearch() != null)
			return getQuery(searchData.getJsonSearch(), p8ProviderImpl);
		if (searchData == null || !StringUtils.hasText(searchData.getQuery())
				&& !StringUtils.hasText(searchData.getSearchTemplateName()))
			throw new ServiceException(SearchServiceExceptionCodes.MISSING_QUERY.getExceptionCode());
		String query = searchData.getQuery();
		// try to get query by template name
		if (!StringUtils.hasText(query)) {
			query = p8SearchConfiguration.getQuery(searchData.getSearchTemplateName());
			if (!StringUtils.hasText(query))
				throw new ServiceException(SearchServiceExceptionCodes.MISSING_TEMPLATE.getExceptionCode(),
						new Object[] { searchData.getSearchTemplateName() });
		}
		// apply search parameters if any specified
		if (!ObjectUtils.isEmpty(searchData.getSearchTemplateParameters()))
			query = MessageFormat.format(query, searchData.getSearchTemplateParameters());
		query = applyWhereConditions(query, searchData.getWhereConditions());
		return applySortOptions(query, searchData.getSortData());
	}

	private String getSearchValue(Object value, int proprtyType) {
		if (proprtyType == PropertyDefinition.STRING)
			return "'" + StringUtils.replace(Objects.toString(value), "'", "''") + "'";
		return Objects.toString(value);
	}

	public String[] p8DateDayInterval(String p8Date) throws ParseException {
		String[] result = new String[2];
		if (p8Date.matches(p8DateFormatRegex[0])) {
			result[0] = p8Date.substring(0, 8) + "T000000Z";
			SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd");
			Date d = sdf.parse(p8Date.substring(0, 8));
			Calendar c = Calendar.getInstance();
			c.setTime(d);
			c.add(Calendar.DATE, 1);
			result[1] = sdf.format(c.getTime()) + "T000000Z";
		} else if (p8Date.matches(p8DateFormatRegex[1])) {
			result[0] = p8Date;
			SimpleDateFormat sdf = new SimpleDateFormat(P8_DATE_ONLY_FORMAT);
			Date d = sdf.parse(p8Date.substring(0, 10));
			Calendar c = Calendar.getInstance();
			c.setTime(d);
			c.add(Calendar.DATE, 1);
			result[1] = sdf.format(c.getTime()) + p8Date.substring(10);
		} else if (p8Date.matches(p8DateFormatRegex[2])) {
			result[0] = p8Date.substring(0, 10) + p8Date.substring(19);
			SimpleDateFormat sdf = new SimpleDateFormat(P8_DATE_ONLY_FORMAT);
			Date d = sdf.parse(p8Date.substring(0, 10));
			Calendar c = Calendar.getInstance();
			c.setTime(d);
			c.add(Calendar.DATE, 1);
			result[1] = sdf.format(c.getTime()) + p8Date.substring(19);
		} else
			throw new InvalidRequestException("Unknown date format: {}", p8Date);
		return result;
	}

	/**
	 * Creates the query based on {@link SearchData}.
	 *
	 * @param searchData the search data see {@link SearchData}
	 * @return the query string
	 * @throws ServiceException Signals that searchData is null or searchData.getQuery() returns null or blank value 
	 * @throws ParseException 
	 */
	public String prepareQuery(SearchData searchData, P8ProviderImpl p8ProviderImpl)
			throws ServiceException, ParseException {
		preprocessSearchData(searchData, p8ProviderImpl);
		String query = getQuery(searchData, p8ProviderImpl);
		// check if properties to include place holder exists and replace it with values
		// from configuration file
		if (query.indexOf(PROPERTIES_TO_SELECT_PLACEHOLDER) >= 0) {
			String propertiesToSelect = p8SearchConfiguration
					.getPropertiesToInclude(searchData.getPropertiesToIncludeListName());
			if (propertiesToSelect == null)
				propertiesToSelect = "*";
			query = StringUtils.replace(query, PROPERTIES_TO_SELECT_PLACEHOLDER, propertiesToSelect);
		}
		return query;
	}

	private void preprocessSearchCondition(SearchCondition sc, List<SearchCondition> list,
			P8ProviderImpl p8ProviderImpl) throws ParseException {
		PropertyDefinition propertyDefinition = p8ProviderImpl.getAuthService().getPropertyDefinition(p8ProviderImpl,
				sc.getPropertyName());
		sc.setStringCondition(propertyDefinition.getType() == PropertyDefinition.STRING);
		if (!sc.isStringCondition() && sc.getOperation().toLowerCase().contains("like"))
			sc.setOperation(sc.getOperation().toLowerCase().contains("not") ? "<>" : "=");
		// check if dates are have valid format
		if (propertyDefinition.getType() == PropertyDefinition.DATE) {
			processDateCondition(sc, p8ProviderImpl);
			if (sc.getOperation().trim().equals("=")) {
				String[] interval = p8DateDayInterval(sc.getSearchValue());
				SearchCondition sc1 = new SearchCondition(sc);
				sc1.setPrefix("(");
				sc1.setJoinCondition("AND");
				sc1.setSearchValue(interval[0]);
				sc1.setOperation(">=");
				list.add(sc1);
				sc.setSearchValue(interval[1]);
				sc.setOperation("<");
				sc.setPostfix(")");
			}
		}
		list.add(sc);
	}

	private void preprocessSearchData(SearchData searchData, P8ProviderImpl p8ProviderImpl) throws ParseException {
		if (!CollectionUtils.isEmpty(searchData.getWhereConditions())) {
			List<SearchCondition> list = new ArrayList<>();
			for (SearchCondition sc : searchData.getWhereConditions())
				preprocessSearchCondition(sc, list, p8ProviderImpl);
			searchData.setWhereConditions(list);
		}
	}

	private void processDateCondition(JsonSearchCondition condition, StringBuilder conditions,
			P8ProviderImpl p8ProviderImpl) throws ParseException, ServiceException {
		String[] dateInterval = null;
		if (CollectionUtils.isEmpty(condition.getPropertyValues()) || condition.getPropertyValues().size() == 1) {
			String value = condition.getPropertyValues() != null && condition.getPropertyValues().size() == 1
					? Objects.toString(condition.getPropertyValues().get(0))
					: Objects.toString(condition.getPropertyValue());
			value = processDateValue(value, p8ProviderImpl);
			dateInterval = p8DateDayInterval(value);
		} else if (condition.getPropertyValues().size() == 2) {
			dateInterval = new String[] {
					processDateValue(Objects.toString(condition.getPropertyValues().get(0)), p8ProviderImpl),
					processDateValue(Objects.toString(condition.getPropertyValues().get(1)), p8ProviderImpl) };
		} else
			throw new ServiceException(SearchServiceExceptionCodes.DATE_SEARCH_CARDINALTY.getExceptionCode(),
					(Object) condition.getPropertyName());
		conditions.append(" " + condition.getPrefix());
		conditions.append('(').append(condition.getPropertyName()).append(" >= ").append(dateInterval[0]).append(AND_JOIN);
		conditions.append(condition.getPropertyName()).append(" < ").append(dateInterval[1]).append(')');
	}

	private void processDateCondition(SearchCondition sc, P8ProviderImpl p8ProviderImpl) {
		if (StringUtils.hasText(sc.getSearchValue())) {
			boolean valid = false;
			for (String pattern : p8DateFormatRegex) {
				valid = sc.getSearchValue().matches(pattern);
				if (valid)
					break;
			}
			if (!valid) {
				Date date = p8ProviderImpl.getAuthService().getValidationService().parseDate(sc.getSearchValue());
				sc.setSearchValue(toP8Format(date));
			}
		}
	}

	public String processDateValue(String value, P8ProviderImpl p8ProviderImpl) {
		boolean valid = false;
		for (String pattern : p8DateFormatRegex) {
			valid = value.matches(pattern);
			if (valid)
				break;
		}
		if (!valid) {
			Date date = p8ProviderImpl.getAuthService().getValidationService().parseDate(value);
			value = toP8Format(date);
		}
		return value;
	}

	private void processSearchCondition(JsonSearchCondition condition, StringBuilder conditions,
			P8ProviderImpl p8ProviderImpl, boolean lastCondition) throws ParseException, ServiceException {
		PropertyDefinition propertyDefinition = validateCondition(condition, p8ProviderImpl);
		if (condition.getOperator().toLowerCase().contains("null")) {
			conditions.append(' ').append(condition.getPrefix());
			conditions.append(condition.getPropertyName()).append(condition.getOperator());
		} else if (propertyDefinition.getType() == PropertyDefinition.DATE)
			processDateCondition(condition, conditions, p8ProviderImpl);
		else if (CollectionUtils.isEmpty(condition.getPropertyValues())) {
			conditions.append(' ').append(condition.getPrefix());
			conditions.append(condition.getPropertyName()).append(' ').append(condition.getOperator()).append(' ');
			conditions.append(getSearchValue(condition.getPropertyValue(), propertyDefinition.getType())).append(' ');
		} else {
			conditions.append(' ').append(condition.getPrefix());
			StringBuilder sb = new StringBuilder();
			condition.getPropertyValues()
					.forEach(v -> sb.append(',').append(getSearchValue(v, propertyDefinition.getType())));
			if (propertyDefinition.isMultiValue())
				conditions.append("(").append(sb.substring(1)).append(") IN ").append(condition.getPropertyName());
			else
				conditions.append(condition.getPropertyName()).append(" IN (").append(sb.substring(1)).append(")");
		}
		conditions.append(condition.getPostfix()).append(' ');
		if (!lastCondition)
			conditions.append(condition.getJoin());
	}

	private void processSortBy(JsonSearch jsonSearch, StringBuilder query) {
		if (!CollectionUtils.isEmpty(jsonSearch.getSortBy())) {
			query.append(" ORDER BY ");
			for (int i = 0; i < jsonSearch.getSortBy().size(); i++) {
				JsonSortByCondition jsb = jsonSearch.getSortBy().get(i);
				query.append(jsb.getPropertyName()).append(' ').append(jsb.getOrder());
				if (i != jsonSearch.getSortBy().size() - 1)
					query.append(',');
			}
		}
	}

	/**
	 * Sets the response errors based on caught exception.
	 *
	 * @param baseResponse the base response
	 * @param e            the caught exception
	 */
	public void setResponseErrors(BaseResponse baseResponse, Exception e) {
		setResponseErrors(baseResponse, e, null);
	}

	/**
	 * Sets the response errors based on caught exception.
	 *
	 * @param baseResponse the base response
	 * @param e            the caught exception
	 * @param docId        the document id in operation that caught exception
	 */
	public void setResponseErrors(BaseResponse baseResponse, Exception e, String docId) {
		if (e instanceof ServiceException se)
			setResponseErrors(baseResponse, se.getExceptionCode(), se.getParameters());
		else {
			if (e instanceof EngineRuntimeException ere) {
				System.out.println("Code: " + ere.getExceptionCode().getErrorId() + " / " +ere.getExceptionCode().getId() );
				if (ere.getExceptionCode() == ExceptionCode.E_OBJECT_NOT_FOUND && StringUtils.hasText(docId))
					setResponseErrors(baseResponse,
							GeneralExceptionCodes.DOCUMENT_NOT_FOUND_BY_ID_EXCEPTION.getExceptionCode(),
							new Object[] { docId });
				else if (ere.getExceptionCode() == ExceptionCode.DB_ERROR && ere.getMessage() != null && ere.getMessage().indexOf("ORA-01013") > 0)
					setResponseErrors(baseResponse,
							"p8contentlib.p8exception.engineruntimedbexceptionthrown",
							new Object[] { ere.getExceptionCode().getErrorId(), ere.getMessage() });
				else {
					String exceptionCode = ere.getExceptionCode() == null ? "None"
							: ere.getExceptionCode().getErrorId();
					setResponseErrors(baseResponse, P8ExceptionCodes.ENGINE_RUNTIME_EXCEPTION_THROWN.getExceptionCode(),
							new Object[] { exceptionCode, ere.getMessage() });
				}
			} else
				setResponseErrors(baseResponse, GeneralExceptionCodes.UNCAUGHT_EXCEPTION.getExceptionCode(),
						e.getMessage());
		}
	}

	/**
	 * Sets the response errors.
	 *
	 * @param baseResponse  the base response
	 * @param exceptionCode the exception code
	 */
	public void setResponseErrors(BaseResponse baseResponse, String exceptionCode) {
		setResponseErrors(baseResponse, exceptionCode, null);
	}

	/**
	 * Sets the response errors.
	 *
	 * @param baseResponse  the base response
	 * @param exceptionCode the exception code
	 * @param object        the object
	 */
	public void setResponseErrors(BaseResponse baseResponse, String exceptionCode, Object object) {
		setResponseErrors(baseResponse, exceptionCode, new Object[] { object });
	}

	/**
	 * Sets the response errors.
	 *
	 * @param baseResponse  the base response
	 * @param exceptionCode the exception code
	 * @param parameters    the parameters
	 */
	public void setResponseErrors(BaseResponse baseResponse, String exceptionCode, Object[] parameters) {
		baseResponse.setErrorCode(errorMessages.getMessageCode(exceptionCode));
		baseResponse.setErrorType(errorMessages.getMessageType(exceptionCode));
		if (parameters == null)
			baseResponse
					.setErrorMessage(baseResponse.getErrorCode() + ". " + errorMessages.getMessageText(exceptionCode));
		else
			baseResponse.setErrorMessage(baseResponse.getErrorCode() + ". " + MessageFormatter
					.arrayFormat(errorMessages.getMessageText(exceptionCode), parameters).getMessage());
	}

	/**
	 * Converts Date to P8 search format. Date only with timezone
	 *
	 * @param date the date
	 * @return the string
	 */
	public String toP8Format(Date date) {
		SimpleDateFormat toP8Formatter = new SimpleDateFormat(P8_DATE_ONLY_FORMAT);
		SimpleDateFormat timeZoneFormatter = new SimpleDateFormat("Z");
		String timeZone = timeZoneFormatter.format(new Date());
		timeZone = timeZone.substring(0, timeZone.length() - 2) + ":" + timeZone.substring(timeZone.length() - 2);
		return toP8Formatter.format(date) + timeZone;
	}

	private PropertyDefinition validateCondition(JsonSearchCondition condition, P8ProviderImpl p8ProviderImpl)
			throws ServiceException {
		PropertyDefinition propertyDefinition = p8ProviderImpl.getAuthService().getPropertyDefinition(p8ProviderImpl,
				condition.getPropertyName());
		if (propertyDefinition == null)
			throw new ServiceException(P8ExceptionCodes.UNKNOWN_PROPERTY_NAME.getExceptionCode(),
					(Object) condition.getPropertyName());
		if (!StringUtils.hasText(condition.getOperator()))
			condition.setOperator("=");
		if (!condition.getOperator().toLowerCase().contains("null")
				&& CollectionUtils.isEmpty(condition.getPropertyValues()) && (condition.getPropertyValue() == null
						|| !StringUtils.hasText(condition.getPropertyValue().toString())))
			throw new ServiceException(SearchServiceExceptionCodes.SEARCH_VALUE_REQUIRED.getExceptionCode(),
					condition.getPropertyName());
		return propertyDefinition;
	}

	/**
	 * Validate sort data.
	 *
	 * @param query    the query
	 * @param sortData the sort data
	 * @throws ServiceException 
	 */
	private void validateSortData(String query, SortData sortData) throws ServiceException {
		// if sort order placeholder is present than sorting is required
		if (query.indexOf(SORT_ORDER_PLACEHOLDER) >= 0
				&& (sortData == null || !StringUtils.hasText(sortData.getSortString())
						&& CollectionUtils.isEmpty(sortData.getSortOptions())))
			throw new ServiceException(SearchServiceExceptionCodes.MISSING_SORT_ORDER.getExceptionCode(),
					(Object) query);
	}

	/**
	 * Validate where conditions.
	 *
	 * @param query            the query
	 * @param searchConditions list of search conditions see {@link SearchCondition}
	 * @throws ServiceException 
	 */
	private void validateWhereConditions(String query, List<SearchCondition> searchConditions) throws ServiceException {
		if (!CollectionUtils.isEmpty(searchConditions)) {
			// throw exception if filter place holder is not present in query
			if (query.indexOf(WHERE_FILTER_PLACEHOLDER) < 0)
				throw new ServiceException(
						SearchServiceExceptionCodes.MISSING_WHERE_FILTER_PLACEHOLDER.getExceptionCode());
		} else if (query.indexOf(WHERE_FILTER_PLACEHOLDER) >= 0)
			throw new ServiceException(SearchServiceExceptionCodes.MISSING_WHERE_FILTER.getExceptionCode(),
					(Object) query);
	}

}
