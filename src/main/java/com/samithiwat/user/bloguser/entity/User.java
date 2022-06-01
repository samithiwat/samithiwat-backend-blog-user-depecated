package com.samithiwat.user.bloguser.entity;

import com.samithiwat.user.post.entity.Post;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.UpdateTimestamp;
import org.hibernate.annotations.Where;

import javax.persistence.*;
import java.time.Instant;
import java.util.List;

@Entity
@SQLDelete(sql = "UPDATE user SET deletedDate = CURRENT_DATE WHERE id = ?")
@Where(clause = "deletedDate IS NULL")
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    @ManyToMany(cascade = CascadeType.ALL)
    @JoinTable(name = "user_post_bookmark",
        joinColumns = @JoinColumn(name="user_id", referencedColumnName = "id"),
        inverseJoinColumns = @JoinColumn(name="post_id", referencedColumnName = "id")
    )
    private List<Post> bookmarks;

    @ManyToMany(cascade = CascadeType.ALL)
    @JoinTable(name = "user_post_read",
        joinColumns = @JoinColumn(name="user_id", referencedColumnName = "id"),
        inverseJoinColumns = @JoinColumn(name="post_id",referencedColumnName = "id")
    )
    private List<Post> reads;

    @Column(unique = true)
    private Long userId;

    @Column
    private String description;

    @CreationTimestamp
    private Instant createdDate;

    @UpdateTimestamp
    private Instant updatedDate;

    @Column
    private Instant deletedDate;

    public User() {}

    public List<Post> getReads() {
        return reads;
    }

    public void setReads(List<Post> reads) {
        this.reads = reads;
    }

    public List<Post> getBookmarks() {
        return bookmarks;
    }

    public void setBookmarks(List<Post> bookmarks) {
        this.bookmarks = bookmarks;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}
