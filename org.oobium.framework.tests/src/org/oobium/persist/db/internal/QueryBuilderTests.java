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
package org.oobium.persist.db.internal;

import static org.junit.Assert.*;

import java.sql.SQLException;
import java.util.List;

import org.junit.Test;
import org.oobium.persist.Model;
import org.oobium.persist.ModelDescription;
import org.oobium.persist.Relation;
import org.oobium.persist.db.internal.Query;
import org.oobium.persist.db.internal.QueryBuilder;

public class QueryBuilderTests {

	
//	protected final int dbType = QueryUtils.DERBY;
	protected final int dbType = QueryUtils.MYSQL;
//	protected final int dbType = QueryUtils.POSTGRESQL;

	
	@ModelDescription(hasOne={@Relation(name="a",type=A0.class,include=true)}) class Start0 extends Model { }
	@ModelDescription() class A0 extends Model { }

	@Test
	public void testIncludes0() throws Exception {
		List<Object> includes = QueryBuilder.processModelIncludes(dbType, Start0.class, null);

		assertEquals("[a]", String.valueOf(includes));
	}
	
	@Test
	public void testIncludes01() throws Exception {
		String sql = "include:*";
		List<Object> includes = QueryBuilder.processModelIncludes(dbType, Start0.class, sql);

		assertEquals("[a]", String.valueOf(includes));
	}
	
	@ModelDescription(hasOne={@Relation(name="a",type=A1.class)}) class Start1 extends Model { }
	@ModelDescription(hasOne={@Relation(name="b",type=B1.class),@Relation(name="c",type=C1.class)}) class A1 extends Model { }
	@ModelDescription() class B1 extends Model { }
	@ModelDescription() class C1 extends Model { }

	@Test
	public void testIncludes1() throws Exception {
		List<Object> includes = QueryBuilder.processModelIncludes(dbType, Start1.class, null);

		assertEquals("[]", String.valueOf(includes));
	}
	
	@Test
	public void testIncludes11() throws Exception {
		String sql = "include:{a:[b,c]}";
		List<Object> includes = QueryBuilder.processModelIncludes(dbType, Start1.class, sql);

		assertEquals("[{a=[b, c]}]", String.valueOf(includes));
	}
	
	@Test
	public void testIncludes12() throws Exception {
		String sql = "include:*";
		List<Object> includes = QueryBuilder.processModelIncludes(dbType, Start1.class, sql);

		assertEquals("[a]", String.valueOf(includes));
	}
	
	@Test
	public void testHasOneSql1() throws Exception {
		Query query = QueryBuilder.build(dbType, Start1.class, null);
		
		String expected = "SELECT a.id a_id,a.a a_a FROM start1s a";
		assertEquals(expected, query.getSql());
	}
	
	@Test
	public void testHasOneSql2() throws Exception {
		String sql = "where id < 5 and a is not null";
		Query query = QueryBuilder.build(dbType, Start1.class, sql);
		
		String expected = "SELECT a.id a_id,a.a a_a FROM start1s a WHERE a.id < 5 and a.a is not null";
		assertEquals(expected, query.getSql());
	}
	
	@Test
	public void testHasOneSql3() throws Exception {
		String sql = "where id < 5 and a is not null include:a";
		Query query = QueryBuilder.build(dbType, Start1.class, sql);
		
		String expected = "SELECT a.id a_id,a.a a_a,b.id b_id,b.b b_b,b.c b_c FROM start1s a LEFT JOIN a1s b ON a.a=b.id WHERE a.id < 5 and a.a is not null";
		assertEquals(expected, query.getSql());
	}
	
	@Test
	public void testHasOneSql4() throws Exception {
		String sql = "include:a";
		Query query = QueryBuilder.build(dbType, Start1.class, sql);
		
		String expected = "SELECT a.id a_id,a.a a_a,b.id b_id,b.b b_b,b.c b_c FROM start1s a LEFT JOIN a1s b ON a.a=b.id";
		assertEquals(expected, query.getSql());
	}
	
	@Test(expected=SQLException.class)
	public void testHasOneSql5() throws Exception {
		String sql = "limit 1 where a is not null";
		QueryBuilder.build(dbType, Start1.class, sql);
	}
	
	@Test
	public void testHasOneSql6() throws Exception {
		String sql = "include:a where b is not null";
		Query query = QueryBuilder.build(dbType, Start1.class, sql);
		
		String expected = "SELECT a.id a_id,a.a a_a,b.id b_id,b.b b_b,b.c b_c FROM start1s a LEFT JOIN a1s b ON a.a=b.id AND b.b is not null";
		assertEquals(expected, query.getSql());
	}
	
	@Test
	public void testHasOneSql7() throws Exception {
		String sql = "include:{a:['b where d=1',c]}";
		Query query = QueryBuilder.build(dbType, Start1.class, sql);
		
		String expected = "SELECT a.id a_id,a.a a_a,b.id b_id,b.b b_b,b.c b_c,c.id c_id,d.id d_id " +
							"FROM start1s a LEFT JOIN a1s b ON a.a=b.id LEFT JOIN b1s c ON b.b=c.id AND d=1 LEFT JOIN c1s d ON b.c=d.id";
		assertEquals(expected, query.getSql());
	}

	@ModelDescription(hasOne={@Relation(name="a",type=A2.class)}) class Start2 extends Model { }
	@ModelDescription(hasOne={@Relation(name="b",type=B2.class),@Relation(name="c",type=C2.class)}) class A2 extends Model { }
	@ModelDescription(hasOne={@Relation(name="d",type=D2.class,include=true),@Relation(name="e",type=E2.class,include=true)}) class B2 extends Model { }
	@ModelDescription(hasOne={@Relation(name="d",type=D2.class,include=true),@Relation(name="e",type=E2.class,include=true)}) class C2 extends Model { }
	@ModelDescription() class D2 extends Model { }
	@ModelDescription() class E2 extends Model { }

	@Test
	public void testIncludes2() throws Exception {
		String sql = "include:{a:[b,c]}";
		List<Object> includes = QueryBuilder.processModelIncludes(dbType, Start2.class, sql);

		assertEquals("[{a=[{b=[d, e]}, {c=[d, e]}]}]", String.valueOf(includes));
	}
	
	@Test
	public void testIncludes21() throws Exception {
		String sql = "include:{a:[{b:d},{c:e}]}";
		List<Object> includes = QueryBuilder.processModelIncludes(dbType, Start2.class, sql);

		assertEquals("[{a=[{b=[d, e]}, {c=[d, e]}]}]", String.valueOf(includes));
	}
	
	@Test
	public void testIncludes22() throws Exception {
		String sql = "include:{a:[{b:[d,e]},{c:[d,e]}]}";
		List<Object> includes = QueryBuilder.processModelIncludes(dbType, Start2.class, sql);

		assertEquals("[{a=[{b=[d, e]}, {c=[d, e]}]}]", String.valueOf(includes));
	}
	
	@ModelDescription(hasOne={@Relation(name="a",type=A3.class)}) class Start3 extends Model { }
	@ModelDescription(hasOne={@Relation(name="b",type=B3.class),@Relation(name="c",type=C3.class)}) class A3 extends Model { }
	@ModelDescription(hasOne={@Relation(name="d",type=D3.class,include=true),@Relation(name="e",type=E3.class,include=true)}) class B3 extends Model { }
	@ModelDescription(hasOne={@Relation(name="d",type=D3.class,include=true),@Relation(name="e",type=E3.class,include=true)}) class C3 extends Model { }
	@ModelDescription(hasOne={@Relation(name="f",type=F3.class,include=true)}) class D3 extends Model { }
	@ModelDescription(hasOne={@Relation(name="f",type=F3.class)}) class E3 extends Model { }
	@ModelDescription() class F3 extends Model { }

	@Test
	public void testIncludes3() throws Exception {
		String sql = "include:{a:[b,c]}";
		List<Object> includes = QueryBuilder.processModelIncludes(dbType, Start3.class, sql);

		assertEquals("[{a=[{b=[e, {d=f}]}, {c=[e, {d=f}]}]}]", String.valueOf(includes));
	}
	
	@Test
	public void testIncludes31() throws Exception {
		String sql = "include:{a:[{b:[e,{d:f}]},{c:[e,{d:f}]}]}";
		List<Object> includes = QueryBuilder.processModelIncludes(dbType, Start3.class, sql);

		assertEquals("[{a=[{b=[e, {d=f}]}, {c=[e, {d=f}]}]}]", String.valueOf(includes));
	}
	
	@ModelDescription(hasOne={@Relation(name="a",type=A4.class),@Relation(name="f",type=F4.class)}) class Start4 extends Model { }
	@ModelDescription(hasOne={@Relation(name="b",type=B4.class),@Relation(name="f",type=F4.class)}) class A4 extends Model { }
	@ModelDescription(hasOne={@Relation(name="d",type=D4.class,include=true),@Relation(name="e",type=E4.class,include=true)}) class B4 extends Model { }
	@ModelDescription(hasOne={@Relation(name="d",type=D4.class,include=true),@Relation(name="e",type=E4.class,include=true)}) class C4 extends Model { }
	@ModelDescription(hasOne={@Relation(name="f",type=F4.class,include=true)}) class D4 extends Model { }
	@ModelDescription(hasOne={@Relation(name="f",type=F4.class)}) class E4 extends Model { }
	@ModelDescription() class F4 extends Model { }

	@Test
	public void testIncludes4() throws Exception {
		String sql = "include:[{a:[b,f]},f]";
		List<Object> includes = QueryBuilder.processModelIncludes(dbType, Start4.class, sql);

		assertEquals("[{a=[f, {b=[e, {d=f}]}]}, f]", String.valueOf(includes));
	}

	@Test
	public void testIncludes41() throws Exception {
		String sql = "include:[a,f]";
		List<Object> includes = QueryBuilder.processModelIncludes(dbType, Start4.class, sql);

		assertEquals("[a, f]", String.valueOf(includes));
	}

	@Test
	public void testIncludes42() throws Exception {
		String sql = "include:*";
		List<Object> includes = QueryBuilder.processModelIncludes(dbType, Start4.class, sql);

		assertEquals("[f, a]", String.valueOf(includes));
	}

	@Test
	public void testIncludes43() throws Exception {
		String sql = "include:[*,{a:[b,f]}]";
		List<Object> includes = QueryBuilder.processModelIncludes(dbType, Start4.class, sql);

		assertEquals("[{a=[f, {b=[e, {d=f}]}]}, f]", String.valueOf(includes));
	}

	@Test
	public void testIncludes44() throws Exception {
		String sql = "include:[*,{a:*}]";
		List<Object> includes = QueryBuilder.processModelIncludes(dbType, Start4.class, sql);

		assertEquals("[{a=[f, {b=[e, {d=f}]}]}, f]", String.valueOf(includes));
	}

	@ModelDescription(hasOne={@Relation(name="a",type=A5.class)},hasMany={@Relation(name="as",type=A5.class)}) class Start5 extends Model { }
	@ModelDescription() class A5 extends Model { }

	@Test
	public void testIncludes5() throws Exception {
		List<Object> includes = QueryBuilder.processModelIncludes(dbType, Start5.class, null);

		assertEquals("[]", String.valueOf(includes));
	}

	@Test
	public void testIncludes51() throws Exception {
		String sql = "include:[a,as]";
		List<Object> includes = QueryBuilder.processModelIncludes(dbType, Start5.class, sql);

		assertEquals("[a, as]", String.valueOf(includes));
	}

	@Test
	public void testIncludes52() throws Exception {
		String sql = "include:*";
		List<Object> includes = QueryBuilder.processModelIncludes(dbType, Start5.class, sql);

		assertEquals("[a, as]", String.valueOf(includes));
	}

	@Test
	public void testIncludes53() throws Exception {
		String sql = "include:*1";
		List<Object> includes = QueryBuilder.processModelIncludes(dbType, Start5.class, sql);

		assertEquals("[a]", String.valueOf(includes));
	}

	@Test
	public void testIncludes54() throws Exception {
		String sql = "include:*M";
		List<Object> includes = QueryBuilder.processModelIncludes(dbType, Start5.class, sql);

		assertEquals("[as]", String.valueOf(includes));
	}

	@ModelDescription(hasMany={@Relation(name="a",type=A6.class)}) class Start6 extends Model { }
	@ModelDescription() class A6 extends Model { }
	@ModelDescription(hasMany={@Relation(name="a",type=A6.class)}) class B6 extends Model { }

	@Test
	public void testHasManyToNoneSql() throws Exception {
		String sql = "include:a";
		Query query = QueryBuilder.build(dbType, Start6.class, sql);

		assertEquals("SELECT a.id a_id FROM start6s a", query.getSql());

		assertTrue(query.hasChildren());
		assertEquals(1, query.getChildren().size());
		
		Query child = query.getChildren().get(0);
		
		String expected = "SELECT a.a a_id,b.id b_id FROM a6s__null___start6s__a a INNER JOIN a6s b ON a.b=b.id AND a.a IN (#{ids})";
		assertEquals(expected, child.getSql());
	}
	
	@ModelDescription(hasMany={@Relation(name="a",type=A7.class), @Relation(name="b",type=B7.class,through="a")}) class Start7 extends Model { }
	@ModelDescription(hasMany={@Relation(name="b",type=B7.class)}) class A7 extends Model { }
	@ModelDescription() class B7 extends Model { }

	@Test
	public void testHasManyToNoneThroughSql() throws Exception {
		String sql = "include:b";
		Query query = QueryBuilder.build(dbType, Start7.class, sql);

		assertEquals("SELECT a.id a_id FROM start7s a", query.getSql());

		assertTrue(query.hasChildren());
		assertEquals(1, query.getChildren().size());
		
		Query child = query.getChildren().get(0);
		
		String expected = "SELECT a.a7s__null a_id,b.id b_id FROM a7s__null___start7s__a a INNER JOIN a7s b0 ON a.start7s__a=b0.id AND a.a7s__null IN (#{ids}) INNER JOIN b7s b ON b0.b=b.id";
		assertEquals(expected, child.getSql());
	}
	
	@ModelDescription(hasMany={@Relation(name="a",type=A8.class,opposite="start")}) class Start8 extends Model { }
	@ModelDescription(hasMany={@Relation(name="start",type=Start8.class,opposite="a")}) class A8 extends Model { }

	@Test
	public void testHasManyToManySql() throws Exception {
		String sql = "include:a";
		Query query = QueryBuilder.build(dbType, Start8.class, sql);

		assertEquals("SELECT a.id a_id FROM start8s a", query.getSql());

		assertTrue(query.hasChildren());
		assertEquals(1, query.getChildren().size());
		
		Query child = query.getChildren().get(0);
		
		String expected = "SELECT a.a a_id,b.id b_id FROM a8s__start___start8s__a a INNER JOIN a8s b ON a.b=b.id AND a.a IN (#{ids})";
		assertEquals(expected, child.getSql());
	}
	
	@ModelDescription(hasMany={@Relation(name="a",type=A9.class,opposite="start"), @Relation(name="b",type=B9.class,through="a")}) class Start9 extends Model { }
	@ModelDescription(hasMany={@Relation(name="start",type=Start9.class,opposite="a"), @Relation(name="b",type=A9.class)}) class A9 extends Model { }
	@ModelDescription() class B9 extends Model { }

	@Test
	public void testHasManyToManyThroughSql() throws Exception {
		String sql = "include:b";
		Query query = QueryBuilder.build(dbType, Start9.class, sql);

		assertEquals("SELECT a.id a_id FROM start9s a", query.getSql());

		assertTrue(query.hasChildren());
		assertEquals(1, query.getChildren().size());
		
		Query child = query.getChildren().get(0);
		
		String expected = "SELECT a.a9s__start a_id,b.id b_id FROM a9s__start___start9s__a a INNER JOIN a9s b0 ON a.start9s__a=b0.id AND a.a9s__start IN (#{ids}) INNER JOIN b9s b ON b0.b=b.id";
		assertEquals(expected, child.getSql());
	}
	
	@ModelDescription(hasMany={@Relation(name="a",type=A10.class,opposite="start")}) class Start10 extends Model { }
	@ModelDescription(hasOne={@Relation(name="start",type=Start10.class,opposite="a")}) class A10 extends Model { }

	@Test
	public void testHasManyToOneSql() throws Exception {
		String sql = "include:a";
		Query query = QueryBuilder.build(dbType, Start10.class, sql);

		assertEquals("SELECT a.id a_id FROM start10s a", query.getSql());

		assertTrue(query.hasChildren());
		assertEquals(1, query.getChildren().size());
		
		Query child = query.getChildren().get(0);
		
		String expected = "SELECT b.start a_id,b.id b_id,b.start b_start FROM a10s b WHERE b.start IN (#{ids})";
		assertEquals(expected, child.getSql());
	}
	
	@ModelDescription(hasMany={@Relation(name="a",type=A11.class,opposite="start"), @Relation(name="b",type=B11.class,through="a:b")}) class Start11 extends Model { }
	@ModelDescription(hasOne={@Relation(name="start",type=Start11.class,opposite="a")}, hasMany={@Relation(name="b",type=A11.class)}) class A11 extends Model { }
	@ModelDescription() class B11 extends Model { }

	@Test
	public void testHasManyToOneThroughSql() throws Exception {
		String sql = "include:b";
		Query query = QueryBuilder.build(dbType, Start11.class, sql);

		assertEquals("SELECT a.id a_id FROM start11s a", query.getSql());

		assertTrue(query.hasChildren());
		assertEquals(1, query.getChildren().size());
		
		Query child = query.getChildren().get(0);
		
		String expected = "SELECT a.start a_id,b.id b_id FROM a11s a INNER JOIN b11s b ON a.b=b.id AND a.start IN (#{ids})";
		assertEquals(expected, child.getSql());
	}

	@ModelDescription(hasOne={@Relation(name="a",type=A12.class,opposite="start")}) class Start12 extends Model { }
	@ModelDescription(hasMany={@Relation(name="start",type=Start12.class,opposite="a")}) class A12 extends Model { }

	@Test
	public void testHasOneToManySql() throws Exception {
		String sql = "include:a";
		Query query = QueryBuilder.build(dbType, Start12.class, sql);

		assertEquals("SELECT a.id a_id,a.a a_a,b.id b_id FROM start12s a LEFT JOIN a12s b ON a.a=b.id", query.getSql());

		assertFalse(query.hasChildren());
	}
	
}
