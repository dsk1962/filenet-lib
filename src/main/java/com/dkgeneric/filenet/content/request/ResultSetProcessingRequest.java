package com.dkgeneric.filenet.content.request;

import com.dkgeneric.filenet.content.model.IResultSetProcessor;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

/**
 * This class contains data to search p8 objects and process them <br>
 * {@link #toString()} implemented through Lombok @ToString
 */
@Getter
@Setter
@NoArgsConstructor
@ToString(callSuper = true)

public class ResultSetProcessingRequest extends SearchRequest {

	/**
	 * Result set processor instance 
	 * 
	 * @param resultSetProcessor the new result set processor instance
	 * @return Current result set processor instance
	 */
	private IResultSetProcessor resultSetProcessor;

}
