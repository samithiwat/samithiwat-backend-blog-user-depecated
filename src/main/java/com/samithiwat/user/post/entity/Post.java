package com.samithiwat.user.post.entity;

import com.samithiwat.user.bloguser.entity.User;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.UpdateTimestamp;
import org.hibernate.annotations.Where;

import javax.persistence.*;
import java.time.Instant;
import java.util.List;

@Entity
@Table(name = "post")
@SQLDelete(sql = "UPDATE user SET deletedDate = CURRENT_DATE WHERE id = ?")
@Where(clause = "deletedDate IS NULL")
public class Post {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    @ManyToMany(mappedBy = "bookmarks")
    private List<User> userBookmarks;

    @ManyToMany(mappedBy = "reads")
    private List<User> userReads;

    @Column(unique = true)
    private Long postId;

    @CreationTimestamp
    private Instant createdDate;

    @UpdateTimestamp
    private Instant updatedDate;

    @Column
    private Instant deletedDate;

    public Post(){}

    public Post(Long postId){
        setPostId(postId);
    }

    public List<User> getUserBookmarks() {
        return userBookmarks;
    }

    public void setUserBookmarks(List<User> userBookmarks) {
        this.userBookmarks = userBookmarks;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getPostId() {
        return postId;
    }

    public void setPostId(Long postId) {
        this.postId = postId;
    }

    @Override
    public String toString() {
        return "Post{" +
                "id=" + id +
                ", userBookmarks=" + userBookmarks +
                ", postId=" + postId +
                '}';
    }
}
