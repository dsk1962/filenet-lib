package com.dkgeneric.filenet.content.request;

import com.dkgeneric.filenet.content.model.P8AnnotationObject;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;


@Getter
@Setter
@NoArgsConstructor
@ToString(callSuper = true)
public class CreateAnnotationRequest extends BaseRequest{

	/**
	 * FileNet data required to create annotation
	 * 
	 * @param P8AnnotationObject the new annotation object
	 * @return Current annotation data
	 */
	private P8AnnotationObject p8AnnotationObject;
}
