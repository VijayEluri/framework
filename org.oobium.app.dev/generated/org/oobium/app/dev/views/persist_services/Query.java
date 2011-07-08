package org.oobium.app.dev.views.persist_services;

import static org.oobium.app.http.Action.*;
import static org.oobium.utils.ArrayUtils.*;
import static org.oobium.utils.StringUtils.*;
import static org.oobium.utils.Utils.*;
import static org.oobium.utils.json.JsonUtils.*;

import org.oobium.app.controllers.HttpController;
import org.oobium.app.views.View;

@SuppressWarnings("unused")
public class Query extends View {

	public int id;
	public String query;

	public Query(int id) {
		this.id = id;
		this.query = null;
	}

	public Query(int id, String query) {
		this.id = id;
		this.query = query;
	}

	@Override
	public void doRenderBody(StringBuilder __sb__) throws Exception {
		__sb__.append("<script src='/jquery-1.4.4.js'></script><script src='/application.js'></script><div style=\"margin-top: 15px\"><form action=\"").append(h(pathTo("persist_service", id))).append("\" method=\"get\"><label for=\"q\">Query:</label><input type=\"text\" name=\"q\" style=\"width: 500px\" value=\"").append(h(query)).append("\" /><input type=\"submit\" id=\"submit\" onclick=\"$('#submit').hide();$('#executing').show();\" value=\"Submit\" /><img id=\"executing\" style=\"display:none\" src=\"/executing.gif\"></img></form></div>");
	}

}