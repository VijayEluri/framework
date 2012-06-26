package org.oobium.build.esp.dom.elements;

import org.oobium.build.esp.dom.EspElement;
import org.oobium.build.esp.dom.EspPart;

public class InnerText extends EspElement {

	private EspPart innerText;
	private boolean literal;
	private boolean promptLine;
	private boolean wordGroup;
	
	public InnerText() {
		super(Type.InnerTextElement);
	}
	
	public boolean isPromptLine() {
		return promptLine;
	}
	
	public boolean isWordGroup() {
		return wordGroup;
	}

	public EspPart getInnerText() {
		return innerText;
	}

	public boolean hasInnerText() {
		return innerText != null;
	}

	public boolean isLiteral() {
		return literal;
	}

	public void setInnerText(EspPart innerText) {
		this.innerText = innerText;
	}
	
	public void setLiteral(boolean literal) {
		this.literal = literal;
	}
	
	public void setPromptLine(boolean promptLine) {
		this.promptLine = promptLine;
	}
	
	public void setWordGroup(boolean wordGroup) {
		this.wordGroup = wordGroup;
	}
	
}
