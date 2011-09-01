///*******************************************************************************
// * Copyright (c) 2010 Oobium, Inc.
// * All rights reserved. This program and the accompanying materials
// * are made available under the terms of the Eclipse Public License v1.0
// * which accompanies this distribution, and is available at
// * http://www.eclipse.org/legal/epl-v10.html
// * 
// * Contributors:
// *     Jeremy Dowdall <jeremy@oobium.com> - initial API and implementation
// ******************************************************************************/
//package org.oobium.persist;
//
//import static java.util.Arrays.asList;
//
//import java.util.Collection;
//import java.util.HashSet;
//import java.util.Iterator;
//import java.util.LinkedHashSet;
//import java.util.Set;
//
//public class RequiredSet<E extends Model> implements Set<E> {
//
//	private Model owner;
//	private String ownerField;
//	private String memberField;
//	
//	private Set<E> members;
//	private Set<E> removed;
//
//	RequiredSet(Model owner, String ownerField, String memberField) {
//		this(owner, ownerField, memberField, null);
//	}
//	
//	RequiredSet(Model owner, String ownerField, String memberField, E[] members) {
//		this.owner = owner;
//		this.ownerField = ownerField;
//		this.members = (members != null) ? new LinkedHashSet<E>(asList(members)) : new LinkedHashSet<E>();
//		this.memberField = memberField;
//		this.removed = new HashSet<E>();
//	}
//
//	@Override
//	public boolean add(E e) {
//		if(members.add(e)) {
//			setOpposite(e);
//			return true;
//		}
//		return false;
//	}
//	
//	@Override
//	public boolean addAll(Collection<? extends E> c) {
//		if(members.addAll(c)) {
//			for(E o : c) {
//				setOpposite(o);
//			}
//			return true;
//		}
//		return false;
//	}
//	
//	/**
//	 * Unsupported method
//	 * @throws UnsupportedOperationException
//	 */
//	@Override
//	public void clear() {
//		throw new UnsupportedOperationException("removeAll is not supported for active lists where the owner class is a required field");
//	}
//	
//	public void clearRemoved() {
//		if(removed != null) {
//			removed.clear();
//		}
//	}
//	
//	@Override
//	public boolean contains(Object o) {
//		return members.contains(o);
//	}
//
//	@Override
//	public boolean containsAll(Collection<?> c) {
//		return members.containsAll(c);
//	}
//
//	@SuppressWarnings("unchecked")
//	boolean doAdd(Object model) {
//		return members.add((E) model);
//	}
//	
//	boolean doRemove(Object model) {
//		return members.remove(model);
//	}
//	
//	public Set<E> getRemoved() {
//		return new HashSet<E>(this.removed);
//	}
//
//	public boolean hasRemoved() {
//		return !removed.isEmpty();
//	}
//
//	@Override
//	public boolean isEmpty() {
//		return members.isEmpty();
//	}
//
//	@Override
//	public Iterator<E> iterator() {
//		return members.iterator();
//	}
//	
//	/**
//	 * Unsupported method
//	 * @throws UnsupportedOperationException
//	 */
//	@Override
//	public boolean remove(Object o) {
//		throw new UnsupportedOperationException("removeAll is not supported for active lists where the owner class is a required field");
//	}
//	
//	/**
//	 * Unsupported method
//	 * @throws UnsupportedOperationException
//	 */
//	@Override
//	public boolean removeAll(Collection<?> c) {
//		throw new UnsupportedOperationException("removeAll is not supported for active lists where the owner class is a required field");
//	}
//
//	/**
//	 * Unsupported method
//	 * @throws UnsupportedOperationException
//	 */
//	@Override
//	public boolean retainAll(Collection<?> c) {
//		throw new UnsupportedOperationException("retainAll is not currently supported");
//	}
//
//	private void setOpposite(Model object) {
//		Model previousOwner = (Model) (object.isSet(memberField) ? object.get(memberField) : null);
//		if(previousOwner != owner) {
//			if(previousOwner != null) {
//				if(previousOwner.isSet(ownerField)) {
//					((RequiredSet<?>) previousOwner.get(ownerField)).doRemove(object);
//				}
//			}
//			object.put(memberField, owner);
//		}
//	}
//	
//	@SuppressWarnings("unchecked")
//	public void setRemoved(Set<?> set) {
//		this.removed = (Set<E>) set;
//	}
//	
//	@Override
//	public int size() {
//		return members.size();
//	}
//
//	@Override
//	public Object[] toArray() {
//		return members.toArray();
//	}
//
//	@Override
//	public <T> T[] toArray(T[] a) {
//		return members.toArray(a);
//	}
//
//	@Override
//	public String toString() {
//		return owner.toString() + " <- " + members.toString();
//	}
//	
//}
