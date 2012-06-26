package org.oobium.build.esp.parser;

import static org.oobium.utils.StringUtils.when;

import org.oobium.build.esp.dom.EspDom;
import org.oobium.build.esp.dom.EspDom.DocType;
import org.oobium.build.esp.parser.internal.parsers.Scanner;
import org.oobium.logging.LogProvider;
import org.oobium.logging.Logger;

public class EspBuilder {

	public static EspBuilder newEspBuilder(String name) {
		return newEspBuilder(name, LogProvider.getLogger(EspBuilder.class));
	}
	
	public static EspBuilder newEspBuilder(String name, Logger logger) {
		EspBuilder builder = new EspBuilder();
		builder.setName(name);
		builder.setLogger(logger);
		return builder;
	}

	
	private DocType type;
	private char[] name;
	private Logger logger;

	private EspBuilder() {
		// private constructor
	}
	
	public EspDom parse(CharSequence src) {
		Scanner scanner = new Scanner(type, name, src, logger);
		return scanner.parseDom();
	}
	
	public void setLogger(Logger logger) {
		this.logger = logger;
	}
	
	public void setName(String name) {
		if(name == null) {
			this.name = new char[0];
			this.type = DocType.ESP;
		} else {
			int ix = name.lastIndexOf('.');
			if(ix == -1) {
				this.name = name.toCharArray();
				this.type = DocType.ESP;
			} else {
				this.name = new char[ix];
				name.getChars(0, ix, this.name, 0);
				String type = name.substring(ix+1);
				switch(when(type.toLowerCase(), "esp", "emt", "ess", "ejs", "css", "js", "json")) {
				case 0: this.type = DocType.ESP; break;
				case 1: this.type = DocType.EMT; break;
				case 2: this.type = DocType.ESS; break;
				case 3: this.type = DocType.EJS; break;
				case 4: this.type = DocType.CSS; break;
				case 5: this.type = DocType.JS; break;
				case 6: this.type = DocType.JS; break;
				}
			}
		}
	}
	
}
