package org.oobium.build.esp.compiler;

import static org.oobium.utils.StringUtils.varName;

import java.util.Iterator;

import org.oobium.build.esp.dom.EspPart;
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
		parent.indent(sb);
		sb.append("addExternalScript(");
		buildNewDynamicAsset(element, sb);
		sb.append(");\n");
	}
	
	protected void buildExternalEss(StyleElement element) {
		StringBuilder sb = parent.getBody();
		parent.prepForJava(sb);
		sb.append("addExternalStyle(");
		parent.build(element.getJavaType(), sb);
		sb.append(".class);\n");
	}
	
	private String buildDynamicAssetVar(MarkupElement element, StringBuilder sb) {
		EspPart type = element.getJavaType();
		String typeName = parent.getJavaTypeName(type);
		String varName = varName(typeName) + "$" + element.getStart();
		sb.append(typeName).append(' ').append(varName).append(" = ");
		buildNewDynamicAsset(element, sb);
		sb.append(";\n");
		return varName;
	}
	
	private void buildNewDynamicAsset(MarkupElement element, StringBuilder sb) {
		EspPart type = element.getJavaType();
		sb.append("new ");
		parent.build(type, sb);
		if(element.hasArgs()) {
			sb.append("(");
			for(Iterator<MethodArg> iter = element.getArgs().iterator(); iter.hasNext(); ) {
				parent.build(iter.next(), sb);
				if(iter.hasNext()) sb.append(',').append(' ');
			}
			sb.append(")");
		} else {
			sb.append("()");
		}
	}
	
	protected void buildInlineEjs(MethodArg type, ScriptElement element) {
		StringBuilder sb = parent.getBody();
		parent.prepForMarkup(sb);
		String prev = parent.prepFor(element);
		sb.append("<script>");
		parent.prepForJava(sb);
		buildDynamicAssetVar(element, sb);
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
		String varName = buildDynamicAssetVar(element, sb);
		parent.indent(sb);
		sb.append(varName).append(".render(" + parent.sbName(sb) + ");\n");
		parent.prepForMarkup(sb);
		sb.append("</style>");
		parent.prepFor(prev);
	}
	
}
