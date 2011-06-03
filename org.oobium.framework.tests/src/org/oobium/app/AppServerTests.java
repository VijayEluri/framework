/*******************************************************************************
 * Copyright (c) 2010 Oobium, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Jeremy Dowdall <jeremy@oobium.com> - initial API and implementation
 ******************************************************************************/
package org.oobium.app;

import static org.jboss.netty.handler.codec.http.HttpResponseStatus.*;

import static org.mockito.Matchers.*;
import static org.mockito.Mockito.*;

import java.io.File;
import java.net.URL;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.jboss.netty.handler.codec.http.websocket.DefaultWebSocketFrame;
import org.jboss.netty.handler.codec.http.websocket.WebSocketFrame;
import org.junit.Test;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.oobium.app.controllers.Controller;
import org.oobium.app.controllers.WebsocketController;
import org.oobium.app.handlers.HttpRequestHandler;
import org.oobium.app.http.MimeType;
import org.oobium.app.request.Request;
import org.oobium.app.response.Response;
import org.oobium.app.response.StaticResponse;
import org.oobium.app.routing.AppRouter;
import org.oobium.app.server.HandlerTask;
import org.oobium.app.server.Websocket;
import org.oobium.app.views.View;
import org.oobium.logging.LogProvider;
import org.oobium.logging.Logger;
import org.oobium.utils.Config;
import org.oobium.utils.FileUtils;
import org.oobium.utils.json.JsonUtils;

public class AppServerTests {

	@Test
	public void testX() throws Exception {
		File folder = new File(System.getProperty("user.dir"));
		folder.mkdirs();
		
		final File css = FileUtils.writeFile(folder, "application.css", "/* nothing to see here */");
		final File js = FileUtils.writeFile(folder, "application.js", "// nothing to see here");
		final URL jquery = getClass().getResource("/scripts/jquery_1.4.4.js");
		final URL logo = getClass().getResource("/images/logo.png");
		
		HttpRequestHandler handler = mock(HttpRequestHandler.class);
		when(handler.getPort()).thenReturn(5000);
		when(handler.handleRequest(any(Request.class))).thenAnswer(new Answer<Object>() {
			@Override
			public Object answer(InvocationOnMock invocation) throws Throwable {
				Request request = (Request) invocation.getArguments()[0];
				String path = request.getPath();
				if("/".equals(path)) {
					return new HandlerTask(request) {
						@Override
						protected Response handleRequest(Request request) throws Exception {
							try {
								Thread.sleep(5000);
							} catch(InterruptedException e) {
							}
							Response response = new Response(OK);
							response.setContent(
									"<head>\n" +
									"<link rel='stylesheet' type='text/css' href='/application.css' /n" +
									"<script src='jquery.js'></script>\n" +
									"<script src='application.js'></script>\n" +
									"</head>\n" +
									"<body>\n" +
									"<img src='/logo.png' /><div>Hello World!</div>\n" +
									"</body>"
							);
							return response;
						};
					};
				} else if("/application.css".equals(path)) {
					return new StaticResponse(MimeType.TAR_GZ, css.toURI().toURL().openStream(), css.length(), css.lastModified());
				} else if("/application.js".equals(path)) {
					return new StaticResponse(MimeType.TAR_GZ, js.toURI().toURL().openStream(), js.length(), js.lastModified());
				} else if("/jquery.js".equals(path)) {
					return new StaticResponse(MimeType.TAR_GZ, jquery.openStream(), 78601, 0);
				} else if("/logo.png".equals(path)) {
					return new StaticResponse(MimeType.TAR_GZ, logo.openStream(), 6513, 0);
				} else {
					System.out.println("requested path: " + path);
					return null;
				}
			}
		});

		Logger logger = LogProvider.getLogger();
		logger.setConsoleLevel(Logger.DEBUG);
		
		AppServer server = new AppServer(logger);
		server.addHandler(handler);
		
		while(true) {
			Thread.sleep(100);
		}
	}
	
	public static class TestController extends Controller {
		@Override
		public void handleRequest() throws SQLException {
			render(MimeType.HTML,
					"<head>\n" +
					"<link rel='stylesheet' type='text/css' href='/application.css' /n" +
					"<script src='jquery.js'></script>\n" +
					"<script src='application.js'></script>\n" +
					"</head>\n" +
					"<body>\n" +
					"<img src='/logo.png' />\n" +
					"<div>thread: " + Thread.currentThread().getName() + "</div>\n" +
					"<div>count: " + Thread.activeCount() + "</div>\n" +
					"</body>"
			);
		}
	}

	public static class TestView extends View {
		@Override
		protected void doRenderBody(StringBuilder sb) throws Exception {
			sb.append(
				"<body>\n" +
				"<img src='/logo.png' />\n" +
				"<div>thread: " + Thread.currentThread().getName() + "</div>\n" +
				"<div>count: " + Thread.activeCount() + "</div>\n" +
				"<a href=\"/controller\">controller</a>\n" +
				"</body>"
			);
		}
		@Override
		protected void doRenderScript(StringBuilder sb) {
			sb.append(
				"<script src='jquery.js'></script>" +
				"<script src='application.js'></script>"
			);
		}
		@Override
		protected void doRenderStyle(StringBuilder sb) {
			sb.append(
				"<link rel='stylesheet' type='text/css' href='/application.css' />"
			);
		}
	}

	@Test
	public void testAppService() throws Exception {
		Logger logger = LogProvider.getLogger();
		logger.setConsoleLevel(Logger.DEBUG);
		
		AppService service = new AppService(logger) {
			public void addRoutes(org.oobium.utils.Config config, AppRouter router) {
				router.setHome(TestView.class);
				router.addRoute("/controller", TestController.class);
			};
			@Override
			protected Config loadConfiguration() {
				Map<String, Object> map = new HashMap<String, Object>();
				map.put("host", "localhost");
				map.put("port", 5000);
				return new Config(map);
			}
		};
		service.startApp();

		AppServer server = new AppServer(logger);
		server.addHandler(service);
		
		while(true) {
			Thread.sleep(100);
		}
	}

	public static class WebsocketsView extends View {
		@Override
		protected void doRenderBody(StringBuilder sb) throws Exception {
			sb.append(
					"<body>\n" +
					"<script type=\"text/javascript\">\n" +
					"var lowercase;\n" +
					"var uppercase;\n" +
					"if (window.WebSocket) {\n" +
					"  lowercase = new WebSocket(\"ws://localhost:5000/lowercase\");\n" +
					"  lowercase.onmessage = function(event) { alert(event.data); };\n" +
					"  lowercase.onopen = function(event) { alert(\"LowerCase Web Socket opened!\"); };\n" +
					"  lowercase.onclose = function(event) { alert(\"LowerCase Web Socket closed.\"); };\n" +
					"  uppercase = new WebSocket(\"ws://localhost:5000/uppercase\");\n" +
					"  uppercase.onmessage = function(event) { alert(event.data); };\n" +
					"  uppercase.onopen = function(event) { alert(\"UpperCase Web Socket opened!\"); };\n" +
					"  uppercase.onclose = function(event) { alert(\"UpperCase Web Socket closed.\"); };\n" +
					"} else {\n" +
					"  alert(\"Your browser does not support Web Socket.\");\n" +
					"}\n" +
					"\n" +
					"function send(socket, message) {\n" +
					"  if (!window.WebSocket) { return; }\n" +
					"  if (socket.readyState == WebSocket.OPEN) {\n" +
					"    socket.send(message);\n" +
					"  } else {\n" +
					"    alert(\"The socket is not open.\");\n" +
					"  }\n" +
					"}\n" +
					"</script>\n" +
					"<form onsubmit=\"return false;\">\n" +
					"<input type=\"text\" name=\"message\" value=\"Hello, World!\"/>" +
					"<input type=\"button\" value=\"Send LowerCase Data\" onclick=\"send(lowercase, this.form.message.value)\" />\n" +
					"<input type=\"button\" value=\"Register LowerCase\" onclick=\"send(lowercase, 'registration:{name:lowercase}')\" />\n" +
					"<input type=\"button\" value=\"Send UpperCase Data\" onclick=\"send(uppercase, this.form.message.value)\" />\n" +
					"</form>\n" +
					"</body>"
			);
		}
	}

	public static class LowerCaseController extends WebsocketController {
		@Override
		public void handleRegistration(Websocket socket, Map<String, String> properties) {
			socket.setId(properties.get("name"));
		}
		@Override
		public void handleMessage(WebSocketFrame frame) {
			write(new DefaultWebSocketFrame(frame.getTextData().toLowerCase()));
		}
	}
	
	public static class UpperCaseController extends WebsocketController {
		@Override
		public void handleMessage(WebSocketFrame frame) {
			write(new DefaultWebSocketFrame(frame.getTextData().toUpperCase()));
		}
	}
	
	public static class ShowAllWebsocketsController extends Controller {
		@Override
		public void handleRequest() throws SQLException {
			Set<Websocket> sockets = getRouter().getWebsockets("lcws");
			if(sockets.isEmpty()) {
				render("no sockets");
			} else {
				StringBuilder sb = new StringBuilder();
				sb.append("<ul>");
				for(Websocket socket : sockets) {
					sb.append("<li>").append(socket.getId()).append("</li>");
				}
				sb.append("</ul>");
				render(sb.toString());
			}
		}
	}
	
	public static class WriteToLowerCaseController extends Controller {
		@Override
		public void handleRequest() throws SQLException {
			Websocket socket = getRouter().getWebsocket("android1");
			if(socket == null) {
				render("no socket");
			} else {
				String text = param("text", "hello!");
				socket.write(new DefaultWebSocketFrame(text));
				render("<div>wrote '" + text + "' to lowercase</div>");
			}
		}
	}
	
	@Test
	public void testWebsockets() throws Exception {
		Logger logger = LogProvider.getLogger();
		logger.setConsoleLevel(Logger.DEBUG);
		
		AppService service = new AppService(logger) {
			public void addRoutes(org.oobium.utils.Config config, AppRouter router) {
				router.setHome(WebsocketsView.class);
				router.addWebsocket("/lowercase", LowerCaseController.class).inGroup("lcws");
				router.addWebsocket("/uppercase", UpperCaseController.class).inGroup("ucws");
				router.addRoute("/show_sockets", ShowAllWebsocketsController.class);
				router.addRoute("/write?{text:.*}", WriteToLowerCaseController.class);
			};
			@Override
			protected Config loadConfiguration() {
				String json =
					"host: ['localhost','192.168.2.7'],\n" +
					"port: 5000";
				return new Config(JsonUtils.toMap(json));
			}
		};
		service.startApp();

		AppServer server = new AppServer(logger);
		server.addHandler(service);
		
		while(true) {
			Thread.sleep(100);
		}
	}

}
