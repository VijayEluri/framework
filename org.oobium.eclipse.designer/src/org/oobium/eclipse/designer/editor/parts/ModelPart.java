package org.oobium.eclipse.designer.editor.parts;

import static org.oobium.eclipse.designer.editor.models.ModelElement.*;
import static org.oobium.eclipse.designer.editor.models.ModelElement.PROP_CONN_SOURCE;
import static org.oobium.eclipse.designer.editor.models.ModelElement.PROP_CONN_TARGET;
import static org.oobium.eclipse.designer.editor.models.ModelElement.PROP_LOCATION;
import static org.oobium.eclipse.designer.editor.models.ModelElement.PROP_SIZE;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.draw2d.ChopboxAnchor;
import org.eclipse.draw2d.ConnectionAnchor;
import org.eclipse.draw2d.IFigure;
import org.eclipse.draw2d.geometry.Point;
import org.eclipse.draw2d.geometry.Rectangle;
import org.eclipse.gef.ConnectionEditPart;
import org.eclipse.gef.EditPart;
import org.eclipse.gef.EditPolicy;
import org.eclipse.gef.GraphicalEditPart;
import org.eclipse.gef.NodeEditPart;
import org.eclipse.gef.Request;
import org.eclipse.gef.RequestConstants;
import org.eclipse.gef.commands.Command;
import org.eclipse.gef.editparts.AbstractGraphicalEditPart;
import org.eclipse.gef.editpolicies.ComponentEditPolicy;
import org.eclipse.gef.editpolicies.GraphicalNodeEditPolicy;
import org.eclipse.gef.requests.CreateConnectionRequest;
import org.eclipse.gef.requests.GroupRequest;
import org.eclipse.gef.requests.ReconnectRequest;
import org.eclipse.gef.requests.SelectionRequest;
import org.eclipse.jface.dialogs.Dialog;
import org.oobium.build.console.Eclipse;
import org.oobium.eclipse.designer.editor.dialogs.model.ModelDialog;
import org.oobium.eclipse.designer.editor.figures.ModelFigure;
import org.oobium.eclipse.designer.editor.models.ApplicationElement;
import org.oobium.eclipse.designer.editor.models.Connection;
import org.oobium.eclipse.designer.editor.models.ModelElement;
import org.oobium.eclipse.designer.editor.models.SiteElement;
import org.oobium.eclipse.designer.editor.models.commands.ConnectionCreateCommand;
import org.oobium.eclipse.designer.editor.models.commands.ModelDeleteCommand;
import org.oobium.eclipse.designer.editor.models.commands.ModelEditCommand;

public class ModelPart extends AbstractGraphicalEditPart implements PropertyChangeListener, NodeEditPart {

	@Override
	public void activate() {
		if(!isActive()) {
			super.activate();
			getModel().addListener(this);
		}
	}

	@Override
	protected void createEditPolicies() {
		installEditPolicy(EditPolicy.COMPONENT_ROLE, new ComponentEditPolicy() {
			@Override
			public Command getCommand(Request request) {
				return super.getCommand(request);
			}
			@Override
			protected Command getDeleteCommand(GroupRequest request) {
				ModelElement model = (ModelElement) getHost().getModel();
				return new ModelDeleteCommand(model);
			}
		});
		installEditPolicy(EditPolicy.GRAPHICAL_NODE_ROLE, new GraphicalNodeEditPolicy() {
			private Command createReconnectCommand(ReconnectRequest request, boolean source) {
				EditPart part = request.getTarget();
				if(part instanceof ModelPart) {
//					Connection connection = (Connection) request.getConnectionEditPart().getModel();
//					ModelElement model = (ModelElement) part.getModel();
					Point location = request.getLocation().getCopy();
					getHostFigure().translateToRelative(location);
//					String field = ((ModelPart) part).getFigure().getRelation(location);
//					ConnectionReconnectCommand cmd = new ConnectionReconnectCommand(connection);
//					if(source) {
//						cmd.setSource(model, field);
//					} else {
//						cmd.setTarget(model, field);
//					}
//					return cmd;
				}
				return null;
			}

			@Override
			protected Command getConnectionCompleteCommand(CreateConnectionRequest request) {
				EditPart source = request.getSourceEditPart();
				EditPart target = request.getTargetEditPart();
				if(source instanceof ModelPart && target instanceof ModelPart) {
					ModelElement sourceModel = ((ModelPart) source).getModel();
					ModelElement targetModel = ((ModelPart) target).getModel();
					return new ConnectionCreateCommand(sourceModel, targetModel);
				}
				return null;
			}
			
			@Override
			protected Command getConnectionCreateCommand(CreateConnectionRequest request) {
				EditPart part = request.getTargetEditPart();
				if(part instanceof ModelPart) {
					ModelElement model = ((ModelPart) part).getModel();
					return new ConnectionCreateCommand(model);
				}
				return null;
			}
			
			@Override
			protected Command getReconnectSourceCommand(ReconnectRequest request) {
				return createReconnectCommand(request, true);
			}
			
			@Override
			protected Command getReconnectTargetCommand(ReconnectRequest request) {
				return createReconnectCommand(request, false);
			}
		});
	}
	
	@Override
	protected IFigure createFigure() {
		ModelElement model = getModel();
		ModelFigure figure = new ModelFigure();
		figure.setText(model.getName());
		figure.setColor(model.getColor());
		return figure;
	}
	
	@Override
	public void deactivate() {
		if(isActive()) {
			super.deactivate();
			getModel().removeListener(this);
		}
	}
	
	@Override
	public ModelFigure getFigure() {
		return (ModelFigure) super.getFigure();
	}

	@Override
	public ModelElement getModel() {
		return (ModelElement) super.getModel();
	}

	@Override
	protected List<Connection> getModelSourceConnections() {
		return getModel().getSourceConnections();
	}
	
	@Override
	protected List<Connection> getModelTargetConnections() {
		return getModel().getTargetConnections();
	}

	@Override
	public ConnectionAnchor getSourceConnectionAnchor(ConnectionEditPart conn) {
		if(conn instanceof ConnectionPart) {
			Connection connection = ((ConnectionPart) conn).getModel();
			if(connection.getSourceModel() == connection.getTargetModel()) {
				return new ChopboxAnchor(getFigure()) {
					@Override
					protected Rectangle getBox() {
						Point p = getOwner().getBounds().getRight();
						return new Rectangle(p.x, p.y+15, 1, 1);
					}
				};
			}
		}
		return new ChopboxAnchor(getFigure());
	}
	
	@Override
	public ConnectionAnchor getSourceConnectionAnchor(Request request) {
		return new ChopboxAnchor(getFigure());
	}

	@Override
	public ConnectionAnchor getTargetConnectionAnchor(ConnectionEditPart conn) {
		if(conn instanceof ConnectionPart) {
			Connection connection = ((ConnectionPart) conn).getModel();
			if(connection.getSourceModel() == connection.getTargetModel()) {
				return new ChopboxAnchor(getFigure()) {
					@Override
					protected Rectangle getBox() {
						Point p = getOwner().getBounds().getRight();
						return new Rectangle(p.x, p.y-15, 1, 1);
					}
				};
			}
		}
		return new ChopboxAnchor(getFigure());
	}
	
	@Override
	public ConnectionAnchor getTargetConnectionAnchor(Request request) {
		return new ChopboxAnchor(getFigure());
	}

	@Override
	public void performRequest(Request req) {
		if(req.getType().equals(RequestConstants.REQ_OPEN)) {
			SelectionRequest request = (SelectionRequest) req;
			ModelElement model = getModel();

			if(request.isControlKeyPressed()) {
				File project = model.getModuleElement().getModule().file;
				File file = model.getFile();
				Eclipse.openFile(project, file);
			} else {
				List<String> modelNames = new ArrayList<String>();
				for(Object o : getRoot().getChildren()) {
					if(o instanceof SitePart) {
						SiteElement site = ((SitePart) o).getModel();
						for(ApplicationElement app : site.getApplications()) {
							for(ModelElement m : app.getModels()) {
								modelNames.add(m.getName());
							}
						}
					}
				}
				
				ModelDialog dlg = new ModelDialog(model, modelNames);
				if(dlg.open() == Dialog.OK) {
					ModelEditCommand cmd = new ModelEditCommand(model);
					cmd.setColor(dlg.getColor());
					cmd.setDefinition(dlg.getDefinition());
					if(cmd.canExecute()) {
						getViewer().getEditDomain().getCommandStack().execute(cmd);
					}
				}
			}
		} else {
			super.performRequest(req);
		}
	}
	
	@Override
	public void propertyChange(PropertyChangeEvent evt) {
		String prop = evt.getPropertyName();
		if(PROP_SIZE.equals(prop) || PROP_LOCATION.equals(prop) || PROP_BOUNDS.equals(prop)) {
			refreshVisuals();
		}
		if(PROP_COLOR.equals(prop)) {
			refreshVisuals();
		}
		if(PROP_CONN_SOURCE.equals(prop)) {
			refreshSourceConnections();
		}
		if(PROP_CONN_TARGET.equals(prop)) {
			refreshTargetConnections();
		}
//		if(PROP_FIELD.equals(prop)) {
//			String field = (String) evt.getNewValue();
//			if(field != null) {
//				ModelRelation rel = getModel().getDefinition().getRelation(field);
//				if(rel != null) {
//					String oldName = (String) evt.getOldValue();
//					if(oldName != null) {
//						getFigure().updateRelation(oldName, rel.name, rel.getSimpleType(), rel.hasMany);
//					} else {
//						getFigure().updateRelation(field, rel.name, rel.getSimpleType(), rel.hasMany);
//					}
//				}
//			}
//		}
//		if(PROP_FIELD_ADDED.equals(prop)) {
//			String field = (String) evt.getNewValue();
//			if(field != null) {
//				ModelRelation rel = getModel().getDefinition().getRelation(field);
//				if(rel != null) {
//					getFigure().addRelation(rel.name, rel.getSimpleType(), rel.hasMany);
//				}
//			}
//		}
//		if(PROP_FIELD_REMOVED.equals(prop)) {
//			String field = (String) evt.getNewValue();
//			if(field != null) {
//				getFigure().remove(field);
//			}
//		}
	}

	@Override
	protected void refreshVisuals() {
		ModelElement model = getModel();
		ModelFigure figure = getFigure();
		
		figure.setText(model.getName());
		figure.setColor(model.getColor());
		
		((GraphicalEditPart) getParent()).setLayoutConstraint(this, figure, model.getBounds());
	}
	
}
