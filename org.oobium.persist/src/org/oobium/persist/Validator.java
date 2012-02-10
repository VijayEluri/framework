package org.oobium.persist;

import org.oobium.persist.Model;

public interface Validator<T extends Model> {

	/**
	 * <p>Validate the given model on the given event.</p>
	 * <p>Implement this method to detect errors in the state of the
	 * model that would prevent the event from completing successfully.
	 * For each error detected, add an error message to the model
	 * itself ({@link Model#addError(String, String)}) to stop
	 * execution of the event and allow feedback to be provided.</p>
	 * @param model the model to be validated
	 * @param on the event (one of {@link Validate#CREATE}, {@link Validate#DESTROY}, or {@link Validate#UPDATE})
	 * @see Model#addError(String)
	 * @see Model#addError(String, String)
	 * @see Validate#with()
	 * @see Validate#on()
	 */
	public abstract void validate(T model, int on);
	
}
