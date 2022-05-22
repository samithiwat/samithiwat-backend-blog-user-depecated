package com.samithiwat.user.user;

import com.github.javafaker.Faker;
import com.samithiwat.user.grpc.dto.User;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mockito;
import org.mockito.Spy;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import user.FindOneUserRequest;
import user.UserServiceGrpc;

import java.util.ArrayList;
import java.util.List;

@SpringBootTest
@ExtendWith(SpringExtension.class)
public class UserServiceTest {

    @Spy
    private UserServiceGrpc.UserServiceBlockingStub userServiceStub;
    private List<User> users;
    private User user;
    private Faker faker;

    @Autowired
    private UserService userService;

    @BeforeEach
    void initMockData(){
        this.faker =  new Faker();

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
    public void testFindAllSuccess(){
        Mockito.doReturn(user).when(userServiceStub).findOne(FindOneUserRequest.newBuilder().build());

        User user = userService.findOne(1l);

        Assertions.assertEquals(this.user, user);
    }
}
