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
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

public class ActiveSet<E extends Model> implements Set<E> {

	private final Model owner;
	private final String ownerField;
	private final String memberField;
	private final boolean manyToMany;
	
	private final Set<E> members;

	ActiveSet(Model owner, String ownerField) {
		this(owner, ownerField, null);
	}
	
	ActiveSet(Model owner, String ownerField, E[] members) {
		this.owner = owner;
		this.ownerField = ownerField;
		
		ModelAdapter adapter = ModelAdapter.getAdapter(owner);
		
		this.memberField = adapter.getOpposite(ownerField);
		this.manyToMany = adapter.isManyToMany(ownerField);

		this.members = (members != null) ? new LinkedHashSet<E>(asList(members)) : new LinkedHashSet<E>();
	}

	@Override
	public boolean add(E e) {
		if(members.add(e)) {
			setOpposite(e);
			return true;
		}
		return false;
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
		List<E> ca = new ArrayList<E>(members);
		members.clear();
		for(E o : ca) {
			clearOpposite(o);
		}
		ca.clear();
	}

	@SuppressWarnings("unchecked")
	private void clearOpposite(Model object) {
		if(manyToMany) {
			if(object.isSet(memberField)) {
				((ActiveSet<Model>) object.get(memberField)).doRemove(owner);
			}
		} else {
			object.set(memberField, null);
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
		if(members.remove(o)) {
			E e = (E) o;
			clearOpposite(e);
			return true;
		}
		return false;
	}
	
	@Override
	@SuppressWarnings("unchecked")
	public boolean removeAll(Collection<?> c) {
		if(members.removeAll(c)) {
			Collection<E> e = (Collection<E>) c;
			for(E o : e) {
				clearOpposite(o);
			}
			return true;
		}
		return false;
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
		if(manyToMany) {
			if(object.isSet(memberField)) {
				((ActiveSet<Model>) object.get(memberField)).doAdd(owner);
			}
		} else {
			Model previousOwner = (Model) (object.isSet(memberField) ? object.get(memberField) : null);
			if(previousOwner != owner) {
				if(previousOwner != null) {
					if(previousOwner.isSet(ownerField)) {
						((ActiveSet<?>) previousOwner.get(ownerField)).doRemove(object);
					}
				}
				object.put(memberField, owner);
			}
		}
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
	
}
