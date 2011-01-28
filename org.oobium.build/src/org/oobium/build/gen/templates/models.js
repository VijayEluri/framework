function $Extend(clazz, superClass) {
	function inheritance() {}
	inheritance.prototype = superClass.prototype;
	clazz.prototype = new inheritance();
	clazz.prototype.constructor = clazz;
	clazz.superConstructor = superClass;
	clazz.superClass = superClass.prototype;
}

function Router(modelRoutes) {
	if(modelRoutes) {
		this.modelRoutes = modelRoutes;
	} else {
		$.ajax({
			type: 'HEAD',
			async: true,
			url: '/',
			complete: function(xhr, status) {
				if('success'.equals(status)) {
					var api = xhr.getResponseHeader('API-Location');
					if(api) {
						$.getJSON(api, function(data) {
							modelRoutes = data;
						});
						return;
					}
				}
				alert('error');
			}
		});
	}
}

Router.prototype.getType = function(action, model) {
	return this.modelRoutes[model.type || model][action]['method'];
}

Router.prototype.getPath = function(action, model, plural, id) {
	var modelType, modelPlural, modelId;
	if(plural) {
		modelType = model;
		modelPlural = plural;
		modelId = id;
	} else {
		modelType = model.type;
		modelPlural = model.plural;
		modelId = model.id;
	}
	var route = this.modelRoutes[modelType][action];
	var path = route['path'];
	if(route['fixed']) {
		return path;
	}
	path = path.replace("{models}", modelPlural);
	path = path.replace("{id}", modelId);
	return path;
}

function Model(params) {
	if(typeof params == 'number' || typeof params == 'string' ) {
		this.id = params;
		this.data = {};
	} else {
		if(params) {
			if(params.id) {
				id = params.id;
				delete params.id;
			}
			this.data = params;
		} else {
			this.data = {};
		}
	}
}

<Model.newInstance>

Model.find = function(params) {
	var request = {};
	request.type = $Router.getType('show', params.type);
	request.url = $Router.getPath('show', params.type, params.plural, params.id);
	request.dataType = 'json';
	request.success = function(data, status, xhr) {
		var model = Model.newInstance(params.type, data);
		if(params.success) params.success(model, status, xhr);
	}
	if(params.error) {
		request.error = function(xhr, status, errorThrown) {
			params.error(xhr, status, errorThrown);
		}
	}
	$.ajax(request);
}

Model.findAll = function(params) {
	var request = {};
	request.type = $Router.getType('showAll', params.type);
	request.url = $Router.getPath('showAll', params.type, params.plural);
	request.dataType = 'json';
	request.success = function(data, status, xhr) {
		var models = [];
		for(var i in data) {
			models.push(Model.newInstance(params.type, data[i]));
		}
		if(params.success) params.success(models, status, xhr);
	}
	if(params.error) {
		request.error = function(xhr, status, errorThrown) {
			params.error(xhr, status, errorThrown);
		}
	}
	$.ajax(request);
}

Model.prototype.create = function(success, error) {
	var model = this;
	var request = {};
	request.type = $Router.getType('create', model);
	request.url = $Router.getPath('create', model),
	request.data = {};
	request.data[model.varName] = model.data;
	request.dataType = 'json';
	request.success = function(data, status, xhr) {
		model.id = xhr.getResponseHeader('id');
		if(success) success(model, status, xhr);
	}
	if(error) {
		request.error = function(data, status, xhr) {
			error(data, status, xhr);
		}
	}
	$.ajax(request);
}

Model.prototype.destroy = function(success, error) {
	var model = this;
	var request = {};
	request.type = $Router.getType('destroy', model);
	request.url = $Router.getPath('destroy', model),
	request.dataType = 'json';
	request.success = function(data, status, xhr) {
		model.id = 0;
		model.data = null;
		model.destroyed = true;
		if(success) success(model, status, xhr);
	}
	if(error) {
		request.error = function(data, status, xhr) {
			error(data, status, xhr);
		}
	}
	$.ajax(request);
}

Model.prototype.retrieve = function(success, error) {
	var model = this;
	var request = {};
	request.type = $Router.getType('show', model);
	request.url = $Router.getPath('show', model),
	request.dataType = 'json';
	request.success = function(data, status, xhr) {
		model.data = data;
		if(success) success(model, status, xhr);
	}
	if(error) {
		request.error = function(data, status, xhr) {
			error(data, status, xhr);
		}
	}
	$.ajax(request);
}

Model.prototype.update = function(success, error) {
	var model = this;
	var request = {};
	request.type = $Router.getType('update', model);
	request.url = $Router.getPath('update', model),
	request.data = {};
	request.data[model.varName] = model.data;
	request.dataType = 'json';
	if(success) {
		request.success = function(data, status, xhr) {
			success(model, status, xhr);
		}
	}
	if(error) {
		request.error = function(data, status, xhr) {
			error(data, status, xhr);
		}
	}
	$.ajax(request);
}

Model.prototype.save = function(success, error) {
	if(this.id == 0) {
		this.create(success, error);
	} else {
		this.update(success, error);
	}
}
