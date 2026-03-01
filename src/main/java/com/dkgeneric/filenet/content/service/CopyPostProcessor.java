package com.dkgeneric.filenet.content.service;

import com.dkgeneric.filenet.content.request.CopyDocumentRequest;
import com.filenet.api.core.Document;

public interface CopyPostProcessor {

	public void postProcess(Document source, Document target, CopyDocumentRequest copyDocumentRequest);

}
