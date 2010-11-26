///*******************************************************************************
// * Copyright (c) 2010 Oobium, Inc.
// * All rights reserved. This program and the accompanying materials
// * are made available under the terms of the Eclipse Public License v1.0
// * which accompanies this distribution, and is available at
// * http://www.eclipse.org/legal/epl-v10.html
// * 
// * Contributors:
// *     Jeremy Dowdall <jeremy@oobium.com> - initial API and implementation
// ******************************************************************************/
//package org.oobium.persist;
//
//public class ValidationError {
//
//	public static ValidationError notFound() {
//		return new ValidationError("not found");
//	}
//	
//	public static ValidationError required(String field) {
//		return new ValidationError(field + " is required");
//	}
//	
//	public static ValidationError invalidFormat(String field) {
//		return new ValidationError("The format of " + field + " is invalid");
//	}
//	
//	private String message;
//
//	public ValidationError(String message) {
//		this.message = message;
//	}
//
//	public String getMessage() {
//		return message;
//	}
//
//	@Override
//	public String toString() {
//		return message;
//	}
//	
//}
