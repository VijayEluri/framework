package org.oobium.persist;

import java.util.ArrayList;
import java.util.List;

public class ModelCollector {

	@SuppressWarnings("unchecked")
	public static <T extends Model> List<T> collectHasManyThrough(List<?> models, String field, Class<T> type) {
		List<T> list = new ArrayList<T>();
		for(Object o : models) {
			if(o instanceof Model) {
				Object value = ((Model) o).get(field);
				if(value instanceof List) {
					list.addAll((List<T>) value);
				}
				else if(value != null) {
					list.add((T) value);
				}
			}
		}
		return list;
	}
	
	@SuppressWarnings("unchecked")
	public static <T extends Model> List<T> collectHasManyThrough(Model model, String field, Class<T> type) {
		if(model == null) {
			return new ArrayList<T>(0);
		}
		List<T> list = new ArrayList<T>();
		Object value = model.get(field);
		if(value instanceof List) {
			list.addAll((List<T>) value);
		}
		else if(value != null) {
			list.add((T) value);
		}
		return list;
	}

}