package com.dkgeneric.filenet.content.request;

import java.util.ArrayList;
import java.util.List;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

/**
 * This class contains data to retrieve document content <br>
 * {@link #toString()} implemented through Lombok @ToString
 */
@Getter
@Setter
@NoArgsConstructor
@ToString(callSuper = true)

public class GetClassDefinitionRequest extends BaseRequest {

	/**
	 * Class definition symbolic names
	 * 
	 * @param symbolicNames the new list of P8 object class symbolic names  
	 * @return Current list of P8 object class symbolic names
	 */
	private List<String> symbolicNames;

	/**
	 * Adds the class definition.
	 *
	 * @param symbolicName the new class definition symbolic name
	 */
	public void addSymbolicName(String symbolicName) {
		if (symbolicNames == null)
			symbolicNames = new ArrayList<>();
		symbolicNames.add(symbolicName);

	}
}
