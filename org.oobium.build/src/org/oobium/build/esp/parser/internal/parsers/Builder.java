package org.oobium.build.esp.parser.internal.parsers;

import org.oobium.build.esp.dom.EspElement;
import org.oobium.build.esp.dom.EspPart;
import org.oobium.build.esp.dom.EspPart.Type;
import org.oobium.build.esp.dom.common.MethodSignature;
import org.oobium.build.esp.dom.parts.MethodSigArg;
import org.oobium.build.esp.parser.exceptions.EspEndException;
import org.oobium.build.esp.parser.exceptions.EspException;

public class Builder {

	public static interface BuildRunner<T extends EspPart> {
		public abstract void parse(T element) throws EspEndException;
	}

	
	protected final Scanner scanner;
	
	public Builder(Scanner scanner) {
		this.scanner = scanner;
	}
	
	protected void parseMethodSigArgs(MethodSignature method) throws EspEndException {
		if(scanner.isChar('(')) {
			method.initSigArgs();
			EspPart args = scanner.push(Type.MethodSigArgs);
			scanner.setContainmentToCloser();
			try {
				do { parseMethodSigArg(method); }
				while(scanner.isChar(','));
				scanner.pop(args);
			} catch(EspEndException e) {
				if(scanner.isChar(')')) {
					scanner.popTo(args);
					scanner.next();
					scanner.pop(args);
					scanner.findEndOfContainment();
				} else {
					scanner.pop(args);
					throw e;
				}
			}
		}
	}
	
	protected void parseMethodSigArg(MethodSignature method) throws EspEndException {
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
	
	protected <T extends EspElement> T push(T element, BuildRunner<T> runner) throws EspEndException {
		scanner.push(element);
		try {
			runner.parse(element);
			return scanner.pop(element);
		} catch(EspException e) {
			return scanner.pop(element, e.getOffset());
		}
	}
	
}
