package com.samithiwat.user.bloguser;

import com.samithiwat.user.bloguser.entity.User;
import com.samithiwat.user.grpc.bloguser.*;
import com.samithiwat.user.grpc.dto.BlogUser;
import com.samithiwat.user.user.UserService;
import io.grpc.stub.StreamObserver;
import net.devh.boot.grpc.server.service.GrpcService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;

import java.util.Optional;

@GrpcService
public class BlogUserService extends BlogUserServiceGrpc.BlogUserServiceImplBase {
    @Autowired
    private BlogUserRepository repository;

    @Autowired
    private UserService userService;

    public BlogUserService() {}

    public BlogUserService(BlogUserRepository blogUserRepository, UserService userService) {
        this.repository = blogUserRepository;
        this.userService = userService;
    }

    @Override
    public void findAll(FindAllUserRequest request, StreamObserver<BlogUserPaginationResponse> responseObserver) {
        super.findAll(request, responseObserver);
    }

    @Override
    public void findOne(FindOneUserRequest request, StreamObserver<BlogUserResponse> responseObserver) {
        BlogUserResponse.Builder res = BlogUserResponse.newBuilder();

        Optional<User> query = this.repository.findById(Long.valueOf(request.getId()));
        if(query.isEmpty()){
            res.setStatusCode(HttpStatus.NOT_FOUND.value())
                    .addErrors("Not found user");

            responseObserver.onNext(res.build());
            responseObserver.onCompleted();
            return;
        }

        User user = query.get();
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
        super.create(request, responseObserver);
    }

    @Override
    public void update(UpdateUserRequest request, StreamObserver<BlogUserResponse> responseObserver) {
        super.update(request, responseObserver);
    }

    @Override
    public void delete(DeleteUserRequest request, StreamObserver<BlogUserResponse> responseObserver) {
        super.delete(request, responseObserver);
    }
}
