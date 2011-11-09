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
package org.oobium.app.routing;

import static org.oobium.app.http.Action.*;
import static org.oobium.utils.CharStreamUtils.closer;
import static org.oobium.utils.CharStreamUtils.find;
import static org.oobium.utils.CharStreamUtils.findAny;
import static org.oobium.utils.CharStreamUtils.isEqual;
import static org.oobium.utils.StringUtils.*;

import java.util.List;

import org.oobium.app.routing.routes.HttpRoute;
import org.oobium.app.http.Action;
import org.oobium.persist.Model;
import org.oobium.persist.ModelAdapter;

public class Routes {

	final Router router;
	private final List<Routed> routed;
	
	private final Class<? extends Model> parentClass;
	private final String baseRule;
	
	
	Routes(Router router, List<Routed> routed, Class<? extends Model> parentClass, String baseRule) {
		this.router = router;
		this.routed = routed;
		this.parentClass = parentClass;
		this.baseRule = baseRule;
	}
	
	private void addHasMany(ModelAdapter adapter, String field, Action[] actions) {
		Class<? extends Model> hasManyClass = adapter.getRelationClass(field);
		if(hasManyClass == null) {
			throw new IllegalArgumentException(parentClass.getSimpleName() + " does not contain a field called " + field);
		}

		String rule = baseRule;
		int pix = rule.indexOf('?');
		if(pix == -1) {
			pix = rule.length();
		} else if(pix == 0) {
			rule = "{models}/{id}" + rule;
			pix = 13;
		}

		String parentName = null;
		String parentId = null;
		int[] models = null;
		int[] id = null;

		char[] ca = rule.toCharArray();
		int s1 = find(ca, '{');
		while(s1 != -1 && s1 < pix) {
			int s2 = closer(ca, s1);
			if(s2 == -1) {
				throw new RoutingException("missing closer for variable starting at " + s1 + " in: " + rule);
			}
			int ix = findAny(ca, s1, s2, '=', ':');
			if(ix == -1) ix = s2;
			if(isEqual(ca, s1+1, ix, 'm','o','d','e','l','s')) {
				if(ix < s2 && ca[ix] == '=') {
					parentName = new String(ca, ix+1, s2-ix-1);
				}
				models = new int[] { s1, s2+1 };
				if(id != null) break;
			} else if(isEqual(ca, s1+1, ix, 'i','d')) {
				if(ix < s2 && ca[ix] == '=') {
					int ix2 = find(ca, ':', ix+1);
					if(ix2 == -1) {
						id = new int[] { s1+1, ix+2 };
					} else {
						parentId = new String(ca, ix+1, ix2-ix-1);
						id = new int[] { s1+1, ix2 };
					}
				} else {
					id = new int[] { s1+1, ix };
				}
				if(models != null) break;
			}
			s1 = find(ca, '{', s2 + 1);
		}

		if(parentName == null) {
			parentName = tableName(parentClass);
		}
		if(parentId == null) {
			parentId = "id";
		}
		
		String parentIdParam = varName(parentClass) + "[" + parentId + "]";
		
		StringBuilder sb = new StringBuilder(rule.length() + 50);
		sb.append(rule);
		if(models[0] > id[0]) {
			sb.replace(models[0], models[1], parentName);
			sb.replace(id[0], id[1], parentIdParam);
		} else {
			sb.replace(id[0], id[1], parentIdParam);
			sb.replace(models[0], models[1], parentName);
		}
//		sb.append("/{models}");
		sb.append('/').append(field);

		String hasManyParam = adapter.hasOpposite(field) ? (varName(hasManyClass) + "[" + adapter.getOpposite(field) + "]") : null;
		
		rule = sb.toString();
		
		if(actions.length == 0) {
			actions = new Action[] { create, showAll, showNew };
		}
		
		for(Action action : actions) {
			String key = router.getKey(parentClass, field, action);
			HttpRoute route = null;
			switch(action) {
			case create:
				route = router.addRoute(key, rule, parentClass, field, hasManyClass, action);
				updateParams(route, parentIdParam, hasManyParam);
				break;
			case showAll:
				route = router.addRoute(key, rule, parentClass, field, hasManyClass, action);
				break;
			case showNew:
				route = router.addRoute(key, rule + "/new", parentClass, field, hasManyClass, action);
				updateParams(route, parentIdParam, hasManyParam);
				break;
			case destroy:
			case show:
			case showEdit:
			case update:
				throw new IllegalArgumentException("invalid action for a hasMany route: " + action + " (only create, showAll, and showNew are valid)");
			default:
				throw new IllegalArgumentException("unknown action: " + action);
			}
			router.addHasMany(router.getKey(parentClass, action), key);
			routed.add(new Routed(router, route));
		}
	}

	public RoutedRoutes hasMany() {
		if(baseRule == null) {
			throw new IllegalArgumentException("base rule is null");
		}

		ModelAdapter adapter = ModelAdapter.getAdapter(parentClass);
		for(String field : adapter.getHasManyFields()) {
			addHasMany(adapter, field, new Action[0]);
		}

		return new RoutedRoutes(this, routed);
	}
	
	public RoutedRoutes hasMany(String field, Action...actions) {
		if(baseRule == null) {
			throw new IllegalArgumentException("base rule is null");
		}

		ModelAdapter adapter = ModelAdapter.getAdapter(parentClass);
		addHasMany(adapter, field, actions);
		
		return new RoutedRoutes(this, routed);
	}

	public Routes publish() {
		if(routed != null) {
			for(Routed r : routed) {
				r.publish();
			}
		}
		return this;
	}
	
	public Routes setRealm(String realm) {
		if(routed != null) {
			for(Routed r : routed) {
				r.setRealm(realm);
			}
		}
		return this;
	}

	private void updateParams(HttpRoute route, String parentIdParam, String hasManyParam) {
		if(hasManyParam != null) {
			for(int i = 0; i < route.params.length; i++) {
				if(route.params[i][0].equals(parentIdParam)) {
					int ix1 = parentIdParam.indexOf('[');
					int ix2 = parentIdParam.indexOf("[id]");
					if(ix1 == -1 || ix1 == ix2) {
						route.params[i][0] = hasManyParam;
					} else {
						route.params[i][0] = hasManyParam + route.params[i][0].substring(ix1);
					}
					route.setString();
					break;
				}
			}
		}
	}
	
}
