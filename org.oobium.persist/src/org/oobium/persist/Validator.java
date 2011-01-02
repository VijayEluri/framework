package org.oobium.persist;

public interface Validator<T extends Model> {

	public abstract void validate(T model);
	
}
