package com.dkgeneric.filenet.content.exceptioncodes;

import com.dkgeneric.filenet.content.common.Utilities;
import com.dkgeneric.filenet.content.config.P8SearchConfiguration;
import com.dkgeneric.filenet.content.model.SearchData;

/**
 * The Enum SearchServiceExceptionCodes. Search service exception codes
 */
public enum SearchServiceExceptionCodes {

	/** The missing query. */
	MISSING_QUERY("p8contentlib.searchservice.exception.missing.query"),

	/**
	 * The missing where filter placeholder. Search conditions are added to
	 * {@link SearchData}, but query contains no
	 * {@link Utilities#WHERE_FILTER_PLACEHOLDER}.
	 */
	MISSING_WHERE_FILTER_PLACEHOLDER("p8contentlib.searchservice.exception.missing.wherefilterplaceholder"),

	/**
	 * The missing where filter. No search conditions were added to
	 * {@link SearchData}, but query contains
	 * {@link Utilities#WHERE_FILTER_PLACEHOLDER}.
	 */
	MISSING_WHERE_FILTER("p8contentlib.searchservice.exception.missing.wherefilter"),

	/** The missing sort order. */
	MISSING_SORT_ORDER("p8contentlib.searchservice.exception.missing.sortorder"),
	
	/** The properties to include. */
	NO_PROPERTIES_TO_INCLUDE("p8contentlib.searchservice.exception.missing.nopropertiestoinclude"),

	/** The properties to include. */
	SEARCH_VALUE_REQUIRED ("p8contentlib.searchservice.exception.missing.searchvaluerequired"),
	
	/** The properties to include. */
	DATE_SEARCH_CARDINALTY ("p8contentlib.searchservice.exception.missing.datesearchcardinality"),

	/** The properties to include. */
	MISSING_INDEXED_PROPERTY ("p8contentlib.searchservice.exception.missing.indexedproperty"),

	/**
	 * The query missing template. {@link P8SearchConfiguration#getQuery(String)}
	 * returns blank or null value for specific template name
	 */
	MISSING_TEMPLATE("p8contentlib.searchservice.exception.missing.querytemplate");

	/** The exception code. */
	/* for getting the value of enums */
	private String exceptionCode;

	/**
	 * Instantiates a new search service exception codes.
	 *
	 * @param exceptionCode the exception code
	 */
	private SearchServiceExceptionCodes(String exceptionCode) {
		this.exceptionCode = exceptionCode;
	}

	/**
	 * Gets the exception code.
	 *
	 * @return the exception code
	 */
	public String getExceptionCode() {
		return exceptionCode;
	}
}
