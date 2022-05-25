package com.samithiwat.user.bloguser;

import com.github.javafaker.Faker;
import com.samithiwat.user.TestConfig;
import com.samithiwat.user.bloguser.entity.User;
import com.samithiwat.user.grpc.bloguser.BlogUserResponse;
import com.samithiwat.user.grpc.bloguser.FindOneUserRequest;
import com.samithiwat.user.grpc.bloguser.UpdateUserRequest;
import com.samithiwat.user.grpc.dto.BlogUser;
import com.samithiwat.user.user.UserService;
import io.grpc.internal.testing.StreamRecorder;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mockito;
import org.mockito.Spy;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

@SpringBootTest(properties = {
        "grpc.server.inProcessName=test-bloguser", // Enable inProcess server
        "grpc.server.port=-1", // Disable external server
        "grpc.client.blogUserService.address=in-process:test" // Configure the client to connect to the inProcess server
})
@SpringJUnitConfig(classes = {TestConfig.class})
@ExtendWith(SpringExtension.class)
public class BlogUserServiceTest {
    @Spy
    private UserService userService;

    @Spy
    private BlogUserRepository repository;

    private List<User> users;
    private Optional<User> user;
    private com.samithiwat.user.grpc.dto.User userDto;
    private BlogUser blogUserDto;
    private Faker faker;

    @BeforeEach
    void setup(){
        this.faker = new Faker();

        this.users = new ArrayList<User>();
        this.user = Optional.of(new User());
        this.user.get().setId(1l);
        this.user.get().setDescription(faker.lorem().sentence());
        this.user.get().setUserId(1l);

        User user2 = new User();
        user2.setId(2l);
        user2.setDescription(faker.lorem().sentence());
        user2.setUserId(2l);

        User user3 = new User();
        user3.setId(3l);
        user3.setDescription(faker.lorem().sentence());
        user3.setUserId(3l);

        this.userDto = com.samithiwat.user.grpc.dto.User.newBuilder()
                .setId(1)
                .setFirstname(faker.name().firstName())
                .setLastname(faker.name().lastName())
                .setDisplayName(faker.name().username())
                .build();

        this.blogUserDto = BlogUser.newBuilder()
                .setId(Math.toIntExact(this.user.get().getId()))
                .setFirstname(this.userDto.getFirstname())
                .setLastname(this.userDto.getLastname())
                .setDisplayName(this.userDto.getDisplayName())
                .setImageUrl(this.userDto.getImageUrl())
                .setDescription(this.user.get().getDescription())
                .build();

        this.users.add(user.get());
        this.users.add(user2);
        this.users.add(user3);
    }

    @Test
    public void testFindOneSuccess() throws Exception {
        Mockito.doReturn(userDto).when(this.userService).findOne(1l);
        Mockito.doReturn(user).when(this.repository).findById(1l);

        BlogUserService service = new BlogUserService(this.repository, this.userService);

        FindOneUserRequest req = FindOneUserRequest.newBuilder()
                .setId(1)
                .build();

        StreamRecorder<BlogUserResponse> res = StreamRecorder.create();

        service.findOne(req, res);

        if (!res.awaitCompletion(5, TimeUnit.SECONDS)){
            Assertions.fail();
        }

        List<BlogUserResponse> results = res.getValues();

        Assertions.assertEquals(1, results.size());

        BlogUserResponse result = results.get(0);

        Assertions.assertEquals(HttpStatus.OK.value(), result.getStatusCode());
        Assertions.assertEquals(0, result.getErrorsCount());
        Assertions.assertEquals(this.blogUserDto, result.getData());
    }

    @Test
    public void testFindOneNotFoundInDatabase() throws Exception {
        Mockito.doReturn(Optional.empty()).when(this.repository).findById(1l);
        Mockito.doReturn(userDto).when(this.userService).findOne(1l);

        BlogUserService service = new BlogUserService(this.repository, this.userService);

        FindOneUserRequest req = FindOneUserRequest.newBuilder()
                .setId(1)
                .build();

        StreamRecorder<BlogUserResponse> res = StreamRecorder.create();

        service.findOne(req, res);

        if (!res.awaitCompletion(5, TimeUnit.SECONDS)){
            Assertions.fail();
        }

        List<BlogUserResponse> results = res.getValues();

        Assertions.assertEquals(1, results.size());

        BlogUserResponse result = results.get(0);

        Assertions.assertEquals(HttpStatus.NOT_FOUND.value(), result.getStatusCode());
        Assertions.assertEquals(1, result.getErrorsCount());
        Assertions.assertEquals(BlogUser.newBuilder().build(), result.getData());
    }

    @Test
    public void testFindOneInvalidUserID() throws Exception {
        Mockito.doReturn(user).when(this.repository).findById(1l);
        Mockito.doReturn(null).when(this.userService).findOne(1l);

        BlogUserService service = new BlogUserService(this.repository, this.userService);

        FindOneUserRequest req = FindOneUserRequest.newBuilder()
                .setId(1)
                .build();

        StreamRecorder<BlogUserResponse> res = StreamRecorder.create();

        service.findOne(req, res);

        if (!res.awaitCompletion(5, TimeUnit.SECONDS)){
            Assertions.fail();
        }

        List<BlogUserResponse> results = res.getValues();

        Assertions.assertEquals(1, results.size());

        BlogUserResponse result = results.get(0);

        Assertions.assertEquals(HttpStatus.NOT_FOUND.value(), result.getStatusCode());
        Assertions.assertEquals(1, result.getErrorsCount());
        Assertions.assertEquals(BlogUser.newBuilder().build(), result.getData());
    }

    @Test
    public void testUpdateSuccess() throws Exception {
        com.samithiwat.user.grpc.dto.BlogUser want = com.samithiwat.user.grpc.dto.BlogUser.newBuilder()
                .setId(Math.toIntExact(this.user.get().getUserId()))
                .setDescription(this.user.get().getDescription())
                .build();

        Mockito.doReturn(user).when(this.repository).findById(1l);
        Mockito.doReturn(user.get()).when(this.repository).save(user.get());

        BlogUserService service = new BlogUserService(this.repository, this.userService);

        UpdateUserRequest req = UpdateUserRequest.newBuilder()
                .setId(1)
                .setDescription(user.get().getDescription())
                .build();

        StreamRecorder<BlogUserResponse> res = StreamRecorder.create();

        service.update(req, res);

        if (!res.awaitCompletion(5, TimeUnit.SECONDS)){
            Assertions.fail();
        }

        List<BlogUserResponse> results = res.getValues();

        Assertions.assertEquals(1, results.size());

        BlogUserResponse result = results.get(0);

        Assertions.assertEquals(HttpStatus.OK.value(), result.getStatusCode());
        Assertions.assertEquals(0, result.getErrorsCount());
        Assertions.assertEquals(want, result.getData());
    }

    @Test
    public void testUpdateNotFound() throws Exception {
        Mockito.doReturn(Optional.ofNullable(null)).when(this.repository).findById(1l);
        Mockito.doReturn(user.get()).when(this.repository).save(user.get());

        BlogUserService service = new BlogUserService(this.repository, this.userService);

        UpdateUserRequest req = UpdateUserRequest.newBuilder()
                .setId(1)
                .setDescription(user.get().getDescription())
                .build();

        StreamRecorder<BlogUserResponse> res = StreamRecorder.create();

        service.update(req, res);

        if (!res.awaitCompletion(5, TimeUnit.SECONDS)){
            Assertions.fail();
        }

        List<BlogUserResponse> results = res.getValues();

        Assertions.assertEquals(1, results.size());

        BlogUserResponse result = results.get(0);

        Assertions.assertEquals(HttpStatus.NOT_FOUND.value(), result.getStatusCode());
        Assertions.assertEquals(1, result.getErrorsCount());
        Assertions.assertEquals(BlogUser.newBuilder().build(), result.getData());
    }
}
