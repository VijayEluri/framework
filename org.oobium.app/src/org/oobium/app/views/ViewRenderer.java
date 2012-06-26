package org.oobium.app.views;

import static org.oobium.app.http.MimeType.CSS;
import static org.oobium.app.http.MimeType.JS;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;

import org.oobium.app.controllers.HttpController;
import org.oobium.app.request.Request;
import org.oobium.app.routing.Router;
import org.oobium.logging.Logger;
import org.oobium.pipeline.AssetPipeline;
import org.oobium.pipeline.PipelinedAsset;
import org.oobium.utils.Config.Mode;

public class ViewRenderer {

	private class Position {
		String name;
		int pos;
		Position(String name, int pos) { this.name = name; this.pos = pos; }
	}

	/**
	 * temporary toggle
	 */
	private static final boolean useScriptPipeline = Mode.isNotDEV();

	/**
	 * temporary toggle
	 */
	private static final boolean useStylePipeline = Mode.isNotDEV();
	

	private final Logger logger;
	
	HttpController controller;
	private View view;
	private View layout;
	private boolean partial;

	String title;
	StringBuilder head;
	StringBuilder body;
	
	private List<Position> positions;
	private Map<String, String> contentMap;

	boolean includeScriptEnvironment;
	private LinkedHashSet<String> externalScripts;
	private LinkedHashSet<String> externalStyles;

	public ViewRenderer(HttpController controller, View view) {
		this.logger = controller.getLogger();
		this.controller = controller;
		this.view = view;
	}

	void addExternalScript(Class<? extends ScriptFile> asset) {
		addExternalScript(Router.getAssetName(asset));
	}
	
	void addExternalScript(String src) {
		if(externalScripts == null) {
			externalScripts = new LinkedHashSet<String>();
		}
		externalScripts.add(src);
	}
	
	void addExternalScript(ScriptFile asset) {
		addExternalScript(Router.getAssetName(asset.getClass()));
	}
	
	void addExternalStyle(Class<? extends StyleSheet> asset) {
		addExternalStyle(Router.getAssetName(asset));
	}
	
	void addExternalStyle(String href) {
		if(externalStyles == null) {
			externalStyles = new LinkedHashSet<String>();
		}
		externalStyles.add(href);
	}
	
	void addExternalStyle(StyleSheet asset) {
		addExternalStyle(Router.getAssetName(asset.getClass()));
	}
	
	void addPosition(String name) {
		if(positions == null) {
			positions = new ArrayList<Position>();
		}
		positions.add(new Position(name, body.length()));
	}

	private void appendExternalScripts(StringBuilder sb) {
		if(externalScripts != null && !externalScripts.isEmpty()) {
			if(useScriptPipeline) {
				AssetPipeline service = controller.getApplication().getPipelineService();
				if(service != null) {
					PipelinedAsset asset = service.getAsset(JS, getUrls(externalScripts));
					if(asset.ready()) {
						sb.append("<script type='text/javascript' src='").append(asset.getLocation()).append("'></script>");
						return;
					} else {
						logger.debug("pipelined asset not ready yet; reverting to separate scripts");
						// fall through
					}
				} else {
					logger.warn("asset pipeline service is not available; reverting to separate scripts");
					// fall through
				}
			}
			for(String src : externalScripts) {
				sb.append("<script type='text/javascript' src='").append(src).append("'></script>");
			}
		}
	}
	
	private void appendExternalStyles(StringBuilder sb) {
		if(externalStyles != null && !externalStyles.isEmpty()) {
			if(useStylePipeline) {
				AssetPipeline service = controller.getApplication().getPipelineService();
				if(service != null) {
					PipelinedAsset asset = service.getAsset(CSS, getUrls(externalStyles));
					if(asset.ready()) {
						sb.append("<link rel='stylesheet' type='text/css' href='").append(asset.getLocation()).append("' />");
						return;
					} else {
						logger.debug("pipelined asset not ready yet; reverting to separate stylesheets");
						// fall through
					}
				} else {
					logger.warn("asset pipeline service is not available; reverting to separate stylesheets");
					// fall through
				}
			}
			for(String href : externalStyles) {
				sb.append("<link rel='stylesheet' type='text/css' href='").append(href).append("' />");
			}
		}
	}
	
	private void applyNamedContent() {
		if(positions != null && !positions.isEmpty()) {
			if(contentMap != null && !contentMap.isEmpty()) {
				for(int i = positions.size()-1; i >= 0; i--) {
					Position p = positions.get(i);
					String content = contentMap.get(p.name);
					if(content != null && content.length() > 0) {
						if(p.pos < body.length()) {
							body.insert(p.pos, content);
						} else {
							body.append(content);
						}
					}
				}
			}
		}
	}
	
	String getContent(String name) {
		if(contentMap != null) {
			return contentMap.get(name);
		}
		return null;
	}
	
	private List<String> getUrls(LinkedHashSet<String> assets) {
		List<String> urls = new ArrayList<String>(assets);
		for(int i = 0; i < urls.size(); i++) {
			String url = urls.get(i);
			if(url.indexOf("://") == -1) {
				Request r = controller.getRequest();
				StringBuilder sb = new StringBuilder();
				sb.append(r.isSecure() ? "https://" : "http://");
				sb.append(r.getHost()).append(':').append(r.getPort());
				if(url.charAt(0) != '/') sb.append('/');
				sb.append(url);
				url = sb.toString();
			}
			urls.set(i, url);
		}
		return urls;
	}
	
	boolean hasContent(String name) {
		return (contentMap != null && contentMap.containsKey(name));
	}
	
	String putContent(String name, String content) {
		if(contentMap == null) {
			contentMap = new HashMap<String, String>();
		}
		return contentMap.put(name, content);
	}
	
	public String render() {
		body = new StringBuilder();
		head = new StringBuilder();

		if(partial) {
			view.setRenderer(this);
			view.render();
			applyNamedContent();
			return body.toString();
		} else {
			StringBuilder sb;
			View layout = (this.layout != null) ? this.layout : view.getLayout();
			if(layout != null) {
				layout.setRenderer(this);
				layout.setChild(view);
				sb = render(layout);
			} else {
				view.setRenderer(this);
				sb = render(view);
			}
			return sb.toString();
		}
	}
	
	private StringBuilder render(View view) {
		view.render();
		applyNamedContent();
		
		StringBuilder sb = new StringBuilder(head.length() + body.length() + 75);
//		sb.append("<!DOCTYPE html PUBLIC \"-//W3C//DTD XHTML 1.0 Transitional//EN\" ");
//		sb.append("\"http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd\">\n");
//		sb.append("<html xmlns=\"http://www.w3.org/1999/xhtml\" xml:lang=\"en\" lang=\"en\">\n");
		sb.append("<!DOCTYPE html><html><head>");
		if(title != null) {
			sb.append("<title>").append(title).append("</title>");
		}
		if(includeScriptEnvironment) {
			sb.append("<script>window.$oobenv = {};</script>");
		}
		appendExternalStyles(sb);
		appendExternalScripts(sb);
		if(head.length() > 0) {
			sb.append(head);
		}
		sb.append("</head><body>");
		if(body.length() > 0) {
			sb.append(body);
		}
		sb.append("</body></html>");

		return sb;
	}
	
	public void setLayout(View layout) {
		this.layout = layout;
	}

	public void setPartial(boolean partial) {
		this.partial = partial;
	}
	
	void setTitle(String title) {
		if(title != null && title.startsWith("+=")) {
			this.title += title;
		} else {
			this.title = title;
		}
	}
	
}
