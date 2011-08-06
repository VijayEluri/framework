package org.oobium.persist {

	import flash.utils.getDefinitionByName;
	
	public class Observer {

		private var className:String;
		private var method:String;
		private var callback:Function;

		public var models:Array;
		
		function Observer(className:String, method:String, callback:Function) {
			this.className = className;
			this.method = method;
			this.callback = callback;
		}

		internal function exec(id:int):void {
			if(models == null || models.length == 0) {
				callback(id);
			} else {
				var model:Object = find(id);
				if(model != null) {
					if('afterDestroy' == method) {
						callback(id);
					}
					else if('afterUpdate' == method) {
						var modelClass:Class = getDefinitionByName(className) as Class;
						modelClass['find'](id, callback);
					}
				}
			}
		}
		
		public function forModels(... models):void {
			this.models = models;
		}
		
		private function find(id:int):Object {
			for each(var model:Object in models) {
				if(model is int) {
					if((model as int) == id) return model;
				}else {
					if(model.id == id) return model;
				}
			}
			return null;
		}

	}

}