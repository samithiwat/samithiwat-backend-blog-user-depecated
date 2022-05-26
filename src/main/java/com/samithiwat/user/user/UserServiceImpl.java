package com.samithiwat.user.user;

import com.samithiwat.user.grpc.dto.User;
import net.devh.boot.grpc.client.inject.GrpcClient;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import user.FindOneUserRequest;
import user.UserResponse;
import user.UserServiceGrpc;

@Service
public class UserServiceImpl implements UserService{

    @GrpcClient("UserService")
    private UserServiceGrpc.UserServiceBlockingStub userService;

    public UserServiceImpl() {}

    public UserServiceImpl(UserServiceGrpc.UserServiceBlockingStub userServiceBlockingStub) {
        this.userService = userServiceBlockingStub;
    }

    public User findOne(Long id){
        FindOneUserRequest req = FindOneUserRequest.newBuilder()
                .setId(Math.toIntExact(id))
                .build();

        UserResponse res = userService.findOne(req);

        int statusCode = res.getStatusCode();

        if(statusCode != HttpStatus.OK.value()){
            return null;
        }

        return res.getData();
    }
}
