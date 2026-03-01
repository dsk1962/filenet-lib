package com.dkgeneric.filenet.content.model;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

/**
 * Instances of this class represents general FileNet object. <br>
 * {@link #toString()} implemented through Lombok @ToString
 */
@Getter
@Setter
@NoArgsConstructor
@ToString
public class P8Object {
	/**
	 * Id property value in FileNet.
	 * 
	 * @param id Id value of an object in FileNet.
	 * @return The current value of this FileNet object.
	 */
	private String id;
	/**
	 * Symbolic Name of object type definition.
	 * 
	 * @param documentClass Symbolic Name of an object type definition in FileNet.
	 * @return The current value of Symbolic Name of FileNet object type definition.
	 */
	private String documentClass;
	/**
	 * Object that contains FileNet object property values.
	 * 
	 * @param properties contains FileNet object property values.
	 * @return The current FileNet object properties.
	 */
	private P8Properties properties = new P8Properties();
}
