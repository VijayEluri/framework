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
package org.oobium.persist.db.internal;

import static java.lang.Character.isLetterOrDigit;
import static org.oobium.persist.ModelAdapter.getAdapter;
import static org.oobium.persist.db.internal.QueryUtils.ID;
import static org.oobium.persist.db.internal.QueryUtils.INCLUDE;
import static org.oobium.persist.db.internal.QueryUtils.LIMIT;
import static org.oobium.persist.db.internal.QueryUtils.ORDER_BY;
import static org.oobium.persist.db.internal.QueryUtils.WHERE;
import static org.oobium.persist.db.internal.QueryUtils.valuePattern;
import static org.oobium.utils.SqlUtils.safeSqlWord;
import static org.oobium.utils.StringUtils.blank;
import static org.oobium.utils.StringUtils.columnName;
import static org.oobium.utils.StringUtils.join;
import static org.oobium.utils.StringUtils.tableName;
import static org.oobium.utils.json.JsonUtils.toObject;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.regex.Matcher;

import org.oobium.logging.Logger;
import org.oobium.persist.Model;
import org.oobium.persist.ModelAdapter;
import org.oobium.persist.db.DbPersistService;
import org.oobium.utils.json.JsonUtils;

public class QueryBuilder {

	private static Query build(Class<? extends Model> parentClass, String field, Class<? extends Model> clazz, String sql, Object...values) throws SQLException {
		Query query = new Query(parentClass, field, clazz);
		QueryBuilder builder = new QueryBuilder(parentClass, query, sql, values);
		builder.build();
		return query;
	}
	
	public static Query build(Class<? extends Model> clazz, String sql, Object...values) throws SQLException {
		Query query = new Query(clazz);
		QueryBuilder builder = new QueryBuilder(query, sql, values);
		builder.build();
		return query;
	}
	
	static List<Object> processModelIncludes(Class<? extends Model> clazz, String sql, Object...values) throws SQLException {
		Query query = new Query(clazz);
		QueryBuilder builder = new QueryBuilder(query, sql, values);
		builder.build();
		return builder.includes;
	}
	
	ModelAdapter parentAdapter;
	
	Query query;
	ModelAdapter adapter;
	String input;
	Object[] values;

	List<String> columns;
	List<String> tables;
	List<String> whereClauses;
	List<String> orderClauses;
	int offset;
	int limit;
	List<Object> includes;

	String alias;

	private QueryBuilder(Class<? extends Model> parentClass, Query query, String sql, Object...values) {
		this(query, sql, values);
		parentAdapter = ModelAdapter.getAdapter(parentClass);
	}
	
	private QueryBuilder(Query query, String sql, Object...values) {
		this.query = query;

		this.adapter = ModelAdapter.getAdapter(query.getType());
		this.input = sql;
		this.values = values;
		this.columns = new ArrayList<String>();
		this.tables = new ArrayList<String>();
		this.whereClauses = new ArrayList<String>();
		this.orderClauses = new ArrayList<String>();
		
		this.offset = -1;
		this.limit = -1;
	}

	private void addColumns(String alias, ModelAdapter adapter) {
		columns.add(column(alias, ID));
		for(String field : adapter.getFields()) {
			if(!adapter.hasMany(field) && !adapter.isVirtual(field)) {
				columns.add(column(alias, field));
			}
		}
	}
	
	private void addIncludes() {
		if(!addWildcards(adapter, includes)) {
			for(String field : adapter.getRelations()) {
				if(adapter.isIncluded(field)) {
					if(!contains(includes, field)) {
						includes.add(field);
					}
				}
			}
		}
		
		if(!includes.isEmpty()) {
			addIncludes(adapter, includes);
		}
	}

	@SuppressWarnings("unchecked")
	private void addIncludes(ModelAdapter adapter, List<Object> list) {
		for(Object object : list.toArray()) {
			if(object instanceof String) {
				if(isWildcard(getField(object))) {
					addWildcards(adapter, list);
					addIncludes(adapter, list);
				} else {
					addIncludesForStringInList(adapter, list, (String) object);
				}
			} else if(object instanceof Map<?,?>) {
				addIncludesForMap(adapter, (Map<?,Object>) object);
			} else {
				throw new IllegalArgumentException("lists cannot include objects of type " + ((object != null) ? object.getClass().getCanonicalName() : "null"));
			}
		}
	}

	// parentAdapter is the adapter for the container of the map
	private void addIncludesForMap(ModelAdapter parentAdapter, Map<?,Object> map) {
		Entry<?,Object> entry = map.entrySet().iterator().next();
		String field = (String) entry.getKey();
		Object value = entry.getValue();
		Class<? extends Model> clazz = parentAdapter.getRelationClass(getField(field));
		ModelAdapter adapter = getAdapter(clazz);
		List<Object> children = getModelIncludes(adapter);
		if(value instanceof List<?>) {
			for(Object object : (List<?>) value) {
				if(!contains(children, object)) { // object is a String or a Map
					children.add(object);
				}
			}
		} else { // a String or Map
			if(!contains(children, value)) {
				children.add(value);
			}
		}
		addIncludes(adapter, children);
		if(children.size() == 1) {
			entry.setValue(children.get(0));
		} else {
			entry.setValue(children);
		}
	}
	
	// parentAdapter is the adapter for the list
	private void addIncludesForStringInList(ModelAdapter parentAdapter, List<Object> list, String field) {
		Class<? extends Model> clazz = parentAdapter.getRelationClass(getField(field));
		ModelAdapter adapter = getAdapter(clazz);
		List<Object> children = getModelIncludes(adapter);
		if(!children.isEmpty()) {
			addIncludes(adapter, children);
			list.remove(field);
			Map<String, Object> map = new HashMap<String, Object>();
			if(field.matches("\\w+\\s+\\w+")) { // contains whitespace
				StringBuilder sb = new StringBuilder(field.length()+2);
				if(field.charAt(0) != '\'' && field.charAt(0) != '"') {
					sb.append('\'');
				}
				sb.append(field);
				if(field.charAt(0) != '\'' && field.charAt(0) != '"') {
					sb.append('\'');
				}
			}
			if(children.size() == 1) {
				map.put(field, children.get(0));
			} else {
				map.put(field, children);
			}
			list.add(map);
		}
	}
	
	public void addOrders(String orderBy) {
		String order = escape(adapter, orderBy);
		String[] orderBys = order.split("\\s*,\\s*");
		orderClauses.addAll(Arrays.asList(orderBys));
	}

	private String escape(ModelAdapter adapter, String where) {
		StringBuilder sb = new StringBuilder(where);
		appendAliasIfNeeded(sb, "id");
		for(String field : adapter.getFields()) {
			appendAliasIfNeeded(sb, field);
		}
		return sb.toString();
	}
	
	public void addWheres(String where) {
		whereClauses.add(escape(adapter, where));
	}
	
	private boolean addWildcards(ModelAdapter adapter, List<Object> list) {
		if(list.remove("*")) {
			for(String field : adapter.getRelations()) {
				if(!contains(list, field)) {
					list.add(field);
				}
			}
			return true;
		} else {
			if(list.remove("*1")) {
				for(String field : adapter.getHasOneFields()) {
					if(!contains(list, field)) {
						list.add(field);
					}
				}
			}
			if(list.remove("*M")) {
				for(String field : adapter.getHasManyFields()) {
					if(!contains(list, field)) {
						list.add(field);
					}
				}
			}
		}
		return false;
	}

	private void appendAliasIfNeeded(StringBuilder sb, String field) {
		String column = safeSqlWord(columnName(field));
		int ln = column.length();
		int ix = sb.indexOf(column);
		while(ix != -1) {
			if( (ix == 0 || (!isLetterOrDigit(sb.charAt(ix-1)) && sb.charAt(ix-1) != '.')) && 
					(ix+ln == sb.length() || (!isLetterOrDigit(sb.charAt(ix+ln)) && sb.charAt(ix+ln) != '.')) ) {
				sb.insert(ix, '.');
				sb.insert(ix, alias);
			}
			ix = sb.indexOf(column, ix+alias.length()+field.length()+1);
		}
	}

	private void build() throws SQLException {
		try {
			if(parentAdapter != null) {
				initAsSubQuery();
			} else {
				initAsTopLevel();
			}
			parseInput();
			addIncludes();
			if(!blank(includes)) {
				processInclude();
			}
			query.setSql(buildSql());
		} catch(Exception e) {
			Logger.getLogger(DbPersistService.class).warn(e);
			throw new SQLException(e);
		}
	}

	private String buildSql() {
		StringBuilder sb = new StringBuilder();
		sb.append("SELECT ");
		sb.append(join(columns, ','));
		sb.append(" FROM ");
		sb.append(join(tables, ' '));
		if(!whereClauses.isEmpty()) {
			sb.append(" WHERE ");
			sb.append(join(whereClauses, " AND "));
		}
		if(!orderClauses.isEmpty()) {
			sb.append(" ORDER BY ");
			sb.append(join(orderClauses, ','));
		}
		if(offset > 0) {
			if(offset == 1) {
				sb.append(" OFFSET 1 ROW");
			} else {
				sb.append(" OFFSET ").append(offset).append(" ROWS");
			}
		}
		if(limit > 0) {
			if(limit == 1) {
				sb.append(" FETCH NEXT ROW ONLY");
			} else {
				sb.append(" FETCH NEXT ").append(limit).append(" ROWS ONLY");
			}
		}
		return sb.toString();
	}
	
	private String column(String alias, String field) {
		String name = columnName(field);
		if(alias == null) {
			return safeSqlWord(name);
		} else {
			StringBuilder sb = new StringBuilder();
			sb.append(alias).append('.').append(safeSqlWord(name));
			sb.append(' ');
			sb.append(alias).append('_').append(name);
			return sb.toString();
		}
	}

	private boolean contains(List<Object> list, Object object) {
		String field = getField(object);
		for(Object o : list) {
			if(equals(field, o)) {
				return true;
			}
		}
		return false;
	}

	private String getField(Object object) {
		String field;
		if(object instanceof Map<?,?>) {
			field = (String) ((Map<?,?>) object).keySet().iterator().next();
		} else {
			StringBuilder sb = new StringBuilder((String) object);
			if(sb.charAt(0) == '\'') {
				sb.delete(0, 1);
			}
			if(sb.charAt(sb.length()-1) == '\'') {
				sb.delete(sb.length()-1, sb.length());
			}
			field = sb.toString().split("\\s+", 2)[0];
		}
		return field;
	}
	
	private boolean equals(String field, Object object) {
		return (object instanceof String && field.equals(object)) || 
		(object instanceof Map<?, ?> && ((Map<?, ?>) object).containsKey(field)) ||
		(object instanceof List<?> && ((List<?>) object).contains(field));
	}
	
	private List<Object> getModelIncludes(ModelAdapter adapter) {
		List<Object> includes = new ArrayList<Object>();
		for(String field : adapter.getRelations()) {
			if(adapter.isIncluded(field)) {
				includes.add(field);
			}
		}
		return includes;
	}
	
	private void incrementAlias() {
		StringBuilder sb = new StringBuilder(alias);

		int i = sb.length() - 1;
		while(i >= 0 && sb.charAt(i) == 'z') {
			sb.setCharAt(i, 'a');
			i--;
		}
		if(i < 0) {
			sb.insert(0, 'a');
		} else {
			sb.setCharAt(i, (char) ((int) sb.charAt(i) + 1));
		}
		
		alias = sb.toString();
		if("as".equals(alias)) {
			alias = "au";
		}
	}
	
	private void initAsSubQuery() {
		String field = query.getField();
		if(parentAdapter.isThrough(field)) {
			String through = parentAdapter.getThrough(field);
			if(parentAdapter.isManyToOne(through)) {
				String table1 = tableName(parentAdapter.getThroughClass(field));
				String table2 = tableName(query.getType());
				String safeCol1 = safeSqlWord(columnName(field));
				String safeCol2 = safeSqlWord(columnName(parentAdapter.getOpposite(through)));

				String table = table1 + " a INNER JOIN " + table2 + " b ON a." + safeCol1 + "=b.id " +
											"AND a." + safeCol2 + " IN (" + Query.ID_MARKER + ")";
				tables.add(table);
				
				columns.add("a." + safeCol2 + " a_id");
				addColumns("b", adapter);
				alias = "b";
			} else {
				Class<?> class1 = parentAdapter.getModelClass();
				Class<?> class2 = parentAdapter.getThroughClass(field);
				String rel1 = parentAdapter.getThrough(field);
				String rel2 = (parentAdapter.getOpposite(rel1));
				String col1 = columnName(class1, rel1);
				String col2 = columnName(class2, rel2);
				String table1 = tableName(col1, col2);
				String table2 = tableName(class2);
				String table3 = tableName(query.getType());
				
				String safeCol1 = safeSqlWord(col1);
				String safeCol2 = safeSqlWord(col2);
				String safeCol3 = safeSqlWord(field);
				
				String table = table1 + " a INNER JOIN " + table2 + " b0 ON a." + safeCol1 + "=b0.id AND a." + safeCol2 + " IN (" + Query.ID_MARKER + ") " +
											"INNER JOIN " + table3 + " b ON b0." + safeCol3 + "=b.id";
				tables.add(table);
				
				columns.add("a." + safeCol2 + " a_id");
				addColumns("b", adapter);
				alias = "b";
			}
		} else {
			if(parentAdapter.isManyToOne(field)) {
				String col = "b." + safeSqlWord(columnName(parentAdapter.getOpposite(field)));
	
				columns.add(col + " a_id");
				addColumns("b", adapter);
				tables.add(tableName(adapter.getModelClass()) + " b");
				whereClauses.add(col + " IN (" + Query.ID_MARKER + ")");
				alias = "b";
			} else {
				Class<?> class1 = parentAdapter.getModelClass();
				Class<?> class2 = query.getType();
				String rel1 = field;
				String rel2 = (parentAdapter.getOpposite(field));
				String col1 = columnName(class1, rel1);
				String col2 = columnName(class2, rel2);
				String table1 = tableName(col1, col2);
				String table2 = tableName(class2);
				
				String safeCol1 = safeSqlWord(col1);
				String safeCol2 = safeSqlWord(col2);
				
				columns.add("a." + safeCol2 + " a_id");
				addColumns("b", adapter);
				tables.add(table1 + " a INNER JOIN " + table2 + " b ON a." + safeCol1 + "=b.id AND a." + safeCol2 + " IN (" + Query.ID_MARKER + ")");
				alias = "b";
			}
		}
	}

	private void initAsTopLevel() {
		tables.add(tableName(adapter.getModelClass()) + " a");
		addColumns("a", adapter);
		alias = "a";
	}

	private boolean isWildcard(Object s) {
		return "*".equals(s) || "*1".equals(s) || "*M".equals(s);
	}
	
	private void parseInput() {
		if(!blank(input)) {
			String input = this.input.trim();
			String lower = input.toLowerCase();
			int include = lower.indexOf(INCLUDE);
			String[] sa;
			if(include == -1) {
				sa = new String[1];
				sa[0] = lower;
			} else {
				sa = new String[2];
				sa[0] = lower.substring(0, include);
				sa[1] = lower.substring(include);
			}
			int[][] ixs = new int[4][1];
			ixs[0] = new int[] { sa[0].indexOf(WHERE), WHERE.length() };
			ixs[1] = new int[] { sa[0].indexOf(ORDER_BY), ORDER_BY.length() };
			ixs[2] = new int[] { sa[0].indexOf(LIMIT), LIMIT.length() };
			ixs[3] = new int[] { include, INCLUDE.length() };
			if((ixs[0][0] != 0) && (ixs[1][0] != 0) && (ixs[2][0] != 0) && (ixs[3][0] != 0)) {
				throw new IllegalArgumentException("input must start with either: where, order by, limit, include");
			}
			int pos = -1;
			for(int i = 0; i < ixs.length; i++) {
				if(ixs[i][0] != -1) {
					if(ixs[i][0] > pos) {
						pos = ixs[i][0];
					} else {
						throw new IllegalArgumentException("invalid order of input elements.  order must be: where, order by, limit, include");
					}
				}
			}
			for(int i = 0; i < 3; i++) {
				if(ixs[i][0] != -1) {
					int next = sa[0].length();
					for(int j = i+1; j < ixs.length; j++) {
						if(ixs[j][0] != -1) {
							next = ixs[j][0];
							break;
						}
					}
					String s = input.substring(ixs[i][0]+ixs[i][1], next).trim();
					switch(i) {
					case 0: addWheres(s); break;
					case 1: addOrders(s); break;
					case 2: setLimit(s); break;
					}
				}
			}
			if(sa.length == 2) {
				setInclude(input.substring(ixs[3][0]+ixs[3][1]));
			}
		}
		if(includes == null) {
			includes = new ArrayList<Object>();
		}
	}

	private void processInclude() throws NoSuchFieldException, SQLException {
		query.putAdapter(alias, adapter);
		processInclude(alias, includes);
	}
	
	private String processInclude(ModelAdapter parentAdapter, String parentAlias, String field, String where) throws NoSuchFieldException {
		Class<? extends Model> includeClass = parentAdapter.getRelationClass(field);
		String includeTable = tableName(includeClass);
		
		incrementAlias();

		ModelAdapter adapter = ModelAdapter.getAdapter(includeClass);
		addColumns(alias, adapter);
		query.putParentAlias(alias, parentAlias);
		query.putAdapter(alias, adapter);
		query.putField(alias, field);
		
		String column = safeSqlWord(columnName(field));
		
		StringBuilder sb = new StringBuilder();
		sb.append("LEFT JOIN ").append(includeTable).append(' ').append(alias);
		sb.append(" ON ").append(parentAlias).append('.').append(column).append('=').append(alias).append(".id");
		if(!blank(where)) {
			sb.append(" AND ").append(escape(adapter, where));
		}
		
		tables.add(sb.toString());
		
		return alias;
	}

	private void processInclude(String parentAlias, Object obj) throws NoSuchFieldException, SQLException {
		if(obj instanceof List<?>) {
			List<?> list = (List<?>) obj;
			for(Object o : list) {
				processInclude(parentAlias, o);
			}
		} else {
			String field = null;
			String where = null;
			Object child = null;
			if(obj instanceof String) {
				field = (String) obj;
			} else if(obj instanceof Map<?,?>) {
				Map<?, ?> map = (Map<?, ?>) obj;
				if(map.size() != 1) {
					throw new IllegalArgumentException("a map in the include statement must contain one, and only one, entry");
				}
				Entry<?, ?> entry = map.entrySet().iterator().next();
				field = (String) entry.getKey();
				child = entry.getValue();
			}
			int ix = field.toLowerCase().indexOf(" where ");
			if(ix != -1) {
				where = field.substring(ix + 7);
				field = field.substring(0, ix);
			}

			ModelAdapter parentAdapter = query.getAdapter(parentAlias);
			if(parentAdapter.hasOne(field)) {
				String alias = processInclude(parentAdapter, parentAlias, field, where);
				if(!blank(child)) {
					processInclude(alias, child);
				}
			} else if(parentAdapter.hasMany(field)) {
				Class<? extends Model> parentClass = parentAdapter.getModelClass();
				Class<? extends Model> clazz = parentAdapter.getRelationClass(field);
				String sql = blank(child) ? "" : ("include:" + JsonUtils.toJson(child));
				Query query = QueryBuilder.build(parentClass, field, clazz, sql);
				this.query.addChild(query);
			} else {
				throw new SQLException("field not found: " + field + " in " + parentAdapter);
			}
		}
	}

	@SuppressWarnings("unchecked")
	public void setInclude(String include) {
		StringBuilder sb = new StringBuilder(include);
		Matcher matcher = valuePattern.matcher(sb);
		while(matcher.find()) {
			int ix = Integer.parseInt(matcher.group(1));
			sb.replace(matcher.start(), matcher.end(), (String) values[ix]);
		}
		Object object = toObject(sb.toString());
		if(object instanceof List<?>) {
			this.includes = (List<Object>) object;
		} else {
			this.includes = new ArrayList<Object>();
			this.includes.add(object);
		}
	}
	
	void setLimit(String limit) {
		StringBuilder sb = new StringBuilder(limit);
		Matcher matcher = valuePattern.matcher(sb);
		while(matcher.find()) {
			int ix = Integer.parseInt(matcher.group(1));
			sb.replace(matcher.start(), matcher.end(), (String) values[ix]);
		}
		String[] sa = sb.toString().trim().split("\\s*,\\s*");
		if(sa.length == 1) {
			this.offset = 0;
			this.limit = Integer.parseInt(sa[0]);
		} else if(sa.length == 2) {
			this.offset = Integer.parseInt(sa[0]);
			this.limit = Integer.parseInt(sa[1]);
		} else {
			throw new IllegalStateException("cannot parse LIMIT clause: " + limit);
		}
	}
	
}
