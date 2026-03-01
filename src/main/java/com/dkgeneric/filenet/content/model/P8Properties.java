package com.dkgeneric.filenet.content.model;

import java.util.Date;
import java.util.HashMap;
import java.util.List;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

/**
 * Map&lt;String,Object&gt; wrapper to keep FileNet property values <br>
 * {@link #toString()} implemented through Lombok @ToString.
 */
@SuppressWarnings("serial")
@Getter
@Setter
@NoArgsConstructor
@ToString
public class P8Properties extends HashMap<String, Object> {

	/**
	 * Adds the property.
	 *
	 * @param name  the property name
	 * @param value the property value
	 */
	public void addProperty(String name, Object value) {
		put(name, value);
	}

	/**
	 * Gets the boolean property value.
	 *
	 * @param name the property name
	 * @return the boolean property value
	 */
	public Boolean getBooleanValue(String name) {
		return (Boolean) get(name);
	}

	/**
	 * Gets the date property value.
	 *
	 * @param name the property name
	 * @return the date property value
	 */
	public Date getDateValue(String name) {
		return (Date) get(name);
	}

	/**
	 * Gets the double property value.
	 *
	 * @param name the property name
	 * @return the double property value
	 */
	public Double getDoubleValue(String name) {
		return (Double) get(name);
	}

	/**
	 * Gets the integer property value.
	 *
	 * @param name the property name
	 * @return the integer property value
	 */
	public Integer getIntegerValue(String name) {
		return (Integer) get(name);
	}

	/**
	 * Gets the list property value.
	 *
	 * @param <T>  expected property type
	 * @param name the property name
	 * @return the list property value
	 */
	@SuppressWarnings("unchecked")
	public <T> List<T> getListValue(String name) {
		return (List<T>) get(name);
	}

	/**
	 * Gets the string property value.
	 *
	 * @param name the property name
	 * @return the string property value
	 */
	public String getStringValue(String name) {
		return (String) get(name);
	}

	/**
	 * Gets the  property value as String.
	 *
	 * @param name the property name
	 * @return the string property value as text
	 */
	public String getValueAsText(String name) {
		Object value = get(name);
		return value == null ? null : value.toString();
	}

	/**
	 * Removes the property.
	 *
	 * @param name the property name
	 */
	public void removeProperty(String name) {
		remove(name);
	}
}
