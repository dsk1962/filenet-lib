package com.dkgeneric.filenet.content.model;

import java.util.ArrayList;
import java.util.List;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

/**
 * POJO class to keep FileNet sort order <br>
 * {@link #toString()} implemented through Lombok @ToString.
 */
@Getter
@Setter
@NoArgsConstructor
@ToString
public class SortData {

	/**
	 * The sort string. Any valid FileNet search order by string like: ORDER BY
	 * DocumentTitle ASC <br>
	 * If specified {@link #getSortOptions()} value will be ignored
	 * 
	 * @param sortString the new sort string
	 * @return Current sort string
	 */
	private String sortString;

	/**
	 * The sort option list
	 * 
	 * @param sortOptions the new sort option list
	 * @return Current sort option list
	 */
	private List<SortOption> sortOptions;

	/**
	 * Adds the sort option.
	 *
	 * @param sortOption the new sort option
	 */
	public void addSortOption(SortOption sortOption) {
		if (sortOption == null)
			return;
		if (sortOptions == null)
			sortOptions = new ArrayList<>(5);
		sortOptions.add(sortOption);
	}

	/**
	 * Adds the sort option.
	 *
	 * @param propertyName the property name. Default sort order is ascending
	 */
	public void addSortOption(String propertyName) {
		addSortOption(new SortOption(propertyName));
	}

	/**
	 * Adds the sort option.
	 *
	 * @param propertyName the property name
	 * @param sortOrder    the sort order
	 */
	public void addSortOption(String propertyName, String sortOrder) {
		addSortOption(new SortOption(propertyName, sortOrder));
	}

}
