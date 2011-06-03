/*
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
*/
package org.oobium.persist.http.models;

import java.lang.String;
import java.sql.SQLException;
import java.util.Date;
import java.util.List;
import java.util.Map;
import org.oobium.persist.Model;
import org.oobium.persist.Paginator;
import org.oobium.persist.PersistService;
import org.oobium.utils.json.JsonModel;

public abstract class CommentModel extends Model {

	public static final String CREATED_AT = "createdAt";
	public static final String MESSAGE = "message";
	public static final String NAME = "name";
	public static final String POST = "post";
	public static final String UPDATED_AT = "updatedAt";
	public static final String WEBSITE = "website";

	/**
	 * Get the PersistService appropriate for the Comment class.
	*/
	public static PersistService getPersistService() {
		return Model.getPersistService(Comment.class);
	}

	/**
	 * Create a new instance of Comment and set its id to the given value
	*/
	public static Comment newInstance(int id) {
		Comment comment = new Comment();
		comment.setId(id);
		return comment;
	}

	/**
	 * Create a new instance of Comment and initialize it with the given fields
	*/
	public static Comment newInstance(Map<String, Object> fields) {
		Comment comment = new Comment();
		comment.setAll(fields);
		return comment;
	}

	/**
	 * Create a new instance of Comment and initialize it with the given json data
	*/
	public static Comment newInstance(String json) {
		Comment comment = new Comment();
		comment.setAll(json);
		return comment;
	}

	/**
	 * Get the count of all instances of Comment
	*/
	public static int count() throws SQLException {
		return Model.count(Comment.class);
	}

	/**
	 * Get the count of all instances of Comment using the given sql query and values.
	*/
	public static int count(String sql, Object...values) throws SQLException {
		return Model.count(Comment.class, sql, values);
	}

	/**
	 * Find the Comment with the given id
	*/
	public static Comment find(int id) throws SQLException {
		return Model.find(Comment.class, id);
	}

	/**
	 * Find the Comment with the given id and include the given fields.
	 * The include option can start with 'include:', but it is not required.
	*/
	public static Comment find(int id, String include) throws SQLException {
		String sql = (include.startsWith("include:") ? "where id=? " : "where id=? include:") + include;
		return Model.find(Comment.class, sql, id);
	}

	/**
	 * Find the Comment using the given sql query and values.  Note that only one instance will be returned.
	 * Prepend the query with 'where' to enter only the where clause.
	*/
	public static Comment find(String sql, Object...values) throws SQLException {
		return Model.find(Comment.class, sql, values);
	}

	/**
	 * Find all instances of Comment
	*/
	public static List<Comment> findAll() throws SQLException {
		return Model.findAll(Comment.class);
	}

	/**
	 * Find all instances of Comment using the given sql query and values.
	*/
	public static List<Comment> findAll(String sql, Object...values) throws SQLException {
		return Model.findAll(Comment.class, sql, values);
	}

	public static Paginator<Comment> paginate(int page, int perPage) throws SQLException {
		return Paginator.paginate(Comment.class, page, perPage);
	}

	public static Paginator<Comment> paginate(int page, int perPage, String sql, Object...values) throws SQLException {
		return Paginator.paginate(Comment.class, page, perPage, sql, values);
	}

	public CommentModel() {
		super();
	}

	public Date getCreatedAt() {
		return get(CREATED_AT, Date.class);
	}

	public String getMessage() {
		return get(MESSAGE, String.class);
	}

	public String getName() {
		return get(NAME, String.class);
	}

	public Post getPost() {
		return get(POST, Post.class);
	}

	public Date getUpdatedAt() {
		return get(UPDATED_AT, Date.class);
	}

	public String getWebsite() {
		return get(WEBSITE, String.class);
	}

	public boolean hasCreatedAt() {
		return get(CREATED_AT) != null;
	}

	public boolean hasMessage() {
		return get(MESSAGE) != null;
	}

	public boolean hasName() {
		return get(NAME) != null;
	}

	public boolean hasPost() {
		return get(POST) != null;
	}

	public boolean hasUpdatedAt() {
		return get(UPDATED_AT) != null;
	}

	public boolean hasWebsite() {
		return get(WEBSITE) != null;
	}

	@Override
	public Comment put(String field, Object value) {
		return (Comment) super.put(field, value);
	}

	@Override
	public Comment putAll(JsonModel model) {
		return (Comment) super.putAll(model);
	}

	@Override
	public Comment putAll(Map<String, Object> data) {
		return (Comment) super.putAll(data);
	}

	@Override
	public Comment putAll(String json) {
		return (Comment) super.putAll(json);
	}

	@Override
	public Comment set(String field, Object value) {
		return (Comment) super.set(field, value);
	}

	@Override
	public Comment setAll(Map<String, Object> data) {
		return (Comment) super.setAll(data);
	}

	@Override
	public Comment setAll(String json) {
		return (Comment) super.setAll(json);
	}

	public Comment setCreatedAt(Date createdAt) {
		return set(CREATED_AT, createdAt);
	}

	@Override
	public Comment setId(int id) {
		return (Comment) super.setId(id);
	}

	public Comment setMessage(String message) {
		return set(MESSAGE, message);
	}

	public Comment setName(String name) {
		return set(NAME, name);
	}

	public Comment setPost(Post post) {
		return set(POST, post);
	}

	public Comment setUpdatedAt(Date updatedAt) {
		return set(UPDATED_AT, updatedAt);
	}

	public Comment setWebsite(String website) {
		return set(WEBSITE, website);
	}

}