package com.dkgeneric.filenet.content.resources;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

import org.springframework.util.FileCopyUtils;

import com.dkgeneric.filenet.content.common.ServiceException;
import com.dkgeneric.filenet.content.exceptioncodes.P8ExceptionCodes;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

/**
 * This class describes document content object <br>
 * {@link #toString()} implemented through Lombok @ToString. resourceObject is
 * excluded.
 */
@Getter
@Setter
@NoArgsConstructor
@ToString(exclude = { "resourceObject" })
public class P8ContentResource {

	/**
	 * File name
	 * 
	 * @param fileName the new file name
	 * @return Current file name
	 */
	private String fileName;

	/**
	 * URL to retrieve document content
	 * 
	 * @param contentLocation the new content URL
	 * @return Current content URL
	 */
	private String contentLocation;

	/**
	 * Content type (mimetype)
	 * 
	 * @param contentType the new content type
	 * @return Current content type
	 */
	private String contentType;

	/**
	 * Content size (-1 if size is unknown)
	 * 
	 * @param size the new content size
	 * @return Current content size
	 */
	private long size = -1;

	/**
	 * Broken resource flag (FNRCC0019 error)
	 * 
	 * @param brokenResource the new flag value
	 * @return Current flag value
	 */
	private boolean brokenResource = false;

	/**
	 * Resource object. Current implementation supports {@link InputStream} or
	 * byte[]
	 * 
	 * @param resourceObject the new resource object
	 * @return Current resource object
	 */
	private Object resourceObject;

	/**
	 * Gets resource as byte array.
	 *
	 * @return the bytes
	 * @throws IOException Signals that an I/O exception has occurred.
	 * @throws ServiceException Signals that internal resource object can't be processed (unknown)
	 */
	public byte[] getBytes() throws IOException, ServiceException {
		if (brokenResource)
			throw new ServiceException(P8ExceptionCodes.BROKEN_RESOURCE_OBJECT.getExceptionCode());
		if (resourceObject == null)
			return new byte[0];
		if (resourceObject instanceof InputStream) {
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			FileCopyUtils.copy(getInputStream(), baos);
			return baos.toByteArray();
		} else if (resourceObject instanceof byte[] byteArray)
			return byteArray;
		else
			throw new ServiceException(P8ExceptionCodes.UNKNOWN_RESOURCE_OBJECT.getExceptionCode(),
					new Object[] { resourceObject.getClass().getCanonicalName() });
	}

	/**
	 * Gets resource as input stream.
	 *
	 * @return the input stream
	 * @throws ServiceException Signals that internal resource object can't be processed (unknown)
	 */
	public InputStream getInputStream() throws ServiceException {
		if (brokenResource)
			throw new ServiceException(P8ExceptionCodes.BROKEN_RESOURCE_OBJECT.getExceptionCode());
		if (resourceObject == null)
			return null;
		if (resourceObject instanceof InputStream is)
			return is;
		else if (resourceObject instanceof byte[] byteArray)
			return new ByteArrayInputStream(byteArray);
		else
			throw new ServiceException(P8ExceptionCodes.UNKNOWN_RESOURCE_OBJECT.getExceptionCode(),
					new Object[] { resourceObject.getClass().getCanonicalName() });
	}
}
