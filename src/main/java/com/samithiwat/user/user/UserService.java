package com.samithiwat.user.user;

import com.samithiwat.user.grpc.dto.User;
import net.devh.boot.grpc.client.inject.GrpcClient;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import user.FindOneUserRequest;
import user.UserServiceGrpc;

@Service
public class UserService {

    @GrpcClient("UserService")
    private UserServiceGrpc.UserServiceBlockingStub userService;

    public UserService() {}

    public UserService(UserServiceGrpc.UserServiceBlockingStub userServiceBlockingStub) {
        this.userService = userServiceBlockingStub;
    }

    public User findOne(Long id){
        FindOneUserRequest req = FindOneUserRequest.newBuilder()
                .setId(Math.toIntExact(id))
                .build();
        int statusCode = userService.findOne(req).getStatusCode();

        if(statusCode != HttpStatus.OK.value()){
            return null;
        }

        return userService.findOne(req).getData();
    }
}
