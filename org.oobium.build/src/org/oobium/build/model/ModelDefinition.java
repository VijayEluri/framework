/*******************************************************************************
 * Copyright (c) 2010 Oobium, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Jeremy Dowdall <jeremy@oobium.com> - initial API and implementation
 ******************************************************************************/
package org.oobium.build.model;

import static org.oobium.persist.ModelAttributes.createdAt;
import static org.oobium.persist.ModelAttributes.createdOn;
import static org.oobium.persist.ModelAttributes.updatedAt;
import static org.oobium.persist.ModelAttributes.updatedOn;
import static org.oobium.utils.StringUtils.controllerSimpleName;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;

import org.oobium.persist.Attribute;
import org.oobium.persist.ModelDescription;
import org.oobium.persist.Relation;

public class ModelDefinition {

	private String canonicalName;
	private ModelDescription model;
	
	private Map<String, ModelAttribute> attributes;
	private Map<String, ModelRelation> relations;
	
	private String[] indexes;

	public ModelDefinition(String canonicalName, ModelDescription model, String[] indexes) {
		this.canonicalName = canonicalName;
		this.model = model;
		this.indexes = indexes;
	}
	
	public Map<String, ModelAttribute> attributes() {
		return attributes;
	}
	
	public boolean build() {
		if(model == null) {
			return false;
		}

		attributes = new LinkedHashMap<String, ModelAttribute>();
		relations = new LinkedHashMap<String, ModelRelation>();

		for(Attribute attr : model.attrs()) {
			ModelAttribute mattr = new ModelAttribute(this, attr);
			attributes.put(mattr.getName(), mattr);
		}
		
		if(model.datestamps()) {
			attributes.put(createdOn.name(), new ModelAttribute(this, createdOn));
			attributes.put(updatedOn.name(), new ModelAttribute(this, updatedOn));
		}
		
		if(model.timestamps()) {
			attributes.put(createdAt.name(), new ModelAttribute(this, createdAt));
			attributes.put(updatedAt.name(), new ModelAttribute(this, updatedAt));
		}
		
		for(Relation relation : model.hasOne()) {
			ModelRelation mrelation = new ModelRelation(this, relation, false);
			relations.put(mrelation.getName(), mrelation);
		}

		for(Relation relation : model.hasMany()) {
			ModelRelation mrelation = new ModelRelation(this, relation, true);
			relations.put(mrelation.getName(), mrelation);
		}
		
		return true;
	}
	
	public ModelAttribute getAttribute(String name) {
		return attributes.get(name);
	}
	
	public Collection<ModelAttribute> getAttributes() {
		return attributes.values();
	}
	
	public String getCanonicalName() {
		return canonicalName;
	}
	
	public String getControllerName() {
		return controllerSimpleName(canonicalName);
	}

	public String[] getIndexes() {
		return indexes;
	}
	
	public ModelDescription getModel() {
		return model;
	}
	
	public String getPackageName() {
		int ix = canonicalName.lastIndexOf('.');
		if(ix == -1) {
			return canonicalName;
		}
		return canonicalName.substring(0, ix);
	}

	public ModelRelation getRelation(String name) {
		return relations.get(name);
	}
	
	public Collection<ModelRelation> getRelations() {
		return relations.values();
	}
	
	public String getSimpleName() {
		int ix = canonicalName.lastIndexOf('.');
		if(ix == -1) {
			return canonicalName;
		}
		return canonicalName.substring(ix+1);
	}
	
	public boolean hasAttributes() {
		return attributes != null && !attributes.isEmpty();
	}
	
	public boolean hasRelations() {
		return relations != null && !relations.isEmpty();
	}

	public Map<String, ModelRelation> relations() {
		return relations;
	}

	public void setOpposites(Collection<ModelDefinition> models) {
		for(ModelRelation relation : relations.values()) {
			relation.setOpposite(models);
		}
	}
	
	public void setRelations(Map<String, ModelRelation> relations) {
		this.relations = relations;
	}
	
	@Override
	public String toString() {
		return super.toString()+" {"+canonicalName+" => "+model+"}";
	}

}
