package org.oobium.persist {

	import com.test.ws.models.RemoteClient;
	
	import mx.controls.Alert;
	import mx.messaging.ChannelSet;
	import mx.messaging.Consumer;
	import mx.messaging.channels.AMFChannel;
	import mx.messaging.events.MessageEvent;
	import mx.messaging.events.MessageFaultEvent;
	import mx.rpc.events.ResultEvent;

	[RemoteClass(alias="org.oobium.persist.Observers")]

	public class Observers {

		private static var channels:Object = {};
		private static var observers:Object = {};

		private static function key(className:String, method:String):String {
			return className + "::" + method;
		}
		
		public static function addObserver(className:String, method:String, callback:Function):void {
			if(className != null && method != null && callback != null) {
				var key:String = key(className, method);
				if(observers[key]) {
					observers[key].push(callback);
				} else {
					observers[key] = [ callback ];
				}
			}
		}

		public static function onChannelAdded(event:ResultEvent):void {
			var channelName:String = event.result as String;
			if(!channels[channelName]) {
				var consumer:Consumer = new Consumer();
				consumer.destination = channelName;
				
				var channelSet:ChannelSet = new ChannelSet();
				channelSet.addChannel(new AMFChannel("my-polling-amf", "{serverUrl}/messagebroker/amfpolling"));
				
				consumer.channelSet = channelSet;
				consumer.addEventListener(MessageEvent.MESSAGE, onChannelEvent);
				consumer.addEventListener(MessageFaultEvent.FAULT, onChannelError);
				consumer.subscribe();

				channels[channelName] = consumer;
			}
		}

		public static function onChannelError(event:MessageFaultEvent):void {
			Alert.show("onChannelError");
		}

		public static function onChannelEvent(event:MessageEvent):void {
			var className:String = event.message.headers['class'];
			var method:String = event.message.headers['method'];
			var key:String = key(className, method);
			if(observers[key]) {
				var id:int = event.message.headers['id'];
				for each(var f:Function in observers[key]) {
					f(id);
				}
			}
		}

	}

}