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

import org.junit.Test;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.oobium.app.controllers.Controller;
import org.oobium.app.handlers.HttpRequestHandler;
import org.oobium.app.http.MimeType;
import org.oobium.app.request.Request;
import org.oobium.app.response.Response;
import org.oobium.app.response.StaticResponse;
import org.oobium.app.routing.AppRouter;
import org.oobium.app.server.HandlerTask;
import org.oobium.app.views.View;
import org.oobium.logging.LogProvider;
import org.oobium.logging.Logger;
import org.oobium.utils.Config;
import org.oobium.utils.FileUtils;

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
		File folder = new File(System.getProperty("user.dir"));
		folder.mkdirs();
		
		final File css = FileUtils.writeFile(folder, "application.css", "/* nothing to see here */");
		final File js = FileUtils.writeFile(folder, "application.js", "// nothing to see here");
		final URL jquery = getClass().getResource("/scripts/jquery_1.4.4.js");
		final URL logo = getClass().getResource("/images/logo.png");
		
		
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

}
