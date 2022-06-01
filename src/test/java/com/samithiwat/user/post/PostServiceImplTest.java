package com.samithiwat.user.post;

import com.samithiwat.user.TestConfig;
import com.samithiwat.user.post.entity.Post;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mockito;
import org.mockito.Spy;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;

import java.util.Optional;

@SpringBootTest(properties = {
        "grpc.server.inProcessName=test-user", // Enable inProcess server
        "grpc.server.port=-1", // Disable external server
        "grpc.client.userService.address=in-process:test" // Configure the client to connect to the inProcess server
})
@SpringJUnitConfig(classes = {TestConfig.class})
@DirtiesContext
@ExtendWith(SpringExtension.class)
class PostServiceImplTest {
    @Spy
    private PostRepository repository;

    @InjectMocks
    private PostServiceImpl service;

    private Optional<Post> post;

    @BeforeEach
    void setup(){
        this.post = Optional.of(new Post(1L));
        this.post.get().setId(1L);
    }

    @Test
    public void testFindOneOrCreateFound(){
        Mockito.doReturn(this.post).when(this.repository).findOneByPostId(1L);
        Mockito.doReturn(this.post.get()).when(this.repository).save(Mockito.any());

        Post result = this.service.findOneOrCreate(1L);

        Assertions.assertEquals(this.post.get(), result);

        Mockito.verify(this.repository,Mockito.times(1)).findOneByPostId(1L);
        Mockito.verify(this.repository,Mockito.times(0)).save(Mockito.any());
    }

    @Test
    public void testFindOneOrCreateNotFound(){
        Mockito.doReturn(Optional.empty()).when(this.repository).findOneByPostId(1L);
        Mockito.doReturn(this.post.get()).when(this.repository).save(Mockito.any());

        Post result = this.service.findOneOrCreate(1L);

        Assertions.assertEquals(this.post.get(), result);

        Mockito.verify(this.repository,Mockito.times(1)).findOneByPostId(1L);
        Mockito.verify(this.repository,Mockito.times(1)).save(Mockito.any());
    }
}