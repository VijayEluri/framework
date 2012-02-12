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
package org.oobium.persist;

import static java.util.Arrays.asList;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;

import org.oobium.utils.ArrayUtils;

public class ModelList<E extends Model> implements List<E> {

	private static final int MANY_TO_NONE = 0;
	private static final int MANY_TO_ONE  = 1;
	private static final int MANY_TO_MANY = 2;
	
	private final Model owner;
	private final String ownerField;
	private final String memberField;
	private final int type;
	private final boolean through;
	
	private final List<E> members;

	ModelList(Model owner, String ownerField) {
		this(owner, ownerField, null);
	}

	
	// TODO handle :through and ManyToNone... (these used to be handled by a simple LinkedHashSet in Model)
	
	ModelList(Model owner, String ownerField, E[] members) {
		this.owner = owner;
		this.ownerField = ownerField;
		
		ModelAdapter adapter = ModelAdapter.getAdapter(owner);
		
		this.memberField = adapter.getOpposite(ownerField);

		if(adapter.isManyToNone(ownerField)) {
			this.type = MANY_TO_NONE;
		}
		else if(adapter.isManyToOne(ownerField)) {
			this.type = MANY_TO_ONE;
		}
		else {
			this.type = MANY_TO_MANY;
		}

		this.through = adapter.isThrough(ownerField);
		
		this.members = (members != null) ? new ArrayList<E>(asList(members)) : new ArrayList<E>();
	}

	@Override
	public boolean add(E e) {
		members.add(e);
		setOpposite(e);
		return true;
	}
	
	@Override
	public boolean addAll(Collection<? extends E> c) {
		if(members.addAll(c)) {
			for(E o : c) {
				setOpposite(o);
			}
			return true;
		}
		return false;
	}
	
	@Override
	public void clear() {
		if(type == MANY_TO_NONE) {
			members.clear();
		}
		else {
			List<E> ca = new ArrayList<E>(members);
			members.clear();
			for(E o : ca) {
				clearOpposite(o);
			}
			ca.clear();
		}
	}

	@SuppressWarnings("unchecked")
	private void clearOpposite(Model object) {
		if(object == null) {
			return;
		}
		switch(type) {
		case MANY_TO_MANY:
			if(object.isSet(memberField)) {
				((ModelList<Model>) object.get(memberField)).doRemove(owner);
			}
			break;
		case MANY_TO_ONE:
			object.set(memberField, null);
			break;
		default:
			throw new IllegalStateException("why are we here?");
		}
	}
	
	@Override
	public boolean contains(Object o) {
		return members.contains(o);
	}
	
	@Override
	public boolean containsAll(Collection<?> c) {
		return members.containsAll(c);
	}
	
	@Override
	public boolean isEmpty() {
		return members.isEmpty();
	}

	@Override
	public Iterator<E> iterator() {
		// TODO create a custom iterator - this exposes the internal members collection
		return members.iterator();
	}

	@SuppressWarnings("unchecked")
	boolean doAdd(Object model) {
		return members.add((E) model);
	}
	
	boolean doRemove(Object model) {
		return members.remove(model);
	}
	
	@Override
	@SuppressWarnings("unchecked")
	public boolean remove(Object o) {
		if(type == MANY_TO_NONE) {
			return members.remove(o);
		}
		else {
			if(members.remove(o)) {
				E e = (E) o;
				clearOpposite(e);
				return true;
			}
			return false;
		}
	}
	
	@Override
	@SuppressWarnings("unchecked")
	public boolean removeAll(Collection<?> c) {
		if(type == MANY_TO_NONE) {
			return members.removeAll(c);
		}
		else {
			if(members.removeAll(c)) {
				Collection<E> e = (Collection<E>) c;
				for(E o : e) {
					clearOpposite(o);
				}
				return true;
			}
			return false;
		}
	}

	/**
	 * Unsupported method
	 * @throws UnsupportedOperationException
	 */
	@Override
	public boolean retainAll(Collection<?> c) {
		throw new UnsupportedOperationException("retainAll is not currently supported");
	}
	
	@SuppressWarnings("unchecked")
	private void setOpposite(Model object) {
		if(object == null) {
			return;
		}
		switch(type) {
		case MANY_TO_MANY:
			if(object.isSet(memberField)) {
				((ModelList<Model>) object.get(memberField)).doAdd(owner);
			}
			break;
		case MANY_TO_ONE:
			Model previousOwner = (Model) (object.isSet(memberField) ? object.get(memberField) : null);
			if(previousOwner != owner) {
				if(previousOwner != null) {
					if(previousOwner.isSet(ownerField)) {
						((ModelList<?>) previousOwner.get(ownerField)).doRemove(object);
					}
				}
				object.put(memberField, owner);
			}
			break;
		// default: nothing to do, just exit
		}
	}
	
	public boolean save() {
		// TODO
		return false;
	}
	
	@Override
	public int size() {
		return members.size();
	}

	@Override
	public Object[] toArray() {
		return members.toArray();
	}

	@Override
	public <T> T[] toArray(T[] a) {
		return members.toArray(a);
	}

	@Override
	public String toString() {
		return String.valueOf(owner) + " <- " + String.valueOf(members);
	}


	public boolean add(E...model) {
		// TODO
		return false;
	}
	
	public boolean remove(E...model) {
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
	
//	public ActiveSet<E> findAll() {
//		// TODO
//		return null;
//	}
//	
//	public ActiveSet<E> findAll(String where, Object...values) {
//		// TODO
//		return null;
//	}

	public E findFirst() {
		// TODO
		return null;
	}
	
	public E findLast() {
		// TODO
		return null;
	}
	
	public E first() {
		return members.isEmpty() ? null : members.get(0);
	}
	
	public E last() {
		return members.isEmpty() ? null : members.get(members.size()-1);
	}

	public ModelList<E> reverse() {
		if(members != null) {
			ArrayUtils.reverse(members);
		}
		return this;
	}

	public void add(int index, E e) {
		members.add(index, e);
		setOpposite(e);
	};
	
	@Override
	public boolean addAll(int index, Collection<? extends E> c) {
		if(members.addAll(index, c)) {
			for(E e : c) {
				setOpposite(e);
			}
			return true;
		}
		return false;
	}

	@Override
	public E get(int index) {
		return members.get(index);
	}

	@Override
	public E set(int index, E element) {
		E orig = members.set(index, element);
		clearOpposite(orig);
		setOpposite(element);
		return orig;
	}

	@Override
	public E remove(int index) {
		E orig = members.remove(index);
		clearOpposite(orig);
		return orig;
	}

	@Override
	public int indexOf(Object o) {
		return members.indexOf(o);
	}

	@Override
	public int lastIndexOf(Object o) {
		return members.lastIndexOf(o);
	}

	@Override
	public ListIterator<E> listIterator() {
		// TODO create a custom iterator - this exposes the internal members collection
		return members.listIterator();
	}

	@Override
	public ListIterator<E> listIterator(int index) {
		// TODO create a custom iterator - this exposes the internal members collection
		return members.listIterator(index);
	}

	@Override
	public List<E> subList(int fromIndex, int toIndex) {
		// TODO create a new ModelList?
		return members.subList(fromIndex, toIndex);
	}
	
	

}
