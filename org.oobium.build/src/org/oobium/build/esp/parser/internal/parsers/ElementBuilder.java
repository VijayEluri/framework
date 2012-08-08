package org.oobium.build.esp.parser.internal.parsers;

import org.oobium.build.esp.dom.EspPart.Type;
import org.oobium.build.esp.dom.elements.InnerText;
import org.oobium.build.esp.parser.exceptions.EspEndException;


public class ElementBuilder extends Builder {

	public ElementBuilder(Scanner scanner) {
		super(scanner);
	}
	
	public void parseChildren() throws EspEndException {
		scanner.findChild(scanner.getLevel());
		while(scanner.hasNext()) {
			if(scanner.isChar('-')) {
				scanner.parseJavaElement();
			} else if(scanner.isCharSequence('s', 'c', 'r', 'i', 'p', 't')) {
				scanner.parseScriptElement();
			} else if(scanner.isCharSequence('s', 't', 'y', 'l', 'e')) {
				scanner.parseStyleElement();
			} else if(scanner.isCharSequence('!', '-', '-')) {
				scanner.parseMarkupComment();
			} else if(scanner.isChar('+')) {
				parseInnerTextElement();
			} else if(scanner.isWhitespace()) {
				scanner.forward();
			} else {
				scanner.parseMarkupElement();
			}
		}
	}

	public void parseInnerTextElement() throws EspEndException {
		push(new InnerText(), new BuildRunner<InnerText>() {
			public void parse(InnerText element) throws EspEndException {
				scanner.setContainmentToEOL();
				try {
					switch(scanner.next().getChar()) {
					case ' ':
						scanner.next();
						break;
					case '=':
						if( ! scanner.next().isChar(' ')) scanner.findEndOfContainment();
						element.setLiteral(true);
						scanner.next();
						break;
					case '>':
						if( ! scanner.next().isChar(' ')) scanner.findEndOfContainment();
						element.setLiteral(true);
						element.setPromptLine(true);
						scanner.next();
						break;
					case 'w':
						if( ! scanner.next().isChar(' ')) scanner.findEndOfContainment();
						element.setLiteral(true);
						element.setWordGroup(true);
						scanner.next();
						break;
					default:
						scanner.findEndOfContainment();
						return;
					}
					element.setInnerText(scanner.push(Type.InnerTextPart));
					scanner.findEndOfContainment();
				} catch(EspEndException e) {
					scanner.handleContainmentEnd();
					scanner.popTo(element);
				}
				scanner.setContainmentToEOE();
				scanner.findEndOfContainment();
			}
		});

	}
	
}
