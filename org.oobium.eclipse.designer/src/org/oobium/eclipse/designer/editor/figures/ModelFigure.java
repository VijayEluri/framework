package org.oobium.eclipse.designer.editor.figures;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.draw2d.ChopboxAnchor;
import org.eclipse.draw2d.ColorConstants;
import org.eclipse.draw2d.ConnectionAnchor;
import org.eclipse.draw2d.Figure;
import org.eclipse.draw2d.IFigure;
import org.eclipse.draw2d.Label;
import org.eclipse.draw2d.LabelAnchor;
import org.eclipse.draw2d.LineBorder;
import org.eclipse.draw2d.Panel;
import org.eclipse.draw2d.ToolbarLayout;
import org.eclipse.draw2d.geometry.Point;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Display;

public class ModelFigure extends Figure {

	private static final Image attrImage = new Image(Display.getDefault(), ModelFigure.class.getResourceAsStream("attribute.gif"));
	private static final Image hasOneImage = new Image(Display.getDefault(), ModelFigure.class.getResourceAsStream("has_one.gif"));
	private static final Image hasManyImage = new Image(Display.getDefault(), ModelFigure.class.getResourceAsStream("has_many.gif"));
	private static final Image errorImage = new Image(Display.getDefault(), ModelFigure.class.getResourceAsStream("error.gif"));
	
	private Label name;
	private Panel body;
	private final SectionFigure attrs;
	private final SectionFigure hasOne;
	private final SectionFigure hasMany;
	private final SectionFigure errors;
	
	private Map<Label, String> fields;
	private Map<String, Label> labels;
	
	public ModelFigure() {
		ToolbarLayout layout = new ToolbarLayout();
		setLayoutManager(layout);
		setBorder(new LineBorder(ColorConstants.blue));
		setBackgroundColor(ColorConstants.green);
		setOpaque(true);

		add(name = new Label());
		
		add(body = new Panel());
		layout = new ToolbarLayout();
		layout.setSpacing(5);
		body.setLayoutManager(layout);
		body.setBorder(new LineBorder(ColorConstants.green, 5));
		body.setBackgroundColor(ColorConstants.green);
		body.setOpaque(true);
		
		body.add(attrs = new SectionFigure());
		body.add(hasOne = new SectionFigure());
		body.add(hasMany = new SectionFigure());
		body.add(errors = new SectionFigure());

		attrs.setVisible(false);
		hasOne.setVisible(false);
		hasMany.setVisible(false);
		errors.setVisible(false);
	}

	public void addAttribute(String field, String type) {
		Label label= new Label(field + ": " + type, attrImage);
		attrs.add(label);
		put(field, label);
		attrs.setVisible(true);
	}
	
	private Label addError(String field) {
		Label lbl = new Label(field, errorImage);
		errors.add(lbl);
		errors.setVisible(true);
		return lbl;
	}

	private void addHasMany(Label label) {
		hasMany.add(label);
		hasMany.setVisible(true);
	}
	
	private void addHasOne(Label label) {
		hasOne.add(label);
		hasOne.setVisible(true);
	}

	public void addRelation(String field, String type, boolean hasMany) {
		Label label = new Label(field + ": " + type, hasMany ? hasManyImage : hasOneImage);
		if(hasMany) {
			addHasMany(label);
		}
		else {
			addHasOne(label);
		}
		put(field, label);
	}
	
	public ConnectionAnchor getAnchor(String field) {
		if(field == null || field.trim().length() == 0) {
			return new ChopboxAnchor(this);
		}
		if(labels != null) {
			Label label = labels.get(field);
			if(label != null) {
				return new LabelAnchor(label);
			}
		}
		return new LabelAnchor(addError(field));
	}
	
	public String getAttribute(Point pt) {
		String field = getField(pt);
		if(field != null) {
			IFigure parent = labels.get(field).getParent();
			if(parent == attrs) {
				return field;
			}
		}
		return null;
	}

	public String getField(Point pt) {
		if(fields != null) {
			return fields.get(findFigureAt(pt));
		}
		return null;
	}
	
	public String getRelation(Point pt) {
		String field = getField(pt);
		if(field != null) {
			IFigure parent = labels.get(field).getParent();
			if(parent == hasOne || parent == hasMany) {
				return field;
			}
		}
		return null;
	}
	
	private void put(String field, Label label) {
		if(labels == null) {
			labels = new HashMap<String, Label>();
		}
		labels.put(field, label);
		if(fields == null) {
			fields = new HashMap<Label, String>();
		}
		fields.put(label, field);
	}
	
	public void remove(String field) {
		Label label = labels.remove(field);
		if(label != null) {
			fields.remove(label);
			IFigure section = label.getParent();
			if(section == attrs) {
				attrs.remove(label);
				if(attrs.getChildren().isEmpty()) {
					attrs.setVisible(false);
				}
			}
			if(section == hasOne) {
				hasOne.remove(label);
				if(hasOne.getChildren().isEmpty()) {
					hasOne.setVisible(false);
				}
			}
			if(section == hasMany) {
				hasMany.remove(label);
				if(hasMany.getChildren().isEmpty()) {
					hasMany.setVisible(false);
				}
			}
		}
	}
	
	public void removeAttributes() {
		if(attrs != null) {
			for(Object o : attrs.getChildren()) {
				if(o instanceof Label) {
					String field = fields.remove(labels);
					labels.remove(field);
				}
			}
			attrs.removeAll();
			attrs.setVisible(false);
		}
	}
	
	public void setName(String name) {
		this.name.setText(name);
	}
	
	public void updateRelation(String oldField, String newField, String type, boolean hasMany) {
		Label label = labels.remove(oldField);
		label.setText(newField + ": " + type);

		if(hasMany && label.getParent() == hasOne) {
			hasOne.remove(label);
			if(hasOne.getChildren().isEmpty()) hasOne.setVisible(false);
			label.setIcon(hasManyImage);
			addHasMany(label);
		}
		else if(!hasMany && label.getParent() == this.hasMany) {
			this.hasMany.remove(label);
			if(this.hasMany.getChildren().isEmpty()) this.hasMany.setVisible(false);
			label.setIcon(hasOneImage);
			addHasOne(label);
		}
		
		fields.remove(label);
		labels.put(newField, label);
		fields.put(label, newField);
	}
	
}
