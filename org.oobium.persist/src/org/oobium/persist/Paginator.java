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

import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;

import org.oobium.utils.json.JsonUtils;

public class Paginator<E extends Model> implements List<E> {

	public static final String DEFAULT_PAGE_KEY = "p";
	
	public static <T extends Model> Paginator<T> paginate(Class<T> clazz, int page, int perPage) throws Exception {
		return paginate(clazz, page, perPage, null);
	}
	
	public static <T extends Model> Paginator<T> paginate(Class<T> clazz, int page, int perPage, String query, Object...values) throws Exception {
		long total = Model.getPersistService(clazz).count(clazz, query, values);
		Map<String, Object> paginatedQuery = (query != null) ? JsonUtils.toMap(query) : new HashMap<String, Object>();
		paginatedQuery.put("$limit", limit(page, perPage));
		List<T> models = Model.getPersistService(clazz).findAll(clazz, paginatedQuery, values);
		Paginator<T> paginator = new Paginator<T>(models, total, page, perPage);
		return paginator;
	}

	private static String limit(int page, int perPage) {
		int offset = (((page < 1) ? 1 : page) - 1) * perPage;
		int limit = perPage;
		return offset + "," + limit; 
	}
    
	
	private List<E> models;
	private long total;
	private int page;
	private int perPage;
	
	private String path;
	private String pageKey;

	private Paginator(List<E> models, long total, int page, int perPage) {
		this.models = models;
		this.total = total;
		this.page = (page < 1) ? 1 : page;
		this.perPage = perPage;
		this.pageKey = DEFAULT_PAGE_KEY;
	}

	@Override
	public boolean add(E e) {
		return models.add(e);
	}
	
	@Override
	public void add(int index, E element) {
		models.add(index, element);
	}

	@Override
	public boolean addAll(Collection<? extends E> c) {
		return models.addAll(c);
	}
	
	@Override
	public boolean addAll(int index, Collection<? extends E> c) {
		return models.addAll(index, c);
	}
	
	@Override
	public void clear() {
		models.clear();
	}
	
	@Override
	public boolean contains(Object o) {
		return models.contains(o);
	}
	
	@Override
	public boolean containsAll(Collection<?> c) {
		return models.containsAll(c);
	}
	
	@Override
	public E get(int index) {
		return models.get(index);
	}
	
	public int getFirst() {
		return ((page - 1) * perPage) + 1;
	}
	
	public int getLast() {
		return ((page - 1) * perPage) + size();
	}
	
	public int getNextPage() {
		return Math.min((page + 1), getTotalPages());
	}

	public int getPage() {
		return page;
	}

	public int getPerPage() {
		return perPage;
	}

	public int getPreviousPage() {
		return Math.max((page - 1), 1);
	}

	public long getTotal() {
		return total;
	}

	public int getTotalPages() {
		return (int) Math.ceil(total / (float)perPage);
	}

	public boolean hasMultiplePages() {
		return getTotalPages() > 1;
	}
	
	public boolean hasNextPage() {
		return page < getTotalPages();
	}
	
	public boolean hasPages() {
		return total > 0;
	}
	
	public boolean hasPreviousPage() {
		return page > 1;
	}
	
	@Override
	public int indexOf(Object o) {
		return models.indexOf(o);
	}

	@Override
	public boolean isEmpty() {
		return models.isEmpty();
	}

	public boolean isFirstPage() {
		return page == 1;
	}
	
	public boolean isLastPage() {
		return page == getTotalPages();
	}

	@Override
	public Iterator<E> iterator() {
		return models.iterator();
	}
	
	@Override
	public int lastIndexOf(Object o) {
		return models.lastIndexOf(o);
	}
	
	@Override
	public ListIterator<E> listIterator() {
		return models.listIterator();
	}

	@Override
	public ListIterator<E> listIterator(int index) {
		return models.listIterator(index);
	}

	public String pathToCurrentPage() {
		return pathToPage(page);
	}
	
	public String pathToFirstPage() {
		return pathToPage(1);
	}
	
	public String pathToLastPage() {
		return pathToPage(getTotalPages());
	}

	public String pathToNextPage() {
		return pathToPage(getNextPage());
	}

	public String pathToPage(int page) {
		String path = this.path.replaceAll("[\\?\\&]*" + pageKey + "=\\d+", "");
		if(path.contains("?")) {
			return path + "&" + pageKey + "=" + page;
		}
		return path + "?" + pageKey + "=" + page;
	}

	public String pathToPreviousPage() {
		return pathToPage(getPreviousPage());
	}

	@Override
	public E remove(int index) {
		return models.remove(index);
	}

	@Override
	public boolean remove(Object o) {
		return models.remove(o);
	}

	@Override
	public boolean removeAll(Collection<?> c) {
		return models.removeAll(c);
	}

	@Override
	public boolean retainAll(Collection<?> c) {
		return models.retainAll(c);
	}

	@Override
	public E set(int index, E element) {
		return models.set(index, element);
	}

	public void setPageKey(String pageKey) {
		this.pageKey = pageKey;
	}

	public void setPath(String path) {
		this.path = path;
	}

	@Override
	public int size() {
		return models.size();
	}

	@Override
	public List<E> subList(int fromIndex, int toIndex) {
		return models.subList(fromIndex, toIndex);
	}

	@Override
	public Object[] toArray() {
		return models.toArray();
	}

	@Override
	public <T> T[] toArray(T[] a) {
		return models.toArray(a);
	}
	
}
