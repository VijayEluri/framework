package org.oobium.persist.http;

import static org.oobium.utils.StringUtils.*;
import static org.oobium.app.http.Action.create;
import static org.oobium.app.http.MimeType.JSON;
import static org.oobium.persist.http.PathBuilder.path;
import static org.oobium.utils.coercion.TypeCoercer.coerce;
import static org.oobium.utils.json.JsonUtils.*;

import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.jboss.netty.handler.codec.http.HttpResponseStatus;
import org.oobium.client.Client;
import org.oobium.client.ClientResponse;
import org.oobium.persist.Model;
import org.oobium.persist.http.HttpApiService.Request;

public class ModelForm {

	public class Field {
		private final ModelForm resource;
		private final String name;
		private Field(ModelForm resource, String name) {
			this.resource = resource;
			this.name = name;
			as(null);
		}
		public void as(Object value) {
			resource.fields.put(name, value);
		}
	}
	
	private final HttpApiService api;
	private final Class<? extends Model> modelClass;
	private final Map<String, Object> fields;

	private int id;
	private List<String> errors;
	
	public ModelForm(Class<? extends Model> modelClass) {
		this.api = HttpApiService.getInstance();
		this.modelClass = modelClass;
		this.fields = new HashMap<String, Object>();
	}
	
	public ModelForm(Model model) {
		this(model.getClass());
	}
	
	public Field add(String...fields) {
		return new Field(this, fieldName(fields));
	}

	private void addError(String error) {
		if(errors == null) {
			errors = new ArrayList<String>();
		}
		errors.add(error);
	}
	
	private void clearErrors() {
		if(errors != null) {
			errors.clear();
			errors = null;
		}
	}
	
	public boolean create() {
		clearErrors();
		
		Request request = api.getRequest(modelClass, create);
		if(request == null) {
			addError("no published route found for " + modelClass + ": create");
		} else {
			try {
				Client client = Client.client(request.url);
				client.setAccepts(JSON.acceptsType);
	
				for(Entry<String, Object> entry : fields.entrySet()) {
					client.addParameter(entry.getKey(), entry.getValue());
				}
				
				String path = path(request.path, modelClass);
				ClientResponse response = client.request(request.method, path);
				if(response.isSuccess()) {
					int id = coerce(response.getHeader("id"), int.class);
					setId(id);
				}else {
					if(response.exceptionThrown()) {
						addError(response.getException().getLocalizedMessage());
					} else {
						Map<String, Object> map = toMap(response.getBody(), true);
						Object o = map.get("errors");
						if(o instanceof List) {
							for(Object error : (List<?>) o) {
								addError(String.valueOf(error));
							}
						} else {
							HttpResponseStatus status = response.getStatus();
							addError("error: " + status.getCode() + " " + status.getReasonPhrase());
						}
					}
				}
			} catch(MalformedURLException e) {
				addError("malformed URL should have been caught earlier!");
			}
		}
		return !hasErrors();
	}
	
	private String fieldName(String[] sa) {
		StringBuilder sb = new StringBuilder();
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
		return sb.toString();
	}
	
	public List<String> getErrors() {
		return errors;
	}
	
	public int getId() {
		return id;
	}

	public boolean hasErrors() {
		return errors != null;
	}
	
	public boolean isNew() {
		return id <= 0;
	}
	
	public void setId(int id) {
		this.id = id;
	}
	
}
