package org.oobium.test;

import static org.oobium.utils.literal.*;
import static org.oobium.utils.coercion.TypeCoercer.*;
import static org.oobium.app.server.controller.Action.*;
import static org.oobium.http.constants.Header.ACCEPT;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.*;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.atomic.AtomicInteger;

import org.mockito.ArgumentCaptor;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.oobium.app.AppService;
import org.oobium.app.server.controller.Action;
import org.oobium.app.server.controller.Controller;
import org.oobium.app.server.response.Response;
import org.oobium.app.server.routing.AppRouter;
import org.oobium.app.server.routing.Router;
import org.oobium.app.server.view.View;
import org.oobium.http.HttpCookie;
import org.oobium.http.HttpRequest;
import org.oobium.http.HttpSession;
import org.oobium.http.constants.ContentType;
import org.oobium.http.constants.Header;
import org.oobium.http.constants.RequestType;
import org.oobium.http.constants.StatusCode;
import org.oobium.http.impl.Cookie;
import org.oobium.http.impl.Headers;
import org.oobium.logging.Logger;
import org.oobium.persist.Model;
import org.oobium.utils.Base64;
import org.oobium.utils.json.JsonUtils;

public class ControllerTester {

	private static void check(Class<?> clazz) {
		int modifiers = clazz.getModifiers();
		if(Modifier.isAbstract(modifiers)) {
			throw new IllegalArgumentException(clazz.getCanonicalName() + " cannot be routed because it is an abstract class");
		}
		if(Modifier.isPrivate(modifiers)) {
			throw new IllegalArgumentException(clazz.getCanonicalName() + " cannot be routed because it is a private class");
		}
		try {
			clazz.getConstructor();
		} catch(SecurityException e) {
			throw new IllegalArgumentException(clazz.getCanonicalName() + " cannot be routed", e);
		} catch(NoSuchMethodException e) {
			throw new IllegalArgumentException(clazz.getCanonicalName() + " cannot be routed because it does not have a no-args constructor");
		}
	}


	public final Class<? extends Controller> controllerClass;
	public final Logger logger;
	public final AppService application;
	public final AppRouter router;
	public final TestPersistService persistor;
	private final AtomicInteger persistorSessionCount;
	
	public String host;
	public int port;

	public Controller controller;
	private Headers headers;
	private HttpSession session;
	private Map<String, HttpCookie> cookies;
	private boolean authorize;
	
	private Map<String, Object> flashOut;

	/**
	 * @param controllerClass the class of Controller to be tested
	 * @throws IllegalArgumentException if the given class is abstract, private, or does not have a no-args constructor
	 */
	public ControllerTester(Class<? extends Controller> controllerClass) {
		check(controllerClass);
		this.controllerClass = controllerClass;
		this.router = mockRouter();

		logger = spy(Logger.getLogger());
		logger.setConsoleLevel(Logger.INFO);
		this.application = mockService(logger, router);
		Model.setLogger(logger);
		
		this.persistor = TestPersistService.useSimple();
		this.persistorSessionCount = new AtomicInteger();
		
		this.host = "testhost";
		this.port = 0;
		
		accept(ContentType.HTML);
	}

	public ControllerTester accept(ContentType...types) {
		if(types.length > 0) {
			set(Header.ACCEPT, types[0].getRequestProperty());
			for(int i = 1; i < types.length; i++) {
				add(Header.ACCEPT, types[i].getRequestProperty());
			}
		}
		return this;
	}
	
	public ControllerTester add(Header header, String value) {
		if(headers == null) {
			headers = new Headers();
		}
		headers.add(header, value);
		return this;
	}
	
	public ControllerTester authorize() {
		return authorize(true);
	}
	
	public ControllerTester authorize(boolean force) {
		this.authorize = force;
		if(force) {
			authorize("name", "password");
		} else {
			set(Header.AUTHORIZATION, null);
		}
		return this;
	}
	
	public ControllerTester authorize(String name, String password) {
		set(Header.AUTHORIZATION, "Basic " + Base64.encode(name + ":" + password));
		return this;
	}
	
	public ControllerTester clearCookie(String name) {
		if(cookies != null) {
			cookies.remove(name);
			if(cookies.isEmpty()) {
				cookies = null;
			}
		}
		return this;
	}
	
	public ControllerTester clearCookies() {
		if(cookies != null) {
			cookies.clear();
			cookies = null;
		}
		return this;
	}
	
	public ControllerTester clearHeaders() {
		headers = null;
		return this;
	}
	
	private Map<String, Object> convert(Map<String, ?> params) {
		final Map<String, Object> parameters = new LinkedHashMap<String, Object>();
		try {
			for(Entry<String, ?> entry : params.entrySet()) {
				String key = entry.getKey();
				Object val = entry.getValue();

				if(val instanceof File) {
					File file = (File) val;
					val = new FileInputStream(file);
					if(!params.containsKey("filename")) {
						parameters.put("filename", file.getName());
					}
				}
				if(val instanceof URL) {
					URL url = (URL) val;
					val = url.openStream();
					if(!params.containsKey("filename")) {
						String name = url.getPath();
						parameters.put("filename", name.substring(name.lastIndexOf('/')+1));
					}
				}
				if(val instanceof InputStream) {
					InputStream in = (InputStream) val;
					ByteArrayOutputStream out = new ByteArrayOutputStream(200*1024);
					
					byte[] bytes = new byte[1024];
					int read;
					while((read = in.read(bytes)) > 0) {
						out.write(bytes, 0, read);
					}
					
					val = out.toByteArray();
				}
				
				parameters.put(key, val);
			}
		} catch(Exception e) {
			throw new RuntimeException(e.getMessage(), e);
		}
		return parameters;
	}

	public ControllerTester execute(Action action) {
		return execute(action, null, null);
	}

	public ControllerTester execute(Action action, HttpRequest request) {
		try {
			persistor.openSession("test session " + persistorSessionCount.incrementAndGet());
			Model.setPersistService(persistor);
			controller = mockController();
			controller.initialize(router, request, null);
			controller.execute(action);
		} catch(RuntimeException e) {
			throw e;
		} catch(Exception e) {
			throw new RuntimeException(e.getMessage(), e);
		}
		return this;
	}
	
	public ControllerTester execute(Action action, int id) {
		return execute(action, Map("id", 1), null);
	}
	
	public ControllerTester execute(Action action, Map<String, Object> params) {
		return execute(action, params, null);
	}
	
	public ControllerTester execute(Action action, Map<String, Object> params, Map<String, Object> flash) {
		if(flash != null) {
			setCookie(Controller.FLASH_KEY, JsonUtils.toJson(flash));
		}
		HttpRequest request = mockRequest(action.getRequestType(), params);
		return execute(action, request);
	}
	
	public ControllerTester execute(Action action, Object...params) {
		Map<String, Object> map = new LinkedHashMap<String, Object>();
		for(int i = 0; i < params.length - 1; i++) {
			String key = (params[i] != null) ? (String) params[i] : null;
			Object val = (++i < params.length) ? params[i] : null;
			map.put(key, val);
		}
		return execute(action, map, null);
	}
	
	public ControllerTester execute(HttpRequest request) {
		return execute(null, request);
	}
	
	public ControllerTester execute(RequestType type) {
		return execute(type, null, null);
	}
	
	public ControllerTester execute(RequestType type, Map<String, ?> params) {
		return execute(type, params, null);
	}
	
	public ControllerTester execute(RequestType type, Map<String, ?> params, Map<String, ?> flash) {
		if(flash != null) {
			setCookie(Controller.FLASH_KEY, JsonUtils.toJson(flash));
		}
		HttpRequest request = mockRequest(type, params);
		return execute(request);
	}
	
	/**
	 * Retrieve the argument with the given name that was assigned to the rendered
	 * view. The name should be the name of the field in the view. For example:
	 * for the view constructor ShowPost(Post post, boolean first), this method would retrieve the
	 * second argument like so: getArg("first"). Of course, this only works if the ShowPost
	 * view was actually rendered, otherwise an AssertionError is thrown.
	 * @param name
	 * @return the argument passed into the rendered view
	 * @throws AssertionError
	 */
	public Object getArg(String name) {
		View view = getView();
		if(view != null) {
			try {
				Field field = view.getClass().getDeclaredField(name);
				field.setAccessible(true);
				return field.get(view);
			} catch(RuntimeException e) {
				throw e;
			} catch(Exception e) {
				throw new RuntimeException(e.getMessage(), e);
			}
		}
		return null;
	}
	
	public View getChild() {
		View view = getView();
		if(view != null) {
			return view.getChild();
		}
		return null;
	}
	
	public String getContent() {
		return controller.getResponse().getBody();
	}
	
	public ContentType getContentType() {
		return controller.getResponse().getContentType();
	}

	public String getError() {
		return getFlash(Controller.FLASH_ERROR, String.class);
	}
	
	public String getError(String name) {
		String errors = null;
		if(controller.wantsHtml()) {
			errors = getError();
		} else {
			errors = getContent();
		}
		if(errors == null) {
			return null;
		}
		Object o = JsonUtils.toMap(errors).get(name);
		if(o instanceof List<?>) {
			o = ((List<?>) o).get(0);
		}
		if(o == null) {
			return null;
		}
		return String.valueOf(o);
	}
	
	public int getErrorCount() {
		String errors = null;
		if(controller.wantsHtml()) {
			errors = getError();
		} else {
			errors = getContent();
		}
		if(errors == null) {
			return 0;
		}
		Map<?,?> map = JsonUtils.toMap(errors);
		int count = 0;
		for(Object name : map.keySet()) {
			Object val = map.get(name);
			if(val instanceof List<?>) {
				count += ((List<?>) val).size();
			} else {
				count++;
			}
		}
		return count;
	}
	
	public String[] getErrors() {
		String errors = null;
		if(controller.wantsHtml()) {
			errors = getError();
		} else {
			errors = getContent();
		}
		if(errors == null) {
			return null;
		}
		Map<?,?> map = JsonUtils.toMap(errors);
		List<String> list = new ArrayList<String>();
		for(Object name : map.keySet()) {
			Object val = map.get(name);
			if(val instanceof List<?>) {
				for(Object o : (List<?>) val) {
					list.add(name + " " + o);
				}
			} else {
				list.add(name + " " + val);
			}
		}
		return list.toArray(new String[list.size()]);
	}
	
	public String[] getErrors(String name) {
		String errors = null;
		if(controller.wantsHtml()) {
			errors = getError();
		} else {
			errors = getContent();
		}
		if(errors == null) {
			return null;
		}
		Object o = JsonUtils.toMap(errors).get(name);
		if(o instanceof List<?>) {
			List<?> list = (List<?>) o;
			String[] sa = new String[list.size()];
			for(int i = 0; i < sa.length; i++) {
				sa[i] = String.valueOf(list.get(i));
			}
			return sa;
		}
		if(o == null) {
			return null;
		}
		return new String[] { String.valueOf(o) };
	}
	
	@SuppressWarnings("unchecked")
	public Map<?,?> getFlash() {
		if(flashOut == null) {
			try {
				Field field = Controller.class.getDeclaredField("flashOut");
				field.setAccessible(true);
				flashOut = (Map<String, Object>) field.get(controller);
				if(flashOut == null) {
					flashOut = new HashMap<String, Object>(0);
				}
			} catch(Exception e) {
				throw new UnsupportedOperationException("getFlash method has failed", e);
			}
		}
		return flashOut;
	}
	
	public Object getFlash(String name) {
		return getFlash().get(name);
	}
	
	public <T> T getFlash(String name, Class<T> type) {
		return coerce(getFlash(name), type);
	}

	
	
	public Headers getHeaders() {
		return controller.getResponse().getHeaders();
	}

	public View getLayout() {
		View view = getView();
		if(view != null) {
			return view.getLayout();
		}
		return null;
	}

	public String getLocation() {
		return controller.getResponse().getHeaders().get(Header.LOCATION);
	}
	
	/**
	 * Retrieve the argument with the given name that was assigned to the rendered
	 * view. The name should be the name of the field in the view. For example:
	 * for the view constructor ShowPost(Post post), this method would retrieve the
	 * post model like so: getArg("post"). Of course, this only works if the ShowPost
	 * view was actually rendered, otherwise an AssertionError is thrown.
	 * @param name
	 * @return the model
	 * @throws AssertionError
	 */
	public Model getModel(String name) {
		return (Model) getArg(name);
	}
	
	public String getNotice() {
		return getFlash(Controller.FLASH_NOTICE, String.class);
	}

	public HttpSession getSession() {
		return session;
	}
	
	public StatusCode getStatus() {
		Response response = controller.getResponse();
		if(response != null) {
			return response.getStatus();
		}
		return StatusCode.NOT_FOUND;
	}
	
	public int getStatusCode() {
		return getStatus().getCode();
	}
	
	public View getView() {
		ArgumentCaptor<View> argument = ArgumentCaptor.forClass(View.class);
		verify(controller).render(argument.capture(), anyBoolean());
		return argument.getValue();
	}
	
	public <T extends View> T getView(Class<T> viewClass) {
		View view = getView();
		if(viewClass.isAssignableFrom(view.getClass())) {
			return viewClass.cast(view);
		}
		return null;
	}
	
	public String getWarning() {
		return getFlash(Controller.FLASH_WARNING, String.class);
	}
	
	public boolean hasError() {
		return getFlash(Controller.FLASH_ERROR) != null;
	}
	
	public boolean hasNotice() {
		return getFlash(Controller.FLASH_NOTICE) != null;
	}
	
	public boolean hasWarning() {
		return getFlash(Controller.FLASH_WARNING) != null;
	}
	
	public boolean isPartial() {
		ArgumentCaptor<Boolean> argument = ArgumentCaptor.forClass(Boolean.class);
		verify(controller).render(any(View.class), argument.capture());
		return argument.getValue();
	}

	public boolean isRendered() {
		return controller.isRendered();
	}

	public boolean isSuccess() {
		return getStatus().isSuccess();
	}

	private Controller mockController() throws Exception {
		Controller controller = spy(controllerClass.newInstance());

		if(authorize) {
			doReturn(true).when(controller).authorize(anyString(), anyString());
		}
		
		return controller;
	}
	
	/**
	 * Create a mocked Request object for the given request type and full path.
	 * The returned mocked object can be used to stub out its method if necessary.
	 */
	private HttpRequest mockRequest(RequestType type, Map<String, ?> params) {
		HttpRequest request = mock(HttpRequest.class);
		
		when(request.getHandler()).thenReturn(application);
		when(request.getHost()).thenAnswer(new Answer<String>() {
			@Override
			public String answer(InvocationOnMock invocation) throws Throwable {
				return host;
			}
		});
		when(request.getPort()).thenAnswer(new Answer<Integer>() {
			@Override
			public Integer answer(InvocationOnMock invocation) throws Throwable {
				return port;
			}
		});
		when(request.getType()).thenReturn(type);
		
		if(headers != null) {
			when(request.getHeader(any(Header.class))).thenAnswer(new Answer<String>() {
				@Override
				public String answer(InvocationOnMock invocation) throws Throwable {
					return headers.get((Header) invocation.getArguments()[0]);
				}
			});
			when(request.getHeaders()).thenReturn(headers.toArray());
			if(headers.has(Header.ACCEPT)) {
				when(request.getContentTypes()).thenAnswer(new Answer<ContentType[]>() {
					@Override
					public ContentType[] answer(InvocationOnMock invocation) throws Throwable {
						String str = headers.get(ACCEPT);
						if(str != null) {
							return ContentType.getAll(str.split(",")[0].trim());
						}
						return new ContentType[0];
					}
				});
			}
		}
//		when(request.getPath()).thenReturn(path);
//		when(request.getFullPath()).thenReturn(fullPath);

		if(params != null && !params.isEmpty()) {
			final Map<String, Object> parameters = convert(params);
			when(request.getParameter(anyString())).thenAnswer(new Answer<Object>() {
				@Override
				public Object answer(InvocationOnMock invocation) throws Throwable {
					return parameters.get((String) invocation.getArguments()[0]);
				}
			});
			when(request.getParameters()).thenReturn((Map<String, Object>) parameters);
			when(request.hasParameters()).thenReturn(true);
		}
		
		if(cookies != null && !cookies.isEmpty()) {
			when(request.getCookie(anyString())).thenAnswer(new Answer<HttpCookie>() {
				@Override
				public HttpCookie answer(InvocationOnMock invocation) throws Throwable {
					return cookies.get(invocation.getArguments()[0]);
				}
			});
			when(request.hasCookie(anyString())).thenAnswer(new Answer<Boolean>() {
				@Override
				public Boolean answer(InvocationOnMock invocation) throws Throwable {
					return cookies.containsKey(invocation.getArguments()[0]);
				}
			});
		}
		
		return request;
	}

	@SuppressWarnings("unchecked")
	private AppRouter mockRouter() {
		AppRouter router = mock(AppRouter.class);
		
		when(router.pathTo(any(Router.class), any(Class.class))).thenAnswer(new Answer<String>() {
			public String answer(InvocationOnMock invocation) throws Throwable {
				return ((Class<?>) invocation.getArguments()[1]).getSimpleName() + "Controller#" + showAll;
			}
		});
		when(router.pathTo(any(Router.class), any(Class.class), any(Action.class))).thenAnswer(new Answer<String>() {
			public String answer(InvocationOnMock invocation) throws Throwable {
				Object[] args = invocation.getArguments();
				return ((Class<?>) args[1]).getSimpleName() + "Controller#" + args[2];
			}
		});
		when(router.pathTo(any(Router.class), anyString())).thenAnswer(new Answer<String>() {
			public String answer(InvocationOnMock invocation) throws Throwable {
				Object[] args = invocation.getArguments();
				return (String) args[1];
			}
		});
		when(router.pathTo(any(Router.class), any(Model.class))).thenAnswer(new Answer<String>() {
			public String answer(InvocationOnMock invocation) throws Throwable {
				Model model = (Model) invocation.getArguments()[1];
				return model.getClass().getSimpleName() + "Controller#" + show + "(id=" + model.getId() + ")";
			}
		});
		when(router.pathTo(any(Router.class), any(Model.class), any(Action.class))).thenAnswer(new Answer<String>() {
			public String answer(InvocationOnMock invocation) throws Throwable {
				Object[] args = invocation.getArguments();
				Model model = (Model) args[1];
				return model.getClass().getSimpleName() + "Controller#" + args[2] + "(id=" + model.getId() + ")";
			}
		});
		when(router.pathTo(any(Router.class), any(Model.class), anyString())).thenAnswer(new Answer<String>() {
			public String answer(InvocationOnMock invocation) throws Throwable {
				Object[] args = invocation.getArguments();
				Model model = (Model) args[1];
				return model.getClass().getSimpleName() + "Controller#" + showAll + "(" + args[2] + ")";
			}
		});
		when(router.pathTo(any(Router.class), any(Model.class), anyString(), any(Action.class))).thenAnswer(new Answer<String>() {
			public String answer(InvocationOnMock invocation) throws Throwable {
				Object[] args = invocation.getArguments();
				Model model = (Model) args[1];
				return model.getClass().getSimpleName() + "Controller#" + args[3] + "(" + args[2] + ")";
			}
		});

		return router;
	}
	
	private AppService mockService(Logger logger, AppRouter router) {
		AppService service = mock(AppService.class);
		
		when(service.getLogger()).thenReturn(logger);
		when(service.getRouter()).thenReturn(router);
		when(service.getSession(anyInt(), anyString(), anyBoolean())).thenAnswer(new Answer<HttpSession>() {
			@Override
			public HttpSession answer(InvocationOnMock invocation) throws Throwable {
				int id = (Integer) invocation.getArguments()[0];
				String uuid = (String) invocation.getArguments()[1];
				if(session != null) {
					int sid = session.getId();
					String suuid = session.getUuid();
					if((sid < 0 || sid == id) && (suuid.equals("*") || suuid.equals(uuid))) {
						return session;
					}
				}
				boolean create = (Boolean) invocation.getArguments()[2];
				if(create) {
					return new Session(id, uuid);
				}
				return null;
			}
		});
		
		return service;
	}

	public ControllerTester seed(Model model) {
		persistor.seed(model);
		return this;
	}
	
	public ControllerTester set(Header header, String value) {
		if(headers == null) {
			headers = new Headers();
		}
		headers.set(header, value);
		return this;
	}
	
	public ControllerTester setCookie(String name, HttpCookie cookie) {
		if(cookies == null) {
			cookies = new LinkedHashMap<String, HttpCookie>();
		}
		cookies.put(name, cookie);
		return this;
	}
	
	public ControllerTester setCookie(String name, String value) {
		Cookie cookie = Cookie.create(name, value);
		return setCookie(name, cookie);
	}
	
	public ControllerTester setFlash(Map<String, ?> flash) {
		return setCookie(Controller.FLASH_KEY, JsonUtils.toJson(flash));
	}
	
	public ControllerTester setFlash(String name, Object value) {
		return setFlash(Map(name, value));
	}

	public void setSession(HttpSession session) {
		this.session = session;
	}

}
