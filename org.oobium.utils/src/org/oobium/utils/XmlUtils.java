package org.oobium.utils;

import java.io.StringWriter;
import java.util.Map;
import java.util.Map.Entry;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.oobium.utils.json.JsonUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

class XmlUtils {

	private static void appendChildren(Element parent, Map<?, ?> map) throws Exception {
		Document doc = parent.getOwnerDocument();
		for(Entry<?, ?> entry : map.entrySet()) {
			Element child = doc.createElement(String.valueOf(entry.getKey()));
			Object o = entry.getValue();
			if(o instanceof Map) {
				appendChildren(child, (Map<?,?>) o);
			} else if(o instanceof Iterable) {
				throw new UnsupportedOperationException("not yet implemented");
			} else {
				child.setTextContent(JsonUtils.toJson(o));
			}
			parent.appendChild(doc.createTextNode("\t"));
			parent.appendChild(child);
		}
	}
	
	static String toXml(Class<?> clazz, Map<?, ?> map) throws Exception {
		DocumentBuilderFactory docBuilderFactory = DocumentBuilderFactory.newInstance();
		DocumentBuilder docBuilder = docBuilderFactory.newDocumentBuilder();
		Document doc = docBuilder.newDocument();
		Element root = doc.createElement(clazz.getSimpleName());
		doc.appendChild(root);

		appendChildren(root, map);

		TransformerFactory transfac = TransformerFactory.newInstance();
		Transformer trans = transfac.newTransformer();
		trans.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");
		trans.setOutputProperty(OutputKeys.INDENT, "yes");

		StringWriter sw = new StringWriter();
		StreamResult result = new StreamResult(sw);
		DOMSource source = new DOMSource(doc);
		trans.transform(source, result);
		String xml = sw.toString();

		return xml;
	}
	
}
