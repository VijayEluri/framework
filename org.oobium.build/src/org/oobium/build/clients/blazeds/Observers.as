package org.oobium.persist {

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

		public static function addObserver(className:String, observer:Observer):void {
			if(className != null || observer != null) {
				if(observers[className]) {
					observers[className].push(observer);
				} else {
					observers[className] = [ observer ];
				}
			}
		}

		public static function onChannelAdded(channelName:String):void {
			if(!channels[channelName]) {
				var consumer:Consumer = new Consumer();
				consumer.destination = channelName;
				
				var channelSet:ChannelSet = new ChannelSet();
				channelSet.addChannel(new AMFChannel("my-polling-amf", "{serverUrl}messagebroker/amfpolling"));
				
				consumer.channelSet = channelSet;
				consumer.addEventListener(MessageEvent.MESSAGE, onChannelEvent);
				consumer.addEventListener(MessageFaultEvent.FAULT, onChannelError);
				consumer.subscribe();	

				channels[channelName] = consumer;
			}
		}

		private static function onChannelError(event:MessageFaultEvent):void {
			// TODO
		}

		private static function onChannelEvent(event:ResultEvent):void {
			Alert.show(event.result as String);
		}

	}

}