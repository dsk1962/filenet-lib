package com.dkgeneric.filenet.content.model;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

/**
 * Instances of this class represents FileNet property audit information. <br>
 * {@link #toString()} implemented through Lombok @ToString
 */
@Getter
@Setter
@NoArgsConstructor
@ToString
public class PropertyAuditInfo {

	/**
	 * FileNet property symbolic name.
	 * 
	 * @param propertyName the new property symbolic name
	 * @return Current property symbolic name
	 */
	private String propertyName;

	/**
	 * FileNet property original value. Always null for create operations
	 * 
	 * @param oldValue the property old value
	 * @return Current old value
	 */
	private Object oldValue;

	/**
	 * FileNet property new value.
	 * 
	 * @param newValue the property new value
	 * @return Current  new value
	 */
	private Object newValue;

	/**
	 * Instantiates a new property audit info.
	 *
	 * @param propertyName the property name
	 * @param oldValue the property old value
	 * @param newValue the property new value
	 */
	public PropertyAuditInfo(String propertyName, Object oldValue, Object newValue) {
		this.propertyName = propertyName;
		this.oldValue = oldValue;
		this.newValue = newValue;
	}
}
