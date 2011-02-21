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
/**
 * 
 */
package org.oobium.http.constants;

import static org.oobium.http.constants.RequestType.*;

import org.oobium.http.HttpRequest;

/**
 * Used primarily in resource routing, actions denote controller methods that will
 * be called to handle certain requests.
 * <p>By convention, actions are mapped to HTTP verbs and URLs:</p>
 * <table>
 *   <tr align="left"><th>Action</th><th>Verb</th><th>Path</th><th>Purpose</th></tr>
 *   <tr><td>{@link #create}</td><td>POST</td><td>/{models}</td><td>create a new model</td></tr>
 *   <tr><td>{@link #update}</td><td>PUT</td><td>/{models}/{id}</td><td>update a specific model</td></tr>
 *   <tr><td>{@link #destroy}</td><td>DELETE</td><td>/{models}/{id}</td><td>destroy a specific model</td></tr>
 *   <tr><td>{@link #show}</td><td>GET</td><td>/{models}/{id}</td><td>show a specific model</td></tr>
 *   <tr><td>{@link #showAll}</td><td>GET</td><td>/{models}</td><td>show all models</td></tr>
 *   <tr><td>{@link #showEdit}</td><td>GET</td><td>/{models}/{id}/edit</td><td>return an HTML form to edit a specific model</td></tr>
 *   <tr><td>{@link #showNew}</td><td>GET</td><td>/{models}/new</td><td>return an HTML form to create a new model</td></tr>
 * </table>
 * <p>{models} refers to the plural of the model name that this action is used with; for example, the showAll path
 * for the Post model would be: "/posts" and it purpose would be to show all models of type Post.</p>
 */
public enum Action { 

	/**
	 * Create a new model object.<br/>
	 * By convention, uses the POST request type and calls the controller's create method.
	 * @see HttpRequest.Type#POST
	 * @see Controller#create()
	 */
	create, 

	/**
	 * Update a specific model object.<br/>
	 * By convention, uses the PUT request type and calls the controller's update method.
	 * @see HttpRequest.Type#PUT
	 * @see Controller#update()
	 */
	update, 

	/**
	 * Destroy a specific model object.<br/>
	 * By convention, uses the DELETE request type and calls the controller's destroy method.
	 * @see HttpRequest.Type#DELETE
	 * @see Controller#destroy()
	 */
	destroy, 

	/**
	 * Retrieve a specific model object.<br/>
	 * By convention, uses the GET request type and calls the controller's show method.
	 * @see HttpRequest.Type#GET
	 * @see Controller#show()
	 */
	show, 

	/**
	 * Retrieve all models of a given type.<br/>
	 * By convention, uses the GET request type and calls the controller's showAll method.
	 * @see HttpRequest.Type#GET
	 * @see Controller#showAll()
	 */
	showAll, 

	/**
	 * Get an HTML form to edit a specific model object.<br/>
	 * By convention, uses the GET request type and calls the controller's showEdit method.
	 * @see HttpRequest.Type#GET
	 * @see Controller#showEdit()
	 */
	showEdit, 

	/**
	 * Get an HTML form to create a new model object.<br/>
	 * By convention, uses the GET request type and calls the controller's showNew method.
	 * @see HttpRequest.Type#GET
	 * @see Controller#showNew()
	 */
	showNew;

	public RequestType getRequestType() {
		switch(this) {
		case create:	return POST;
		case update:	return PUT;
		case destroy:	return DELETE;
		default:		return GET;
		}
	}
}
