package com.dkgeneric.filenet.content.model;

import com.fasterxml.jackson.annotation.JsonAlias;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

/**
 * POJO class to keep FileNet search condition like: DocumentTitle = 'Test' AND
 * <br>
 * {@link #toString()} implemented through Lombok @ToString.
 */
@Getter
@Setter
@NoArgsConstructor
@ToString
public class SearchCondition {

	/**
	 * Property name.
	 * 
	 * @param propertyName the property name
	 * @return Current key value
	 */
	@JsonAlias({ "PropertyName", "key" })
	private String propertyName;

	/**
	 * Condition prefix.
	 * 
	 * @param prefix the new prefix
	 * @return Current prefix value
	 */
	private String prefix="";
	
	/**
	 * Condition postfix.
	 * 
	 * @param postfix the new postfix
	 * @return Current postfix value
	 */
	private String postfix="";

	/**
	 * Value to search.
	 * 
	 * @param searchValue new search value
	 * @return search value
	 */
	@JsonAlias({ "PropertyValue", "value" })
	private String searchValue;

	/**
	 * Search value type flag. If set to true search value will be surrounded with
	 * single quota. True by default.
	 * 
	 * @param isStringCondition sets this flag value
	 * @return isStringCondition flag value
	 */
	private boolean isStringCondition = true;

	/**
	 * Search operation. By default is "LIKE" if {@link #isStringCondition()} is
	 * true, "=" otherwise
	 * 
	 * @param operation the new search operation
	 * @return Current search operation
	 */
	@JsonAlias({ "Operand", "operation" })
	private String operation = "LIKE";

	/**
	 * Join condition. "AND" by default
	 * 
	 * @param joinCondition the new join condition
	 * @return Current join condition
	 */
	@JsonAlias({ "Operator", "operand" })
	private String joinCondition = "AND";

	/**
	 * Copy constructor.
	 *
	 * @param sc              search condition to copy
	 */
	public SearchCondition(SearchCondition sc) {
		isStringCondition = sc.isStringCondition();
		joinCondition = sc.getJoinCondition();
		operation = sc.getOperation();
		propertyName = sc.getPropertyName();
		searchValue = sc.getSearchValue();
	}

	/**
	 * Instantiates a new search condition.
	 *
	 * @param name  the property name
	 * @param value the search value
	 */
	public SearchCondition(String name, String value) {
		this(name, value, true);
	}

	/**
	 * Instantiates a new search condition.
	 *
	 * @param name              the property name
	 * @param value             the search value
	 * @param isStringCondition the is string condition flag. Use false to search by
	 *                          non-string values
	 */
	public SearchCondition(String name, String value, boolean isStringCondition) {
		this(name, value, isStringCondition, null);
	}

	/**
	 * Instantiates a new search condition.
	 *
	 * /** Instantiates a new search condition.
	 *
	 * @param name              the property name
	 * @param value             the search value
	 * @param isStringCondition the is string condition flag. Use false to search by
	 *                          non-string values
	 * @param operation         the search operation
	 */
	public SearchCondition(String name, String value, boolean isStringCondition, String operation) {
		propertyName = name;
		searchValue = value;
		this.isStringCondition = isStringCondition;
		if (operation == null)
			this.operation = isStringCondition ? "LIKE" : "=";
		else
			this.operation = operation;
	}
}
