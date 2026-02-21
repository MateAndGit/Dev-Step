package com.mateandgit.devstep.domain.postlike.service;

import com.mateandgit.devstep.domain.post.entity.Post;
import com.mateandgit.devstep.domain.post.repository.PostRepository;
import com.mateandgit.devstep.domain.user.entity.User;
import com.mateandgit.devstep.domain.user.repository.UserRepository;
import com.mateandgit.devstep.global.security.CustomUserDetails;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ActiveProfiles("test")
class PostLikeConcurrencyTest {

    @Autowired
    private PostLikeService postLikeService;
    @Autowired
    private PostRepository postRepository;
    @Autowired
    private UserRepository userRepository;

    private Long postId;
    private final List<Long> userIds = new ArrayList<>();

    @BeforeEach
    void setUp() {
        User author = userRepository.save(User.createUser("author", "author@test.com", "pw"));
        Post post = postRepository.save(Post.createPost("Title", "Content", author));
        this.postId = post.getId();

        for (int i = 0; i < 100; i++) {
            User user = userRepository.save(User.createUser("user" + i, "user" + i + "@test.com", "pw"));
            userIds.add(user.getId());
        }
    }

    @Test
    @DisplayName("Concurrency Test: 100 users like the same post at once")
    void likePost_Concurrency() throws InterruptedException {
        int threadCount = 100;
        ExecutorService executorService = Executors.newFixedThreadPool(32);
        CountDownLatch latch = new CountDownLatch(threadCount);

        for (Long userId : userIds) {
            executorService.execute(() -> {
                try {

                    User user = userRepository.findById(userId).orElseThrow();
                    CustomUserDetails userDetails = new CustomUserDetails(user);

                    postLikeService.likePost(postId, userDetails);
                } catch (Exception e) {
                    System.out.println("Error: " + e.getMessage());
                } finally {
                    latch.countDown();
                }
            });
        }

        latch.await();

        Post post = postRepository.findById(postId).orElseThrow();
        System.out.println("Final Like Count: " + post.getLikeCount());

        assertThat(post.getLikeCount()).isEqualTo(100);
    }

    @Test
    @DisplayName("Concurrency Test: 50 likes and 50 cancels at the same time")
    void likeAndCancel_Concurrency() throws InterruptedException {
        int threadCount = 100;
        ExecutorService executorService = Executors.newFixedThreadPool(32);
        CountDownLatch latch = new CountDownLatch(threadCount);

        for (int i = 0; i < threadCount; i++) {
            final int index = i;
            executorService.execute(() -> {
                try {
                    User user = userRepository.findById(userIds.get(index)).orElseThrow();
                    CustomUserDetails userDetails = new CustomUserDetails(user);

                    if (index % 2 == 0) {
                        postLikeService.likePost(postId, userDetails);
                    } else {
                        postLikeService.likePost(postId, userDetails);
                        postLikeService.cancelLikePost(postId, userDetails);
                    }
                } catch (Exception e) {
                    System.err.println("Error during concurrency: " + e.getMessage());
                } finally {
                    latch.countDown();
                }
            });
        }

        latch.await();

        Post post = postRepository.findById(postId).orElseThrow();
        assertThat(post.getLikeCount()).isEqualTo(50);
    }
}