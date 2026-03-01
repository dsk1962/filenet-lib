package com.dkgeneric.filenet.content.service;

import static com.dkgeneric.filenet.content.common.ECMConstants.*;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import com.dkgeneric.filenet.content.config.P8SearchConfiguration;
import com.dkgeneric.filenet.content.request.CopyDocumentRequest;
import com.filenet.api.core.Document;

@Component
@ConditionalOnProperty(name = "com.davita.ecm.p8.content.service.enabled", matchIfMissing = true)
public class DavitaCopyPostProcessor implements CopyPostProcessor {

	private final P8SearchConfiguration p8SearchConfiguration;

	public DavitaCopyPostProcessor(P8SearchConfiguration p8SearchConfiguration) {
		this.p8SearchConfiguration = p8SearchConfiguration;
	}

	@Override
	public void postProcess(Document source, Document target, CopyDocumentRequest copyDocumentRequest) {

		if (!target.getProperties().isPropertyPresent(DVA_LINKTOCONTENT_PROPERTYNAME))
			target.getProperties().putObjectValue(DVA_LINKTOCONTENT_PROPERTYNAME, source);
		if (!target.getProperties().isPropertyPresent(DVA_LINKEDDOCID_PROPERTYNAME))
			target.getProperties().putObjectValue(DVA_LINKEDDOCID_PROPERTYNAME, source.get_Id().toString());
		if (!target.getProperties().isPropertyPresent(DVA_DOCSOURCE_PROPERTYNAME))
			target.getProperties().putValue(DVA_DOCSOURCE_PROPERTYNAME, p8SearchConfiguration.getDefaultDVADocSource());
		if (!target.getProperties().isPropertyPresent(DVA_AVAILABILITYSTATUS_PROPERTYNAME))
			target.getProperties().putValue(DVA_AVAILABILITYSTATUS_PROPERTYNAME, DVA_AVAILABILITY_STATUS_ACTIVE);
	}
}
