package org.oobium.build.esp.compiler;

import static org.oobium.build.esp.dom.EspDom.DocType.EJS;

import java.util.List;

import org.oobium.build.esp.dom.EspPart;
import org.oobium.build.esp.dom.common.AssetPart;
import org.oobium.build.esp.dom.elements.ScriptElement;
import org.oobium.build.esp.dom.parts.ScriptPart;

public class ScriptCompiler extends AssetCompiler {

	private ScriptElement element;
	
	public ScriptCompiler(EspCompiler parent) {
		super(parent);
	}

	public boolean inScript() {
		return element != null;
	}
	
	public void compile(ScriptElement element) {
		this.element = element;
		build();
		this.element = null;
	}
	
	private void build() {
		StringBuilder sb = parent.getBody();
		if(parent.getDom().is(EJS)) {
			parent.inAsset(true);
			parent.prepForMarkup(sb);
			parent.build(element.getAsset().getPart(), sb);
			parent.inAsset(false);
			return;
		}
		
		if(element.hasJavaType()) {
			buildExternalEjs(element);
		} else {
			if(element.hasArgs()) {
				// these are links to plain JavaScript files, either local (in assets) or remote
				//   they cannot contain Java parts like a script element or EJS file can
				if(parent.inHead(element)) {
					parent.prepForJava(sb);
					for(EspPart arg : element.getArgs()) {
						parent.indent(sb);
						sb.append("addExternalScript(");
						parent.build(arg, sb);
						sb.append(");\n");
					}
				} else {
					parent.prepForMarkup(sb);
					parent.prepForBody();
					for(EspPart arg : element.getArgs()) {
						sb.append("<script src=\\\"");
						parent.build(arg, sb);
						sb.append("\\\"></script>");
					}
				}
			}
			if(element.hasAsset()) {
				// contained Java parts must be dealt with
				ScriptPart script = element.getAsset();
				List<EspPart> containers = script.getJavaContainers();
				if(containers.size() > 0) {
					parent.prepForJava(sb);
					sb.append("includeScriptEnvironment();\n");
				}
				parent.prepForMarkup(sb);
				String oldSection = parent.prepFor(element);
				sb.append("<script>");
				if(containers.size() > 0) {
					for(EspPart container : containers) {
						sb.append(parent.getCodeVar(container)).append(" = ");
						parent.build(container, sb);
						sb.append(";");
					}
				}
				build(script, sb);
				sb.append("</script>");
				parent.prepFor(oldSection);
			}
		}
	}
	
	void build(ScriptPart part, StringBuilder sb) {
		if(part == null) {
			return; // occurs when part is supposed to be the value of an entry part, but it has not been created yet: "key:"
		}

		String text = part.getText();
		List<EspPart> containers = part.getJavaContainers();
		
		if(containers.isEmpty()) {
			parent.appendEscaped(sb, text);
		} else {
			for(int i = 0; i < containers.size(); i++) {
				EspPart sub = containers.get(i);
				int s1 = sub.getStart() - part.getStart();
				int s2 = s1 + sub.length();
				if(i == 0) { // if first
					if(s1 > 0) parent.appendEscaped(sb, text, 0, s1);
				} else {
					int s0 = containers.get(i-1).getEnd() - part.getStart();
					if(s0 < s1) parent.appendEscaped(sb, text, s0, s1);
				}
				sb.append(parent.getCodeVar(sub));
				if(i == containers.size() - 1) { // if last
					if(s2 < text.length()) {
						parent.appendEscaped(sb, text, s2, text.length());
					}
				}
			}
		}
	}

	public boolean buildInitializer(ScriptElement element) {
		if(element.hasAsset()) {
			AssetPart code = element.getAsset();
			List<EspPart> containers = code.getJavaContainers();
			if(containers.size() > 0) {
				this.element = element;
				StringBuilder body = parent.getBody();
				parent.prepForMarkup(body);
				for(EspPart container : containers) {
					body.append(parent.getCodeVar(container)).append(" = ");
					parent.build(container, body);
					body.append(';');
				}
				this.element = null;
				return true;
			}
		}
		return false;
	}
	
	public void buildModels() {
		StringBuilder body = parent.getBody();
		parent.prepForJava(body);
		body.append("includeScriptModels();\n");
	}

}
