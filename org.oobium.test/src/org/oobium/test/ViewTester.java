package org.oobium.test;

import static org.jboss.netty.handler.codec.http.HttpMethod.GET;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyBoolean;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.oobium.app.http.Action.show;
import static org.oobium.app.http.Action.showAll;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.atomic.AtomicInteger;

import org.jboss.netty.buffer.ChannelBuffer;
import org.jboss.netty.handler.codec.http.HttpMethod;
import org.jboss.netty.handler.codec.http.HttpVersion;
import org.jboss.netty.handler.codec.http.QueryStringEncoder;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Attribute;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.oobium.app.AppService;
import org.oobium.app.controllers.HttpController;
import org.oobium.app.http.Action;
import org.oobium.app.request.Request;
import org.oobium.app.routing.AppRouter;
import org.oobium.app.routing.Router;
import org.oobium.app.sessions.Session;
import org.oobium.app.views.View;
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
	
	public static ViewTester render(View view, HttpMethod type) {
		return new ViewTester(view).render(type);
	}
	
	public static ViewTester render(View view, HttpMethod type, boolean partial) {
		return new ViewTester(view).render(type, partial);
	}

	public static ViewTester render(View view, HttpMethod type, Map<String, ?> params) {
		return new ViewTester(view).render(type, params);
	}
	
	public static ViewTester render(View view, HttpMethod type, Map<String, ?> params, boolean partial) {
		return new ViewTester(view).render(type, params, partial);
	}

	
	public final View view;
	public final AppService application;
	public final AppRouter router;
	public final TestPersistService persistor;
	private final AtomicInteger persistorSessionCount;
	
	public HttpController controller;
	private Request request;
	private Session session;

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
		
		this.request = new Request(HttpVersion.HTTP_1_1, GET, "/", 5000);
		this.request.setHandler(application);
	}

	private String baseUri() {
		String host = request.getHost();
		int port = request.getPort();
		StringBuilder sb = new StringBuilder(host);
		if(port != 80) {
			sb.append(':').append(port);
		}
		return sb.toString();
	}
	
	public String getContent() {
		ChannelBuffer buff = controller.getResponse().getContent();
		return new String(buff.array());
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
		when(service.getSession(anyInt(), anyString(), anyBoolean())).thenAnswer(new Answer<Session>() {
			@Override
			public Session answer(InvocationOnMock invocation) throws Throwable {
				int id = (Integer) invocation.getArguments()[0];
				String uuid = (String) invocation.getArguments()[1];
				if(session != null) {
					int sid = session.getId(int.class);
					String suuid = session.getUuid();
					if((sid < 0 || sid == id) && (suuid.equals("*") || suuid.equals(uuid))) {
						return session;
					}
				}
				boolean create = (Boolean) invocation.getArguments()[2];
				if(create) {
					return new Session(30*60);
				}
				return null;
			}
		});
		
		return service;
	}
	
	public ViewTester render() {
		return render(request.getMethod(), null, false);
	}

	public ViewTester render(boolean partial) {
		return render(request.getMethod(), null, partial);
	}

	public ViewTester render(Map<String, ?> params) {
		return render(request.getMethod(), params, false);
	}
	
	public ViewTester render(Map<String, ?> params, boolean partial) {
		return render(request.getMethod(), params, partial);
	}
	
	public ViewTester render(HttpMethod type) {
		return render(type, null, false);
	}
	
	public ViewTester render(HttpMethod type, boolean partial) {
		return render(type, null, partial);
	}
	
	public ViewTester render(HttpMethod type, Map<String, ?> params) {
		return render(type, params, false);
	}

	private void setParams(Map<String, ?> params) {
		String uri = request.getUri();
		int ix = uri.indexOf('?');
		if(ix != -1) {
			uri = uri.substring(0, ix);
		}
		QueryStringEncoder enc = new QueryStringEncoder(uri);
		for(Entry<String, ?> entry : params.entrySet()) {
			enc.addParam(entry.getKey(), String.valueOf(entry.getValue()));
		}
		request.setUri(enc.toString());
	}
	
	public ViewTester render(HttpMethod method, Map<String, ?> params, boolean partial) {
		try {
			if(method != null) request.setMethod(method);
			if(params != null) setParams(params);
			persistor.openSession("test session " + persistorSessionCount.incrementAndGet());
			Model.setPersistService(persistor);
			controller = new HttpController();
			controller.initialize(router, request, null);
			controller.render(view, partial || controller.isXhr());
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
	
	/**
	 * Get elements that match the given selector query as run on the root document.
	 * @param query the query to use in finding the element (follows JQuery CSS selectors approach)
	 * @return an Elements object; never null
	 */
	public Elements select(String query) {
		return document.select(query);
	}

}
