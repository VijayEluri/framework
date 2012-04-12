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
public class ShowQueryResults extends View {

	public int id;
	public String query;
	public List<Map<String, Object>> results;

	public ShowQueryResults(int id, String query) {
		this.id = id;
		this.query = query;
		this.results = null;
	}

	public ShowQueryResults(int id, String query, List<Map<String, Object>> results) {
		this.id = id;
		this.query = query;
		this.results = results;
	}

	@Override
	public void render(StringBuilder __head__, StringBuilder __body__) throws Exception {
		if(hasFlashError()) {
			messagesBlock(__body__);
		} else if(results.isEmpty()) {
			__body__.append("<div>Empty result set</div>");
		} else {
			__body__.append("<style>.alt{background-color:cyan}</style><table><tr>");
			for(String k : results.get(0).keySet()) {
				__body__.append("<th>").append(h(k)).append("</th>");
			}
			__body__.append("</tr>");
			for(Map<String, Object> map : results) {
				__body__.append("<tr class=\"").append(h(alt())).append("\">");
				for(Object v : map.values()) {
					__body__.append("<td>").append(h(v)).append("</td>");
				}
				__body__.append("</tr>");
			}
			__body__.append("</table>");
		}
		yield(new Query(id, query), __body__);
	}

}