package com.dkgeneric.filenet.content.model;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

/**
 * POJO class to keep FileNet search parameters <br>
 * {@link #toString()} implemented through Lombok @ToString.
 */
@Getter
@Setter
@NoArgsConstructor
@ToString
public class SearchParameters {
	/**
	 * Max results to return. If above 0 this value will be added as SELECT TOP XXX
	 * to query string
	 * 
	 * @param maxSize the max result set size
	 * @return Current max result set size
	 */
	private int maxSize = -1;
	/**
	 * Page size for pagination search. If above 0 FileNet pagination iterator will
	 * be used to walk through result set
	 * 
	 * @param pageSize the new page size
	 * @return Current page size
	 */
	private int pageSize = 0;
	/**
	 * Start page. This value will be used if {@link #getPageSize()} &gt; 0
	 * 
	 * @param startPage new start page value
	 * @return Current start page value
	 */
	private int startPage = 0;
}
