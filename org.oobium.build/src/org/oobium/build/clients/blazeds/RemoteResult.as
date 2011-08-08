package org.oobium.persist {

	import mx.collections.ArrayCollection;
	import mx.rpc.events.ResultEvent;

	public dynamic class RemoteResult {

		public var event:ResultEvent;
		
		public var caller:Object;
		public var model:Object;
		public var models:ArrayCollection;
		public var length:int;
		
		public function RemoteResult(event:ResultEvent, caller:Object = null) {
			this.event = event;
			this.caller = caller;
			if(event.result is ArrayCollection) {
				models = event.result as ArrayCollection;
				length = models.length;
				if(length > 0) {
					model = models[0];
				}
			} else {
				model = event.result;
				models = new ArrayCollection([event.result]);
				length = 1;
			}
		}

		public function first():Object {
			return (length > 0) ? models[0] : null; 
		}

		public function isEmpty():Boolean {
			return length == 0;
		}
		
		public function last():Object {
			return (length > 0) ? models[length-1] : null;
		}
		
	}

}
