/*******************************************************************************
 * Copyright (c) 2010 Oobium, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Jeremy Dowdall <jeremy@oobium.com> - initial API and implementation
 ******************************************************************************/
package org.oobium.app.views;

import static org.oobium.utils.StringUtils.camelCase;
import static org.oobium.utils.StringUtils.h;
import static org.oobium.utils.StringUtils.pluralize;
import static org.oobium.utils.StringUtils.titleize;

import java.lang.reflect.Constructor;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.oobium.app.controllers.HttpController;
import org.oobium.app.controllers.IFlash;
import org.oobium.app.controllers.IHttp;
import org.oobium.app.controllers.IParams;
import org.oobium.app.controllers.ISessions;
import org.oobium.app.http.Action;
import org.oobium.app.http.MimeType;
import org.oobium.app.request.Request;
import org.oobium.app.response.Response;
import org.oobium.app.routing.IPathRouting;
import org.oobium.app.routing.IUrlRouting;
import org.oobium.app.routing.Router;
import org.oobium.app.sessions.Session;
import org.oobium.logging.Logger;
import org.oobium.persist.Model;
import org.oobium.utils.json.JsonUtils;

public class View implements IFlash, IParams, IPathRouting, IUrlRouting, ISessions, IHttp {

	/**
	 * Render a view of the given class for the given request. Since this method does not take a 
	 * Router object then it may not resolve pathTo requests from the perspective of the Application
	 * rather than the given view class's Module. Thus, this method is primarily suited for views that
	 * will not make use of pathTo, or are top-level views whose use of pathTo is only relative to the
	 * application. Default error views (404 and 500) use this method.
	 * @param viewClass the class of view to be rendered
	 * @param request the request to use while rendering the view
	 * @return the rendered Response object
	 * @throws Exception this will run user generated content - be prepared for anything.
	 */
	public static Response render(Class<? extends View> viewClass, Request request) throws Exception {
		View view = viewClass.newInstance();
		return render(null, view, request, new HashMap<String, Object>(0));
	}
	
	/**
	 * Render the given view for the given request. Since this method does not take a
	 * Router object then it may not resolve pathTo requests from the perspective of the Application
	 * rather than the given view class's Module. Thus, this method is primarily suited for views that
	 * will not make use of pathTo, or are top-level views whose use of pathTo is only relative to the
	 * application.
	 * @param view the view to be rendered
	 * @param request the request to use while rendering the view
	 * @return the rendered Response object
	 * @throws Exception this will run user generated content - be prepared for anything.
	 */
	public static Response render(View view, Request request) throws Exception {
		return render(null, view, request, new HashMap<String, Object>(0));
	}

	/**
	 * Render the given view for the given request. Any pathTo requests will be resolved from the perspective of 
	 * the given Router.
	 * @param router the router from which resolution of pathTo requests will begin
	 * @param view the view to be rendered
	 * @param request the request to use while rendering the view
	 * @param params a map of parameters that are to be available to the view
	 * @return the rendered Response object
	 * @throws Exception this will run user generated content - be prepared for anything.
	 */
	public static Response render(Router router, View view, Request request, Map<String, Object> params) throws Exception {
		HttpController controller = new HttpController();
		controller.initialize(router, request, params);
		controller.render(view);
		return controller.getResponse();
	}
	

	private ViewRenderer renderer;

	protected Logger logger;
	protected HttpController controller;
	protected Request request;

	private View child;
	
	private String layoutName;
	private Class<? extends View> layout;
	private int altCount;

	
	@Override
	public boolean accepts(MimeType type) {
		return controller.accepts(type);
	}
	
	public String alt() {
		return alt("alt");
	}
	
	public String alt(String s) {
		return (altCount++ % 2 == 0) ? s : "";
	}
	
	public String concat(String base, String opt, boolean condition) {
		if(condition) {
			return base.concat(opt);
		}
		return base;
	}
	
	protected void doRenderBody(StringBuilder sb) throws Exception {
		// subclasses to override if necessary
	}
	
	protected void doRenderMeta(StringBuilder sb) {
		// subclasses to override if necessary
	}
	
	protected void doRenderScript(StringBuilder sb) {
		// subclasses to override if necessary
	}
	
	protected void doRenderStyle(StringBuilder sb) {
		// subclasses to override if necessary
	}
	
	protected void doRenderTitle(StringBuilder sb) {
		// subclasses to override if necessary
	}
	
	@Override
	public String flash(String name) {
		return controller.flash(name);
	}
	
	@Override
	public <T> T flash(String name, Class<T> type) {
		return controller.flash(name, type);
	}

	@Override
	public <T> T flash(String name, T defaultValue) {
		return controller.flash(name, defaultValue);
	}

	@Override
	public Action getAction() {
		return controller.getAction();
	}
	
	@Override
	public String getActionName() {
		return controller.getActionName();
	}
	
	@Override
	public <T extends Model> T getAuthenticated(Class<T> clazz) {
		return controller.getAuthenticated(clazz);
	}
	
	public View getChild() {
		return child;
	}
	
	protected String getContent(String name) {
		return renderer.getContent(name);
	}
	
	@Override
	public String getControllerName() {
		return controller.getControllerName();
	}
	
	@Override
	public String getFlash(String name) {
		return controller.getFlash(name);
	}
	
	@Override
	public <T> T getFlash(String name, Class<T> type) {
		return controller.getFlash(name, type);
	}
	
	@Override
	public <T> T getFlash(String name, T defaultValue) {
		return controller.getFlash(name, defaultValue);
	}
	
	@Override
	public String getFlashError() {
		return controller.getFlashError();
	}
	
	@Override
	public String getFlashNotice() {
		return controller.getFlashNotice();
	}
	
	@Override
	public String getFlashWarning() {
		return controller.getFlashWarning();
	}
	
	public View getLayout() {
		Class<?> layout = this.layout;
		
		if(layout == null) {
			String pname = getClass().getPackage().getName();
			int ix = pname.lastIndexOf('.');
			if(layoutName != null) {
				try {
					String fname = pname.substring(0, ix) + "._layouts." + layoutName;
					layout = Class.forName(fname, true, getClass().getClassLoader());
				} catch(ClassNotFoundException e) {
					// oh well...
				}
			} else {
				// look for a view specific layout
				try {
					String fname = pname.substring(0, ix) + "._layouts." + camelCase(pname.substring(ix+1)) + "Layout";
					layout = Class.forName(fname, true, getClass().getClassLoader());
				} catch(ClassNotFoundException e1) {
					// look for the default view layout (cache...)
					try {
						String fname = pname.substring(0, ix) + "._layouts._Layout";
						layout = Class.forName(fname, true, getClass().getClassLoader());
					} catch(ClassNotFoundException e2) {
						// oh well...
					}
				}
			}
		}
		
		if(layout != null) {
			if(View.class.isAssignableFrom(layout)) {
				try {
					Constructor<?> c = layout.getConstructor();
					return (View) c.newInstance();
				} catch(Exception e) {
					// oh well...
				}
			}
		}
		return null;
	}
	
	@Override
	public Object getParam(String name) {
		return controller.getParam(name);
	}
	
	@Override
	public <T> T getParam(String name, Class<T> clazz) {
		return controller.getParam(name, clazz);
	}
	
	@Override
	public <T> T getParam(String name, T defaultValue) {
		return controller.getParam(name, defaultValue);
	}
	
	@Override
	public Set<String> getParams() {
		return controller.getParams();
	}
	
	@Override
	public Session getSession() {
		return controller.getSession();
	}
	
	@Override
	public Session getSession(boolean create) {
		return controller.getSession(create);
	}

	public String getTitle() {
		StringBuilder sb = new StringBuilder();
		renderTitle(sb);
		return sb.toString();
	}
	
	public boolean hasChild() {
		return child != null;
	}
	
	protected boolean hasContent(String name) {
		return renderer.hasContent(name);
	}
	
	@Override
	public boolean hasFlash(String name) {
		return controller.hasFlash(name);
	}
	
	@Override
	public boolean hasFlashError() {
		return controller.hasFlashError();
	}
	
	@Override
	public boolean hasFlashNotice() {
		return controller.hasFlashNotice();
	}

	@Override
	public boolean hasFlashWarning() {
		return controller.hasFlashWarning();
	}
	
	public boolean hasMeta() {
		return false;
	}
	
	public boolean hasMany(String field) {
		return field != null && field.equals(getParam("hasMany"));
	}
	
	@Override
	public boolean hasParam(String name) {
		return controller.hasParam(name);
	}
	
	@Override
	public boolean hasParams() {
		return controller.hasParams();
	}
	
	public boolean hasScript() {
		return false;
	}
	
	@Override
	public boolean hasSession() {
		return controller.hasSession();
	}
	
	public boolean hasStyle() {
		return false;
	}
	
	public boolean hasTitle() {
		return false;
	}
	
	@Override
	public boolean isAction(Action action) {
		return controller.isAction(action);
	}
	
	@Override
	public boolean isAuthenticated() {
		return controller.isAuthenticated();
	}
	
	@Override
	public boolean isAuthenticated(Model model) {
		return controller.isAuthenticated(model);
	}
	
	@Override
	public boolean isPath(String path) {
		return request.getPath().equals(path);
	}
	
	@Override
	public boolean isXhr() {
		return controller.isXhr();
	}
	
	protected void messagesBlock(StringBuilder sb) {
		messagesBlock(sb, true, true, true);
	}

	protected void errorsBlock(StringBuilder sb, Model model, String title, String message) {
		if(model.hasErrors()) {
			List<String> errors = model.getErrorsList();
			sb.append("<div class=\"errorExplanation\">");
			if(title == null) {
				String s1 = pluralize(errors.size(), "error");
				String s2 = titleize(model.getClass().getSimpleName()).toLowerCase();
				sb.append("<h2>").append(s1).append(" prohibited this ").append(s2).append(" from being saved").append("</h2>");
			} else if(title.length() > 0) {
				sb.append("<h2>").append(h(title)).append("</h2>");
			}
			if(message == null) {
				sb.append("<p>There were problems with the following fields:</p>");
			} else if(message.length() > 0) {
				sb.append("<p>").append(h(message)).append("</p>");
			}
			sb.append("<ul>");
			for(String error : model.getErrorsList()) {
				sb.append("<li>").append(h(error)).append("</li>");
			}
			sb.append("</ul>");
			sb.append("</div>");
		}
	}
	
	protected void messagesBlock(StringBuilder sb, boolean errors, boolean warnings, boolean notices) {
		if(errors && hasFlashError()) {
			sb.append("<div class=\"errors\">");
			sb.append("<ul>");
			Object error = JsonUtils.toObject(getFlashError());
			if(error instanceof Iterable<?>) {
				for(Object o : (Iterable<?>) error) {
					sb.append("<li>").append(h(o)).append("</li>");
				}
			} else {
				sb.append("<li>").append(h(error)).append("</li>");
			}
			sb.append("</ul>");
			sb.append("</div>");
		}

		if(warnings && hasFlashWarning()) {
			sb.append("<div class=\"warnings\">");
			sb.append("<ul>");
			Object error = JsonUtils.toObject(getFlashWarning());
			if(error instanceof Iterable<?>) {
				for(Object o : (Iterable<?>) error) {
					sb.append("<li>").append(h(o)).append("</li>");
				}
			} else {
				sb.append("<li>").append(h(error)).append("</li>");
			}
			sb.append("</ul>");
			sb.append("</div>");
		}
		
		if(notices && hasFlashNotice()) {
			sb.append("<div class=\"notices\">");
			sb.append("<ul>");
			Object error = JsonUtils.toObject(getFlashNotice());
			if(error instanceof Iterable<?>) {
				for(Object o : (Iterable<?>) error) {
					sb.append("<li>").append(h(o)).append("</li>");
				}
			} else {
				sb.append("<li>").append(h(error)).append("</li>");
			}
			sb.append("</ul>");
			sb.append("</div>");
		}
	}

	@Override
	public String param(String name) {
		return controller.param(name);
	}

	@Override
	public <T> T param(String name, Class<T> clazz) {
		return controller.param(name, clazz);
	}

	@Override
	public <T> T param(String name, T defaultValue) {
		return controller.param(name, defaultValue);
	}

	@Override
	public Set<String> params() {
		return controller.params();
	}
	
	@Override
	public String pathTo(Class<? extends Model> modelClass) {
		return controller.pathTo(modelClass);
	}
	
	@Override
	public String pathTo(Class<? extends Model> modelClass, Action action) {
		return controller.pathTo(modelClass, action);
	}

	@Override
	public String pathTo(Model model) {
		return controller.pathTo(model);
	}
	
	@Override
	public String pathTo(Model model, Action action) {
		return controller.pathTo(model, action);
	}
	
	@Override
	public String pathTo(Model parent, String field) {
		return controller.pathTo(parent, field);
	}
	
	@Override
	public String pathTo(Model parent, String field, Action action) {
		return controller.pathTo(parent, field, action);
	}

	@Override
	public String pathTo(String routeName) {
		return controller.pathTo(routeName);
	}
	
	@Override
	public String pathTo(String routeName, Model model) {
		return controller.pathTo(routeName, model);
	}
	
	@Override
	public String pathTo(String routeName, Object... params) {
		return controller.pathTo(routeName, params);
	}
	
	protected String putContent(String name, String content) {
		return renderer.putContent(name, content);
	}

	void render() {
		render(renderer.body);
	}
	
	private void render(StringBuilder body) {
		try {
			doRenderStyle(renderer.style);
			doRenderScript(renderer.script);
			doRenderBody(body);
		} catch(Exception e) {
			if(e instanceof RuntimeException) {
				throw (RuntimeException) e;
			} else {
				throw new RuntimeException("Exception thrown during render", e);
			}
		}
	}

	public void renderMeta(StringBuilder sb) {
		doRenderMeta(sb);
		if(hasChild()) {
			child.renderMeta(sb);
		}
	}

	public void renderTitle(StringBuilder sb) {
		if(hasChild() && child.hasTitle()) {
			child.renderTitle(sb);
		} else {
			doRenderTitle(sb);
		}
	}

	public View setChild(View child) {
		this.child = child;
		this.child.setRenderer(renderer);
		return this;
	}

	public View setLayout(Class<? extends View> layout) {
		this.layout = layout;
		return this;
	}

	public View setLayout(String layoutName) {
		this.layoutName = layoutName;
		return this;
	}

	public void setRenderer(ViewRenderer renderer) {
		this.renderer = renderer;
		if(renderer == null) {
			controller = null;
			logger = null;
			request = null;
		} else {
			controller = renderer.controller;
			logger = controller.getLogger();
			request = controller.getRequest();
		}
	}
	
	@Override
	public String urlTo(Class<? extends Model> modelClass) {
		return controller.urlTo(modelClass);
	}
	
	@Override
	public String urlTo(Class<? extends Model> modelClass, Action action) {
		return controller.urlTo(modelClass, action);
	}
	
	@Override
	public String urlTo(Model model) {
		return controller.urlTo(model);
	}

	@Override
	public String urlTo(Model model, Action action) {
		return controller.urlTo(model, action);
	}

	@Override
	public String urlTo(Model parent, String field) {
		return controller.urlTo(parent, field);
	}

	@Override
	public String urlTo(Model parent, String field, Action action) {
		return controller.urlTo(parent, field, action);
	}

	@Override
	public String urlTo(String routeName) {
		return controller.urlTo(routeName);
	}

	@Override
	public String urlTo(String routeName, Model model) {
		return controller.urlTo(routeName, model);
	}

	@Override
	public String urlTo(String routeName, Object... params) {
		return controller.urlTo(routeName, params);
	}
	
	@Override
	public MimeType wants() {
		return controller.wants();
	}
	
	@Override
	public MimeType.Name wants(MimeType... options) {
		return controller.wants(options);
	}

	@Override
	public boolean wants(MimeType type) {
		return controller.wants(type);
	}

	protected void yield(StringBuilder body) {
		if(hasChild()) {
			child.render(body);
		}
	}
	
	protected void yield(String name, StringBuilder body) {
		if(body != renderer.body) {
			logger.warn(new IllegalStateException("named yields cannot be inside contentFor or capture elements"));
		} else {
			if(name != null && name.length() > 0) {
				renderer.addPosition(name);
			}
		}
	}
	
	protected void yield(View view, StringBuilder body) {
		if(view != null) {
			view.setRenderer(renderer);
			view.render(body);
		}
	}
	
}
