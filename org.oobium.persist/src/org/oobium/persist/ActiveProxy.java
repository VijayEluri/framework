package org.oobium.persist;

import java.util.Collection;

public class ActiveProxy<E extends Model> {

	private final Model model;
	private final String field;
	
	public ActiveProxy(Model model, String field) {
		this.model = model;
		this.field = field;
	}
	
	public boolean add(E...model) {
		// TODO
		return false;
	}
	
	public boolean addAll(Collection<E> model) {
		// TODO
		return false;
	}
	
	public boolean remove(E...model) {
		// TODO
		return false;
	}
	
	public boolean removeAll(Collection<E> model) {
		// TODO
		return false;
	}
	
	public boolean destroy(E model) {
		// TODO
		return false;
	}
	
	public boolean hasAny(E model) {
		// TODO
		return false;
	}
	
	public boolean isEmpty(E model) {
		// TODO
		return false;
	}
	
	public int count(E model) {
		// TODO
		return 0;
	}

	public E find(String where, Object...values) {
		return null;
	}
	
	public ActiveSet<E> findAll() {
		// TODO
		return null;
	}
	
	public ActiveSet<E> findAll(String where, Object...values) {
		// TODO
		return null;
	}

	public E findFirst() {
		// TODO
		return null;
	}
	
	public E findLast() {
		// TODO
		return null;
	}
	
}
