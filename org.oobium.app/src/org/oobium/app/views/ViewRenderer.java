package org.oobium.app.views;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.oobium.app.controllers.HttpController;

public class ViewRenderer {

	private class Position {
		String name;
		int pos;
		Position(String name, int pos) { this.name = name; this.pos = pos; }
	}
	
	HttpController controller;
	private View view;
	private View layout;
	private boolean partial;

	StringBuilder body;
	StringBuilder head;
	
	private List<Position> positions;
	private Map<String, String> contentMap;


	public ViewRenderer(HttpController controller, View view) {
		this.controller = controller;
		this.view = view;
	}

	void addPosition(String name) {
		if(positions == null) {
			positions = new ArrayList<Position>();
		}
		positions.add(new Position(name, body.length()));
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
}
