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
package org.oobium.server;

import static org.oobium.http.HttpRequest.Type.*;

import org.oobium.http.HttpRequest.Type;

class Data {

	static final byte TIMED_OUT = -6;
	static final byte INVALID_HOST = -4;
	static final byte INVALID_PROTOCOL = -3;
	static final byte INVALID_PATH = -2;
	static final byte INVALID_TYPE = -1;
	static final byte UNKNOWN = 0;
	static final byte HAS_TYPE = 1;
	static final byte HAS_PATH = 2;
	static final byte HAS_PROTOCOL = 3;
	static final byte HAS_HOST = 4;
	static final byte HAS_HEADERS = 5;

	static final byte HTTP_1_0 = 0;
	static final byte HTTP_1_1 = 1;
	
	final long start;
	long lastRead;
	
	byte state; // hasType, hasPath, hasHost, hasHeaders
				//  if type == [GET|HEAD], we can route it as soon as we receive the path and host
				//  if type == POST, we need all the headers (in case there is a _method header)
				//    still, we can route it before the data has been received (may want to check permissions before uploading a big file...)
	
	Type type;
	byte protocol;
	
	int mark;
	byte[] data;
	int[] marks; // 1-1st space, 2-2nd space, 3-start of host header, 4-start of content

	Data(byte[] data) {
		this.data = data;
		start = lastRead = System.currentTimeMillis();
		mark = 0;
		marks = new int[5];
		if(data.length > 6) {
			process();
		}
	}

	void add(byte[] newData) {
		if(state >= UNKNOWN) {
			lastRead = System.currentTimeMillis();
			byte[] tmp = new byte[data.length + newData.length];
			System.arraycopy(data, 0, tmp, 0, data.length);
			System.arraycopy(newData, 0, tmp, data.length, newData.length);
			data = tmp;
			if(state < HAS_HEADERS) {
				process();
			}
		}
	}
	
	boolean canRoute() {
		return state >= HAS_HOST;
	}

	private void findEndOfHeaders() {
		for(int i = marks[2] + 1; i < data.length - 1; i++) {
			if(isBlankLine(i)) {
				if(data[i + 1] == '\n') {
					i += 2;
				} else {
					i += 3;
				}
				state = HAS_HEADERS;
				marks[4] = i;
				break;
			}
		}
	}
	
	private void findHost() {
		int i = marks[2];
		while(i < data.length - 5) {
			if(isBlankLine(i)) {
				state = INVALID_HOST; // end of header section and host not found
				break;
			}
			if(data[i++] == '\n' && isHost(i)) {
				state = HAS_HOST;
				marks[3] = i;
				break;
			}
		}
	}
	
	private void findPath() {
		int i = marks[0] + 1;
		while(i < data.length) {
			if(data[i] != ' ') {
				break;
			} else if(data[i] == '\n') {
				state = INVALID_PATH;
				return;
			} else {
				i++;
			}
		}
		while(i < data.length) {
			if(data[i] == ' ') {
				state = HAS_PATH;
				marks[1] = i;
				break;
			} else if(data[i] == '\n') {
				state = INVALID_PATH;
				return;
			} else {
				i++;
			}
		}
	}

	private void findProtocol() {
		int i = marks[1] + 1;
		while(i < data.length) {
			if(data[i] != ' ') {
				break;
			} else if(data[i] != '\n') {
				state = INVALID_PROTOCOL;
				return;
			} else {
				i++;
			}
		}
		if((i + 8) < data.length && (data[i]   == 'H' || data[i]   == 'h') && 
									(data[i+1] == 'T' || data[i+1] == 't') && 
									(data[i+2] == 'T' || data[i+2] == 't') && 
									(data[i+3] == 'P' || data[i+3] == 'p') && data[i+4] == '/' && data[i+5] == '1' && data[i+6] == '.') {
			if(data[i+8] == '\r' || data[i+8] == '\n') {
				i = i + 7;
				if(data[i] == '1') {
					state = HAS_PROTOCOL;
					marks[2] = i + 1;
					protocol = HTTP_1_1;
				} else if(data[i] == '0') {
					state = HAS_HOST; // host is not provided in HTTP 1.0
					marks[2] = i + 1;
					protocol = HTTP_1_0;
				} else {
					state = INVALID_PROTOCOL;
				}
			} else {
				state = INVALID_PROTOCOL;
			}
		} else {
			state = INVALID_PROTOCOL;
		}
	}
	
	private void findType() {
		if(isGet()) {
			state = HAS_TYPE;
			marks[0] = 3;
			type = GET;
		} else if(isPut()) {
			state = HAS_TYPE;
			marks[0] = 3;
			type = PUT;
		} else if(isHead()) {
			state = HAS_TYPE;
			marks[0] = 4;
			type = HEAD;
		} else if(isPost()) {
			state = HAS_TYPE;
			marks[0] = 4;
			type = POST;
		} else if(isDelete()) {
			state = HAS_TYPE;
			marks[0] = 6;
			type = DELETE;
		} else {
			state = INVALID_TYPE;
		}
	}
	
	byte[] getContent(int length) {
		byte[] content = new byte[Math.min(length, data.length - marks[4])];
		System.arraycopy(data, marks[4], content, 0, content.length);
		return content;
	}
	
	String getHost() {
		if(protocol == HTTP_1_1) {
			int start = marks[3] + 5;
			while(data[start] == ' ') {
				start++;
			}
			int end = start + 1;
			while(data[end] != ' ' && data[end] != '\r' && data[end] != '\n') {
				end++;
			}
			return new String(data, start, end-start);
		} else {
			return null;
		}
	}
	
	String getPath() {
		int start = marks[0] + 1;
		while(data[start] == ' ') {
			start++;
		}
		return new String(data, start, marks[1]-start);
	}
	
	String getProtocol() {
		switch(protocol) {
		case 0: return "HTTP/1.0";
		case 1: return "HTTP/1.1";
		default: return "";
		}
	}
	
	void goToHeaders() {
		mark = marks[2];
		while(data[mark] == ' ' || data[mark]  == '\r' || data[mark] == '\n') {
			mark++;
		}
	}
	
	boolean invalid() {
		return state < UNKNOWN;
	}
	
	boolean isBlankLine(int i) {
		if(data[i] == '\n') {
			if(data[i + 1] == '\n') {
				return true;
			}
			if(data[i + 1] == '\r' && (i+2) < data.length && data[i + 2] == '\n') {
				return true;
			}
		}
		return false;
	}
	
	private boolean isDelete() {
		return (data[6] == ' ' && (data[0] == 'D' || data[0] == 'd') && (data[1] == 'E' || data[1] == 'e') && (data[2] == 'L' || data[2] == 'l') && (data[3] == 'E' || data[3] == 'e') && (data[4] == 'T' || data[4] == 't') && (data[5] == 'E' || data[5] == 'e'));
	}
	
	private boolean isGet() {
		return (data[3] == ' ' && (data[0] == 'G' || data[0] == 'g') && (data[1] == 'E' || data[1] == 'e') && (data[2] == 'T' || data[2] == 't'));
	}
	
	private boolean isHead() {
		return (data[4] == ' ' && (data[0] == 'H' || data[0] == 'h') && (data[1] == 'E' || data[1] == 'e') && (data[2] == 'A' || data[2] == 'a') && (data[3] == 'D' || data[3] == 'd'));
	}
	
	private boolean isHost(int i) {
		if((i+3) < data.length  && 	(data[i]   == 'H' || data[i]   == 'h') &&
									(data[i+1] == 'O' || data[i+1] == 'o') &&
									(data[i+2] == 'S' || data[i+2] == 's') &&
									(data[i+3] == 'T' || data[i+3] == 't') ) {
			i += 4;
			while(i < data.length) {
				if(data[i] == ':') {
					return true;
				} else if(data[i] == ' ') {
					i++;
				} else {
					return false;
				}
			}
		}
		return false;
	}
	
	private boolean isLength(int i) {
		if((i+13) < data.length &&  (data[i]    == 'C' || data[i]    == 'c') &&
									(data[i+1]  == 'O' || data[i+1]  == 'o') &&
									(data[i+2]  == 'N' || data[i+2]  == 'n') &&
									(data[i+3]  == 'T' || data[i+3]  == 't') &&
									(data[i+4]  == 'E' || data[i+4]  == 'e') &&
									(data[i+5]  == 'N' || data[i+5]  == 'n') &&
									(data[i+6]  == 'T' || data[i+6]  == 't') &&
									 data[i+7]  == '-' &&
									(data[i+8]  == 'L' || data[i+8]  == 'l') &&
									(data[i+9]  == 'E' || data[i+9]  == 'e') &&
									(data[i+10] == 'N' || data[i+10] == 'n') &&
									(data[i+11] == 'G' || data[i+11] == 'g') &&
									(data[i+12] == 'T' || data[i+12] == 't') &&
									(data[i+13] == 'H' || data[i+13] == 'h') ) {
			i += 14;
			while(i < data.length) {
				if(data[i] == ':') {
					return true;
				} else if(data[i] == ' ') {
					i++;
				} else {
					return false;
				}
			}
		}
		return false;
	}

	private boolean isPost() {
		return (data[4] == ' ' && (data[0] == 'P' || data[0] == 'p') && (data[1] == 'O' || data[1] == 'o') && (data[2] == 'S' || data[2] == 's') && (data[3] == 'T' || data[3] == 't'));
	}

	private boolean isPut() {
		return (data[3] == ' ' && (data[0] == 'P' || data[0] == 'p') && (data[1] == 'U' || data[1] == 'u') && (data[2] == 'T' || data[2] == 't'));
	}

	void process() {
		if(state == UNKNOWN) {
			findType();
		}
		if(state == HAS_TYPE) {
			findPath();
		}
		if(state == HAS_PATH) {
			findProtocol();
		}
		if(state == HAS_PROTOCOL) {
			findHost();
		}
		if(state == HAS_HOST) {
			findEndOfHeaders();
		}
	}
	
	byte read() {
		if(mark < data.length) {
			return data[mark++];
		}
		return -1;
	}

	String readLine() {
		if(mark >= data.length) {
			return null;
		}

		int start = mark;
		int end = start;

		while(end < data.length && (data[end]) != '\n') {
			end++;
		}

		if(end == data.length) {
			return null;
		} else {
			mark = end + 1;
			if((data[end - 1]) == '\r') {
				end--;
			}
			if(end == start) {
				return "";
			} else {
				char[] ca = new char[end - start];
				for(int i = 0; i < ca.length; i++) {
					ca[i] = (char) data[i + start];
				}
				return new String(ca);
			}
		}
	}

	boolean ready() {
		int length = -1;
		int i = marks[2];
		while(i < data.length - 1) {
			if(isBlankLine(i)) {
				if(length == -1) {
					return true;
				} else  {
					if(data[i + 1] == '\n') {
						i += 2;
					} else {
						i += 3;
					}
					return data.length >= (i + length);
				}
			}
			if(data[i++] == '\n' && isLength(i)) {
				int start = (i + 14);
				while(data[start++] != ':');
				int end = start;
				while(end < data.length && data[end++] != '\n');
				try {
					length = Integer.parseInt(new String(data, start, end-start-1).trim());
				} catch(NumberFormatException e) {
					e.printStackTrace();
					System.out.println(new String(data));
				}
				i = end - 1;
			}
		}
		return false;
	}

	@Override
	public String toString() {
		return super.toString() + "\n" + new String(data);
	}
	
}
