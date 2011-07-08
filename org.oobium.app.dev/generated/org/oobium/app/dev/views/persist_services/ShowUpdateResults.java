package org.oobium.app.dev.views.persist_services;

import static org.oobium.app.http.Action.*;
import static org.oobium.utils.ArrayUtils.*;
import static org.oobium.utils.StringUtils.*;
import static org.oobium.utils.Utils.*;
import static org.oobium.utils.json.JsonUtils.*;

import java.util.*;

import org.oobium.app.controllers.HttpController;
import org.oobium.app.views.View;

@SuppressWarnings("unused")
public class ShowUpdateResults extends View {

	public int id;
	public String query;
	public int results;

	public ShowUpdateResults(int id, String query, int results) {
		this.id = id;
		this.query = query;
		this.results = results;
	}

	@Override
	public void doRenderBody(StringBuilder __sb__) throws Exception {
		__sb__.append("<div>The command returned a value of: ").append(h(results)).append("</div>");
		yield(new Query(id, query), __sb__);
	}

}