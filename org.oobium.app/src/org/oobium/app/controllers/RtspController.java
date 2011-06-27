package org.oobium.app.controllers;

import java.lang.reflect.Method;
import java.util.Map;

import org.jboss.netty.handler.codec.http.HttpResponseStatus;
import org.jboss.netty.handler.codec.http.HttpVersion;
import org.oobium.app.AppService;
import org.oobium.app.request.Request;
import org.oobium.app.response.Response;
import org.oobium.logging.Logger;


public class RtspController {

	public enum RtspMethod {
		OPTIONS, DESCRIBE, ANNOUNCE, SETUP, PLAY, PAUSE, TEARDOWN, GET_PARAMETER, SET_PARAMETER, REDIRECT, RECORD, EMBED
	}

	protected Logger logger;
	private AppService application;
	protected Request request;

	public Response execute(RtspMethod method) {
		switch(method) {
		case OPTIONS:		return handleOptions();
		case DESCRIBE:		return handleDescribe();
		case ANNOUNCE:		return handleAnnounce();
		case SETUP:			return handleSetup();
		case PLAY:			return handlePlay();
		case PAUSE:			return handlePause();
		case TEARDOWN:		return handleTeardown();
		case GET_PARAMETER:	return handleGetParameter();
		case SET_PARAMETER:	return handleSetParameter();
		case REDIRECT:		return handleRedirect();
		case RECORD:		return handleRecord();
		case EMBED:			return handleEmbed();
		}
		return null;
	}
	
	public AppService getApplication() {
		return application;
	}
	
	public void initialize(Request request, Map<String, Object> routeParams) {
		this.request = request;
		this.application = (AppService) request.getHandler();
		this.logger = application.getLogger();
	}

	protected Response createResponse() {
		return createResponse(HttpResponseStatus.OK);
	}
	
	protected Response createResponse(HttpResponseStatus status) {
		Response response = new Response(HttpVersion.valueOf("RTSP/1.0"), status);
		response.setHeader("CSeq", request.getHeader("Cseq"));
		return response;
	}
	
	public Response handleOptions() {
		StringBuilder sb = new StringBuilder();
		try {
			for(Method method : getClass().getDeclaredMethods()) {
				String name = method.getName();
				if(name.startsWith("handle")) {
					if(sb.length() != 0) sb.append(", ");
					sb.append(name.substring(6));
				}
			}
			Response response = createResponse();
			response.setHeader("PUBLIC", sb.toString());
			return response;
		} catch(Exception e) {
			logger.warn(e);
		}
		return null;
	}
	
	public Response handleDescribe() {
		// subclasses to override
		return null;
	}
	
	public Response handleAnnounce() {
		// subclasses to override
		return null;
	}
	
	public Response handleSetup() {
		// subclasses to override
		return null;
	}
	
	public Response handlePlay() {
		// subclasses to override
		return null;
	}
	
	public Response handlePause() {
		// subclasses to override
		return null;
	}
	
	public Response handleTeardown() {
		// subclasses to override
		return null;
	}
	
	public Response handleGetParameter() {
		// subclasses to override
		return null;
	}
	
	public Response handleSetParameter() {
		// subclasses to override
		return null;
	}
	
	public Response handleRedirect() {
		// subclasses to override
		return null;
	}
	
	public Response handleRecord() {
		// subclasses to override
		return null;
	}
	
	public Response handleEmbed() {
		// subclasses to override
		return null;
	}
	
}
