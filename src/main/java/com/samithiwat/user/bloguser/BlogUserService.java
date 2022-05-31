package com.samithiwat.user.bloguser;

import com.samithiwat.user.bloguser.entity.User;
import com.samithiwat.user.grpc.bloguser.*;
import com.samithiwat.user.grpc.dto.BlogUser;
import com.samithiwat.user.post.PostService;
import com.samithiwat.user.post.PostServiceImpl;
import com.samithiwat.user.post.entity.Post;
import com.samithiwat.user.user.UserService;
import com.samithiwat.user.user.UserServiceImpl;
import io.grpc.stub.StreamObserver;
import net.devh.boot.grpc.server.service.GrpcService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.http.HttpStatus;

import java.util.List;

@GrpcService
public class BlogUserService extends BlogUserServiceGrpc.BlogUserServiceImplBase {
    @Autowired
    private BlogUserRepository repository;

    @Autowired
    private UserService userService;

    @Autowired
    private PostService postService;

    public BlogUserService(){}

    public BlogUserService(BlogUserRepository repository, UserService userService, PostService postService){
        this.repository = repository;
        this.userService = userService;
        this.postService = postService;
    }

    @Override
    public void addBookmark(AddBookmarkRequest request, StreamObserver<BookmarkResponse> responseObserver) {
        BookmarkResponse.Builder res = BookmarkResponse.newBuilder();

        User user = this.repository.findById((long) request.getUserId()).orElse(null);

        if(user == null){
            res.setStatusCode(HttpStatus.NOT_FOUND.value())
                    .addErrors("Not found user");

            responseObserver.onNext(res.build());
            responseObserver.onCompleted();
            return;
        }

        List<Post> posts = user.getBookmarks();
        Post post = this.postService.findOneOrCreate((long) request.getPostId());

        posts.add(post);

        this.repository.save(user);

        for (Post p:posts) {
            System.out.println(p);
            res.addData(Math.toIntExact(p.getId()));
        }

        res.setStatusCode(HttpStatus.OK.value());

        responseObserver.onNext(res.build());
        responseObserver.onCompleted();
    }

    @Override
    public void findAllBookmark(FindAllBookmarkRequest request, StreamObserver<BookmarkResponse> responseObserver) {
        super.findAllBookmark(request, responseObserver);
    }

    @Override
    public void deleteBookmark(DeleteBookmarkRequest request, StreamObserver<BookmarkResponse> responseObserver) {
        super.deleteBookmark(request, responseObserver);
    }

    @Override
    public void read(ReadRequest request, StreamObserver<ReadResponse> responseObserver) {
        super.read(request, responseObserver);
    }

    @Override
    public void findOne(FindOneUserRequest request, StreamObserver<BlogUserResponse> responseObserver) {
        BlogUserResponse.Builder res = BlogUserResponse.newBuilder();

        User user = this.repository.findById(Long.valueOf(request.getId())).orElse(null);
        if(user == null){
            res.setStatusCode(HttpStatus.NOT_FOUND.value())
                    .addErrors("Not found user");

            responseObserver.onNext(res.build());
            responseObserver.onCompleted();
            return;
        }

        com.samithiwat.user.grpc.dto.User userDto = this.userService.findOne(user.getUserId());

        if(userDto == null){
            res.setStatusCode(HttpStatus.NOT_FOUND.value())
                    .addErrors("Invalid user data");

            responseObserver.onNext(res.build());
            responseObserver.onCompleted();
            return;
        }

        BlogUser result = BlogUser.newBuilder()
                .setId(Math.toIntExact(user.getId()))
                .setFirstname(userDto.getFirstname())
                .setLastname(userDto.getLastname())
                .setDisplayName(userDto.getDisplayName())
                .setImageUrl(userDto.getImageUrl())
                .setDescription(user.getDescription())
                .build();

        res.setStatusCode(HttpStatus.OK.value())
                .setData(result);

        responseObserver.onNext(res.build());
        responseObserver.onCompleted();
    }

    @Override
    public void create(CreateUserRequest request, StreamObserver<BlogUserResponse> responseObserver) {
        BlogUserResponse.Builder res = BlogUserResponse.newBuilder();

        com.samithiwat.user.grpc.dto.User userDto = this.userService.findOne((long) request.getUserId());

        if(userDto == null){
            res.setStatusCode(HttpStatus.NOT_FOUND.value())
                    .addErrors("Not found user");

            responseObserver.onNext(res.build());
            responseObserver.onCompleted();
            return;
        }

        User dto = new User();
        dto.setUserId((long) request.getUserId());
        dto.setDescription(request.getDescription());

        User user = this.repository.save(dto);

        BlogUser result = BlogUser.newBuilder()
                .setId(Math.toIntExact(user.getId()))
                .setFirstname(userDto.getFirstname())
                .setLastname(userDto.getLastname())
                .setDisplayName(userDto.getDisplayName())
                .setImageUrl(userDto.getImageUrl())
                .setDescription(user.getDescription())
                .build();

        res.setStatusCode(HttpStatus.CREATED.value())
                .setData(result);

        responseObserver.onNext(res.build());
        responseObserver.onCompleted();
    }

    @Override
    public void update(UpdateUserRequest request, StreamObserver<BlogUserResponse> responseObserver) {
        BlogUserResponse.Builder res = BlogUserResponse.newBuilder();

        User user = this.repository.findById(Long.valueOf(request.getId())).map(u -> {
            u.setDescription(request.getDescription());
            return this.repository.save(u);
        }).orElse(null);

        if(user == null){
            res.setStatusCode(HttpStatus.NOT_FOUND.value())
                    .addErrors("Not found user");

            responseObserver.onNext(res.build());
            responseObserver.onCompleted();
            return;
        }

        BlogUser result = BlogUser.newBuilder()
                .setId(Math.toIntExact(user.getId()))
                .setDescription(user.getDescription())
                .build();

        res.setStatusCode(HttpStatus.OK.value())
                .setData(result);

        responseObserver.onNext(res.build());
        responseObserver.onCompleted();
    }

    @Override
    public void delete(DeleteUserRequest request, StreamObserver<BlogUserResponse> responseObserver) {
        BlogUserResponse.Builder res = BlogUserResponse.newBuilder();

        try{
            this.repository.deleteById((long) request.getId());
            res.setStatusCode(HttpStatus.NO_CONTENT.value())
                    .setData(BlogUser.newBuilder().build());

            responseObserver.onNext(res.build());
            responseObserver.onCompleted();
        }catch(EmptyResultDataAccessException err){
            res.setStatusCode(HttpStatus.NOT_FOUND.value())
                    .addErrors("Not found user");

            responseObserver.onNext(res.build());
            responseObserver.onCompleted();
        }
    }
}
