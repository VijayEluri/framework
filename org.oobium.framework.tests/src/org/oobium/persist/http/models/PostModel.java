/*
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
*/
package org.oobium.persist.http.models;

import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.oobium.persist.Model;
import org.oobium.persist.Paginator;
import org.oobium.persist.PersistService;
import org.oobium.utils.json.JsonModel;

public abstract class PostModel extends Model {

	public static final String COMMENTS = "comments";
	public static final String CONTENT = "content";
	public static final String CREATED_AT = "createdAt";
	public static final String PUBLISHED = "published";
	public static final String TITLE = "title";
	public static final String UPDATED_AT = "updatedAt";

	/**
	 * Get the PersistService appropriate for the Post class.
	*/
	public static PersistService getPersistService() {
		return Model.getPersistService(Post.class);
	}

	/**
	 * Create a new instance of Post and set its id to the given value
	*/
	public static Post newInstance(int id) {
		Post post = new Post();
		post.setId(id);
		return post;
	}

	/**
	 * Create a new instance of Post and initialize it with the given fields
	*/
	public static Post newInstance(Map<String, Object> fields) {
		Post post = new Post();
		post.setAll(fields);
		return post;
	}

	/**
	 * Create a new instance of Post and initialize it with the given json data
	*/
	public static Post newInstance(String json) {
		Post post = new Post();
		post.setAll(json);
		return post;
	}

	/**
	 * Get the count of all instances of Post
	*/
	public static long count() throws Exception {
		return getPersistService().count(Post.class);
	}

	/**
	 * Get the count of all instances of Post using the given sql query and values.
	*/
	public static long count(String sql, Object...values) throws Exception {
		return getPersistService().count(Post.class, sql, values);
	}

	/**
	 * Find the Post with the given id
	*/
	public static Post find(int id) throws Exception {
		return getPersistService().findById(Post.class, id);
	}

	/**
	 * Find the Post with the given id and include the given fields.
	 * The include option can start with 'include:', but it is not required.
	*/
	public static Post find(int id, String include) throws Exception {
		String sql = (include.startsWith("include:") ? "where id=? " : "where id=? include:") + include;
		return getPersistService().find(Post.class, sql, id);
	}

	/**
	 * Find the Post using the given sql query and values.  Note that only one instance will be returned.
	 * Prepend the query with 'where' to enter only the where clause.
	*/
	public static Post find(String sql, Object...values) throws Exception {
		return getPersistService().find(Post.class, sql, values);
	}

	/**
	 * Find all instances of Post
	*/
	public static List<Post> findAll() throws Exception {
		return getPersistService().findAll(Post.class);
	}

	/**
	 * Find all instances of Post using the given sql query and values.
	*/
	public static List<Post> findAll(String sql, Object...values) throws Exception {
		return getPersistService().findAll(Post.class, sql, values);
	}

	public static Paginator<Post> paginate(int page, int perPage) throws Exception {
		return Paginator.paginate(Post.class, page, perPage);
	}

	public static Paginator<Post> paginate(int page, int perPage, String sql, Object...values) throws Exception {
		return Paginator.paginate(Post.class, page, perPage, sql, values);
	}

	public PostModel() {
		super();
	}

	@SuppressWarnings("unchecked")
	public Set<Comment> comments() {
		return (Set<Comment>) get(COMMENTS);
	}

	public String getContent() {
		return get(CONTENT, String.class);
	}

	public Date getCreatedAt() {
		return get(CREATED_AT, Date.class);
	}

	public Date getPublished() {
		return get(PUBLISHED, Date.class);
	}

	public String getTitle() {
		return get(TITLE, String.class);
	}

	public Date getUpdatedAt() {
		return get(UPDATED_AT, Date.class);
	}

	public boolean hasContent() {
		return get(CONTENT) != null;
	}

	public boolean hasCreatedAt() {
		return get(CREATED_AT) != null;
	}

	public boolean hasPublished() {
		return get(PUBLISHED) != null;
	}

	public boolean hasTitle() {
		return get(TITLE) != null;
	}

	public boolean hasUpdatedAt() {
		return get(UPDATED_AT) != null;
	}

	@Override
	public Post put(String field, Object value) {
		return (Post) super.put(field, value);
	}

	@Override
	public Post putAll(JsonModel model) {
		return (Post) super.putAll(model);
	}

	@Override
	public Post putAll(Map<String, Object> data) {
		return (Post) super.putAll(data);
	}

	@Override
	public Post putAll(String json) {
		return (Post) super.putAll(json);
	}

	@Override
	public Post set(String field, Object value) {
		return (Post) super.set(field, value);
	}

	@Override
	public Post setAll(Map<String, Object> data) {
		return (Post) super.setAll(data);
	}

	@Override
	public Post setAll(String json) {
		return (Post) super.setAll(json);
	}

	public Post setContent(String content) {
		return set(CONTENT, content);
	}

	public Post setCreatedAt(Date createdAt) {
		return set(CREATED_AT, createdAt);
	}

	@Override
	public Post setId(Object id) {
		return (Post) super.setId(id);
	}

	public Post setPublished(Date published) {
		return set(PUBLISHED, published);
	}

	public Post setTitle(String title) {
		return set(TITLE, title);
	}

	public Post setUpdatedAt(Date updatedAt) {
		return set(UPDATED_AT, updatedAt);
	}

}