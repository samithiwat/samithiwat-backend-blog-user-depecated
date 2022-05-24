package com.samithiwat.user.user;

import com.github.javafaker.Faker;
import com.samithiwat.user.TestConfig;
import com.samithiwat.user.grpc.dto.User;
import io.grpc.ManagedChannel;
import io.grpc.inprocess.InProcessChannelBuilder;
import io.grpc.inprocess.InProcessServerBuilder;
import io.grpc.stub.StreamObserver;
import io.grpc.testing.GrpcCleanupRule;
import org.junit.Rule;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;
import user.FindOneUserRequest;
import user.UserResponse;
import user.UserServiceGrpc;

import java.util.ArrayList;
import java.util.List;

@SpringBootTest(properties = {
        "grpc.server.inProcessName=test-user", // Enable inProcess server
        "grpc.server.port=-1", // Disable external server
        "grpc.client.userService.address=in-process:test" // Configure the client to connect to the inProcess server
})
@SpringJUnitConfig(classes = {TestConfig.class})
@DirtiesContext
@ExtendWith(SpringExtension.class)
public class UserServiceTest {
    @Rule
    public final GrpcCleanupRule grpcCleanup = new GrpcCleanupRule();

    private List<User> users;
    private User user;
    private Faker faker;

    @BeforeEach
    void setup(){
        this.faker = new Faker();

        this.users = new ArrayList<User>();
        this.user = User.newBuilder()
                .setId(1)
                .setFirstname(faker.name().firstName())
                .setLastname(faker.name().lastName())
                .setDisplayName(faker.name().username())
                .build();

        User user2 = User.newBuilder()
                .setId(2)
                .setFirstname(faker.name().firstName())
                .setLastname(faker.name().lastName())
                .setDisplayName(faker.name().username())
                .build();

        User user3 = User.newBuilder()
                .setId(3)
                .setFirstname(faker.name().firstName())
                .setLastname(faker.name().lastName())
                .setDisplayName(faker.name().username())
                .build();

        this.users.add(user);
        this.users.add(user2);
        this.users.add(user3);
    }

    @Test
    public void testFindOneSuccess(){
        UserServiceGrpc.UserServiceImplBase userService = new UserServiceGrpc.UserServiceImplBase() {
            @Override
            public void findOne(FindOneUserRequest request, StreamObserver<UserResponse> responseObserver) {
                UserResponse res = UserResponse.newBuilder()
                        .setStatusCode(HttpStatus.OK.value())
                        .setData(user)
                        .build();

                responseObserver.onNext(res);
                responseObserver.onCompleted();
            }
        };

        try{
            grpcCleanup.register(InProcessServerBuilder.forName("user-service-test-findOne-success").directExecutor().addService(userService).build().start());
        }catch(Exception err){
            System.out.println("Error occurs while generating testing service");
            System.out.println(err.getMessage());
        }

        ManagedChannel chan = grpcCleanup.register(InProcessChannelBuilder.forName("user-service-test-findOne-success").directExecutor().build());
        UserServiceGrpc.UserServiceBlockingStub userBlockingStub = UserServiceGrpc.newBlockingStub(chan);
        UserService service = new UserService(userBlockingStub);

        Assertions.assertEquals(this.user,service.findOne(1l));
    }

    @Test
    public void testFindOneNotFound(){
        UserServiceGrpc.UserServiceImplBase userService = new UserServiceGrpc.UserServiceImplBase() {
            @Override
            public void findOne(FindOneUserRequest request, StreamObserver<UserResponse> responseObserver) {
                UserResponse res = UserResponse.newBuilder()
                        .setStatusCode(HttpStatus.NOT_FOUND.value())
                        .addErrors("Not found user")
                        .build();

                responseObserver.onNext(res);
                responseObserver.onCompleted();
            }
        };

        try{
            grpcCleanup.register(InProcessServerBuilder.forName("user-service-test-findOne-failed").directExecutor().addService(userService).build().start());
        }catch(Exception err){
            System.out.println("Error occurs while generating testing service");
            System.out.println(err.getMessage());
        }

        ManagedChannel chan = grpcCleanup.register(InProcessChannelBuilder.forName("user-service-test-findOne-failed").directExecutor().build());
        UserServiceGrpc.UserServiceBlockingStub userBlockingStub = UserServiceGrpc.newBlockingStub(chan);
        UserService service = new UserService(userBlockingStub);

        Assertions.assertEquals(null,service.findOne(1l));
    }
}
