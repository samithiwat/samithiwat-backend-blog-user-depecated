package com.samithiwat.user.bloguser;

import com.github.javafaker.Faker;
import com.samithiwat.user.TestConfig;
import com.samithiwat.user.bloguser.entity.User;
import com.samithiwat.user.grpc.bloguser.*;
import com.samithiwat.user.grpc.dto.BlogUser;
import com.samithiwat.user.post.PostService;
import com.samithiwat.user.post.entity.Post;
import com.samithiwat.user.user.UserServiceImpl;
import io.grpc.internal.testing.StreamRecorder;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mockito;
import org.mockito.Spy;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.dao.EmptyResultDataAccessException;
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
    private UserServiceImpl userService;

    @Spy
    private PostService postService;

    @Spy
    private BlogUserRepository repository;

    @InjectMocks
    private BlogUserService service;

    private Optional<User> user;
    private com.samithiwat.user.grpc.dto.User userDto;
    private BlogUser blogUserDto;
    private Post post;

    @BeforeEach
    void setup(){
        Faker faker = new Faker();

        List<Post> posts = new ArrayList<Post>();
        Post post1 = new Post(1L);
        post1.setId(1L);

        Post post2 = new Post(2L);
        post2.setId(2L);

        Post post3 = new Post(3L);
        post3.setId(3L);

        posts.add(post1);
        posts.add(post2);
        posts.add(post3);

        this.post = new Post(1L);
        this.post.setId(1L);

        this.user = Optional.of(new User());
        this.user.get().setId(1l);
        this.user.get().setDescription(faker.lorem().sentence());
        this.user.get().setUserId(1l);
        this.user.get().setBookmarks(posts);
        this.user.get().setReads(posts);

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
    }

    @Test
    public void testFindOneSuccess() throws Exception {
        Mockito.doReturn(userDto).when(this.userService).findOne(1l);
        Mockito.doReturn(user).when(this.repository).findById(1l);

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
    public void testCreateSuccess() throws Exception {
        Mockito.doReturn(this.userDto).when(this.userService).findOne(1l);
        Mockito.doReturn(user.get()).when(this.repository).save(Mockito.any());

        CreateUserRequest req = CreateUserRequest.newBuilder()
                .setUserId(Math.toIntExact(user.get().getUserId()))
                .setDescription(user.get().getDescription())
                .build();

        StreamRecorder<BlogUserResponse> res = StreamRecorder.create();

        service.create(req, res);

        if (!res.awaitCompletion(5, TimeUnit.SECONDS)){
            Assertions.fail();
        }

        List<BlogUserResponse> results = res.getValues();

        Assertions.assertEquals(1, results.size());

        BlogUserResponse result = results.get(0);

        Assertions.assertEquals(HttpStatus.CREATED.value(), result.getStatusCode());
        Assertions.assertEquals(0, result.getErrorsCount());
        Assertions.assertEquals(this.blogUserDto, result.getData());
    }

    @Test
    public void testCreateUserNotFound() throws Exception {
        Mockito.doReturn(null).when(this.userService).findOne(1l);
        Mockito.doReturn(user.get()).when(this.repository).save(user.get());

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

    @Test
    public void testUpdateSuccess() throws Exception {
        com.samithiwat.user.grpc.dto.BlogUser want = com.samithiwat.user.grpc.dto.BlogUser.newBuilder()
                .setId(Math.toIntExact(this.user.get().getUserId()))
                .setDescription(this.user.get().getDescription())
                .build();

        Mockito.doReturn(user).when(this.repository).findById(1l);
        Mockito.doReturn(user.get()).when(this.repository).save(user.get());

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

    @Test
    public void testDeleteSuccess() throws Exception {
        Mockito.doNothing().when(this.repository).deleteById(1l);

        DeleteUserRequest req = DeleteUserRequest.newBuilder()
                .setId(1)
                .build();

        StreamRecorder<BlogUserResponse> res = StreamRecorder.create();

        service.delete(req, res);

        if (!res.awaitCompletion(5, TimeUnit.SECONDS)){
            Assertions.fail();
        }

        List<BlogUserResponse> results = res.getValues();

        Assertions.assertEquals(1, results.size());

        BlogUserResponse result = results.get(0);

        Assertions.assertEquals(HttpStatus.NO_CONTENT.value(), result.getStatusCode());
        Assertions.assertEquals(0, result.getErrorsCount());
        Assertions.assertEquals(BlogUser.newBuilder().build(), result.getData());
    }

    @Test
    public void testDeleteNotFound() throws Exception {
        Mockito.doThrow(new EmptyResultDataAccessException("Not found user", 1)).when(this.repository).deleteById(1l);

        DeleteUserRequest req = DeleteUserRequest.newBuilder()
                .setId(1)
                .build();

        StreamRecorder<BlogUserResponse> res = StreamRecorder.create();

        service.delete(req, res);

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
    public void testAddBookmarkFounded() throws Exception{
        List<Integer> want = new ArrayList<Integer>();
        want.add(1);
        want.add(2);
        want.add(3);
        want.add(4);

        this.post.setId(4L);

        Mockito.doReturn(this.user).when(this.repository).findById(1L);
        Mockito.doReturn(this.user.get()).when(this.repository).save(Mockito.any());
        Mockito.doReturn(this.post).when(this.postService).findOneOrCreate(4L);

        AddBookmarkRequest req = AddBookmarkRequest.newBuilder()
                .setUserId(1)
                .setPostId(4)
                .build();

        StreamRecorder<BookmarkResponse> res = StreamRecorder.create();

        service.addBookmark(req, res);

        if (!res.awaitCompletion(5, TimeUnit.SECONDS)){
            Assertions.fail();
        }

        List<BookmarkResponse> results = res.getValues();

        Assertions.assertEquals(1, results.size());

        BookmarkResponse result = results.get(0);

        Assertions.assertEquals(HttpStatus.OK.value(), result.getStatusCode());
        Assertions.assertEquals(0, result.getErrorsCount());
        Assertions.assertEquals(want, result.getDataList());
    }

    @Test
    public void testAddBookmarkNotFound() throws Exception{
        List<Integer> want = new ArrayList<Integer>();
        want.add(1);
        want.add(2);
        want.add(3);
        want.add(4);

        this.post.setId(4L);

        Mockito.doReturn(this.user).when(this.repository).findById(1L);
        Mockito.doReturn(this.user.get()).when(this.repository).save(Mockito.any());
        Mockito.doReturn(this.post).when(this.postService).findOneOrCreate(4L);

        AddBookmarkRequest req = AddBookmarkRequest.newBuilder()
                .setUserId(1)
                .setPostId(4)
                .build();

        StreamRecorder<BookmarkResponse> res = StreamRecorder.create();

        service.addBookmark(req, res);

        if (!res.awaitCompletion(5, TimeUnit.SECONDS)){
            Assertions.fail();
        }

        List<BookmarkResponse> results = res.getValues();

        Assertions.assertEquals(1, results.size());

        BookmarkResponse result = results.get(0);

        Assertions.assertEquals(HttpStatus.OK.value(), result.getStatusCode());
        Assertions.assertEquals(0, result.getErrorsCount());
        Assertions.assertEquals(want, result.getDataList());
    }

    @Test
    public void testAddBookmarkNotFoundUser() throws Exception{
        Mockito.doReturn(Optional.empty()).when(this.repository).findById(1L);
        Mockito.doReturn(this.user.get()).when(this.repository).save(Mockito.any());
        Mockito.doReturn(this.post).when(this.postService).findOneOrCreate(1L);

        AddBookmarkRequest req = AddBookmarkRequest.newBuilder()
                .setUserId(1)
                .setPostId(1)
                .build();

        StreamRecorder<BookmarkResponse> res = StreamRecorder.create();

        service.addBookmark(req, res);

        if (!res.awaitCompletion(5, TimeUnit.SECONDS)){
            Assertions.fail();
        }

        List<BookmarkResponse> results = res.getValues();

        Assertions.assertEquals(1, results.size());

        BookmarkResponse result = results.get(0);

        Assertions.assertEquals(HttpStatus.NOT_FOUND.value(), result.getStatusCode());
        Assertions.assertEquals(1, result.getErrorsCount());
        Assertions.assertEquals(new ArrayList<Integer>(), result.getDataList());
    }

    @Test
    public void testFindAllBookmarkSuccess() throws Exception{
        List<Integer> want = new ArrayList<Integer>();
        want.add(1);
        want.add(2);
        want.add(3);

        Mockito.doReturn(this.user).when(this.repository).findById(1L);

        FindAllBookmarkRequest req = FindAllBookmarkRequest.newBuilder()
                .setUserId(1)
                .build();

        StreamRecorder<BookmarkResponse> res = StreamRecorder.create();

        service.findAllBookmark(req, res);

        if (!res.awaitCompletion(5, TimeUnit.SECONDS)){
            Assertions.fail();
        }

        List<BookmarkResponse> results = res.getValues();

        Assertions.assertEquals(1, results.size());

        BookmarkResponse result = results.get(0);

        Assertions.assertEquals(HttpStatus.OK.value(), result.getStatusCode());
        Assertions.assertEquals(0, result.getErrorsCount());
        Assertions.assertEquals(want, result.getDataList());
    }

    @Test
    public void testFindAllBookmarkUserNotFound() throws Exception{
        Mockito.doReturn(Optional.empty()).when(this.repository).findById(1L);

        FindAllBookmarkRequest req = FindAllBookmarkRequest.newBuilder()
                .setUserId(1)
                .build();

        StreamRecorder<BookmarkResponse> res = StreamRecorder.create();

        service.findAllBookmark(req, res);

        if (!res.awaitCompletion(5, TimeUnit.SECONDS)){
            Assertions.fail();
        }

        List<BookmarkResponse> results = res.getValues();

        Assertions.assertEquals(1, results.size());

        BookmarkResponse result = results.get(0);

        Assertions.assertEquals(HttpStatus.NOT_FOUND.value(), result.getStatusCode());
        Assertions.assertEquals(1, result.getErrorsCount());
        Assertions.assertEquals(new ArrayList<Integer>(), result.getDataList());
    }

    @Test
    public void testDeleteBookmarkSuccess() throws Exception{
        List<Integer> want = new ArrayList<Integer>();
        want.add(1);
        want.add(2);

        Mockito.doReturn(this.user).when(this.repository).findById(1L);

        DeleteBookmarkRequest req = DeleteBookmarkRequest.newBuilder()
                .setUserId(1)
                .setPostId(3)
                .build();

        StreamRecorder<BookmarkResponse> res = StreamRecorder.create();

        service.deleteBookmark(req, res);

        if (!res.awaitCompletion(5, TimeUnit.SECONDS)){
            Assertions.fail();
        }

        List<BookmarkResponse> results = res.getValues();

        Assertions.assertEquals(1, results.size());

        BookmarkResponse result = results.get(0);

        Assertions.assertEquals(HttpStatus.OK.value(), result.getStatusCode());
        Assertions.assertEquals(0, result.getErrorsCount());
        Assertions.assertEquals(want, result.getDataList());
    }

    @Test
    public void testDeleteBookmarkUserNotFound() throws Exception{
        Mockito.doReturn(Optional.empty()).when(this.repository).findById(1L);

        DeleteBookmarkRequest req = DeleteBookmarkRequest.newBuilder()
                .setUserId(1)
                .setPostId(3)
                .build();

        StreamRecorder<BookmarkResponse> res = StreamRecorder.create();

        service.deleteBookmark(req, res);

        if (!res.awaitCompletion(5, TimeUnit.SECONDS)){
            Assertions.fail();
        }

        List<BookmarkResponse> results = res.getValues();

        Assertions.assertEquals(1, results.size());

        BookmarkResponse result = results.get(0);

        Assertions.assertEquals(HttpStatus.NOT_FOUND.value(), result.getStatusCode());
        Assertions.assertEquals(1, result.getErrorsCount());
        Assertions.assertEquals(new ArrayList<Integer>(), result.getDataList());
    }

    @Test
    public void testReadPostSuccess() throws Exception{
        Mockito.doReturn(this.user).when(this.repository).findById(1L);
        Mockito.doReturn(this.user.get()).when(this.repository).save(Mockito.any());
        Mockito.doReturn(this.post).when(this.postService).findOneOrCreate(3L);

        ReadRequest req = ReadRequest.newBuilder()
                .setUserId(1)
                .setPostId(3)
                .build();

        StreamRecorder<ReadResponse> res = StreamRecorder.create();

        service.read(req, res);

        if (!res.awaitCompletion(5, TimeUnit.SECONDS)){
            Assertions.fail();
        }

        List<ReadResponse> results = res.getValues();

        Assertions.assertEquals(1, results.size());

        ReadResponse result = results.get(0);

        Assertions.assertEquals(HttpStatus.OK.value(), result.getStatusCode());
        Assertions.assertEquals(0, result.getErrorsCount());
        Assertions.assertTrue(result.getData());
    }

    @Test
    public void testReadPostNotFoundUser() throws Exception{
        Mockito.doReturn(Optional.empty()).when(this.repository).findById(1L);
        Mockito.doReturn(this.user.get()).when(this.repository).save(Mockito.any());
        Mockito.doReturn(this.post).when(this.postService).findOneOrCreate(3L);

        ReadRequest req = ReadRequest.newBuilder()
                .setUserId(1)
                .setPostId(3)
                .build();

        StreamRecorder<ReadResponse> res = StreamRecorder.create();

        service.read(req, res);

        if (!res.awaitCompletion(5, TimeUnit.SECONDS)){
            Assertions.fail();
        }

        List<ReadResponse> results = res.getValues();

        Assertions.assertEquals(1, results.size());

        ReadResponse result = results.get(0);

        Assertions.assertEquals(HttpStatus.NOT_FOUND.value(), result.getStatusCode());
        Assertions.assertEquals(1, result.getErrorsCount());
        Assertions.assertFalse(result.getData());
    }

    @Test
    public void testReadPostNotFoundPost() throws Exception{
        Mockito.doReturn(this.user).when(this.repository).findById(1L);
        Mockito.doReturn(this.user.get()).when(this.repository).save(Mockito.any());
        Mockito.doReturn(this.post).when(this.postService).findOneOrCreate(3L);

        ReadRequest req = ReadRequest.newBuilder()
                .setUserId(1)
                .setPostId(3)
                .build();

        StreamRecorder<ReadResponse> res = StreamRecorder.create();

        service.read(req, res);

        if (!res.awaitCompletion(5, TimeUnit.SECONDS)){
            Assertions.fail();
        }

        List<ReadResponse> results = res.getValues();

        Assertions.assertEquals(1, results.size());

        ReadResponse result = results.get(0);

        Assertions.assertEquals(HttpStatus.OK.value(), result.getStatusCode());
        Assertions.assertEquals(0, result.getErrorsCount());
        Assertions.assertTrue(result.getData());
    }
}
