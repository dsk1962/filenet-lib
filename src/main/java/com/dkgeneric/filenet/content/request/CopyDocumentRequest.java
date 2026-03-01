package com.dkgeneric.filenet.content.request;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import com.dkgeneric.filenet.content.model.P8ContentObject;
import com.dkgeneric.filenet.content.service.CopyPostProcessor;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

/**
 * This class contains data to complete document creation <br>
 * {@link #toString()} implemented through Lombok @ToString
 */
@Getter
@Setter
@NoArgsConstructor
@ToString(callSuper = true)

public class CopyDocumentRequest extends BaseRequest {

	/**
	 * 
	 * @param p8ContentObject data to be applied to copy
	 * @return Current data to be applied to copy
	 */
	private List<P8ContentObject> p8ContentObjectList = new ArrayList<>();

	/**
	 * 
	 * @param sourceDocumentId id of the document to copy
	 * @return Current sourceDocumentId of the document to copy
	 */
	private String sourceDocumentId;

	/**
	 * 
	 * @param propertyNamesToCopy list of property names to copy from source to target
	 * @return Current list of property names to copy from source to target
	 */
	private Collection<String> propertyNamesToCopy = new ArrayList<>();

	/**
	 * 
	 * @param copyPostProcessor code to be run after document is created but before save
	 * @return Current code to be run after document is created but before save
	 */
	private CopyPostProcessor copyPostProcessor;

	public void addP8ContentObject(P8ContentObject contentObject) {
		p8ContentObjectList.add(contentObject);
	}
}
