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

import static org.junit.Assert.*;

import java.util.Collection;

import org.junit.Test;
import org.oobium.persist.Attribute;
import org.oobium.persist.Model;
import org.oobium.persist.ModelDescription;
import org.oobium.persist.Relation;

public class ModelRelationTests {

	@ModelDescription(hasOne = {@Relation(name="b", type=BModel.class)}) public static class AModel extends Model { }
	@ModelDescription(attrs = {@Attribute(name="name", type=String.class)}) public static class BModel extends Model { }
	
	@Test
	public void testHasOneToNone() throws Exception {
		BModel b = new BModel();
		AModel a = new AModel();

		a.set("b", b);
		assertEquals(a.get("b"), b);
		
		a.set("b", null);
		assertNull(a.get("b"));
	}

	
	@ModelDescription(hasOne = {@Relation(name="d", type=DModel.class)}) public static class CModel extends Model { }
	@ModelDescription(hasOne = {@Relation(name="c", type=CModel.class)}) public static class DModel extends Model { }
	
	@Test
	public void testHasOneToNone_Bidi() throws Exception {
		DModel d = new DModel();
		CModel c = new CModel();

		c.set("d", d);
		assertNull(d.get("c"));
	}
	

	@ModelDescription(hasOne = {@Relation(name="f", type=FModel.class, opposite="e")}) public static class EModel extends Model { }
	@ModelDescription(hasOne = {@Relation(name="e", type=EModel.class, opposite="f")}) public static class FModel extends Model { }
	
	@Test
	public void testHasOneToOne() throws Exception {
		FModel f = new FModel();
		EModel e = new EModel();

		e.set("f", f);
		assertEquals(f.get("e"), e);
		
		e.set("f", null);
		assertNull(f.get("e"));
		
		f.set("e", e);
		assertEquals(e.get("f"), f);
		
		f.set("e", null);
		assertNull(e.get("f"));
	}

	@ModelDescription(hasOne = {@Relation(name="h", type=HModel.class, opposite="g")}) public static class GModel extends Model { }
	@ModelDescription(hasMany = {@Relation(name="g", type=GModel.class, opposite="h")}) public static class HModel extends Model { }
	
	@SuppressWarnings("unchecked")
	@Test
	public void testHasOneToMany() throws Exception {
		GModel g = new GModel();
		HModel h = new HModel();

		g.set("h", h);
		assertEquals(h, g.get("h"));
		assertEquals(1, ((Collection<?>) h.get("g")).size());
		assertTrue(((Collection<?>) h.get("g")).contains(g));

		g.set("h", null);
		assertNull(g.get("h"));
		assertTrue(((Collection<?>) h.get("g")).isEmpty());
		
		((Collection<Object>) h.get("g")).add(g);
		assertEquals(h, g.get("h"));
		assertEquals(1, ((Collection<?>) h.get("g")).size());
		assertTrue(((Collection<?>) h.get("g")).contains(g));
		
		((Collection<?>) h.get("g")).clear();
		assertNull(g.get("h"));
		assertTrue(((Collection<?>) h.get("g")).isEmpty());
	}
	
}
