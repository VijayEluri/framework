package org.oobium.eclipse.designer.views.monitor;

import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IWorkbenchActionConstants;
import org.eclipse.ui.part.ViewPart;
import org.oobium.console.ConsolePage;
import org.oobium.eclipse.designer.manager.DataService;
import org.oobium.eclipse.designer.manager.DataServiceEvent;
import org.oobium.eclipse.designer.manager.DataServiceEvent.Type;
import org.oobium.eclipse.designer.manager.DataServiceListener;
import org.oobium.eclipse.designer.manager.DataServiceManager;
import org.oobium.eclipse.designer.manager.ModelEvent;
import org.oobium.eclipse.designer.manager.ModelListener;
import org.oobium.eclipse.designer.views.IDataServiceView;
import org.oobium.eclipse.designer.views.actions.ConnectAction;
import org.oobium.utils.Config.Mode;

public class MonitorView extends ViewPart implements DataServiceListener, IDataServiceView, ModelListener {

	private ConnectAction connectAction;
	private ConsolePage consolePage;
	
	private DataService service;

	public void connect(String service, Mode mode) {
		disconnect();
		this.service = DataServiceManager.instance().getService(service, mode);
		this.service.connect(this);
		this.service.addServiceListener(this);
		this.service.addModelListener(this);
	}
	
	public void disconnect() {
		if(service != null) {
			service.removeModelListener(this);
			service.removeServiceListener(this);
			service.disconnect(this);
			service = null;
		}
	}
	
	private void createActions() {
		connectAction = new ConnectAction(this, false);
	}

	@Override
	public void createPartControl(Composite parent) {
		parent.setLayout(new FillLayout());
		
		consolePage = new ConsolePage(parent, SWT.READ_ONLY);

		createActions();
		// createMenu();
		createToolBar();
	}

	private void createToolBar() {
		IToolBarManager manager = getViewSite().getActionBars().getToolBarManager();

		manager.add(connectAction);
		
		manager.add(new Separator(IWorkbenchActionConstants.MB_ADDITIONS));
	}

	@Override
	public Shell getShell() {
		return getSite().getShell();
	}
	
	@Override
	public void handleDataServiceEvent(DataServiceEvent event) {
		if(event.type == Type.Connected) {
			Display.getDefault().syncExec(new Runnable() {
				@Override
				public void run() {
					consolePage.getConsole().clear();
				}
			});
		}
	}
	
	@Override
	public void handleModelEvent(ModelEvent event) {
		consolePage.getConsole().out.println(event.getMessage());
	}
	
	@Override
	public void setFocus() {
		consolePage.getConsole().setFocus();
	}

}
