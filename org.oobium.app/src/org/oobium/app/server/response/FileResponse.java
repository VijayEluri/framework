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
package org.oobium.app.server.response;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintStream;

import org.oobium.http.constants.ContentType;
import org.oobium.http.constants.Header;
import org.oobium.http.constants.RequestType;
import org.oobium.http.constants.StatusCode;

public class FileResponse extends Response {

	private static final File notFound = new File(System.getProperty("user.dir") + 
			File.separator + "resources" + File.separator + "html" + File.separator + "404_Not_Found.html");

	private File file;
	
	public FileResponse(RequestType requestType, File file) {
		super(requestType);
		if(file.canRead()) {
			this.file = file;
			String fileName = file.getName();
			int ix = fileName.lastIndexOf('.');
			if(ix > 0 && ix < fileName.length() - 1) {
				setContentType(ContentType.getFromExtension(fileName.substring(ix + 1), ContentType.HTML));
			} else {
				setContentType(ContentType.HTML);
			}

			setStatus(StatusCode.OK);

			addHeader(Header.CONTENT_LENGTH, Long.toString(file.length()));
		} else {
			this.file = notFound;
			setContentType(ContentType.HTML);
			setStatus(StatusCode.NOT_FOUND);
		}
	}

	public File getFile() {
		return file;
	}
	
	public void sendContent(PrintStream out) throws IOException {
		if(getContentType().isBinary()){
			sendBinary(out);
		} else {
			sendString(out);
		}
	}

	private void sendBinary(PrintStream out) throws IOException {
        int n;
	    byte[] buf = new byte[2048];
	    BufferedInputStream bis = null;
	    try {
			bis = new BufferedInputStream(new FileInputStream(file));
	        while ((n = bis.read(buf)) > 0) {
	            out.write(buf, 0, n);
	        }
	    } finally {
	    	if(bis != null) {
		    	try {
					bis.close();
				} catch(IOException e) {
				}
	    	}
	    }
	}
	
	private void sendString(PrintStream out) throws IOException {
		BufferedReader reader = null;
		try {
			String line;
			reader = new BufferedReader(new FileReader(file));
			while((line = reader.readLine()) != null && line.length() > 0) {
				out.print(line);
			}
		} catch(FileNotFoundException e) {
			// best attempt made early in order to report a 404...
			throw new IOException(e.getMessage(), e);
	    } finally {
	    	if(reader != null) {
		    	try {
		    		reader.close();
				} catch(IOException e) {
				}
	    	}
		}
	}
}
