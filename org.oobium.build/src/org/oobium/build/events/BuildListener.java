package org.oobium.build.events;

public interface BuildListener {

	public abstract void handleEvent(BuildEvent event);
	
}
