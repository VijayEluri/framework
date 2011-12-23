package org.oobium.persist.mongo.internal;

import java.util.Date;

import org.bson.types.ObjectId;
import org.oobium.utils.coercion.Coercer;

public class ObjectIdCoercer implements Coercer {

	public ObjectId coerce(Object o, Class<?> toType) {
		if(o instanceof String) {
			return new ObjectId((String) o);
		}
		if(o instanceof Date) {
			return new ObjectId((String) o);
		}
		if(o.getClass().isArray() && o.getClass().getComponentType() == byte.class) {
			return new ObjectId((byte[]) o);
		}
		throw new IllegalArgumentException();
	}
	
	public ObjectId coerce(byte[] ba, Class<?> toType) {
		return new ObjectId(ba);
	}
	
	public ObjectId coerce(Date date, Class<?> toType) {
		return new ObjectId(date);
	}
	
	public ObjectId coerce(String s, Class<?> toType) {
		return new ObjectId(s);
	}

	@Override
	public Object coerceNull(Class<?> toType) {
		return null;
	}

	@Override
	public Class<?> getType() {
		return ObjectId.class;
	}

	@Override
	public boolean handleSubTypes() {
		return false;
	}

}
