package org.oobium.test;

import static org.mockito.Matchers.*;
import static org.mockito.Mockito.*;

import java.util.HashMap;
import java.util.Map;

import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.oobium.app.AppService;
import org.oobium.app.ModuleService;
import org.oobium.app.server.controller.Action;
import org.oobium.app.server.controller.Controller;
import org.oobium.app.server.routing.AppRouter;
import org.oobium.app.server.routing.IPathRouting;
import org.oobium.app.server.routing.IUrlRouting;
import org.oobium.app.server.routing.RouteHandler;
import org.oobium.app.server.routing.Router;
import org.oobium.http.HttpRequest;
import org.oobium.http.constants.RequestType;
import org.oobium.logging.Logger;
import org.oobium.persist.Model;
import org.oobium.utils.Config;
import org.oobium.utils.Config.Mode;
import org.osgi.framework.Bundle;

public class RouteTester implements IPathRouting, IUrlRouting {

	private final String name;
	private final Class<? extends ModuleService> clazz;
	private final Config config;
	private AppRouter router;
	private Map<String, Bundle> bundles;
	
	public RouteTester(Class<? extends ModuleService> clazz) throws Exception {
		this(clazz.getPackage().getName(), clazz, null);
	}

	public RouteTester(String name, Class<? extends ModuleService> clazz) throws Exception {
		this(name, clazz, null);
	}

	public RouteTester(String name, Class<? extends ModuleService> clazz, Mode mode) {
		this.name = name;
		this.clazz = clazz;
		this.config = Config.loadConfiguration(clazz, mode);
		bundles = new HashMap<String, Bundle>();
		addBundle(name, clazz);
	}
	
	public void addBundle(String name, final Class<? extends ModuleService> clazz) {
		try {
			Bundle bundle = mock(Bundle.class);
			when(bundle.loadClass(any(String.class))).thenAnswer(new Answer<Class<?>>() {
				@Override
				public Class<?> answer(InvocationOnMock invocation) throws Throwable {
					String arg = (String) invocation.getArguments()[0];
					return clazz.getClassLoader().loadClass(arg);
				}
			});
			bundles.put(name, bundle);
		} catch(ClassNotFoundException e) {
			e.printStackTrace();
		}
	}
	
	public boolean hasHome() {
		return getRoute(RequestType.GET, "/") != null;
	}

	private Bundle getBundle(String module) {
		Bundle bundle = bundles.get(module);
		if(bundle != null) {
			return bundle;
		}

		int ix = module.lastIndexOf('_');
		if(ix == -1) {
			for(String key : bundles.keySet()) {
				ix = key.lastIndexOf('_');
				if(ix != -1 && module.equals(key.substring(0, ix))) {
					return bundles.get(key);
				}
			}
		} else {
			String name = module.substring(0, ix);
			String version = module.substring(ix+1);
			for(String key : bundles.keySet()) {
				ix = key.lastIndexOf('_');
				if(ix == -1) {
					// TODO
				}
			}
		}
		
		return null;
	}
	
	private Class<? extends Controller> getControllerClass(Class<?> modelClass) {
		String controllerName = modelClass.getSimpleName() + "Controller";
		Class<? extends Controller> controllerClass = getControllerClass(config, controllerName, bundles.get(name));
		if(controllerClass != null) {
			return controllerClass;
		}
		for(String module : config.getModules()) {
			Bundle bundle = getBundle(module);
			if(bundle != null) {
				controllerClass = getControllerClass(config, controllerName, bundle);
				if(controllerClass != null) {
					return controllerClass;
				}
			}
		}
		return null;
	}

	private Class<? extends Controller> getControllerClass(Config config, String controllerName, Bundle bundle) {
		String base;
		int ix = name.lastIndexOf('_');
		if(ix == -1) {
			base = name;
		} else {
			base = name.substring(0, ix);
		}
		String name = config.getPathToControllers(base).replace('/', '.') + "." + controllerName;
		try {
			Class<?> clazz = bundle.loadClass(name);
			if(Controller.class.isAssignableFrom(clazz)) {
				return clazz.asSubclass(Controller.class);
			}
		} catch(ClassNotFoundException e) {
			// discard
		}
		return null;
	}

	public String getRoute(RequestType requestType, String path) {
		try {
			init();
		} catch(Exception e) {
			e.printStackTrace();
		}
		
		HttpRequest request = mockRequest(requestType, path);
		RouteHandler handler = router.getHandler(request);
		if(handler != null) {
			return handler.toString();
		}
		return null;
	}

	@SuppressWarnings("unchecked")
	private void init() throws Exception {
		Logger logger = mock(Logger.class);
		AppService service = mock(AppService.class);
		when(service.getLogger()).thenReturn(logger);
		when(service.getControllerClass(any(Class.class))).thenAnswer(new Answer<Class<? extends Controller>>() {
			@Override
			public Class<? extends Controller> answer(InvocationOnMock invocation) throws Throwable {
				return getControllerClass((Class<?>) invocation.getArguments()[0]);
			}
		});
		if(AppService.class.isAssignableFrom(clazz)) {
			router = new AppRouter(service, config.getHosts(), config.getPort());
			AppService tmp = (AppService) clazz.newInstance();
			try {
				clazz.getDeclaredMethod("addRoutes", Config.class, AppRouter.class);
				tmp.addRoutes(config, router);
			} catch(NoSuchMethodException e) {
				clazz.getDeclaredMethod("addRoutes", Config.class, Router.class);
				tmp.addRoutes(config, (Router) router);
			}
		} else {
			router = new AppRouter(null, "nohost", -1);
			ModuleService tmp = clazz.newInstance();
			clazz.getDeclaredMethod("addRoutes", Config.class, Router.class);
			tmp.addRoutes(config, (Router) router);
		}
	}

	@Override
	public String pathTo(Class<? extends Model> modelClass) {
		return router.pathTo(modelClass);
	}

	@Override
	public String pathTo(Class<? extends Model> modelClass, Action action) {
		return router.pathTo(modelClass, action);
	}

	@Override
	public String pathTo(Model model) {
		return router.pathTo(model);
	}

	@Override
	public String pathTo(Model model, Action action) {
		return router.pathTo(model, action);
	}

	@Override
	public String pathTo(Model parent, String field) {
		return router.pathTo(parent, field);
	}

	@Override
	public String pathTo(Model parent, String field, Action action) {
		return router.pathTo(parent, field, action);
	}

	@Override
	public String pathTo(String routeName) {
		return router.pathTo(routeName);
	}

	@Override
	public String pathTo(String routeName, Model model) {
		return router.pathTo(routeName, model);
	}

	@Override
	public String pathTo(String routeName, Object... params) {
		return router.pathTo(routeName, params);
	}

	/**
	 * Create a mocked Request object for the given request type and full path.
	 * The returned mocked object can be used to stub out its method if necessary.
	 */
	public HttpRequest mockRequest(RequestType type, String fullPath) {
		String[] sa = fullPath.split("\\?");
		String path = sa[0];
		boolean hasParams = sa.length == 2;
		
		HttpRequest request = mock(HttpRequest.class);
		when(request.getPort()).thenReturn(router.getPort());
		when(request.getHost()).thenReturn(router.getHosts()[0]);
		when(request.getType()).thenReturn(type);
		when(request.getPath()).thenReturn(path);
		when(request.getFullPath()).thenReturn(fullPath);
		when(request.hasParameters()).thenReturn(hasParams);
		
		return request;
	}

	@Override
	public String urlTo(Class<? extends Model> modelClass) {
		return router.urlTo(modelClass);
	}

	@Override
	public String urlTo(Class<? extends Model> modelClass, Action action) {
		return router.urlTo(modelClass, action);
	}

	@Override
	public String urlTo(Model model) {
		return router.urlTo(model);
	}

	@Override
	public String urlTo(Model model, Action action) {
		return router.urlTo(model, action);
	}

	@Override
	public String urlTo(Model parent, String field) {
		return router.urlTo(parent, field);
	}

	@Override
	public String urlTo(Model parent, String field, Action action) {
		return router.urlTo(parent, field, action);
	}

	@Override
	public String urlTo(String routeName) {
		return router.urlTo(routeName);
	}

	@Override
	public String urlTo(String routeName, Model model) {
		return router.urlTo(routeName, model);
	}

	@Override
	public String urlTo(String routeName, Object... params) {
		return router.urlTo(routeName, params);
	}
	
}
