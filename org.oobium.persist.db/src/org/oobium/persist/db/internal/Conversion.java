package org.oobium.persist.db.internal;

import static org.oobium.utils.SqlUtils.safeSqlWord;
import static org.oobium.utils.StringUtils.columnName;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.oobium.persist.Model;
import org.oobium.persist.ModelAdapter;
import org.oobium.utils.json.JsonUtils;

public class Conversion {

	private static final String IS = "=";
	private static final String NOT = "!=";

	private static final Map<String, String> operators;
	static {
		operators = new HashMap<String, String>();
		operators.put("is", IS);
		operators.put("$is", IS);
		operators.put("not", NOT);
		operators.put("$not", NOT);
		operators.put("$ne", NOT);
		operators.put("lt", "<");
		operators.put("$lt", "<");
		operators.put("lte", "<=");
		operators.put("$lte", "<=");
		operators.put("gt", ">");
		operators.put("$gt", ">");
		operators.put("gte", ">=");
		operators.put("$gte", ">=");
		operators.put("in", " in ");
		operators.put("$in", " in ");
		operators.put("nin", " not in ");
		operators.put("$nin", " not in ");
		operators.put("or", " or ");
		operators.put("$or", " or ");
		operators.put("and", " and ");
		operators.put("$and", " and ");
		operators.put("like", " like ");
		operators.put("$like", " like ");
		operators.put("nlike", " not like ");
		operators.put("$nlike", " not like ");
	}
	
	public static Conversion run(int dbType, Class<? extends Model> modelType, Map<String, Object> map, Object...values) throws Exception {
		Conversion conversion = new Conversion(dbType, map, values);
		conversion.setModelType(modelType);
		conversion.run();
		return conversion;
	}
	
	public static Conversion run(int dbType, Class<? extends Model> modelType, String query, Object...values) throws Exception {
		Map<String, Object> map = JsonUtils.toMap(query, true);
		return run(dbType, modelType, map, values);
	}
	
	
	private final int dbType;
	private final Map<String, Object> inQuery;
	private final Object[] inValues;
	private ModelAdapter adapter;
	
	private String sql;
	private Object[] values;
	
	private StringBuilder sb;
	private List<Object> list;
	private int v;
	
	public Conversion(int dbType, Map<String, Object> query, Object...values) {
		this.dbType = dbType;
		this.inQuery = query;
		this.inValues = values;
	}
	
	private void add(String field, String key, Object value) throws Exception {
		checkField(field);
		
		if(value == null) {
			String s = operators.get(key);
			if(s == IS) {
				sb.append(column(field)).append(" is null");
			} else if(s == NOT) {
				sb.append(column(field)).append(" is not null");
			} else {
				throw new Exception("cannot perform operation '" + key + "' on a null value");
			}
		} else {
			sb.append(column(field)).append(operators.get(key)).append('?');
			if(value.equals("?")) {
				list.add(inValues[v++]);
			} else {
				list.add(value);
			}
		}
	}

	private void and(Object value) throws Exception {
		if(value instanceof Map) {
			handle((Map<?,?>) value, " and ");
		}
		else if(value instanceof List) {
			throw new Exception("'and' not yet supported on a List");
		}
		else {
			throw new Exception("'and' not allowed on " + value);
		}
	}
	
	private void apply(StringBuilder sb, Object order, Object limit, Object include) throws Exception {
		boolean first = (sb.length() == 0 || sb.charAt(sb.length()-1) == ' ');
		if(order != null) {
			first = first(sb, first);
			applyOrder(sb, order);
		}
		if(limit != null) {
			first = first(sb, first);
			applyLimit(sb, limit);
		}
		if(include != null) {
			first = first(sb, first);
			sb.append("include:").append(include);
			if("?".equals(include)) list.add(inValues[v++]);
		}
	}

	private void applyLimit(StringBuilder sb, Object limit) throws Exception {
		sb.append("limit ");
		String value = String.valueOf("?".equals(limit) ? inValues[v++] : limit); 
		String[] sa = value.trim().split("\\s*,\\s*");
		if(sa.length == 1) {
			try {
				int i = Integer.parseInt(sa[0]);
				sb.append(i);
			} catch(NumberFormatException e) {
				throw new Exception("illegal parameter in LIMIT clause: " + value);
			}
		}
		else if(sa.length == 2) {
			try {
				int o = Integer.parseInt(sa[0]);
				int l = Integer.parseInt(sa[1]);
				sb.append(o).append(',').append(l);
			} catch(NumberFormatException e) {
				throw new Exception("illegal parameter in LIMIT clause: " + value);
			}
		}
		else {
			throw new Exception("invalid format of LIMIT clause: " + value);
		}
	}

	private void applyOrder(StringBuilder sb, Object order) throws Exception {
		sb.append("order by ");
		String value = String.valueOf("?".equals(order) ? inValues[v++] : order); 
		String[] sa1 = String.valueOf(value).trim().split("\\s*,\\s*");
		for(int i = 0; i < sa1.length; i++) {
			if(i != 0) sb.append(",");
			String[] sa2 = sa1[i].split("\\s+");
			checkField(sa2[0]);
			if(sa2.length == 1) {
				sb.append(column(sa2[0]));
			}
			else if(sa2.length > 1) {
				sb.append(column(sa2[0]));
				if("DESC".equalsIgnoreCase(sa2[1])) {
					sb.append(" desc");
				}
				else if("ASC".equalsIgnoreCase(sa2[1])) {
					sb.append(" asc");
				}
				else {
					throw new Exception("unknown direction in ORDER BY clause: " + value);
				}
			}
			else {
				throw new Exception("invalid format of ORDER BY clause: " + value);
			}
		}
	}
	
	private void checkField(String field) throws Exception {
		if(adapter != null && !adapter.hasField(field) && !"id".equals(field)) {
			throw new Exception("model of type " + adapter.getModelClass() + " does not contain the field '" + field + "'");
		}
	}
	
	private String column(String field) {
		return safeSqlWord(dbType, columnName(field));
	}
	
	private boolean first(StringBuilder sb, boolean first) {
		if(!first) {
			sb.append(' ');
			return false;
		}
		return true;
	}

	public String getSql() {
		return sql;
	}

	public Object[] getValues() {
		return values;
	}

	private void handle(Map<?,?> map, String separator) throws Exception {
		for(Iterator<?> iter = map.entrySet().iterator(); iter.hasNext(); ) {
			Entry<?,?> entry = (Entry<?,?>) iter.next();

			String field = String.valueOf(entry.getKey());
			Object value = entry.getValue();
			
			if("and".equalsIgnoreCase(field)) {
				if(map.size() == 1) {
					and(value);
				}
				else if(map.size() > 1) {
					sb.append('(');
					and(value);
					sb.append(')');
				}
			}
			else if("or".equalsIgnoreCase(field)) {
				if(map.size() == 1) {
					or(value);
				}
				else if(map.size() > 1) {
					sb.append('(');
					or(value);
					sb.append(')');
				}
			}
			else {
				if(value instanceof Map) {
					Map<?,?> m = (Map<?,?>) value;
					if(m.size() == 1) {
						Entry<?,?> e = m.entrySet().iterator().next();
						String key = String.valueOf(e.getKey()).toLowerCase();
						Object val = e.getValue();
						add(field, key, val);
					}
					else if(m.size() > 1) {
						sb.append("(");
						for(Iterator<?> i = m.entrySet().iterator(); i.hasNext(); ) {
							Entry<?,?> e = (Entry<?,?>) i.next();
							String key = String.valueOf(e.getKey()).toLowerCase();
							Object val = e.getValue();
							add(field, key, val);
							if(i.hasNext()) {
								sb.append(" and ");
							}
						}
						sb.append(")");
					}
				}
				else {
					add(field, "is", value);
				}
	
				if(iter.hasNext()) {
					sb.append(separator);
				}
			}
		}
	}
	
	private void or(Object value) throws Exception {
		if(value instanceof Map) {
			handle((Map<?,?>) value, " or ");
		}
		else if(value instanceof List) {
			throw new Exception("'or' not yet supported on a List");
		}
		else {
			throw new Exception("'or' not allowed on " + value);
		}
	}
	
	private Object removeInclude() {
		Object order = inQuery.remove("$include");
		if(order == null) order = inQuery.remove("include");
		return order;
	}
	
	private Object removeLimit() {
		Object order = inQuery.remove("$limit");
		if(order == null) order = inQuery.remove("limit");
		return order;
	}
	
	private Object removeSort() {
		Object order = inQuery.remove("$sort");
		if(order == null) order = inQuery.remove("sort");
		if(order == null) order = inQuery.remove("$order");
		if(order == null) order = inQuery.remove("order");
		return order;
	}
	
	public void run() throws Exception {
		v = 0;
		sb = new StringBuilder();
		list = new ArrayList<Object>();

		Object order = removeSort();
		Object limit = removeLimit();
		Object include = removeInclude();

		if(inQuery.isEmpty()) {
			apply(sb, order, limit, include);
		} else {
			sb.append("where ");
			and(inQuery);
			apply(sb, order, limit, include);
		}
		
		sql = sb.toString();
		values = list.toArray(new Object[list.size()]);
	}
	
	public void setModelType(Class<? extends Model> modelType) {
		this.adapter = ModelAdapter.getAdapter(modelType);
	}
	
}
