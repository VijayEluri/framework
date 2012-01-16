package org.oobium.eclipse.designer.views;

import org.eclipse.swt.widgets.Shell;
import org.oobium.utils.Config.Mode;

public interface IDataServiceView {

	public abstract void connect(String service, Mode mode);
	
	public abstract void disconnect();

	public abstract Shell getShell();
	
}
