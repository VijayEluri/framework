package org.oobium.persist.http;

import static org.oobium.app.http.Action.create;
import static org.oobium.app.http.MimeType.JSON;
import static org.oobium.persist.http.PathBuilder.path;
import static org.oobium.utils.coercion.TypeCoercer.coerce;
import static org.oobium.utils.json.JsonUtils.toMap;

import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.jboss.netty.handler.codec.http.HttpResponseStatus;
import org.oobium.client.Client;
import org.oobium.client.ClientResponse;
import org.oobium.persist.Model;
import org.oobium.persist.http.HttpApiService.Route;

public class ModelForm extends ModelFields {

	private final HttpApiService api;

	private int id;
	private Map<String, Object> parameters;
	private List<ModelFields> subFields;
	private List<String> errors;
	
	public ModelForm(Class<? extends Model> modelClass) {
		super(null, modelClass);
		this.form = this;
		this.api = HttpApiService.getInstance();
	}

	public ModelForm(Model model) {
		this(model.getClass());
		for(Entry<String, Object> entry : model.getAll().entrySet()) {
			addField(entry.getKey()).as(entry.getValue());
		}
	}

	public Map<String, Object> getParameters() {
		if(parameters == null) {
			return new HashMap<String, Object>(0);
		}
		return new LinkedHashMap<String, Object>(parameters);
	}
	
	void put(String name, Object value) {
		if(parameters == null) {
			parameters = new LinkedHashMap<String, Object>();
		}
		parameters.put(name, value);
	}
	
	private void addError(String error) {
		if(errors == null) {
			errors = new ArrayList<String>();
		}
		errors.add(error);
	}

	public ModelFields addFieldsFor(Class<? extends Model> modelClass) {
		if(subFields == null) {
			subFields = new ArrayList<ModelFields>();
		}
		ModelFields subForm = new ModelFields(this, modelClass);
		subFields.add(subForm);
		return subForm;
	}

	public ModelFields addFieldsFor(Model model) {
		ModelFields subForm = addFieldsFor(model.getClass());
		for(Entry<String, Object> entry : model.getAll().entrySet()) {
			subForm.addField(entry.getKey()).as(entry.getValue());
		}
		return subForm;
	}

	private void clearErrors() {
		if(errors != null) {
			errors.clear();
			errors = null;
		}
	}
	
	public boolean create() {
		clearErrors();
		
		Route request = api.getRoute(modelClass, create);
		if(request == null) {
			addError("no published route found for " + modelClass + ": create");
		} else {
			try {
				Client client = Client.client(request.url);
				client.setAccepts(JSON.acceptsType);
	
				for(Entry<String, Object> entry : parameters.entrySet()) {
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
