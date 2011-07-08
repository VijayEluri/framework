package org.oobium.app.dev.views.persist_services;

import static org.oobium.app.http.Action.*;
import static org.oobium.utils.ArrayUtils.*;
import static org.oobium.utils.StringUtils.*;
import static org.oobium.utils.Utils.*;
import static org.oobium.utils.json.JsonUtils.*;

import java.util.*;

import org.oobium.app.controllers.HttpController;
import org.oobium.app.views.View;
import org.oobium.persist.PersistService;
import org.oobium.persist.ServiceInfo;

@SuppressWarnings("unused")
public class ShowPersistService extends View {

	public int id;
	public PersistService service;

	public ShowPersistService(int id, PersistService service) {
		this.id = id;
		this.service = service;
	}

	@Override
	public void doRenderBody(StringBuilder __sb__) throws Exception {
		ServiceInfo info = service.getInfo();
		if(info == null) {
			__sb__.append("<div>").append(h(service)).append("</div>");
		} else {
			__sb__.append("<table><tr><td>Name:</td><td>").append(h(info.getSymbolicName())).append("</td></tr><tr><td>Provider:</td><td>").append(h(info.getProvider())).append("</td></tr><tr><td>Version:</td><td>").append(h(info.getVersion())).append("</td></tr><tr><td>Description:</td><td>").append(h(info.getName())).append("</td></tr></table>");
		}
		yield(new Query(id), __sb__);
	}

	@Override
	protected void doRenderTitle(StringBuilder __sb__) {
		__sb__.append("Persist Service: ").append(h(service)).append("");
	}

	@Override
	public boolean hasTitle() {
		return true;
	}

}