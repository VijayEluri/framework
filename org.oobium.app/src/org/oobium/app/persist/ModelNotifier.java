package org.oobium.app.persist;

import static org.oobium.app.http.Action.*;
import static org.oobium.utils.StringUtils.join;

import java.util.Set;

import org.oobium.app.AppService;
import org.oobium.app.http.Action;
import org.oobium.app.routing.Router;
import org.oobium.app.server.Websocket;
import org.oobium.persist.Model;
import org.oobium.persist.Observer;

public abstract class ModelNotifier<T extends Model> extends Observer<T> {

	@Override
	protected void afterCreate(T model) {
		sendNotification(model.getId(), null, create);
	}

	@Override
	protected void afterUpdate(T model) {
		sendNotification(model.getId(), model.getAll().keySet(), update);
	}

	@Override
	protected void afterDestroy(Object id) {
		sendNotification(id, null, destroy);
	}

	/**
	 * Select whether the given WebSocket and Action will pass through the filter
	 * and be notified.
	 * @param socket the WebSocket to be notified
	 * @param action the Action which caused the notification
	 * @return true if the socket should be notified; false otherwise.
	 */
	protected abstract boolean select(Websocket socket, Action action);
	
	private void sendNotification(Object id, Set<String> fields, Action action) {
		AppService app = AppService.get();
		Router router = app.getRouter();
		Set<Websocket> sockets = router.getModelNotifiers();
		for(Websocket socket : sockets) {
			if(select(socket, action)) {
				String data = modelClass.getName() + ":" + id;
				switch(action) {
				case create:
					socket.write("CREATED " + data);
					break;
				case update:
					socket.write("UPDATED " + data + "-" + join(fields, ','));
					break;
				case destroy:
					socket.write("DESTROYED " + data);
					break;
				default:
					logger.warn("invalid action: " + action + "; only create, update, and destroy are allowed");
					return; // stop notifications for this action
				}
			}
		}
	}
	
}
