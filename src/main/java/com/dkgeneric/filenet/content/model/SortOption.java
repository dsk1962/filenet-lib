package com.dkgeneric.filenet.content.model;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

/**
 * POJO class to keep FileNet sort order entry <br>
 * {@link #toString()} implemented through Lombok @ToString.
 */
@Getter
@Setter
@NoArgsConstructor
@ToString
public class SortOption {

	/**
	 * Property name
	 * 
	 * @param propertyName the new property name
	 * @return Current property name
	 */
	private String propertyName;

	/**
	 * The sort order. Ascending by default
	 * 
	 * @param sortOrder the new sort order
	 * @return Current sort order
	 */
	private String sortOrder = "ASC";

	/**
	 * Instantiates a new sort option.
	 *
	 * @param propertyName the property name
	 */
	public SortOption(String propertyName) {
		this.propertyName = propertyName;
	}

	/**
	 * Instantiates a new sort option.
	 *
	 * @param propertyName the property name
	 * @param sortOrder    the sort order
	 */
	public SortOption(String propertyName, String sortOrder) {
		this.propertyName = propertyName;
		this.sortOrder = sortOrder;
	}
}
