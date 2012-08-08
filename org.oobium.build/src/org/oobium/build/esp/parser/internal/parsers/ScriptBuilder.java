package org.oobium.build.esp.parser.internal.parsers;

import org.oobium.build.esp.dom.EspDom.DocType;
import org.oobium.build.esp.dom.elements.ScriptElement;
import org.oobium.build.esp.dom.parts.ScriptPart;
import org.oobium.build.esp.parser.exceptions.EspEndException;


public class ScriptBuilder extends MarkupBuilder {

	public ScriptBuilder(Scanner scanner) {
		super(scanner);
	}

	public void parseScriptElement() throws EspEndException {
		push(new ScriptElement(), new BuildRunner<ScriptElement>() {
			public void parse(ScriptElement element) throws EspEndException {
				if(element.getDom().is(DocType.ESP)) {
					scanner.setContainmentToEOL();
					try {
						parseTag(element);
						parseJavaType(element);
						parseArgsAndEntries(element);
					} catch(EspEndException e) {
						scanner.handleContainmentEnd();
						scanner.popTo(element);
					}
					scanner.setContainmentToEOE();
					scanner.forward();
					element.setAsset(scanner.push(new ScriptPart()));
					scanner.findEndOfContainment();
				}
				else {
					scanner.setContainmentToEOE();
					scanner.forward();
					element.setAsset(scanner.push(new ScriptPart()));
					scanner.check();
					while(scanner.hasNext()) {
						try {
							scanner.next();
						} catch(EspEndException e) {
							scanner.handleContainmentEnd();
							// keep going!
						}
					}
				}
			}
		});
	}
	
}
