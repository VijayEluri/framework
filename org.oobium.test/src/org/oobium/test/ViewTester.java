package org.oobium.test;

import static org.mockito.Matchers.anyBoolean;
import static org.mockito.Mockito.*;
import static org.oobium.app.server.controller.Action.*;
import static org.oobium.http.constants.Header.*;
import static org.oobium.http.constants.RequestType.*;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Attribute;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.oobium.app.AppService;
import org.oobium.app.server.controller.Action;
import org.oobium.app.server.controller.Controller;
import org.oobium.app.server.routing.AppRouter;
import org.oobium.app.server.routing.Router;
import org.oobium.app.server.view.View;
import org.oobium.http.HttpCookie;
import org.oobium.http.HttpRequest;
import org.oobium.http.HttpSession;
import org.oobium.http.constants.ContentType;
import org.oobium.http.constants.Header;
import org.oobium.http.constants.RequestType;
import org.oobium.http.impl.Headers;
import org.oobium.logging.Logger;
import org.oobium.persist.Model;

public class ViewTester {

	public static ViewTester render(View view) {
		return new ViewTester(view).render();
	}
	
	public static ViewTester render(View view, boolean partial) {
		return new ViewTester(view).render(partial);
	}
	
	public static ViewTester render(View view, Map<String, ?> params) {
		return new ViewTester(view).render(params);
	}
	
	public static ViewTester render(View view, Map<String, ?> params, boolean partial) {
		return new ViewTester(view).render(params, partial);
	}
	
	public static ViewTester render(View view, RequestType type) {
		return new ViewTester(view).render(type);
	}
	
	public static ViewTester render(View view, RequestType type, boolean partial) {
		return new ViewTester(view).render(type, partial);
	}

	public static ViewTester render(View view, RequestType type, Map<String, ?> params) {
		return new ViewTester(view).render(type, params);
	}
	
	public static ViewTester render(View view, RequestType type, Map<String, ?> params, boolean partial) {
		return new ViewTester(view).render(type, params, partial);
	}

	
	public final View view;
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

	public Document document;
	
	/**
	 * @param view the class of Controller to be tested
	 * @throws IllegalArgumentException if the given class is abstract, private, or does not have a no-args constructor
	 */
	public ViewTester(View view) {
		this.view = view;
		this.router = mockRouter();
		this.application = mockService(router);
		this.persistor = TestPersistService.useSimple();
		this.persistorSessionCount = new AtomicInteger();
		
		this.host = "testhost";
		this.port = 0;
	}

	private String baseUri() {
		StringBuilder sb = new StringBuilder(host);
		if(port != 80) {
			sb.append(':').append(port);
		}
		return sb.toString();
	}
	
	public String getContent() {
		return controller.getResponse().getBody();
	}
	
	/**
	 * Get the combined HTML of all the matched elements.
	 * @param query the query to use in finding the element (follows JQuery CSS selectors approach)
	 * @return the combined HTML of all the matched elements, or an empty string if no elements match; never null
	 */
	public String html(String query) {
		return document.select(query).html();
	}

	/**
	 * Get the value of the attribute of the given name in the 
	 * element specified by the given query.
	 * @param query the query to use in finding the element (follows JQuery CSS selectors approach)
	 * @param attrName the name of the attribute
	 * @return the value of the attribute, or an empty String if the element does
	 * not exist or the attribute does not exist; never null
	 */
	public String attr(String query, String attrName) {
		Element element = document.select(query).first();
		if(element != null) {
			return element.attr(attrName);
		}
		return "";
	}
	
	/**
	 * Get all the attributes in the element specified by the given query.
	 * @param query the query to use in finding the element (follows JQuery CSS selectors approach)
	 * @param attrName the name of the attribute
	 * @return a Map of the attributes, in the same order that they are found in the HTML, or an empty Map if the element does
	 * not exist; never null
	 */
	public Map<String, String> attrs(String query) {
		Element element = document.select(query).first();
		if(element != null) {
			Map<String, String> attrs = new LinkedHashMap<String, String>();
			for(Attribute attr : element.attributes()) {
				attrs.put(attr.getKey(), attr.getValue());
			}
			return attrs;
		}
		return new HashMap<String, String>(0);
	}
	
	/**
	 * Create a mocked Request object for the given request type and full path.
	 * The returned mocked object can be used to stub out its method if necessary.
	 */
	@SuppressWarnings("unchecked")
	private HttpRequest mockRequest(RequestType type, final Map<String, ?> params) {
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
			when(request.getParameter(anyString())).thenAnswer(new Answer<Object>() {
				@Override
				public Object answer(InvocationOnMock invocation) throws Throwable {
					return params.get((String) invocation.getArguments()[0]);
				}
			});
			when(request.getParameters()).thenReturn((Map<String, Object>) params);
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

	private AppService mockService(AppRouter router) {
		Logger logger = mock(Logger.class);
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
	
	public ViewTester render() {
		return render(mockRequest(GET, null), null);
	}

	public ViewTester render(boolean partial) {
		return render(mockRequest(GET, null), partial);
	}

	private ViewTester render(HttpRequest request, Boolean partial) {
		try {
			persistor.openSession("test session " + persistorSessionCount.incrementAndGet());
			Model.setPersistService(persistor);
			controller = new Controller();
			controller.initialize(router, request, null);
			if(partial == null) {
				partial = controller.isXhr();
			}
			controller.render(view, partial);
			if(partial) {
				document = Jsoup.parseBodyFragment(getContent(), baseUri());
			} else {
				document = Jsoup.parse(getContent(), baseUri());
			}
		} catch(RuntimeException e) {
			throw e;
		} catch(Exception e) {
			throw new RuntimeException(e.getMessage(), e);
		}
		return this;
	}
	
	public ViewTester render(Map<String, ?> params) {
		return render(mockRequest(GET, params), null);
	}
	
	public ViewTester render(Map<String, ?> params, boolean partial) {
		return render(mockRequest(GET, params), partial);
	}
	
	public ViewTester render(RequestType type) {
		return render(mockRequest(type, null), null);
	}
	
	public ViewTester render(RequestType type, boolean partial) {
		return render(mockRequest(type, null), partial);
	}
	
	public ViewTester render(RequestType type, Map<String, ?> params) {
		return render(mockRequest(type, params), null);
	}

	public ViewTester render(RequestType type, Map<String, ?> params, boolean partial) {
		return render(mockRequest(type, params), partial);
	}
	
	public ViewTester seed(Model model) {
		persistor.seed(model);
		return this;
	}

	/**
	 * Get elements that match the given selector query as run on the root document.
	 * @param query the query to use in finding the element (follows JQuery CSS selectors approach)
	 * @return an Elements object; never null
	 */
	public Elements select(String query) {
		return document.select(query);
	}

}
