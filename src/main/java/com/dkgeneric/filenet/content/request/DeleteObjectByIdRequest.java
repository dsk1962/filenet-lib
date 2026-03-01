package com.dkgeneric.filenet.content.request;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

/**
 * This class contains data to delete object <br>
 * {@link #toString()} implemented through Lombok @ToString
 */
@Getter
@Setter
@NoArgsConstructor
@ToString(callSuper = true)

public class DeleteObjectByIdRequest extends BaseRequest {

	/**
	 * FileNet object id like {107DE07F-0000-C315-97F6-28C25358CD22}
	 * 
	 * @param objectId the new object id
	 * @return Current object id
	 */
	private String objectId;

}
