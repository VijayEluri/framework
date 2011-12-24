package org.oobium.app.server;

import static org.oobium.utils.coercion.TypeCoercer.coerce;

import org.oobium.app.handlers.HttpRequest404Handler;
import org.oobium.app.handlers.HttpRequest500Handler;
import org.oobium.app.handlers.HttpRequestHandler;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import org.osgi.util.tracker.ServiceTracker;
import org.osgi.util.tracker.ServiceTrackerCustomizer;

@SuppressWarnings("rawtypes")
public class RequestHandlerTrackers {

	private ServiceTracker requestHandlerTracker;
	private ServiceTracker request404HandlerTracker;
	private ServiceTracker request500HandlerTracker;

	public void close() {
		requestHandlerTracker.close();
		requestHandlerTracker = null;
		request404HandlerTracker.close();
		request404HandlerTracker = null;
		request500HandlerTracker.close();
		request500HandlerTracker = null;
	}
	
	@SuppressWarnings("unchecked")
	public void open(final Server appServer, final BundleContext context, final RequestHandlers handlers) {
		requestHandlerTracker = new ServiceTracker(context, HttpRequestHandler.class.getName(), new ServiceTrackerCustomizer() {
			public Object addingService(ServiceReference reference) {
				return appServer.addHandler((HttpRequestHandler) context.getService(reference));
			}
			public void modifiedService(ServiceReference reference, Object service) {
				// nothing to do
			}
			public void removedService(ServiceReference reference, Object service) {
				appServer.removeHandler((HttpRequestHandler) service);
			}
		});
		requestHandlerTracker.open();
		
		request404HandlerTracker = new ServiceTracker(context, HttpRequest404Handler.class.getName(), new ServiceTrackerCustomizer() {
			public Object addingService(ServiceReference reference) {
				return handlers.add404Handler((HttpRequest404Handler) context.getService(reference));
			}
			public void modifiedService(ServiceReference reference, Object service) {
				// nothing to do
			}
			public void removedService(ServiceReference reference, Object service) {
				handlers.remove404Handler((HttpRequest404Handler) service);
			}
		});
		request404HandlerTracker.open();
		
		request500HandlerTracker = new ServiceTracker(context, HttpRequest500Handler.class.getName(), new ServiceTrackerCustomizer() {
			public Object addingService(ServiceReference reference) {
				int port = coerce(reference.getProperty("port"), -1);
				return handlers.add500Handler((HttpRequest500Handler) context.getService(reference));
			}
			public void modifiedService(ServiceReference reference, Object service) {
				// nothing to do
			}
			public void removedService(ServiceReference reference, Object service) {
				int port = coerce(reference.getProperty("port"), -1);
				handlers.remove500Handler((HttpRequest500Handler) service);
			}
		});
		request500HandlerTracker.open();
	}
	
}
