package com.dkgeneric.filenet.content.model;

import com.dkgeneric.filenet.content.resources.P8ContentResource;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

/**
 * Instances of this class represents FileNet object that may have content
 * (Document instances). <br>
 * {@link #toString()} implemented through Lombok @ToString
 */
@Getter
@Setter
@NoArgsConstructor
@ToString(callSuper = true)
public class P8ContentObject extends P8Object {

	/**
	 * FileNet object content data.
	 * 
	 * @param resource the new FileNet object content data
	 * @return Current FileNet object content data
	 */
	private P8ContentResource resource;
}
