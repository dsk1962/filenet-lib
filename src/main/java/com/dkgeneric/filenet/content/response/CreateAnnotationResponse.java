package com.dkgeneric.filenet.content.response;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@NoArgsConstructor
@ToString(callSuper = true)
public class CreateAnnotationResponse extends BaseResponse {
	
	/**
	 * Annotation id of created annotation
	 * 
	 * @param p8AnnotationId the new annotation id
	 * @return Current annotation id
	 */
	private String p8AnnotationId;
}
