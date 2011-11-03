package org.oobium.persist.db.internal;

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

	private static final Map<String, String> operators;
	static {
		operators = new HashMap<String, String>();
		operators.put("is", "=");
		operators.put("not", "!=");
		operators.put("lt", "<");
		operators.put("lte", "<=");
		operators.put("gt", ">");
		operators.put("gte", ">=");
		operators.put("in", " IN ");
		operators.put("nin", " NOT IN ");
		operators.put("or", " OR ");
		operators.put("and", " AND ");
		operators.put("like", " LIKE ");
		operators.put("nlike", " NOT LIKE ");
	}
	
	public static Conversion run(Class<? extends Model> modelType, Map<String, Object> map, Object...values) throws Exception {
		Conversion conversion = new Conversion(map, values);
		conversion.setModelType(modelType);
		conversion.run();
		return conversion;
	}
	
	public static Conversion run(Class<? extends Model> modelType, String query, Object...values) throws Exception {
		Map<String, Object> map = JsonUtils.toMap(query, true);
		return run(modelType, map, values);
	}
	
	
	private final Map<String, Object> inQuery;
	private final Object[] inValues;
	private ModelAdapter adapter;
	
	private String sql;
	private Object[] values;
	
	private StringBuilder sb;
	private List<Object> list;
	private int v;
	
	public Conversion(Map<String, Object> query, Object...values) {
		this.inQuery = query;
		this.inValues = values;
	}
	
	private void add(String field, String key, Object value) throws Exception {
		if(adapter != null && !adapter.hasField(field) && !"id".equals(field)) {
			throw new Exception("model of type " + adapter.getModelClass() + " does not contain the field '" + field + "'");
		}
		
		sb.append(field).append(operators.get(key)).append('?');
		
		if("?".equals(value)) {
			list.add(inValues[v++]);
		} else {
			list.add(value);
		}
	}

	private void and(Object value) throws Exception {
		if(value instanceof Map) {
			handle((Map<?,?>) value, " AND ");
		}
		else if(value instanceof List) {
			throw new Exception("'and' not yet supported on a List");
		}
		else {
			throw new Exception("'and' not allowed on " + value);
		}
	}

	private void apply(StringBuilder sb, Object order, Object limit, Object include) {
		boolean first = (sb.length() == 0 || sb.charAt(sb.length()-1) == ' ');
		if(order != null) {
			first = first(sb, first);
			sb.append("ORDER BY ").append(order);
			if("?".equals(order)) list.add(inValues[v++]);
		}
		if(limit != null) {
			first = first(sb, first);
			sb.append("LIMIT ").append(limit);
			if("?".equals(limit)) list.add(inValues[v++]);
		}
		if(include != null) {
			first = first(sb, first);
			sb.append("INCLUDE:").append(include);
			if("?".equals(include)) list.add(inValues[v++]);
		}
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
								sb.append(" AND ");
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
			handle((Map<?,?>) value, " OR ");
		}
		else if(value instanceof List) {
			throw new Exception("'or' not yet supported on a List");
		}
		else {
			throw new Exception("'or' not allowed on " + value);
		}
	}
	
	public void run() throws Exception {
		v = 0;
		sb = new StringBuilder();
		list = new ArrayList<Object>();

		Object order = inQuery.remove("$sort");
		if(order == null) {
			order = inQuery.remove("$order");
		}
		Object limit = inQuery.remove("$limit");
		Object include = inQuery.remove("$include");

		if(inQuery.isEmpty()) {
			apply(sb, order, limit, include);
		} else {
			sb.append("WHERE ");
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
