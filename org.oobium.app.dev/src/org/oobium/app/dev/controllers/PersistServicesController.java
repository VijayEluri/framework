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
package org.oobium.app.dev.controllers;

import java.util.List;

import org.oobium.app.controllers.HttpController;
import org.oobium.app.dev.views.persist_services.ShowAllPersistServices;
import org.oobium.app.dev.views.persist_services.ShowPersistService;
import org.oobium.app.persist.PersistServices;
import org.oobium.persist.Model;
import org.oobium.persist.PersistService;

public class PersistServicesController extends HttpController {

	@Override
	public void showAll() throws Exception {
		render(new ShowAllPersistServices((PersistServices) Model.getPersistServiceProvider()));
	}
	
	@Override
	public void show() throws Exception {
		List<PersistService> services = ((PersistServices) Model.getPersistServiceProvider()).getServices();
		int id = getId(int.class);
		int ix = id - 1;
		if(ix >= 0 && ix < services.size()) {
			PersistService service = services.get(ix);
			if(hasParam("q")) {
				String query = param("q");
//				try {
//					if(SqlUtils.isUpdate(query)) {
//						int results = service.executeUpdate(query);
//						render(new ShowUpdateResults(id, query, results));
//					} else {
//						List<Map<String, Object>> results = service.executeQuery(query);
//						render(new ShowQueryResults(id, query, results));
//					}
//				} catch(SQLException e) {
//					setFlashError(e.getMessage());
//					render(new ShowQueryResults(id, query));
//				}
			} else {
				render(new ShowPersistService(id, service));
			}
		}
	}
	
}
