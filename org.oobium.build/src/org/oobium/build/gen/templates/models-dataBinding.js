$(document).ready(function() {
	$(document).mousedown(function() { Oobium.DataBinding.commit() });
	$('[data-model]').each(function() { Oobium.DataBinding.bind(this); });
});

(function(){
	var root = this;
	var Oobium = root.Oobium = root.Oobium || {};

	var Bnd = Oobium.DataBinding = new function() {
		var editor = null;
		var trigger = null;
		
		this.bind = function(element, model) {
			element = $(element);
			model = model || Bnd.getModel(element);
			element.data('model', model);
			if(element.attr('onclick')) {
				eval("var action = function(event, model) {" + element.attr('onclick') + "}")
				element.removeAttr('onclick').unbind('click').bind('click', function(event) {
					return action.apply(this, [event, model]);
				});
			}
			bindFields(model, element);
			return model
		};
		
		var bindField = function(model, e) {
			e.data('model', model);

			var field = Bnd.getField(e);
			model.addCallback(function(m,d) {
				if(d[field]) {
					Bnd.updateLabel(e);
				}
			});
			Bnd.updateLabel(e, false);

			if($.inArray(Bnd.getEditorType(e), ['combo','select','text','textarea']) != -1) {
				e.click(function() {
					Bnd.createEditor(e);
				});
			}
		};

		var bindFields = function(model, element) {
			element.children().each(function() {
				var e = $(this);
				if(e.attr('data-model')) {
					return;
				}
				if(e.attr('data-field')) {
					bindField(model, e);
				}
				bindFields(model, e);
			})
		};

		this.close = function() {
			if(editor) {
				editor.remove();
				editor = null;
			}
			if(trigger) {
				if(trigger.autocomplete) {
					trigger.autocomplete('destroy');
				}
				trigger = null;
			}
		};

		this.commit = function(value) {
			if(trigger) {
				if(value == undefined && editor) {
					value = editor.val();
				}
				var model = this.getModel(trigger);
				var field = this.getField(trigger);
				var options = this.getOptions(trigger);
				
				var oldValue = model.get(field);
				var newValue = convertToModelValue(model, field, value, options);
				if(notEqual(newValue, oldValue)) {
					model.set(field, newValue).update(field);
				}

				this.close();
			}
		};

		var convertToEditorValue = function(value, options) {
			// TODO plug-in editor value converters
			if(!value) {
				return '';
			}
			if(value instanceof Date) {
				return value.getTime();
			}
			return value;
		};

		var convertToLabel = function(value, options) {
			// TODO plug-in label converters
			if(!value) {
				return (options && options.alt) ? h(options.alt) : '';
			}
			if(value instanceof Date) {
				if(options && options.format) {
					return h(value.format(options.format));
				} else {
					return (value.getMonth()+1) + '/' + value.getDate() + '/' + value.getFullYear();
				}
			}
			return h(value);
		};
		
		var convertToModelValue = function(model, field, value, options) {
			// TODO plug-in value converters
			// TODO store type information?
			if(value == undefined) {
				return null;
			}
			if(model.get(field) instanceof Date) {
				return new Date(parseInt(value));
			}
			return value;
		};

		this.createEditor = function(triggerElement) {
			$('.tipsy').remove();
			
			trigger = $(triggerElement);
			var opts = this.getOptions(trigger);
			var etype = this.getEditorType(trigger);
			
			if('select' == etype) {
				trigger.autocomplete({
					source: getAutocompleteSource(this.getEditorOptions(trigger)),
					delay: 0,
					minLength: 0,
					create: function(event, ui) {
						trigger.autocomplete('widget').mousedown(function(e) { e.stopPropagation(); });
					},
					select: function(event, ui) {
						if(ui.item) {
							Bnd.commit(ui.item.value);
						} else {
							Bnd.close();
						}
					}
				});
				trigger.autocomplete('search', '');
				return;
			}
			
			if('textarea' == etype) {
				var html = "<textarea class='editbox'></textarea>";
			} else {
				var html = "<input class='editbox' type='text' />";
			}
			editor = $(html).appendTo(trigger.parent());
			editor.val(this.getEditorValue(trigger));
			
			editor.css('position', 'absolute');
			if(opts.editorClass) {
				editor.addClass(opts.editorClass);
			} else {
				editor.addClass(this.getField(trigger));
				if('text' == etype || 'combo' == etype) {
					editor.css('width', trigger.outerWidth());
				}
				else if('textarea' == etype) {
					editor.css('top', '0');
					editor.css('left', '0');
				}
			}
			var hpos = (editor.css('left') != 'auto') ? 'left' : ((editor.css('right') != 'auto') ? 'right' : 'center');
			var vpos = (editor.css('top') != 'auto') ? 'top' : ((editor.css('bottom') != 'auto') ? 'bottom' : 'center');
			editor.position({
				my: hpos + ' ' + vpos,
				at: hpos + ' ' + vpos,
				of: trigger
			});
			editor.keydown(function(e) {
				switch(e.which) {
					case 13: if(etype != 'textarea') { Bnd.commit(); } break;
					case 27: Bnd.close();  break;
				}
			});
			editor.mousedown(function(e) {
				e.stopPropagation();
			})

			if(opts.autocomplete) {
				editor.autocomplete({
					source: getAutocompleteSource(opts.autocomplete.source),
					minLength: opts.autocomplete.minLength || 2,
					create: function(event, ui) {
						editor.autocomplete('widget').mousedown(function(e) { e.stopPropagation(); });
					},
					select: function(event, ui) {
						if(ui.item) {
							Bnd.commit(ui.item.value);
						} else {
							Bnd.close();
						}
					}
				});
			}
			else if('combo' == etype) {
				editor.autocomplete({
					source: getAutocompleteSource(this.getEditorOptions(trigger)),
					delay: 0,
					minLength: 0,
					create: function(event, ui) {
						editor.autocomplete('widget').mousedown(function(e) { e.stopPropagation(); });
					},
					select: function(event, ui) {
						if(ui.item) {
							Bnd.commit(ui.item.value);
						} else {
							Bnd.close();
						}
					}
				});
				editor.autocomplete('search', '');
			}
			
			editor.focus();
		};
		
		var getAutocompleteSource = function(source) {
			if(source instanceof Array) {
				for(var i = 0; i < source.length; i++) {
					if(source[i] instanceof Array) {
						source[i] = {'label': source[i][0], 'value': source[i][1]};
					}
				}
			}
			return source;
		};

		this.getEditorOptions = function(element) {
			var type = this.getOptions(element).editor;
			if(type instanceof Object) {
				for(var key in type) {
					if(type.hasOwnProperty(key)) {
						return type[key] || {};
					}
				}
			}
			return {};
		}
		
		this.getEditorType = function(element) {
			var type = this.getOptions(element).editor;
			if(type == undefined || type == true) {
				return 'text';
			}
			if(type) {
				if(typeof type == 'string') {
					return type.toLowerCase();
				}
				if(type instanceof Object) {
					for(var key in type) {
						if(type.hasOwnProperty(key)) {
							return key;
						}
					}
				}
			}
			return 'none';
		}
		
		this.getEditorValue = function(element) {
			return convertToEditorValue(this.getValue(element), this.getOptions(element));
		}
		
		this.getField = function(element) {
			element = $(element);
			var attr = element.data('field');
			for(var key in attr) {
				if(attr.hasOwnProperty(key)) {
					return key;
				}
			}
			return attr;
		};
		
		this.getLabel = function(element) {
			return convertToLabel(this.getValue(element), this.getOptions(element));
		};

		this.getModel = function(element) {
			if( ! element || element == window) return null;
			element = $(element);
			if(element.length == 0) return null;
			if( ! element.attr('data-model')) {
				return this.getModel(element.closest('[data-model]'))
			}
			var model = element.data('model');
			if(model instanceof Oobium.Model) {
				return model;
			}
			else if(typeof model == 'string' || model instanceof String){
				var name = model;
				model = Oobium.vars[name];
				if(model instanceof Oobium.Model) {
					 return model;
				}
				if(model instanceof Object && model.type) {
					return Oobium.vars[name] = Oobium.Model.newInstance(model.type, model.data || model);
				}
			}
			else if(model instanceof Object && model.type) {
				return Oobium.Model.newInstance(model.type, model.data || model);
			}
			log('cannot create Oobium.Model for: ' + model);
			return null;
		};
		
		this.getOptions = function(element) {
			element = $(element);
			var attr = element.data('field');
			for(var key in attr) {
				if(attr.hasOwnProperty(key)) {
					return attr[key] || {};
				}
			}
			return {};
		};
	
		this.getValue = function(element) {
			return this.getModel(element).get(this.getField(element));
		}
		
		var h = function(s) {
			return s.toString().replace('&', '&amp;').replace('>', '&gt;').replace('<', '&lt;').replace('"', '&quot;');
		}

		var notEqual = function(v1, v2) {
			if(v1 instanceof Date) v1 = v1.getTime();
			if(v2 instanceof Date) v2 = v2.getTime();
			return (v1 != v2);
		}
		
		this.setLabel = function(element, label, overwrite) {
			element = $(element);
			if(overwrite == undefined) {
				overwrite = true;
			}
			if(this.getOptions(element).label == 'title') {
				if(overwrite || !element.attr('title')) {
					element.attr('title', label);
				}
			} else {
				if(overwrite || !element.html()) {
					element.html(label);
					if(label) {
						element.removeClass('empty');
					} else {
						element.addClass('empty');
					}
				}
			}
		};
		
		this.setValue = function(element, value) {
			var model = this.getModel(element);
			var field = this.getField(element);
			var opts = this.getOptions(element);
			model.set(field, convertToModelValue(model, field, value, options));
			model.update(field);
		}
		
		this.updateLabel = function(element, overwrite) {
			this.setLabel(element, this.getLabel(element), overwrite);
		};
		
	}
	
	Oobium.Model.prototype.bind = function(element) {
		Bnd.bind(element, this);
		return this;
	}
	
}).call(this);
