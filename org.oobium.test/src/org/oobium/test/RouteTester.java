package org.oobium.test;

import static org.jboss.netty.handler.codec.http.HttpMethod.*;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.HashMap;
import java.util.Map;

import org.jboss.netty.handler.codec.http.HttpMethod;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.oobium.app.AppService;
import org.oobium.app.ModuleService;
import org.oobium.app.controllers.HttpController;
import org.oobium.app.routing.AppRouter;
import org.oobium.app.routing.IPathRouting;
import org.oobium.app.routing.IUrlRouting;
import org.oobium.app.routing.Path;
import org.oobium.app.routing.RouteHandler;
import org.oobium.app.routing.Router;
import org.oobium.app.request.Request;
import org.oobium.app.http.Action;
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
		return getRoute(GET, "/") != null;
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
	
	private Class<? extends HttpController> getControllerClass(Class<?> modelClass) {
		String controllerName = modelClass.getSimpleName() + "Controller";
		Class<? extends HttpController> controllerClass = getControllerClass(config, controllerName, bundles.get(name));
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

	private Class<? extends HttpController> getControllerClass(Config config, String controllerName, Bundle bundle) {
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
			if(HttpController.class.isAssignableFrom(clazz)) {
				return clazz.asSubclass(HttpController.class);
			}
		} catch(ClassNotFoundException e) {
			// discard
		}
		return null;
	}

	public String getRoute(HttpMethod method, String path) {
		try {
			init();
		} catch(Exception e) {
			e.printStackTrace();
		}
		
		Request request = mockRequest(method, path);
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
		when(service.getControllerClass(any(Class.class))).thenAnswer(new Answer<Class<? extends HttpController>>() {
			@Override
			public Class<? extends HttpController> answer(InvocationOnMock invocation) throws Throwable {
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
	public Path pathTo(Class<? extends Model> modelClass) {
		return router.pathTo(modelClass);
	}

	@Override
	public Path pathTo(Class<? extends Model> modelClass, Action action) {
		return router.pathTo(modelClass, action);
	}

	@Override
	public Path pathTo(Model model) {
		return router.pathTo(model);
	}

	@Override
	public Path pathTo(Model model, Action action) {
		return router.pathTo(model, action);
	}

	@Override
	public Path pathTo(Model parent, String field) {
		return router.pathTo(parent, field);
	}

	@Override
	public Path pathTo(Model parent, String field, Action action) {
		return router.pathTo(parent, field, action);
	}

	@Override
	public Path pathTo(String routeName) {
		return router.pathTo(routeName);
	}

	@Override
	public Path pathTo(String routeName, Model model) {
		return router.pathTo(routeName, model);
	}

	@Override
	public Path pathTo(String routeName, Object... params) {
		return router.pathTo(routeName, params);
	}

	/**
	 * Create a mocked Request object for the given request type and full path.
	 * The returned mocked object can be used to stub out its method if necessary.
	 */
	public Request mockRequest(HttpMethod method, String fullPath) {
		String[] sa = fullPath.split("\\?");
		String path = sa[0];
		boolean hasParams = sa.length == 2;
		
		Request request = mock(Request.class);
		when(request.getPort()).thenReturn(router.getPort());
		when(request.getHost()).thenReturn(router.getHosts()[0]);
		when(request.getMethod()).thenReturn(method);
		when(request.getPath()).thenReturn(path);
		when(request.getUri()).thenReturn(fullPath);
		when(request.hasParameters()).thenReturn(hasParams);
		
		return request;
	}

	@Override
	public Path urlTo(Class<? extends Model> modelClass) {
		return router.urlTo(modelClass);
	}

	@Override
	public Path urlTo(Class<? extends Model> modelClass, Action action) {
		return router.urlTo(modelClass, action);
	}

	@Override
	public Path urlTo(Model model) {
		return router.urlTo(model);
	}

	@Override
	public Path urlTo(Model model, Action action) {
		return router.urlTo(model, action);
	}

	@Override
	public Path urlTo(Model parent, String field) {
		return router.urlTo(parent, field);
	}

	@Override
	public Path urlTo(Model parent, String field, Action action) {
		return router.urlTo(parent, field, action);
	}

	@Override
	public Path urlTo(String routeName) {
		return router.urlTo(routeName);
	}

	@Override
	public Path urlTo(String routeName, Model model) {
		return router.urlTo(routeName, model);
	}

	@Override
	public Path urlTo(String routeName, Object... params) {
		return router.urlTo(routeName, params);
	}
	
}
