package org.oobium.app.dev.views.application;

import static org.oobium.app.http.Action.*;
import static org.oobium.utils.ArrayUtils.*;
import static org.oobium.utils.StringUtils.*;
import static org.oobium.utils.Utils.*;
import static org.oobium.utils.json.JsonUtils.*;

import org.oobium.app.controllers.HttpController;
import org.oobium.app.views.View;

@SuppressWarnings("unused")
public class Error500 extends View {

	private Exception exception;
	private String trace;

	public Error500(Exception exception, String trace) {
		this.exception = exception;
		this.trace = trace;
	}

	@Override
	public void doRenderBody(StringBuilder __sb__) throws Exception {
		__sb__.append("<style>.indent{margin-left:15px} .trace{color:red}</style><div class=\"trace\"><h3>").append(h(exception.getMessage())).append("</h3><div>").append(h(trace)).append("</div></div>");
	}

	@Override
	protected void doRenderTitle(StringBuilder __sb__) {
		__sb__.append("/Error500");
	}

	@Override
	public boolean hasTitle() {
		return true;
	}

}