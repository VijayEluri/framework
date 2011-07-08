package org.oobium.app.dev.views.persist_services;

import static org.oobium.app.http.Action.*;
import static org.oobium.utils.ArrayUtils.*;
import static org.oobium.utils.StringUtils.*;
import static org.oobium.utils.Utils.*;
import static org.oobium.utils.json.JsonUtils.*;

import java.util.List;

import org.oobium.app.controllers.HttpController;
import org.oobium.app.persist.PersistServices;
import org.oobium.app.views.View;

@SuppressWarnings("unused")
public class ShowAllPersistServices extends View {

	public PersistServices services;

	public ShowAllPersistServices(PersistServices services) {
		this.services = services;
	}

	@Override
	public void doRenderBody(StringBuilder __sb__) throws Exception {
		List<String> list = services.getServiceNames();
		if(list.isEmpty()) {
			__sb__.append("<div>No Persist Services</div>");
		} else {
			__sb__.append("<style>.alt{background-color:cyan}</style><table><tr><th>id</th><th>service</th></tr>");
			for(int i = 0; i < list.size(); i++) {
				__sb__.append("<tr class=\"").append(h(alt())).append("\"><td>").append(h(i+1)).append("</td><td><a href=\"").append(pathTo("persist_service", i+1)).append("\">").append(h(list.get(i))).append("</a></td></tr>");
			}
			__sb__.append("</table>");
		}
	}

	@Override
	protected void doRenderTitle(StringBuilder __sb__) {
		__sb__.append("Persist Services");
	}

	@Override
	public boolean hasTitle() {
		return true;
	}

}