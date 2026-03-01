package com.dkgeneric.filenet.content.model;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.dkgeneric.commons.model.json.JsonSearch;
import com.dkgeneric.filenet.content.common.Utilities;
import com.dkgeneric.filenet.content.config.P8SearchConfiguration;
import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

/**
 * POJO class to keep FileNet search data<br>
 * {@link #toString()} implemented through Lombok @ToString.
 * 
 * <br><br><b>Code samples:</b>
 * 
 * <br><br>P8SearchConfiguration will load these entries from property file: <br>
 * 
 * # default list of properties to include on search <br>
 * p8contentlib.propertiestoinclude.default=DocumentTitle,Id,Creator,DateCreated <br>
 * p8contentlib.propertiestoinclude.full=DocumentTitle,Id,Creator,DateCreated,LastModifier,DateLastModified <br>
 * # search templates with place holders <br>
 * p8contentlib.searchtemplate.whereconditiontest= select <i><b>$[properties_to_select_placeholder]</b></i> from document where <i><b>$[where_filter_placeholder]</b></i> <br>
 * # search templates with parameter placeholder <br>
 * p8contentlib.searchtemplate.parameterplacholder= select <i><b>$[properties_to_select_placeholder]</b></i> from document where documenttitle like <i><b>''{0}''</b></i>
 * 
 * <br><br><b>Search conditions usage:</b> <br><br>
 * &nbsp;&nbsp;&nbsp; SearchService searchService; <br><br>
 * &nbsp;&nbsp;&nbsp; SearchRequest request = new SearchRequest(); <br>
 * &nbsp;&nbsp;&nbsp; request.getSearchData().setSearchTemplateName("whereconditiontest"); <br>
 * &nbsp;&nbsp;&nbsp; request.getSearchData().addSearchCondition("DocumentTitle", "Test%"); <br>
 * &nbsp;&nbsp;&nbsp; request.getSearchData().getSortData().addSortOption("DocumentTitle","ASC"); <br> <br>
 * &nbsp;&nbsp;&nbsp; SearchResponse response = searchService.searchDocuments(request); <br> <br>
 * Response will include search results for this query('default' property list is used): <br>
 * select DocumentTitle,Id,Creator,DateCreated from document where DocumentTitle LIKE 'Test%' ORDER BY DocumentTitle ASC
 * 
 * <br><br>
 * <b>Search parameters usage:</b> <br><br>
 * &nbsp;&nbsp;&nbsp; SearchService searchService; <br><br>
 * &nbsp;&nbsp;&nbsp; SearchRequest request = new SearchRequest(); <br>
 * &nbsp;&nbsp;&nbsp; request.getSearchData().setSearchTemplateName("parameterplacholder"); <br>
 * &nbsp;&nbsp;&nbsp; request.getSearchData().setPropertiesToIncludeListName("full"); <br>
 * &nbsp;&nbsp;&nbsp; request.getSearchData().setSearchTemplateParameters(new String[] { "%test" }); <br>
 * &nbsp;&nbsp;&nbsp; request.getSearchData().getSortData().addSortOption("DocumentTitle","DESC");
 * <br>
 * <br>
 * &nbsp;&nbsp;&nbsp; SearchResponse response = searchService.searchDocuments(request);
 * 
 * <br><br>
 * Response will include search results for this query('full' property list is used): <br>
 * select DocumentTitle,Id,Creator,DateCreated,LastModifier,DateLastModified from document where DocumentTitle LIKE 'Test%' ORDER BY DocumentTitle DESC
 */
@Getter
@Setter
@NoArgsConstructor
@ToString

public class SearchData {

	/**
	 * FileNet query string. Like SELECT * FROM DOCUMENT. May contain place holders.
	 * See placeholder constants in {@link Utilities}
	 * 
	 * @param query the new query text
	 * @return query text
	 */
	private String query;

	/**
	 * FileNet query template name. If specified will be used as parameter in
	 * {@link P8SearchConfiguration#getQuery(String)} call to get query string
	 * 
	 * @param searchTemplateName the search template name
	 * @return Current search template name
	 */
	private String searchTemplateName;

	/**
	 * Property list name. If specified will be used as parameter in {@link P8SearchConfiguration#getPropertiesToInclude(String)} call to get list
	 * text. Resulting list will replace {@link Utilities#PROPERTIES_TO_SELECT_PLACEHOLDER} in query string
	 * 
	 * @param propertiesToIncludeListName the property list name
	 * @return Current property list name
	 */
	private String propertiesToIncludeListName;

	/**
	 * Array of objects to apply to search template. FileNet query should match
	 * {@link MessageFormat} specification
	 * 
	 * @param searchTemplateParameters the array of parameters
	 * @return Current parameters
	 */
	private Object[] searchTemplateParameters;

	/**
	 * Sort settings. Sort settings will replace
	 * {@link Utilities#SORT_ORDER_PLACEHOLDER} in query string or added to the end
	 * 
	 * @param sortData the new sorting settings
	 * @return Current sorting settings
	 */
	private SortData sortData = new SortData();

	/**
	 * List of search conditions. Search conditions will replace
	 * {@link Utilities#WHERE_FILTER_PLACEHOLDER} in query string
	 * 
	 * @param whereConditions new search conditions
	 * @return Current search conditions
	 */
	private List<SearchCondition> whereConditions;

	/**
	 * Adds the search condition.
	 *
	 * @param searchCondition the new search condition
	 * @return the search condition
	 */
	public SearchCondition addSearchCondition(SearchCondition searchCondition) {
		if (whereConditions == null)
			whereConditions = new ArrayList<>(10);
		whereConditions.add(searchCondition);
		return searchCondition;
	}

	/**
	 * Adds the search condition.
	 *
	 * @param name  the property name
	 * @param value the search value
	 * @return the search condition
	 */
	public SearchCondition addSearchCondition(String name, String value) {
		return addSearchCondition(name, value, true);
	}

	/**
	 * Adds the search condition.
	 *
	 * @param name              the property name
	 * @param value             the search value
	 * @param isStringCondition the is string condition flag
	 * @return the search condition
	 */
	public SearchCondition addSearchCondition(String name, String value, boolean isStringCondition) {
		return addSearchCondition(new SearchCondition(name, value, isStringCondition));
	}

	/**
	 * Search query in json format
	 * 
	 * @param jsonSearch the new search query in json format
	 * @return Current search query in json format
	 */
	private JsonSearch jsonSearch;

	@JsonAlias({ "Property", "searchCondition" })
	public void setWhereConditionsJson(JsonNode jsonNode) throws JsonProcessingException, IllegalArgumentException {
		if (jsonNode.isArray()) {
			SearchCondition[] scArray = new ObjectMapper().treeToValue(jsonNode, SearchCondition[].class);
			whereConditions = Arrays.asList(scArray);
		} else {
			whereConditions = new ArrayList<>();
			whereConditions.add(new ObjectMapper().treeToValue(jsonNode, SearchCondition.class));
		}
	}

}
