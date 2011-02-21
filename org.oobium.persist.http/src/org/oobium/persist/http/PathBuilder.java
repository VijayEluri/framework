package org.oobium.persist.http;

import static org.oobium.utils.CharStreamUtils.*;
import static org.oobium.utils.StringUtils.blank;
import static org.oobium.utils.StringUtils.encode;
import static org.oobium.utils.StringUtils.tableName;

import java.util.regex.Pattern;

import org.oobium.persist.Model;

public class PathBuilder {

	private static String getValue(Model model, char[] ca, int s1, int s2) {
		int ix = findAny(ca, s1, s2, ':', '=');
		if(ix == -1) ix = s2;
		if(ix < s2 && ca[ix] == '=') {
			String param = new String(ca, ix+1, s2-ix-1).trim();
			return param;
		} else if(isEqual(ca, s1, ix, 'm','o','d','e','l','s')) {
			return tableName(model);
		} else {
			String param = new String(ca, s1, ix-s1).trim();
			Object value = model.get(param);
			if(value instanceof Model) {
				return String.valueOf(((Model) value).getId());
			} else {
				return String.valueOf(model.get(param));
			}
		}
	}
	
	public static String path(String path, Class<?> clazz) {
		return pathToClass(path, clazz);
	}

	public static String path(String path, Model model) {
		return pathToModel(path, model);
	}

	private static String pathToClass(String path, Class<?> clazz, Object...params) {
		StringBuilder sb = new StringBuilder(path.length() + 20);
		char[] ca = path.toCharArray();
		int pix = find(ca, '?');
		if(pix == -1) pix = ca.length;
		int s0 = 0;
		int s1 = find(ca, '{');
		int i = 0;
		while(s1 != -1) {
			sb.append(ca, s0, s1-s0);
			int s2 = closer(ca, s1);

			int ix = find(ca, '=', s1, s2);
			if(ix != -1) {
				if(s1 < pix) {
					sb.append(new String(ca, ix+1, s2-ix-1).trim());
				} // else skip - it is handled by the routing and does not need to be in the path
			} else {
				ix = find(ca, ':', s1, s2);
				if(ix != -1) {
					if(i < params.length) {
						String value = String.valueOf(params[i]);
						String regex = new String(ca, ix+1, s2-ix-1).trim();
						if(Pattern.matches(regex, value)) {
							sb.append(params[i]);
						} else {
							throw new IllegalArgumentException("invalid value for " + new String(ca, s1, s2-s1+1) + ": " + value);
						}
					} else {
						throw new IllegalArgumentException("cannot evaluate " + new String(ca, s1, s2-s1+1) + ": no parameter given");
					}
					i++;
				} else {
					String s = new String(ca, s1+1, s2-s1-1).trim();
					if("models".equals(s)) {
						if(clazz != null) {
							sb.append(tableName(clazz));
						} else if(i < params.length && params[i] instanceof Class<?>) {
							sb.append(tableName((Class<?>) params[i]));
						} else {
							throw new IllegalArgumentException("cannot evaluate {models}: no class given");
						}
					} else if("id".equals(s)) {
						if(i < params.length) {
							if(params[i] instanceof Number) {
								sb.append(((Number) params[i]).longValue());
								i++;
							} else {
								throw new IllegalArgumentException("cannot evaluate {id}: " + params[i] + " is not a number");
							}
						} else {
							throw new IllegalArgumentException("cannot evaluate {id}: no parameter given");
						}
					} else {
						throw new IllegalArgumentException("class path contains an unknown variable: " + new String(ca));
					}
				}
			}
			
			s0 = s2 + 1;
			s1 = find(ca, '{', s0);
		}
		if(s0 < ca.length) {
			sb.append(ca, s0, ca.length-s0);
		}
		if(sb.charAt(sb.length()-1) == '?') {
			sb.deleteCharAt(sb.length()-1);
		}
		return sb.toString();
	}

	private static String pathToModel(String path, Model model) {
		StringBuilder sb = new StringBuilder(path.length() + 20);
		char[] ca = path.toCharArray();
		int pix = find(ca, '?');
		if(pix == -1) pix = ca.length;
		int s0 = 0;
		int s1 = find(ca, '{');
		while(s1 != -1) {
			sb.append(ca, s0, s1-s0);
			int s2 = closer(ca, s1);
			s1++;
			if(s1 < pix) {
				sb.append(getValue(model, ca, s1, s2));
			} else { // in parameter section
				int ix = find(ca, ':', s1, s2);
				if(ix != -1) {
					String field = new String(ca, s1, ix-s1).trim();
					Object value = model.get(field);
					sb.append(field).append('=');
					if(!blank(value)) sb.append(encode(value.toString()));
				} else {
					if(find(ca, '=', s1, s2) == -1) {
						sb.append(getValue(model, ca, s1, s2));
					}
				}
			}
			s0 = s2 + 1;
			s1 = find(ca, '{', s0);
		}
		if(s0 < ca.length) {
			sb.append(ca, s0, ca.length-s0);
		}
		if(sb.charAt(sb.length()-1) == '?') {
			sb.deleteCharAt(sb.length()-1);
		}
		return sb.toString();
	}

}
