package org.oobium.persist.http.models;

import org.oobium.persist.Attribute;
import org.oobium.persist.ModelDescription;
import org.oobium.persist.Relation;
import org.oobium.persist.Text;

@ModelDescription(
	attrs = {
		@Attribute(name="name", type=String.class),
		@Attribute(name="website", type=String.class),
		@Attribute(name="message", type=Text.class)
	},
	hasOne = {
		@Relation(name="post", type=Post.class, opposite="comments")
	},
	timestamps = true
)
public class Comment extends CommentModel {

}