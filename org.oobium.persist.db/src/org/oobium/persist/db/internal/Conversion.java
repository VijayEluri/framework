package org.oobium.persist.db.internal;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

public class Conversion {

	private static final Map<String, String> operators;
	static {
		operators = new HashMap<String, String>();
		operators.put("lt", "<");
		operators.put("lte", "<=");
		operators.put("gt", ">");
		operators.put("gte", ">=");
		operators.put("in", " IN ");
		operators.put("nin", " NOT IN ");
		operators.put("not", "!=");
		operators.put("or", " OR ");
		operators.put("and", " AND ");
		operators.put("like", " LIKE ");
		operators.put("nlike", " NOT LIKE ");
	}
	
	
	private final Map<String, Object> inQuery;
	private final Object[] inValues;
	
	private String sql;
	private Object[] values;
	
	private StringBuilder sb;
	private List<Object> list;
	private int v;
	private String limit;
	private Object include;
	
	public Conversion(Map<String, Object> query, Object...values) {
		this.inQuery = query;
		this.inValues = values;
		convert();
	}
	
	public String getSql() {
		return sql;
	}

	public Object[] getValues() {
		return values;
	}
	
	private void add(String field, String key, Object value) {
		sb.append(field).append(operators.get(key)).append('?');
		
		if("?".equals(value)) {
			list.add(inValues[v++]);
		} else {
			list.add(value);
		}
	}
	
	private void and(Object value) {
		if(value instanceof Map) {
			handle((Map<?,?>) value, " AND ");
		}
		else if(value instanceof List) {
			throw new UnsupportedOperationException("'and' not yet supported on a List");
		}
		else {
			throw new IllegalStateException("'and' not allowed on " + value);
		}
	}

	private void or(Object value) {
		if(value instanceof Map) {
			handle((Map<?,?>) value, " OR ");
		}
		else if(value instanceof List) {
			throw new UnsupportedOperationException("'or' not yet supported on a List");
		}
		else {
			throw new IllegalStateException("'or' not allowed on " + value);
		}
	}

	private void removeLast(String separator) {
		if(sb.length() > separator.length() && separator.equals(sb.substring(sb.length()-separator.length()))) {
			sb.delete(sb.length()-separator.length(), sb.length());
		}
	}
	
	private void handle(Map<?,?> map, String separator) {
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
			else if("$limit".equalsIgnoreCase(field)) {
				limit = String.valueOf(value);
				removeLast(separator);
			}
			else if("$include".equalsIgnoreCase(field)) {
				include = String.valueOf(value);
				removeLast(separator);
			}
			else {
				if("?".equals(value)) {
					sb.append(field).append("=?");
					list.add(inValues[v++]);
				}
				else if(value instanceof Map) {
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
					sb.append(field).append("=?");
					list.add(value);
				}
	
				if(iter.hasNext()) {
					sb.append(separator);
				}
			}
		}
	}
	
	public void convert() {
		v = 0;
		sb = new StringBuilder();
		list = new ArrayList<Object>();

		sb.append("WHERE ");
		
		and(inQuery);
		
		if(limit != null) {
			sb.append(" LIMIT ").append(limit);
		}
		if(include != null) {
			sb.append(" INCLUDE:").append(include);
		}
		
		sql = sb.toString();
		values = list.toArray(new Object[list.size()]);
	}
	
}
