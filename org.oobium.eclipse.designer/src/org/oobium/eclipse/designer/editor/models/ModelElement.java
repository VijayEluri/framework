package org.oobium.eclipse.designer.editor.models;

import java.io.File;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.draw2d.geometry.Dimension;
import org.eclipse.draw2d.geometry.Point;
import org.eclipse.draw2d.geometry.Rectangle;
import org.eclipse.ui.views.properties.IPropertyDescriptor;
import org.oobium.build.model.ModelAttribute;
import org.oobium.build.model.ModelDefinition;
import org.oobium.build.model.ModelRelation;

public class ModelElement extends Element {

	public static final String PROP_STATE = "Model.State";
	public static final String PROP_LOCATION = "Model.Location";
	public static final String PROP_SIZE = "Model.Size";
	public static final String PROP_BOUNDS = "Model.Bounds";
	public static final String PROP_CONN_SOURCE = "Model.Connection.Source";
	public static final String PROP_CONN_TARGET = "Model.Connection.Target";
	public static final String PROP_FIELD = "Model.Field";
	public static final String PROP_FIELD_ADDED = "Model.Field.Added";
	public static final String PROP_FIELD_REMOVED = "Model.Field.Removed";
	
	public static final int DELETED = -1;
	public static final int CREATED = 1;
	
	private final ModuleElement module;
	
	private int state;
	
	private Point location;
	private Dimension size;
	
	private ModelDefinition definition;
	
	private final List<Connection> sourceConnections;
	private final List<Connection> targetConnections;
	
	public ModelElement(ModuleElement module, ModelDefinition definition) {
		this(module, definition, 0);
	}
	
	public ModelElement(ModuleElement module, ModelDefinition definition, int state) {
		this.module = module;
		this.definition = definition;
		this.state = state;
		
		location = new Point(10, 10);
		size = new Dimension(100, 100);
		
		sourceConnections = new ArrayList<Connection>();
		targetConnections = new ArrayList<Connection>();
	}

	public Map<String, Object> commit() {
		definition.save();
		Map<String, Object> data = new LinkedHashMap<String, Object>();
		data.put("bounds", getBounds());
		return data;
	}
	
	public boolean isDeleted() {
		return state < 0;
	}
	
	public boolean isCreated() {
		return state > 0;
	}
	
	public void setTimestamps(boolean timestamps) {
		if(definition.timestamps != timestamps) {
			definition.timestamps = timestamps;
			if(timestamps) {
				firePropertyChanged(PROP_FIELD_ADDED, null, "timestamps");
			} else {
				firePropertyChanged(PROP_FIELD_REMOVED, null, "timestamps");
			}
		}
	}
	
	public void setDatestamps(boolean datestamps) {
		if(definition.datestamps != datestamps) {
			definition.datestamps = datestamps;
			if(datestamps) {
				firePropertyChanged(PROP_FIELD_ADDED, null, "timestamps");
			} else {
				firePropertyChanged(PROP_FIELD_REMOVED, null, "timestamps");
			}
		}
	}

	public void setAttributeOrder(String[] names) {
		definition.setAttributeOrder(names);
		firePropertyChanged(PROP_FIELD, null, "all");
	}
	
	public void setAllowDelete(boolean allow) {
		if(definition.allowDelete != allow) {
			definition.allowDelete = allow;
			System.out.println("TODO firePropertyChange for setAllowDelete");
		}
	}
	
	public void setAllowUpdate(boolean allow) {
		if(definition.allowUpdate != allow) {
			definition.allowUpdate = allow;
			System.out.println("TODO firePropertyChange for setAllowUpdate");
		}
	}
	
	public void setDeleted(boolean deleted) {
		if(deleted) {
			if(state != DELETED) {
				state = DELETED;
				firePropertyChanged(PROP_STATE, null, state);
				module.firePropertyChanged(ModuleElement.PROP_MODELS, null, this);
			}
		} else {
			if(state == DELETED) {
				state = 0;
				firePropertyChanged(PROP_STATE, null, state);
				module.addModel(this); // fires PROP_MODELS
			}
		}
	}
	
	void addConnection(Connection connection) {
		if(connection.getSourceModel() == this) {
			if(!sourceConnections.contains(connection)) {
				sourceConnections.add(connection);
			}
			firePropertyChanged(PROP_CONN_SOURCE, null, connection);
		}
		else if(connection.getTargetModel() == this) {
			if(!targetConnections.contains(connection)) {
				targetConnections.add(connection);
			}
			firePropertyChanged(PROP_CONN_TARGET, null, connection);
		}
	}

	void removeConnection(Connection connection) {
		if(connection.getSourceModel() == this) {
			sourceConnections.remove(connection);
			firePropertyChanged(PROP_CONN_SOURCE, connection, null);
		}
		else if(connection.getTargetModel() == this) {
			targetConnections.remove(connection);
			firePropertyChanged(PROP_CONN_TARGET, connection, null);
		}
	}
	
	public List<Connection> getSourceConnections() {
		return sourceConnections;
	}
	
	public List<Connection> getTargetConnections() {
		return targetConnections;
	}
	
	public boolean hasAttributes() {
		return definition.hasAttributes();
	}
	
	public boolean hasAttribute(String name) {
		return definition.hasAttribute(name);
	}
	
	public boolean hasRelation(String field) {
		return definition.hasRelation(field);
	}
	
	public void remove(String field) {
		if(definition.remove(field)) {
			firePropertyChanged(PROP_FIELD_REMOVED, null, field);
		}
	}
	
	public void setRelation(ModelRelation relation) {
		if(relation == null) {
			return;
		}
		ModelRelation rel = definition.getRelation(relation.name);
		if(rel == null) {
			definition.addRelation(relation);
			firePropertyChanged(PROP_FIELD_ADDED, null, relation.name);
		} else {
			definition.remove(relation.name);
			definition.addRelation(relation);
			firePropertyChanged(PROP_FIELD, null, relation.name);
		}
	}

	public void replace(ModelRelation oldRelation, ModelRelation newRelation) {
		definition.remove(oldRelation.name);
		definition.addRelation(newRelation);
		firePropertyChanged(PROP_FIELD, (oldRelation.name.equals(newRelation.name) ? null : oldRelation.name), newRelation.name);
	}
	
	public void setAttribute(ModelAttribute attribute) {
		if(attribute == null) {
			return;
		}
		ModelAttribute attr = definition.getAttribute(attribute.name);
		if(attr == null) {
			definition.addAttribute(attribute);
			firePropertyChanged(PROP_FIELD_ADDED, null, attribute.name);
		} else {
			definition.remove(attribute.name);
			definition.addAttribute(attribute);
			firePropertyChanged(PROP_FIELD_ADDED, null, attribute.name);
		}
	}
	
	public ModelRelation setRelation(String name, String type, String opposite, boolean hasMany) {
		ModelRelation rel = definition.getRelation(name);
		if(rel == null) {
			rel = definition.addRelation(name, type, hasMany);
			rel.opposite = opposite;
			firePropertyChanged(PROP_FIELD_ADDED, null, name);
		} else {
			rel = definition.addRelation(rel); // make sure we're working with a copy
			rel.name = name;
			rel.type = type;
			rel.opposite = opposite;
			firePropertyChanged(PROP_FIELD, null, name);
		}
		return rel;
	}

	public ModelAttribute setAttribute(String name, String type) {
		ModelAttribute attr = definition.getAttribute(name);
		if(attr == null) {
			attr = definition.addAttribute(name, type);
			firePropertyChanged(PROP_FIELD_ADDED, null, name);
		} else {
			attr = definition.addAttribute(attr);
			attr.type = type;
			firePropertyChanged(PROP_FIELD, null, name);
		}
		return attr;
	}
	
	public ModelDefinition getDefinition() {
		return definition;
	}
	
	public File getFile() {
		return definition.getFile();
	}
	
	public ModuleElement getModuleElement() {
		return module;
	}
	
	void setProperties(Map<?,?> data) {
		if(data == null) {
			return;
		}
		String s = (String) data.get("bounds");
		if(s != null) {
			String[] sa = s.substring(10, s.length()-1).split("\\s*,\\s*");
			if(sa.length == 4) {
				int x = Integer.parseInt(sa[0]);
				int y = Integer.parseInt(sa[1]);
				int w = Integer.parseInt(sa[2]);
				int h = Integer.parseInt(sa[3]);
				setBounds(new Rectangle(x,y,w,h));
			}
		}
	}
	
	public String getName() {
		return definition.getSimpleName();
	}

	public String getType() {
		return definition.getCanonicalName();
	}
	
	public Point getLocation() {
		return location.getCopy();
	}
	
	public Dimension getSize() {
		return size.getCopy();
	}
	
	public Rectangle getBounds() {
		return new Rectangle(location, size);
	}
	
	public void setBounds(Rectangle bounds) {
		if(bounds != null) {
			this.location = bounds.getLocation();
			this.size = bounds.getSize();
			firePropertyChanged(PROP_BOUNDS, null, location.getCopy());
		}
	}
	
	public void setLocation(Point location) {
		if(location != null) {
			this.location.setLocation(location);
			firePropertyChanged(PROP_LOCATION, null, location.getCopy());
		}
	}
	
	public void setSize(Dimension size) {
		if(size != null) {
			this.size.setSize(size);
			firePropertyChanged(PROP_SIZE, null, size.getCopy());
		}
	}
	
	@Override
	public IPropertyDescriptor[] getPropertyDescriptors() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Object getPropertyValue(Object id) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean isPropertySet(Object id) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void resetPropertyValue(Object id) {
		// TODO Auto-generated method stub

	}

	@Override
	public void setPropertyValue(Object id, Object value) {
		// TODO Auto-generated method stub

	}

	@Override
	public String toString() {
		return definition.toString();
	}
	
}
