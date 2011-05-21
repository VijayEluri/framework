package org.oobium.persist.http;

import static org.oobium.utils.StringUtils.varName;

import org.oobium.persist.Model;

public class ModelFields {

	public class Parameter {
		private final ModelFields fields;
		private final String name;
		Parameter(ModelFields resource, String name) {
			this.fields = resource;
			this.name = name;
			as(null);
		}
		public ModelFields as(Object value) {
			fields.form.put(name, value);
			return fields;
		}
	}
	
	ModelForm form;
	protected final Class<? extends Model> modelClass;

	ModelFields(ModelForm form, Class<? extends Model> modelClass) {
		this.form = form;
		this.modelClass = modelClass;
	}
	
	public Parameter add(String...names) {
		return new Parameter(this, parameterName(names, false));
	}
	
	public Parameter addField(String...fields) {
		return new Parameter(this, parameterName(fields, true));
	}

	protected String parameterName(String[] sa, boolean isField) {
		StringBuilder sb = new StringBuilder();
		if(isField) {
			sb.append(varName(modelClass)).append('[');
			if(sa.length == 1) {
				sb.append(sa[0]);
			}
			else {
				for(int i = 0; i < sa.length; i++) {
					if(i != 0) sb.append("][");
					sb.append(sa[i]);
				}
			}
			sb.append(']');
		} else {
			if(sa.length == 1) {
				sb.append(sa[0]);
			}
			else {
				sb.append(varName(sa[0])).append('[');
				for(int i = 1; i < sa.length; i++) {
					if(i != 1) sb.append("][");
					sb.append(sa[i]);
				}
				sb.append(']');
			}
		}
		return sb.toString();
	}
	
}
