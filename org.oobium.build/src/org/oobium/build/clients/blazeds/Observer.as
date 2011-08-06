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

		/**
		 * create - want the object
		 * update - if in models list, want object, else want id
		 * destroy - only id available
		 */
		internal function exec(id:int):void {
			if(models == null || models.length == 0) {
				switch(method) {
				case "afterCreate":
					execFinder(id);
					break;
				case "afterUpdate":
				case "afterDestroy":
					callback(id);
					break;
				default:
					trace("unknown method: " + method);
				}
			} else {
				if(contains(id)) {
					switch(method) {
					case "afterCreate":
						// does this make sense?
						//   maybe you want notification when the 10th model is created...?
						execFinder(id);
						break;
					case "afterUpdate":
						execFinder(id);
						break;
					case "afterDestroy":
						callback(id);
						break;
					default:
						trace("unknown method: " + method);
					}
				}
			}
		}
		
		private function execFinder(id:int):void {
			var modelClass:Class = getDefinitionByName(className) as Class;
			modelClass['find'](id, function(result:RemoteResult):void {
				callback(result.model);
			});
		}
		
		public function forModels(... models):void {
			this.models = models;
		}
		
		private function contains(id:int):Boolean {
			for each(var model:Object in models) {
				if(model is int) {
					if((model as int) == id) return true;
				} else {
					if(model.id == id) return true;
				}
			}
			return false;
		}

	}

}