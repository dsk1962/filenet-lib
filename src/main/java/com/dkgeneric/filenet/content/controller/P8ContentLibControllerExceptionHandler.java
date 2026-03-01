package com.dkgeneric.filenet.content.controller;

import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;

import com.dkgeneric.commons.controller.BaseContollerExceptionHandler;
import com.dkgeneric.commons.model.ErrorLogEntry;
import com.dkgeneric.filenet.content.common.ServiceException;
import com.fasterxml.jackson.core.JsonProcessingException;

import lombok.extern.slf4j.Slf4j;

@ControllerAdvice
@Order(Ordered.LOWEST_PRECEDENCE - 1)
@Slf4j
public class P8ContentLibControllerExceptionHandler extends BaseContollerExceptionHandler {

	@ResponseBody
	@ExceptionHandler(ServiceException.class)
	public ResponseEntity<String> serviceExceptionHandler(ServiceException ex) throws JsonProcessingException {
		ErrorLogEntry logEntry = logRequestData(ex);
		logEntry.setErrorCode(ex.getExceptionCode());
		HttpStatus errorType = HttpStatus.INTERNAL_SERVER_ERROR;
		if (ex.getMessage() == null) {
			logEntry.setErrorMessage(ecmErrorMessages.getMessage(ex.getExceptionCode(), ex.getParameters()));
			errorType = HttpStatus.valueOf(ecmErrorMessages.getMessageType(ex.getExceptionCode()));
		} else
			logEntry.setErrorMessage(ex.getMessage());
		log.error(ex.getMessage(), ex);
		return createResponseEntity(mapper.writeValueAsString(logEntry), errorType);
	}
}
