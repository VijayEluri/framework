package org.oobium.persist.http;

import java.util.List;

public class RemoteException extends Exception {

	private static final long serialVersionUID = 2412935136276094638L;
	
	private List<String> errors;
	
	public RemoteException(String message) {
		super(message);
	}
	
	public RemoteException(List<String> errors) {
		this.errors = errors;
	}
	
	public List<String> getErrors() {
		return errors;
	}
	
	@Override
	public String getMessage() {
		if(errors != null) {
			StringBuilder sb = new StringBuilder();
			for(int i = 0; i < errors.size(); i++) {
				if(i != 0) sb.append('\n');
				sb.append(errors.get(i));
			}
			return sb.toString();
		}
		return super.getMessage();
	}
	
}
