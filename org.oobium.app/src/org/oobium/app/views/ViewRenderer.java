package org.oobium.app.views;

import static org.oobium.app.http.MimeType.CSS;
import static org.oobium.app.http.MimeType.JS;
import static org.oobium.utils.StringUtils.j;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.oobium.app.controllers.HttpController;
import org.oobium.app.request.Request;
import org.oobium.app.routing.Router;
import org.oobium.logging.Logger;
import org.oobium.persist.Model;
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
	private Map<Model, String> includedScriptModels;
	private Map<Class<? extends Model>, Boolean> includedScriptModelClasses;
	private LinkedHashSet<String> externalScripts;
	private List<ScriptFile> externalScriptFiles;
	private LinkedHashSet<String> externalStyles;

	public ViewRenderer(HttpController controller, View view) {
		this.logger = controller.getLogger();
		this.controller = controller;
		this.view = view;
	}
	
	void addExternalScript(ScriptFile script) {
		addExternalScript(Router.getAssetName(script.getClass(), "js"));
		if(script.hasInitializer()) {
			includeScriptEnvironment = true;
			addExternalScriptFile(script);
		}
	}

	void addExternalScript(String src) {
		if(externalScripts == null) {
			externalScripts = new LinkedHashSet<String>();
		}
		externalScripts.add(src);
	}

	private void addExternalScriptFile(ScriptFile asset) {
		if(externalScriptFiles == null) {
			externalScriptFiles = new ArrayList<ScriptFile>();
		}
		externalScriptFiles.add(asset);
	}
	
	void addExternalStyle(Class<? extends StyleSheet> asset) {
		addExternalStyle(Router.getAssetName(asset, "css"));
	}
	
	void addExternalStyle(String href) {
		if(externalStyles == null) {
			externalStyles = new LinkedHashSet<String>();
		}
		externalStyles.add(href);
	}
	
	void addExternalStyle(StyleSheet asset) {
		addExternalStyle(Router.getAssetName(asset.getClass(), "css"));
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
	
	private void appendScriptEnvironment(StringBuilder sb) {
		if(includeScriptEnvironment) {
			sb.append("<script>\n");
			sb.append("window.Oobium = {};\n");
			if(includedScriptModelClasses != null) {
				sb.append("Oobium.routes = ");
				sb.append(j(controller.getRouter().getModelRouteMap(includedScriptModelClasses)));
				sb.append(";\n");
			}
			sb.append("Oobium.vars = {};\n");
			if(includedScriptModels != null) {
				for(Entry<Model, String> entry : includedScriptModels.entrySet()) {
					Model m = entry.getKey();
					sb.append("Oobium.vars.");
					sb.append(entry.getValue());
					sb.append(" = ");
					sb.append("{\"type\": \"" + m.getClass().getName() + "\", \"data\": " + m.toJson() + "}");
					sb.append(";\n");
				}
			}
			if(externalScriptFiles != null) {
				for(ScriptFile script : externalScriptFiles) {
					script.render(this, sb);
					sb.append('\n');
				}
			}
			sb.append("</script>");
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
	
	private List<String> getUrls(Collection<String> assets) {
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
	
	String includeScriptModel(Model model, int position, boolean includeHasMany) {
		includeScriptModels(model.getClass(), includeHasMany);
		addExternalScript("/models-data_binding.js");
		
		if(includedScriptModels == null) {
			includedScriptModels = new HashMap<Model, String>();
		}
		String name = includedScriptModels.get(model);
		if(name == null) {
			name = model.getClass().getSimpleName() + "_" + model.getId() + "$" + position;
			includedScriptModels.put(model, name);
		}
		return name;
	}
	
	void includeScriptModels(Class<? extends Model> modelClass, boolean includeHasMany) {
		includeScriptEnvironment = true;
		addExternalScript("/models.js");
		
		if(includedScriptModelClasses == null) {
			includedScriptModelClasses = new HashMap<Class<? extends Model>, Boolean>();
		}
		includedScriptModelClasses.put(modelClass, includeHasMany);
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
		appendScriptEnvironment(sb);
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
