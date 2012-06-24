package org.oobium.build.esp.parser.internal.parsers;

import static org.oobium.build.esp.parser.internal.parsers.Scanner.EOL;

import org.oobium.build.esp.dom.EspPart;
import org.oobium.build.esp.dom.EspPart.Type;
import org.oobium.build.esp.dom.common.MethodSignature;
import org.oobium.build.esp.dom.elements.Constructor;
import org.oobium.build.esp.dom.elements.ImportElement;
import org.oobium.build.esp.dom.elements.JavaElement;
import org.oobium.build.esp.dom.parts.MethodSigArg;
import org.oobium.build.esp.parser.exceptions.EspEndException;

public class JavaBuilder extends Builder {

	public JavaBuilder(Scanner scanner) {
		super(scanner);
	}

	public Constructor parseConstructorElement() throws EspEndException {
		return push(new Constructor(), new BuildRunner<Constructor>() {
			public void parse(Constructor element) throws EspEndException {
				scanner.setContainmentToEOL();
				EspPart name = scanner.push(Type.JavaKeyword);
				scanner.find('(');
				scanner.pop(name);
				EspPart args = scanner.push(Type.MethodSigArgs);
				scanner.setContainmentToCloser();
				try {
					do { parseMethodSigArg(element); }
					while(scanner.isChar(','));
				} catch(EspEndException e) {
					if(scanner.isChar(')')) {
						scanner.popTo(args);
						scanner.next();
						scanner.pop(args);
						scanner.findEndOfContainment();
					} else {
						throw e;
					}
				}
			}
		});
	}
	
	private void parseMethodSigArg(MethodSignature method) throws EspEndException {
		scanner.next().forward();
		MethodSigArg arg = scanner.push(new MethodSigArg());
		method.addSigArg(arg);
		
		arg.setVarType(scanner.push(Type.JavaContainer));
		scanner.findEndOfJavaType();
		scanner.pop(arg.getVarType());

		scanner.forward();
		if(scanner.check('.','.','.')) {
			arg.setVarArgs(true);
			scanner.move(4);
		}

		scanner.forward();
		arg.setVarName(scanner.push(Type.VarName));
		scanner.findEndOfJavaIdentifier();
		scanner.pop(arg.getVarName());

		scanner.forward();
		if(scanner.isChar('=')) {
			scanner.next().forward();
			arg.setDefaultValue(scanner.push(Type.JavaContainer));
			scanner.findAny(',');
			scanner.pop(arg.getDefaultValue());
		}
	}
	
	public ImportElement parseImportElement() throws EspEndException {
		return push(new ImportElement(), new BuildRunner<ImportElement>() {
			public void parse(ImportElement element) throws EspEndException {
				scanner.setContainmentToEOL();
				EspPart keyword = scanner.push(Type.JavaKeyword);
				scanner.findEndOfWord();
				scanner.pop(keyword);
				scanner.forward();
				if(scanner.check('s', 't', 'a', 't', 'i', 'c', ' ')) {
					element.setStatic(true);
					keyword = scanner.push(Type.JavaKeyword);
					scanner.move(7);
					scanner.pop(keyword);
					scanner.forward();
				}
				element.setImportPart(scanner.push(Type.JavaContainer));
				scanner.findAny(';', EOL);
				scanner.pop(element.getImportPart());
			}
		});
	}

	public JavaElement parseJavaElement() throws EspEndException {
		return push(new JavaElement(), new BuildRunner<JavaElement>() {
			public void parse(JavaElement element) throws EspEndException {
				scanner.setContainmentToEOE();
				scanner.move(1);
				element.setSource(scanner.push(Type.JavaContainer));
				scanner.setContainmentToEOL();
				try {
					scanner.findEndOfContainment();
				} catch(EspEndException e) {
					scanner.pop(element.getSource());
				}
				scanner.parseChildren();
			}
		});
	}
	
}
