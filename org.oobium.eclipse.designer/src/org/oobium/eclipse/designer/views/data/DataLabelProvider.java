package org.oobium.eclipse.designer.views.data;

import java.text.DateFormat;
import java.util.Date;

import org.eclipse.jface.viewers.ILabelProviderListener;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.swt.graphics.Image;
import org.oobium.persist.Model;
import org.oobium.persist.http.RemoteRunnable;
import org.oobium.persist.http.Remote;

public class DataLabelProvider implements ITableLabelProvider {

	private static final DateFormat df = DateFormat.getDateTimeInstance();
	

	private TableViewer viewer;
	
	public DataLabelProvider(TableViewer viewer) {
		this.viewer = viewer;
	}
	
	@Override
	public void addListener(ILabelProviderListener listener) {
		// TODO Auto-generated method stub

	}

	@Override
	public void dispose() {
		// TODO Auto-generated method stub

	}

	@Override
	public boolean isLabelProperty(Object element, String property) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void removeListener(ILabelProviderListener listener) {
		// TODO Auto-generated method stub

	}

	@Override
	public Image getColumnImage(Object element, int columnIndex) {
		// TODO Auto-generated method stub
		return null;
	}

	private String getColumnProperty(int columnIndex) {
		return (String) viewer.getColumnProperties()[columnIndex];
	}
	
	@Override
	public String getColumnText(final Object element, final int columnIndex) {
		if(element instanceof Model) {
			if(columnIndex == 0) {
				return ((Model) element).getId(String.class);
			}
			Object o = Remote.syncExec(new RemoteRunnable<Object>() {
				protected Object run() throws Exception {
					return ((Model) element).get(getColumnProperty(columnIndex));
				};
			});
			if(o instanceof Date) {
				return df.format((Date) o);
			}
			return String.valueOf(o);
		}
		return String.valueOf(element);
	}

}
