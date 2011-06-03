package org.oobium.persist.http.models;

import java.util.Date;

import org.oobium.persist.Attribute;
import org.oobium.persist.ModelDescription;
import org.oobium.persist.Relation;
import org.oobium.persist.Text;

@ModelDescription(
	attrs = {
		@Attribute(name="title", type=String.class),
		@Attribute(name="content", type=Text.class),
		@Attribute(name="published", type=Date.class)
	},
	hasMany = {
		@Relation(name="comments", type=Comment.class, opposite="post")
	},
	timestamps = true
)
public class Post extends PostModel {

}