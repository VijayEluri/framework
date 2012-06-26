package org.oobium.build.esp.compiler;

import static org.oobium.utils.StringUtils.varName;

import java.util.Iterator;
import java.util.List;

import org.oobium.build.esp.dom.EspPart;
import org.oobium.build.esp.dom.EspPart.Type;
import org.oobium.build.esp.dom.elements.MarkupElement;
import org.oobium.build.esp.dom.elements.ScriptElement;
import org.oobium.build.esp.dom.elements.StyleElement;
import org.oobium.build.esp.dom.parts.MethodArg;


public class AssetCompiler {

	protected final EspCompiler parent;
	
	public AssetCompiler(EspCompiler parent) {
		this.parent = parent;
	}

	protected void buildExternalEjs(ScriptElement element) {
		StringBuilder sb = parent.getBody();
		parent.prepForJava(sb);
		 String varName = buildDynamicAssetVar(sb, element);
		 parent.indent(sb);
		 sb.append("addExternalScript(").append(varName).append(");\n");
		 parent.prepForMarkup(sb);
		String prev = parent.prepFor(element);
		 sb.append("<style>");
		 parent.prepForJava(sb);
		 sb.append(varName).append(".render(" + parent.sbName(sb) + ");\n");
		 parent.prepForMarkup(sb);
		 sb.append("</style>");
		 parent.prepFor(prev);
	}
	
	protected void buildExternalEss(StyleElement element) {
		StringBuilder sb = parent.getBody();
		parent.prepForJava(sb);
		sb.append("addExternalStyle(");
		parent.build(element.getJavaType(), sb);
		sb.append(".class);\n");
	}
	
	private String buildDynamicAssetVar(StringBuilder sb, MarkupElement element) {
		EspPart type = element.getJavaType();
		String typeName = parent.getJavaTypeName(type);
		String varName = varName(typeName) + "$" + element.getStart();
		parent.build(type, sb);
		sb.append(' ').append(varName).append(" = new ").append(typeName);
		if(element.hasArgs()) {
			sb.append("(");
			for(Iterator<MethodArg> iter = element.getArgs().iterator(); iter.hasNext(); ) {
				parent.build(iter.next(), sb);
				if(iter.hasNext()) sb.append(',').append(' ');
			}
			sb.append(");\n");
		} else {
			sb.append("();\n");
		}
		return varName;
	}
	
	protected void buildInlineEjs(MethodArg type, ScriptElement element) {
		StringBuilder sb = parent.getBody();
		parent.prepForMarkup(sb);
		String prev = parent.prepFor(element);
		sb.append("<script>");
		parent.prepForJava(sb);
		buildDynamicAssetVar(sb, element);
		parent.prepForMarkup(sb);
		sb.append("</script>");
		parent.prepFor(prev);
	}
	
	protected void buildInlineEss(StyleElement element) {
		StringBuilder sb = parent.getBody();
		parent.prepForMarkup(sb);
		String prev = parent.prepFor(element);
		sb.append("<style>");
		parent.prepForJava(sb);
		String varName = buildDynamicAssetVar(sb, element);
		parent.indent(sb);
		sb.append(varName).append(".render(" + parent.sbName(sb) + ");\n");
		parent.prepForMarkup(sb);
		sb.append("</style>");
		parent.prepFor(prev);
	}
	
}
