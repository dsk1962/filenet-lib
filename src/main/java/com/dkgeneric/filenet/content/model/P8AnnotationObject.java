package com.dkgeneric.filenet.content.model;

import com.dkgeneric.filenet.content.resources.P8ContentResource;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class P8AnnotationObject extends P8Object{

	/**
	 * Name of annotation class
	 */
	private String p8AnnotationClassName;
	/**
	 * Annoated FileNet object
	 */
	private P8Object p8AnnotedObject;
	
	/**
	 * FileNet object content data.
	 * 
	 * @param resource the new FileNet object content data
	 * @return Current FileNet object content data
	 */
	private P8ContentResource resource;
}
