package org.oobium.persist {

	import flash.utils.getDefinitionByName;
	
	public class Observer {

		private var className:String;
		private var method:String;
		private var callback:Function;

		public var models:Array;
		public var includes:String;
		
		function Observer(className:String, method:String, callback:Function, includes:String = null) {
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
			switch(method) {
			case "afterCreate":
				execFinder(id);
				break;
			case "afterUpdate":
				if(models == null || models.length == 0) {
					callback(id);
				} else if(contains(id)) {
					execFinder(id);
				} // else: we're watching models, but not this one - exit
				break;
			case "afterDestroy":
				if(models == null || models.length == 0 || contains(id)) {
					callback(id);
				}
				break;
			default:
				trace("unknown method: " + method);
			}
		}
		
		private function execFinder(id:int):void {
			var modelClass:Class = getDefinitionByName(className) as Class;
			var handler:Function = function(result:RemoteResult):void { callback(result.model); }
			if(includes == null) {
				modelClass['find'](id, handler);
			} else {
				modelClass['find']("where id=" + id + " include:" + includes, handler);
			}
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