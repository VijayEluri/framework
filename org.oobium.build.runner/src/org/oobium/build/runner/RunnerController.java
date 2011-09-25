package org.oobium.build.runner;

import static org.oobium.build.runner.RunnerService.getRunningApp;
import static org.oobium.build.runner.RunnerService.notifyListeners;
import static org.oobium.utils.StringUtils.camelCase;
import static org.oobium.utils.json.JsonUtils.toMap;

import java.util.HashMap;
import java.util.Map;

import org.jboss.netty.handler.codec.http.websocket.WebSocketFrame;
import org.oobium.app.controllers.WebsocketController;
import org.oobium.build.runner.RunEvent.Type;

public class RunnerController extends WebsocketController {

	public void handleMessage(WebSocketFrame frame) {
		String framedata = frame.getTextData();
		logger.debug("message received: {}", framedata);
		
		Map<String, Object> event = toMap(framedata);
		String eventType = camelCase((String) event.get("event"));
		switch(Type.valueOf(eventType)) {
		case Open:
			Map<?,?> data = (HashMap<?,?>) event.get("data");
			notifyListeners(Type.Open, getRunningApp(getId()), (String) data.get("type"), data.get("line"));
			break;
		case Updated:
			notifyListeners(Type.Updated, getRunningApp(getId()));
			break;
		}
	};
	
}
