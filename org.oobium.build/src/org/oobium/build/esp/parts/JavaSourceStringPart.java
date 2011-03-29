package org.oobium.build.esp.parts;

import static org.oobium.utils.CharStreamUtils.*;

import org.oobium.build.esp.EspPart;

public class JavaSourceStringPart extends EspPart {

	private EspPart source;
	
	public JavaSourceStringPart(EspPart parent, int start, int end) {
		super(parent, start);
		type = Type.JavaPart;
		this.end = end;
		parse();
	}
	
	public String getSource() {
		return (source != null) ? source.getText() : "";
	}
	
	public EspPart getSourcePart() {
		return source;
	}

	private void parse() {
		int s1 = start;
		if(s1 != end - 1) {
			s1++;
			if(ca[s1] == '{') { // expression
				int s2 = closer(ca, s1, end) + 1;
				if(s2 == 0) {
					s2 = end;
				}
				end = s2;
				s1 = forward(ca, s1+1);
				s2 = reverse(ca, s2-1);
				source = new EspPart(this, Type.JavaPart, s1, s2);
			} else if(Character.isJavaIdentifierStart(ca[s1])) { // variable
				int s2 = s1+1;
				while(s2 < end) {
					if(!Character.isJavaIdentifierPart(ca[s2])) {
						break;
					}
					s2++;
				}
				source = new EspPart(this, Type.JavaPart, s1, s2);
				end = s2;
			} else {
				end = s1;
			}
		}
	}

}
