package com.samithiwat.user.post;

import com.samithiwat.user.post.entity.Post;
import org.springframework.beans.factory.annotation.Autowired;

public class PostServiceImpl implements PostService{

    @Autowired
    private PostRepository repository;

    public PostServiceImpl(){}

    public PostServiceImpl(PostRepository repository){
        this.repository = repository;
    }

    @Override
    public Post findOneOrCreate(Long postId) {
        return this.repository.findOneByPostId(postId).orElseGet(()->{
            Post post = new Post(postId);
            return this.repository.save(post);
        });
    }
}
