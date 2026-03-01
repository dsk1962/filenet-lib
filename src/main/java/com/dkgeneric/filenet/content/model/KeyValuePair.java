package com.dkgeneric.filenet.content.model;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

/**
 * General keyed value object <br>
 * {@link #toString()} implemented through Lombok @ToString
 */
@Getter
@Setter
@NoArgsConstructor
@ToString
public class KeyValuePair {

	/**
	 * Key value.
	 * 
	 * @param key the key value
	 * @return Current key value
	 */
	private String key;

	/**
	 * Object value.
	 * 
	 * @param value the object value
	 * @return Current object value
	 */
	private Object value;

	/**
	 * Creates new instance with provided key/value
	 * 
	 * @param key   new key
	 * @param value new value
	 */
	public KeyValuePair(String key, Object value) {
		this.key = key;
		this.value = value;
	}
}
