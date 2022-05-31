package com.samithiwat.user.post;

import com.samithiwat.user.post.entity.Post;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface PostRepository extends CrudRepository<Post, Long> {
    @Query("SELECT p FROM Post p WHERE p.postId = :postId")
    Optional<Post> findOneByPostId(@Param("postId") Long postId);
}
