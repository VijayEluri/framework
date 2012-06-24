package org.oobium.build.esp.parser.internal.parsers;

import org.oobium.build.esp.dom.EspElement;
import org.oobium.build.esp.dom.EspPart;
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
