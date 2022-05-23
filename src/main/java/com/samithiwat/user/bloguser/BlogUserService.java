package com.samithiwat.user.bloguser;

import com.samithiwat.user.bloguser.entity.User;
import com.samithiwat.user.grpc.bloguser.*;
import com.samithiwat.user.grpc.dto.BlogUser;
import io.grpc.stub.StreamObserver;
import net.devh.boot.grpc.server.service.GrpcService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;

import java.util.Optional;

@GrpcService
public class BlogUserService extends BlogUserServiceGrpc.BlogUserServiceImplBase {
    @Autowired
    private BlogUserRepository repository;

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
                    .setErrors(0,"Not found user");

            responseObserver.onNext(res.build());
            responseObserver.onCompleted();
            return;
        }

        User user = query.get();
        BlogUser result = BlogUser.newBuilder()
                .setId(Math.toIntExact(user.getId()))
                .setUserId(Math.toIntExact(user.getUserId()))
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
