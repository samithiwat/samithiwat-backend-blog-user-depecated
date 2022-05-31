package com.samithiwat.user.post;

import com.samithiwat.user.post.entity.Post;

public interface PostService {
    Post findOneOrCreate(Long postId);
}
