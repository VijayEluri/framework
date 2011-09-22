package org.oobium.build.runner;

import org.jboss.netty.handler.codec.http.websocket.WebSocketFrame;
import org.oobium.app.controllers.WebsocketController;
import org.oobium.build.runner.RunEvent.Type;
import org.oobium.build.workspace.Application;

public class RunnerController extends WebsocketController {

	public void handleMessage(WebSocketFrame frame) {
		if(hasParam("application")) {
			Application application = RunnerService.getRunningApp(param("application"));
			RunnerService.notifyListeners(Type.Updated, application);
		}
	};
	
}
