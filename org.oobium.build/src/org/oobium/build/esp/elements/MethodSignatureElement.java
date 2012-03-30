package org.oobium.build.esp.elements;

import static org.oobium.utils.CharStreamUtils.closer;
import static org.oobium.utils.CharStreamUtils.forward;
import static org.oobium.utils.CharStreamUtils.reverse;

import java.util.ArrayList;
import java.util.List;

import org.oobium.build.esp.EspElement;
import org.oobium.build.esp.EspPart;
import org.oobium.build.esp.parts.MethodSignatureArg;

public abstract class MethodSignatureElement extends EspElement {
	
	private List<MethodSignatureArg> sigArgs;

	public MethodSignatureElement(EspPart parent, int start) {
		super(parent, start);
	}

	public abstract String getReturnType();
	
	public abstract String getMethodName();
	
	public abstract boolean isStatic();
	
	protected void addSigArg(int start, int end) {
		if(sigArgs == null) {
			sigArgs = new ArrayList<MethodSignatureArg>();
		}
		sigArgs.add(new MethodSignatureArg(this, start, end));
	}

	public List<MethodSignatureArg> getSignatureArgs() {
		return sigArgs;
	}
	
	public boolean hasSignatureArgs() {
		return sigArgs != null && !sigArgs.isEmpty();
	}
	
	protected int parseSignatureArgs(char[] ca, int start, int end) {
		int eoa = closer(ca, start, end);
		if(eoa == -1) {
			eoa = end;
		}
		
		int s1 = forward(ca, start+1, eoa-1);
		if(s1 != -1) {
			int s = s1;
			if(s != -1) {
				while(s < eoa) {
					switch(ca[s]) {
					case '<':
					case '"':
						s = closer(ca, s, eoa) + 1;
						if(s == 0) {
							s = eoa;
						}
						break;
					case ',':
						int s2 = reverse(ca, s-1) + 1;
						addSigArg(s1, s2);
						s = forward(ca, s+1, eoa);
						if(s == -1) {
							s = eoa;
						}
						s1 = s;
						break;
					default:
						s++;
					}
				}
				if(s > s1) {
					int s2 = reverse(ca, s);
					if(s2 == -1) {
						s2 = eoa;
					}
					addSigArg(s1, s2);
				}
			}
		}
		return eoa + 1;
	}

}
