(function(){
	var root = this;
	var Oobium = root.Oobium = root.Oobium || {};

	var Cache = Oobium.Cache = new function() {
		var models = {};
		
		this.get = function(type, id) {
			return models[type] ? models[type][id] : null;
		};
		
		this.put = function(model) {
			var type = model.getType();
			if(!models[type]) {
				models[type] = {};
			}
			models[type][model.getId()] = model;
		}
		
		this.remove = function(model) {
			var type = model.getType();
			if(models[type]) {
				delete models[type][model.getId()];
			}
		}
	};
	
	
	var Router = Oobium.Router = new function() {
		var baseUrl = '/';
		var routes = Oobium.routes || {};
		
		this.loadRoutes = function() {
			$.ajax({
				type: 'HEAD',
				async: true,
				url: baseUrl,
				complete: function(xhr, status) {
					if('success'.equals(status)) {
						var api = xhr.getResponseHeader('API-Location');
						if(api) {
							$.getJSON(api, function(data) {
								routes = data;
							});
							return;
						}
					}
					alert('error loading routes');
				}
			});
		};
		
		this.getMethod = function(action, model) {
			var type = model.getType ? model.getType() : model;
			return routes[type][action]['method'];
		};
		
		this.getPath = function(action, model, id) {
			var type = model.getType ? model.getType() : model;
			var route = routes[type][action];
			var path = route['path'];
			if(route['fixed']) {
				return path;
			}
			path = path.replace("{models}", Model.types[type].models);
			path = path.replace("{id}", id || model.getId());
			return path;
		};

		this.getUrl = function() {
			return baseUrl;
		}

		this.setRoutes = function(map) {
			routes = map;
			return this;
		};
		
		this.setUrl = function(url) {
			baseUrl = url;
			return this;
		}
	}
	
	
	var Sync = Oobium.Sync = new function() {
		var self = this;
		var commitInterval = Oobium.commitInterval || 3000;
		var operations = [];
		var waiting = false;
		var timerId = null;
		var callbacks = {
			queue: $.Callbacks(),
			start: $.Callbacks(),
			complete: $.Callbacks()
		}

		/**
		 * @return a Promise object
		 */
		this.add = function(request) {
			if(operations.length == 0) {
				fire('queue');
			}
			startTimer();
			var op;
			if(request.type == 'PUT') {
				for(var i in operations) {
					var r = operations[i].req;
					if(r.type == request.type && r.url == request.url) {
						op = operations[i];
						break;
					}
				}
			}
			if(op) {
				$.extend(true, op.req.data, request.data);
			} else {
				op = {
						dfd: $.Deferred(),
						fnc: function() {
							$.ajax(this.req).always( this.dfd.resolve );
						},
						req: request
				};
				operations.push(op);
			}
			return op.dfd.promise();
		};
	
		this.auto = function() {
			return commitInterval > 0;
		};
		
		var clearTimer = function() {
			if(timerId) {
				clearTimeout(timerId);
			}
		};
		
		this.commit = function() {
			clearTimer();
			if(operations.length > 0) {
				var dfds = [];
				for(var i in operations) {
					var op = operations[i];
					op.fnc();
					dfds.push(op.dfd);
				}
				$.when.apply($, dfds)
					.then(function() {
						fire('complete');
					});
				operations = [];
				fire('start');
			} else {
				fire('start');
				fire('complete');
			}
		};
	
		this.complete = function(callback) {
			callbacks.complete.add(callback);
		};
		
		this.dirty = function() {
			return operations.length > 0;
		};
	
		var fire = function(eventName) {
			callbacks[eventName].fire(self);
		}
		
		this.queue = function(callback) {
			callbacks.queue.add(callback);
		};
		
		this.setInterval = function(interval) {
			if(interval) {
				commitInterval = parseInt(interval);
				if(operations.length > 0) {
					startTimer();
				}
			} else {
				clearTimer();
			}
		};
		
		this.start = function(callback) {
			callbacks.start.add(callback);
		};
		
		var startTimer = function() {
			clearTimer();
			if(commitInterval > 0) {
				timerId = setTimeout(self.commit, commitInterval);
			}
		};
		
		this.subscribe = function(eventName, callback) {
			callbacks[eventName].add(callback);
		};
		
		this.unsubscribe = function(eventName, callback) {
			callbacks[eventName].remove(callback);
		};
		
	};
	
	Sync.Promise = function(dfd, model) {
		var promise = dfd.promise();
		promise.commit = function() {
			Sync.commit();
			return model;
		}
		return promise;
	};

	
	var Model = Oobium.Model = function(type) {
		var type = type;
		var id = null;
		var data = {};
		var callbacks = $.Callbacks();

		var asJsonData = function() {
			var json = {};
			for(var key in data) {
				if(data.hasOwnProperty(key)) {
					var val = data[key];				
					if(val instanceof Date) {
						json[key] = '/Date(' + val.getTime() + ')/';
					} else {
						json[key] = val;
					}
				}
			}
			var jsonData = {};
			jsonData[Model.types[type].name] = json;
			return jsonData;
		};

		this.addCallback = function(callback) {
			callbacks.add(callback);
		};

		/**
		 * @return a Promise object
		 */
		this.create = function() {
			var dfd = $.Deferred();
			var model = this;
			var request = {
					type: Router.getMethod('create', model),
					url: Router.getPath('create', model),
					data: asJsonData(),
					dataType: 'json'
			};
			(Sync.auto() ? Sync.add(request) : $.ajax(request))
				.done(function(data, status, xhr) {
					model.setId(xhr.getResponseHeader('id'));
					dfd.resolve(model);
				})
				.fail(function(data) { dfd.reject(model, data); });
			return Sync.Promise(dfd, this);
		};

		/**
		 * @return a Promise object
		 */
		this.destroy = function() {
			var dfd = $.Deferred();
			if(this.isNew()) {
				data = {};
				dfd.resolve(this);
			} else {
				var request = {
						type: Router.getMethod('destroy', this),
						url: Router.getPath('destroy', this),
						dataType: 'json'
				};
				var prevId = id;
				id = null;
				data = {};
				$.ajax(request)
					.done(function() { dfd.resolve(prevId) })
					.fail(function(data) { dfd.reject(prevId, data) });
			}
			return Sync.Promise(dfd, this);
		};
		
		this.equals = function(otherModel) {
			return (id && type == otherModel.getType()) && (id == otherModel.getId());
		};
		
		this.get = function(field) {
			return data[field];
		};

		this.getId = function() {
			return id;
		};

		this.getType = function() {
			return type;
		};

		this.isNew = function() {
			return id == null;
		};
		
		/**
		 * @return a Promise object
		 */
		this.load = function() {
			var dfd = $.Deferred();
			var model = this;
			var request = {
					type: Router.getMethod('show', this),
					url: Router.getPath('show', this),
					dataType: 'json'
			};
			$.ajax(request)
				.done(function(data) {
					model.set(data);
					dfd.resolve(model);
					callbacks.fire(this, $.extend({}, model.data));
				})
				.fail(function(data) { dfd.reject(model, data) });
			return dfd.promise();
		};
		
		/**
		 * @return a Promise object
		 */
		this.save = function() {
			return this.isNew() ? this.create() : this.update();
		};

		this.set = function(a, b) {
			if(a) {
				if(b != undefined) {
					var c = {}; c[a] = b; a = c;
				}
				if(a.hasOwnProperty('id')) {
					this.setId(a.id);
				}
				var changes = {};
				for(var key in a) {
					if(key != 'id' && a.hasOwnProperty(key)) {
						var val = a[key];
						if(typeof val == 'string' && val.indexOf('/Date(') == 0) {
							changes[key] = data[key] = new Date(parseInt(val.substring(6, val.length-2)));
						} else {
							changes[key] = data[key] = val;
						}
					}
				}
				callbacks.fire(this, changes);
			}
			return this;
		};
		
		this.setId = function(newId) {
			if(id != newId) {
				if(newId == undefined || newId == 0 || newId == '' || newId == '0' || newId == 'null') {
					id = null;
				} else {
					id = newId;
				}
			}
			return this;
		};

		/**
		 * @return a Promise object
		 */
		this.update = function() {
			var dfd = $.Deferred();
			var model = this;
			var request = {
					type: Router.getMethod('update', this),
					url: Router.getPath('update', this),
					data: asJsonData(),
					dataType: 'json'
			};
			(Sync.auto() ? Sync.add(request) : $.ajax(request))
				.done(function(data, status, xhr) { dfd.resolve(model); })
				.fail(function(data) { dfd.reject(model, data); });
			return Sync.Promise(dfd, this);
		};
		
	}

	Model.extend = function(options) {
		function F() {
			Oobium.Model.call(this, options.type);
		}
		F.prototype = Oobium.Model.prototype;
		F.find = function(query, values) { return Oobium.Model.find(options.type, query, values); }
		F.findAll = function(query, values) { return Oobium.Model.findAll(options.type, query, values); }
		F.findById = function(id) { return Oobium.Model.find(options.type, id); }
		Model.types = Model.types || {};
		Model.types[options.type] = {'name': options.name, 'models': options.models};
		return F;
	}

	/**
	 * @return a Promise object
	 */
	Model.find = function(type, query, values) {
		var dfd = $.Deferred();
		var request = {};
		request.type = Router.getMethod('show', type);
		request.url = Router.getPath('show', type, id);
		request.dataType = 'json';
		request.dataType = {'query': query, 'values': values};
		$.ajax(request)
			.done(function(data) { dfd.resolve(Model.newInstance(type, data)); })
			.fail(function(data) { dfd.reject(data); });
		return dfd.promise();
	}
	
	/**
	 * @return a Promise object
	 */
	Model.findAll = function(type, query, values) {
		var dfd = $.Deferred();
		var request = {};
		request.type = Router.getMethod('showAll', type);
		request.url = Router.getPath('showAll', type);
		request.dataType = 'json';
		request.data = {'query': query, 'values': values};
		$.ajax(request)
			.done(function(data, status, xhr) {
				var models = [];
				for(var i in data) {
					var model = Model.newInstance(type, data[i]);
					Cache.put(model);
					models.push(model);
				}
				dfd.resolve(models);
			})
			.fail(function(data) { dfd.reject(data); });
		return dfd.promise();
	}
	
	/**
	 * @return a Promise object
	 */
	Model.findById = function(type, id) {
		var dfd = $.Deferred();
		var model = Cache.get(type, id);
		if(model) {
			dfd.resolve(model);
		} else {
			var request = {};
			request.type = Router.getMethod('show', type);
			request.url = Router.getPath('show', type, id);
			request.dataType = 'json';
			$.ajax(request)
				.done(function(data) { dfd.resolve(Model.newInstance(type, data)); })
				.fail(function(data) { dfd.reject(data); });
		}
		return dfd.promise();
	}
	
	<Model.newInstance>
	
	
	var Observer = Oobium.Observer = function(modelType) {
		
	}

}).call(this);
