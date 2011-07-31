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
package org.oobium.utils;

import static org.oobium.utils.CharStreamUtils.forward;
import static org.oobium.utils.CharStreamUtils.reverse;
import static org.oobium.utils.StringUtils.blank;
import static org.oobium.utils.StringUtils.varName;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Time;
import java.sql.Timestamp;
import java.sql.Types;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import org.oobium.logging.Logger;
import org.oobium.logging.LogProvider;
import org.oobium.utils.json.JsonModel;
import org.oobium.utils.json.JsonUtils;

public class SqlUtils {

	private static final Logger logger = LogProvider.getLogger(SqlUtils.class);
	
	private static final Set<String> _reservedWords = new HashSet<String>();
	public static final Set<String> reservedWords = Collections.unmodifiableSet(_reservedWords);

	static {
	    _reservedWords.add("ADD".toLowerCase());
	    _reservedWords.add("ALL".toLowerCase());
	    _reservedWords.add("ALLOCATE".toLowerCase());
	    _reservedWords.add("ALTER".toLowerCase());
	    _reservedWords.add("AND".toLowerCase());
	    _reservedWords.add("ANY".toLowerCase());
	    _reservedWords.add("ARE".toLowerCase());
	    _reservedWords.add("AS".toLowerCase());
	    _reservedWords.add("ASC".toLowerCase());
	    _reservedWords.add("ASSERTION".toLowerCase());
	    _reservedWords.add("AT".toLowerCase());
	    _reservedWords.add("AUTHORIZATION".toLowerCase());
	    _reservedWords.add("AVG".toLowerCase());
	    _reservedWords.add("BEGIN".toLowerCase());
	    _reservedWords.add("BETWEEN".toLowerCase());
	    _reservedWords.add("BIT".toLowerCase());
	    _reservedWords.add("BOOLEAN".toLowerCase());
	    _reservedWords.add("BOTH".toLowerCase());
	    _reservedWords.add("BY".toLowerCase());
	    _reservedWords.add("CALL".toLowerCase());
	    _reservedWords.add("CASCADE".toLowerCase());
	    _reservedWords.add("CASCADED".toLowerCase());
	    _reservedWords.add("CASE".toLowerCase());
	    _reservedWords.add("CAST".toLowerCase());
	    _reservedWords.add("CHAR".toLowerCase());
	    _reservedWords.add("CHARACTER".toLowerCase());
	    _reservedWords.add("CHECK".toLowerCase());
	    _reservedWords.add("CLOSE".toLowerCase());
	    _reservedWords.add("COLLATE".toLowerCase());
	    _reservedWords.add("COLLATION".toLowerCase());
	    _reservedWords.add("COLUMN".toLowerCase());
	    _reservedWords.add("COMMIT".toLowerCase());
	    _reservedWords.add("CONNECT".toLowerCase());
	    _reservedWords.add("CONNECTION".toLowerCase());
	    _reservedWords.add("CONSTRAINT".toLowerCase());
	    _reservedWords.add("CONSTRAINTS".toLowerCase());
	    _reservedWords.add("CONTINUE".toLowerCase());
	    _reservedWords.add("CONVERT".toLowerCase());
	    _reservedWords.add("CORRESPONDING".toLowerCase());
	    _reservedWords.add("COUNT".toLowerCase());
	    _reservedWords.add("CREATE".toLowerCase());
	    _reservedWords.add("CURRENT".toLowerCase());
	    _reservedWords.add("CURRENT_DATE".toLowerCase());
	    _reservedWords.add("CURRENT_TIME".toLowerCase());
	    _reservedWords.add("CURRENT_TIMESTAMP".toLowerCase());
	    _reservedWords.add("CURRENT_USER".toLowerCase());
	    _reservedWords.add("CURSOR".toLowerCase());
	    _reservedWords.add("DEALLOCATE".toLowerCase());
	    _reservedWords.add("DEC".toLowerCase());
	    _reservedWords.add("DECIMAL".toLowerCase());
	    _reservedWords.add("DECLARE".toLowerCase());
	    _reservedWords.add("DEFERRABLE".toLowerCase());
	    _reservedWords.add("DEFERRED".toLowerCase());
	    _reservedWords.add("DELETE".toLowerCase());
	    _reservedWords.add("DESC".toLowerCase());
	    _reservedWords.add("DESCRIBE".toLowerCase());
	    _reservedWords.add("DIAGNOSTICS".toLowerCase());
	    _reservedWords.add("DISCONNECT".toLowerCase());
	    _reservedWords.add("DISTINCT".toLowerCase());
	    _reservedWords.add("DOUBLE".toLowerCase());
	    _reservedWords.add("DROP".toLowerCase());
	    _reservedWords.add("ELSE".toLowerCase());
	    _reservedWords.add("END".toLowerCase());
	    _reservedWords.add("ENDEXEC".toLowerCase());
	    _reservedWords.add("ESCAPE".toLowerCase());
	    _reservedWords.add("EXCEPT".toLowerCase());
	    _reservedWords.add("EXCEPTION".toLowerCase());
	    _reservedWords.add("EXEC".toLowerCase());
	    _reservedWords.add("EXECUTE".toLowerCase());
	    _reservedWords.add("EXISTS".toLowerCase());
	    _reservedWords.add("EXPLAIN".toLowerCase());
	    _reservedWords.add("EXTERNAL".toLowerCase());
	    _reservedWords.add("FALSE".toLowerCase());
	    _reservedWords.add("FETCH".toLowerCase());
	    _reservedWords.add("FIRST".toLowerCase());
	    _reservedWords.add("FLOAT".toLowerCase());
	    _reservedWords.add("FOR".toLowerCase());
	    _reservedWords.add("FOREIGN".toLowerCase());
	    _reservedWords.add("FOUND".toLowerCase());
	    _reservedWords.add("FROM".toLowerCase());
	    _reservedWords.add("FULL".toLowerCase());
	    _reservedWords.add("FUNCTION".toLowerCase());
	    _reservedWords.add("GET".toLowerCase());
	    _reservedWords.add("GET_CURRENT_CONNECTION".toLowerCase());
	    _reservedWords.add("GLOBAL".toLowerCase());
	    _reservedWords.add("GO".toLowerCase());
	    _reservedWords.add("GOTO".toLowerCase());
	    _reservedWords.add("GRANT".toLowerCase());
	    _reservedWords.add("GROUP".toLowerCase());
	    _reservedWords.add("HAVING".toLowerCase());
	    _reservedWords.add("HOUR".toLowerCase());
	    _reservedWords.add("IDENTITY".toLowerCase());
	    _reservedWords.add("IMMEDIATE".toLowerCase());
	    _reservedWords.add("IN".toLowerCase());
	    _reservedWords.add("INDICATOR".toLowerCase());
	    _reservedWords.add("INITIALLY".toLowerCase());
	    _reservedWords.add("INNER".toLowerCase());
	    _reservedWords.add("INOUT".toLowerCase());
	    _reservedWords.add("INPUT".toLowerCase());
	    _reservedWords.add("INSENSITIVE".toLowerCase());
	    _reservedWords.add("INSERT".toLowerCase());
	    _reservedWords.add("INT".toLowerCase());
	    _reservedWords.add("INTEGER".toLowerCase());
	    _reservedWords.add("INTERSECT".toLowerCase());
	    _reservedWords.add("INTO".toLowerCase());
	    _reservedWords.add("IS".toLowerCase());
	    _reservedWords.add("ISOLATION".toLowerCase());
	    _reservedWords.add("JOIN".toLowerCase());
	    _reservedWords.add("KEY".toLowerCase());
	    _reservedWords.add("LAST".toLowerCase());
	    _reservedWords.add("LEFT".toLowerCase());
	    _reservedWords.add("LIKE".toLowerCase());
	    _reservedWords.add("LONGINT".toLowerCase());
	    _reservedWords.add("LOWER".toLowerCase());
	    _reservedWords.add("LTRIM".toLowerCase());
	    _reservedWords.add("MATCH".toLowerCase());
	    _reservedWords.add("MAX".toLowerCase());
	    _reservedWords.add("MIN".toLowerCase());
	    _reservedWords.add("MINUTE".toLowerCase());
	    _reservedWords.add("NATIONAL".toLowerCase());
	    _reservedWords.add("NATURAL".toLowerCase());
	    _reservedWords.add("NCHAR".toLowerCase());
	    _reservedWords.add("NVARCHAR".toLowerCase());
	    _reservedWords.add("NEXT".toLowerCase());
	    _reservedWords.add("NO".toLowerCase());
	    _reservedWords.add("NOT".toLowerCase());
	    _reservedWords.add("NULL".toLowerCase());
	    _reservedWords.add("NULLIF".toLowerCase());
	    _reservedWords.add("NUMERIC".toLowerCase());
	    _reservedWords.add("OF".toLowerCase());
	    _reservedWords.add("ON".toLowerCase());
	    _reservedWords.add("ONLY".toLowerCase());
	    _reservedWords.add("OPEN".toLowerCase());
	    _reservedWords.add("OPTION".toLowerCase());
	    _reservedWords.add("OR".toLowerCase());
	    _reservedWords.add("ORDER".toLowerCase());
	    _reservedWords.add("OUT".toLowerCase());
	    _reservedWords.add("OUTER".toLowerCase());
	    _reservedWords.add("OUTPUT".toLowerCase());
	    _reservedWords.add("OVERLAPS".toLowerCase());
	    _reservedWords.add("PAD".toLowerCase());
	    _reservedWords.add("PARTIAL".toLowerCase());
	    _reservedWords.add("PREPARE".toLowerCase());
	    _reservedWords.add("PRESERVE".toLowerCase());
	    _reservedWords.add("PRIMARY".toLowerCase());
	    _reservedWords.add("PRIOR".toLowerCase());
	    _reservedWords.add("PRIVILEGES".toLowerCase());
	    _reservedWords.add("PROCEDURE".toLowerCase());
	    _reservedWords.add("PUBLIC".toLowerCase());
	    _reservedWords.add("READ".toLowerCase());
	    _reservedWords.add("REAL".toLowerCase());
	    _reservedWords.add("REFERENCES".toLowerCase());
	    _reservedWords.add("RELATIVE".toLowerCase());
	    _reservedWords.add("RESTRICT".toLowerCase());
	    _reservedWords.add("REVOKE".toLowerCase());
	    _reservedWords.add("RIGHT".toLowerCase());
	    _reservedWords.add("ROLLBACK".toLowerCase());
	    _reservedWords.add("ROWS".toLowerCase());
	    _reservedWords.add("RTRIM".toLowerCase());
	    _reservedWords.add("SCHEMA".toLowerCase());
	    _reservedWords.add("SCROLL".toLowerCase());
	    _reservedWords.add("SECOND".toLowerCase());
	    _reservedWords.add("SELECT".toLowerCase());
	    _reservedWords.add("SESSION_USER".toLowerCase());
	    _reservedWords.add("SET".toLowerCase());
	    _reservedWords.add("SMALLINT".toLowerCase());
	    _reservedWords.add("SOME".toLowerCase());
	    _reservedWords.add("SPACE".toLowerCase());
	    _reservedWords.add("SQL".toLowerCase());
	    _reservedWords.add("SQLCODE".toLowerCase());
	    _reservedWords.add("SQLERROR".toLowerCase());
	    _reservedWords.add("SQLSTATE".toLowerCase());
	    _reservedWords.add("SUBSTR".toLowerCase());
	    _reservedWords.add("SUBSTRING".toLowerCase());
	    _reservedWords.add("SUM".toLowerCase());
	    _reservedWords.add("SYSTEM_USER".toLowerCase());
	    _reservedWords.add("TABLE".toLowerCase());
	    _reservedWords.add("TEMPORARY".toLowerCase());
	    _reservedWords.add("TIMEZONE_HOUR".toLowerCase());
	    _reservedWords.add("TIMEZONE_MINUTE".toLowerCase());
	    _reservedWords.add("TO".toLowerCase());
	    _reservedWords.add("TRAILING".toLowerCase());
	    _reservedWords.add("TRANSACTION".toLowerCase());
	    _reservedWords.add("TRANSLATE".toLowerCase());
	    _reservedWords.add("TRANSLATION".toLowerCase());
	    _reservedWords.add("TRUE".toLowerCase());
	    _reservedWords.add("TYPE".toLowerCase());
	    _reservedWords.add("UNION".toLowerCase());
	    _reservedWords.add("UNIQUE".toLowerCase());
	    _reservedWords.add("UNKNOWN".toLowerCase());
	    _reservedWords.add("UPDATE".toLowerCase());
	    _reservedWords.add("UPPER".toLowerCase());
	    _reservedWords.add("USER".toLowerCase());
	    _reservedWords.add("USING".toLowerCase());
	    _reservedWords.add("VALUES".toLowerCase());
	    _reservedWords.add("VARCHAR".toLowerCase());
	    _reservedWords.add("VARYING".toLowerCase());
	    _reservedWords.add("VIEW".toLowerCase());
	    _reservedWords.add("WHENEVER".toLowerCase());
	    _reservedWords.add("WHERE".toLowerCase());
	    _reservedWords.add("WITH".toLowerCase());
	    _reservedWords.add("WORK".toLowerCase());
	    _reservedWords.add("WRITE".toLowerCase());
	    _reservedWords.add("XML".toLowerCase());
	    _reservedWords.add("XMLEXISTS".toLowerCase());
	    _reservedWords.add("XMLPARSE".toLowerCase());
	    _reservedWords.add("XMLSERIALIZE".toLowerCase());
	    _reservedWords.add("YEAR".toLowerCase());
	}

	public static List<Map<String, Object>> asFieldMaps(Connection connection, String sql) throws SQLException {
		Statement s = null;
		ResultSet rs = null;
		try {
			s = connection.createStatement();
			rs = s.executeQuery(sql);
			return asFieldMaps(rs);
		} finally {
			if(s != null) {
				s.close();
			}
			if(rs != null) {
				rs.close();
			}
		}
	}

	public static List<Map<String, Object>> asFieldMaps(ResultSet rs) {
		try {
			List<Map<String, Object>> maps = new ArrayList<Map<String, Object>>();
			List<String> columns = new ArrayList<String>();
			ResultSetMetaData meta = rs.getMetaData();
			for(int i = 1; i <= meta.getColumnCount(); i++) {
				columns.add(meta.getColumnLabel(i));
			}
			while(rs.next()) {
				Map<String, Object> row = new LinkedHashMap<String, Object>();
				for(String column : columns) {
					row.put(StringUtils.varName(column), rs.getObject(column));
				}
				maps.add(row);
			}
			return maps;
		} catch(Exception e) {
			e.printStackTrace();
		}
		return new ArrayList<Map<String,Object>>(0);
	}
	
	public static List<List<Object>> asLists(ResultSet rs, boolean includeHeader) {
		try {
			List<List<Object>> lists = new ArrayList<List<Object>>();
			List<Object> columns = new ArrayList<Object>();
			ResultSetMetaData meta = rs.getMetaData();
			for(int i = 1; i <= meta.getColumnCount(); i++) {
				columns.add(meta.getColumnLabel(i));
			}
			if(includeHeader) {
				lists.add(columns);
			}
			while(rs.next()) {
				List<Object> row = new ArrayList<Object>();
				for(Object column : columns) {
					row.add(rs.getObject((String) column));
				}
				lists.add(row);
			}
			return lists;
		} catch(Exception e) {
			logger.warn(e);
		}
		return new ArrayList<List<Object>>(0);
	}

	public static List<Map<String, Map<String, Object>>> asNestedFieldMaps(ResultSet rs) throws SQLException {
		try {
			List<Map<String, Map<String, Object>>> maps = new ArrayList<Map<String,Map<String,Object>>>();
			List<String> columns = new ArrayList<String>();
			ResultSetMetaData meta = rs.getMetaData();
			for(int i = 1; i <= meta.getColumnCount(); i++) {
				columns.add(meta.getColumnLabel(i));
			}
			while(rs.next()) {
				Map<String, Map<String, Object>> row = new TreeMap<String, Map<String,Object>>(new Comparator<String>() {
					public int compare(String o1, String o2) {
						if(o1.equals(o2)) {
							return 0;
						}
						int i1 = 0;
						for(int i = 0; i < o1.length(); i++) {
							i1 += o1.charAt(i);
						}
						int i2 = 0;
						for(int i = 0; i < o2.length(); i++) {
							i2 += o2.charAt(i);
						}
						return (i1 > i2) ? 1 : -1;
					};
				});
				for(int i = 0; i < columns.size(); i++) {
					String column = columns.get(i);
					String[] sa = column.trim().split("\\s*_\\s*", 2);
					String key = sa[0].toLowerCase();
					String var = varName(sa[1]);
					int type = meta.getColumnType(i+1);
					if(!row.containsKey(key)) {
						row.put(key, new HashMap<String, Object>());
					}
					row.get(key).put(var, getValue(type, rs, i+1));
				}
				maps.add(row);
			}
			return maps;
		} catch(Exception e) {
			logger.warn(e);
		}
		return new ArrayList<Map<String,Map<String,Object>>>(0);
	}
	
	private static Object getValue(int type, ResultSet resultSet, int columnIndex) throws SQLException {
		switch(type) {
		case Types.BLOB:		return resultSet.getBytes(columnIndex);
		case Types.CLOB:		return resultSet.getString(columnIndex);
		default:				return resultSet.getObject(columnIndex);
		}
	}

	public static Object getValue(ResultSet resultSet, int columnIndex) throws SQLException {
		return getValue(resultSet.getMetaData().getColumnType(columnIndex), resultSet, columnIndex);
	}

	public static int getSqlType(Class<?> clazz) {
		if(String.class == clazz) {
			return Types.VARCHAR;
		}
		if(Integer.class == clazz || int.class == clazz) {
			return Types.INTEGER;
		}
		if(JsonModel.class.isAssignableFrom(clazz)) {
			return Types.INTEGER; // will use the id field in setObject
		}
		if(Map.class.isAssignableFrom(clazz)) {
			return Types.VARCHAR; // Map attributes are stored as JSON strings
		}
		if(Long.class == clazz || long.class == clazz) {
			return Types.BIGINT;
		}
		if(Boolean.class == clazz || boolean.class == clazz) {
			return Types.BOOLEAN;
		}
		if(Double.class == clazz || double.class == clazz) {
			return Types.DOUBLE;
		}
		if(Date.class == clazz) {
			return Types.BIGINT;
		}
		if(java.sql.Date.class == clazz) {
			return Types.DATE;
		}
		if(Time.class == clazz) {
			return Types.TIME;
		}
		if(Timestamp.class == clazz) {
			return Types.BIGINT;
		}
		if(BigDecimal.class == clazz) {
			return Types.DECIMAL;
		}
		if(byte[].class == clazz) {
			return Types.BLOB;
		}
		if(char[].class == clazz) {
			return Types.CLOB;
		}
		
		return Types.VARCHAR;
	}
	
	public static boolean isDDL(String sql) {
    	try {
	    	String cmd = sql.trim().split(" ", 2)[0].toUpperCase();
	    	return (cmd.equals("ALTER") || cmd.equals("CREATE") || cmd.equals("DROP"));
    	} catch(Exception e) {
    		return false;
    	}
    }
	
	public static boolean isInsert(String sql) {
    	try {
    		return sql.trim().startsWith("INSERT ");
    	} catch(Exception e) {
    		return false;
    	}
    }

	public static boolean isUpdate(String sql) {
    	try {
	    	String cmd = sql.trim().split(" ", 2)[0].toUpperCase();
	    	return (cmd.equals("INSERT") || cmd.equals("UPDATE") || cmd.equals("DELETE") || 
	    			cmd.equals("ALTER") || cmd.equals("CREATE") || cmd.equals("DROP"));
    	} catch(Exception e) {
    		return false;
    	}
    }
	
	public static String limit(String sql, int limit) {
		if(blank(sql)) {
			return "LIMIT " + limit;
		}
		StringBuilder sb = new StringBuilder(sql.length() + 15);
		sb.append(sql);
		String lower = sql.toLowerCase();
		int ix = lower.indexOf("limit ");
		if(ix != -1) {
			char[] ca = lower.toCharArray();
			int ix2 = forward(ca, ix + 6, lower.length());
			while(ix2 < lower.length()-1 && Character.isDigit(ca[ix2])) {
				ix2++;
			}
			if(ix > 0) {
				int i = reverse(ca, ix-1);
				if(i != -1) {
					ix = i + 1;
				}
			}
			sb.delete(ix, ix2+1);
			lower = sb.toString().trim().toLowerCase();
		}
		ix = lower.indexOf("include:");
		if(ix == -1) {
			return sb.append(" LIMIT ").append(limit).toString().trim();
		} else {
			StringBuilder sb2 = new StringBuilder(sb.length() + 15);
			if(ix > 0) {
				sb2.append(sb, 0, ix);
			}
			if(sb2.length() > 0 && sb2.charAt(sb2.length()-1) != ' ') {
				sb2.append(' ');
			}
			sb2.append("LIMIT ").append(limit).append(' ').append(sb, ix, sb.length());
			return sb2.toString().trim();
		}
	}
	
    public static String paginate(String sql, int page, int perPage) {
		int offset = (((page < 1) ? 1 : page) - 1) * perPage;
		int limit = perPage;
		if(blank(sql)) {
			return "LIMIT " + offset + "," + limit;
		}
		int ix = sql.toLowerCase().indexOf("include:");
		if(ix == -1) {
			return sql.trim() + " LIMIT " + offset + "," + limit;
		}
		StringBuilder sb = new StringBuilder(sql.length() + 20);
		if(ix > 0) {
			sb.append(sql, 0, ix);
		}
		sb.append("LIMIT ").append(offset).append(',').append(limit).append(' ').append(sql, ix, sql.length());
		return sb.toString();
	}
    
    /**
	 * Escapes the word if it is an SQL reserved word by surrounding it with quotes (").
	 * @param column
	 * @return a word that is safe to use in an SQL query
	 */
	public static String safeSqlWord(int dbType, String column) {
		if(reservedWords.contains(column)) {
			switch(dbType) {
			case DERBY:      return "\"" + column + "\"";
			case MYSQL:      return "`" + column + "`";
			case POSTGRESQL: return "\"" + column + "\"";
			}
			return "\"" + column + "\"";
		} else {
			return column;
		}
	}
    
    public static void setObject(PreparedStatement ps, int index, Object object) throws SQLException {
    	if(object == null) {
    		throw new SQLException("cannot determine the sql type of a null object");
    	}
		setObject(ps, index, object, getSqlType(object.getClass()));
	}
    
    public static void setObject(PreparedStatement ps, int index, Object object, int type) throws SQLException {
    	if(object == null) {
    		ps.setNull(index, type);
    	} else {
			if(object instanceof Date) {
				long l = ((Date) object).getTime();
				switch(type) {
				case Types.BIGINT:		object = l; break;
				case Types.DATE:		object = new java.sql.Date(l); break;
				case Types.TIME:		object = new Time(l); break;
				case Types.TIMESTAMP:	object = new Timestamp(l); break;
				}
				ps.setObject(index, object, type);
			} else if(type == Types.VARCHAR && object instanceof Map) {
				ps.setObject(index, JsonUtils.toJson((Map<?,?>) object), type);
			} else if(type == Types.INTEGER && object instanceof Map) {
				// when would this ever be true?
				Map<?,?> map = (Map<?,?>) object;
				Object id = map.get("id");
				if(id instanceof Number) {
					ps.setObject(index, ((Number) id).intValue(), type);
				} else {
					try {
						int i = Integer.parseInt(String.valueOf(id));
						ps.setObject(index, i, type);
					} catch(Exception e) {
			    		throw new SQLException("cannot get id from Map: " + object);
					}
				}
			} else if(type == Types.INTEGER && object instanceof JsonModel) {
				ps.setObject(index, ((JsonModel) object).getId(), type);
			} else {
				ps.setObject(index, object, type);
			}
    	}
	}

	public static final int DERBY		= 1;

	public static final int MYSQL		= 2;

	public static final int POSTGRESQL	= 3;
    
}
