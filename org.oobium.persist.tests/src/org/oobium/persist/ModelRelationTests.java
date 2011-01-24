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
import static org.hamcrest.CoreMatchers.*;

import java.util.Set;

import org.junit.Test;
import org.oobium.persist.Attribute;
import org.oobium.persist.Model;
import org.oobium.persist.ModelDescription;
import org.oobium.persist.Relation;

public class ModelRelationTests {

	@ModelDescription(
		attrs = { @Attribute(name="title", type=String.class), @Attribute(name="content", type=Text.class) },
		hasMany = { @Relation(name="comments", type=Comment.class, opposite="post") }
	)
	class Post extends Model {
		@SuppressWarnings("unchecked")
		Set<Comment> comments() { return (Set<Comment>) get("comments"); }
	}

	@ModelDescription(
		attrs = { @Attribute(name="commenter", type=String.class), @Attribute(name="content", type=Text.class) },
		hasOne = { @Relation(name="post", type=Post.class, opposite="comments") }
	)
	class Comment extends Model {
		Post getPost() { return get("post", Post.class); }
		Comment setPost(Post post) { set("post", post); return this; }
	}

	
	@Test
	public void testHasMany() throws Exception {
		Post post = new Post();
		Comment comment = new Comment();
		
		post.comments().add(comment);
		assertThat(post.comments().size(), is(1));
		assertThat(post.comments().contains(comment), is(true));
		assertThat(comment.getPost(), is(post));
		
		post.comments().remove(comment);
		assertThat(post.comments().isEmpty(), is(true));
		assertThat(comment.getPost(), is(nullValue()));
	}

	@Test
	public void testHasOne() throws Exception {
		Post post = new Post();
		Comment comment = new Comment();

		comment.setPost(post);
		assertThat(post.comments().size(), is(1));
		assertTrue(post.comments().contains(comment));
		assertThat(comment.getPost(), is(post));

		comment.setPost(null);
		assertThat(post.comments().isEmpty(), is(true));
		assertThat(comment.getPost(), is(nullValue()));
	}

}
