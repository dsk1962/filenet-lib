package com.dkgeneric.filenet.content.response;

import java.util.ArrayList;
import java.util.List;

import org.springframework.util.CollectionUtils;

import com.dkgeneric.filenet.content.model.ClassDefinition;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

/**
 * Contains results of get document content request <br>
 * {@link #toString()} implemented through Lombok @ToString
 */
@Getter
@Setter
@NoArgsConstructor
@ToString(callSuper = true, exclude = { "classDefinitions" })
public class GetClassDefinitionResponse extends BaseResponse {

	/**
	 * Class definition symbolic names
	 * 
	 * @param classDefinitions the new list of P8 object class symbolic names  
	 * @return Current list of P8 object class symbolic names
	 */
	private List<ClassDefinition> classDefinitions;

	/**
	 * Adds the class definition.
	 *
	 * @param classDefinition the class definition
	 */
	public void addClassDefinition(ClassDefinition classDefinition) {
		if (classDefinitions == null)
			classDefinitions = new ArrayList<>();
		classDefinitions.add(classDefinition);
	}

	/**
	 * Gets the first class definition from list. Returns null if list is empty or null
	 *
	 * @return the class definition
	 */
	public ClassDefinition getClassDefinition() {
		return CollectionUtils.isEmpty(classDefinitions) ? null : classDefinitions.get(0);
	}
}
