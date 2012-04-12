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
	public void render(StringBuilder __head__, StringBuilder __body__) throws Exception {
		ServiceInfo info = service.getInfo();
		if(info == null) {
			__body__.append("<div>").append(h(service)).append("</div>");
		} else {
			__body__.append("<table><tr><td>Name:</td><td>").append(h(info.getSymbolicName())).append("</td></tr><tr><td>Provider:</td><td>").append(h(info.getProvider())).append("</td></tr><tr><td>Version:</td><td>").append(h(info.getVersion())).append("</td></tr><tr><td>Description:</td><td>").append(h(info.getName())).append("</td></tr></table>");
		}
		yield(new Query(id), __body__);
	}

}