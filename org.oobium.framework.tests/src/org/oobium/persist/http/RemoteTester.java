package org.oobium.persist.http;

import java.util.List;

import org.oobium.persist.http.models.Comment;
import org.oobium.persist.http.models.Post;

public class RemoteTester {

	public static void main(String[] args) {
		HttpApiService.getInstance().setDiscoveryUrl("localhost:5000");

		RemoteWorkers.submit(new RemoteWorker<List<Post>>() {
			@Override
			protected List<Post> run() throws Exception {
				List<Post> posts = Post.findAll();
				for(Post post : posts) {
					if(post.comments().isEmpty()) continue;
					for(Comment comment : post.comments()) {
						System.out.println(comment.toJson());
					}
				}
				posts = Post.findAll();
				return posts;
			}
		});
	}
	
}
