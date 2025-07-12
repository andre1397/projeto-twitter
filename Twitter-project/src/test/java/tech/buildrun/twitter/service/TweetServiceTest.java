package tech.buildrun.twitter.service;

import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertThrows;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.springframework.http.HttpStatus;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.web.server.ResponseStatusException;

import tech.buildrun.twitter.entities.Role;
import tech.buildrun.twitter.entities.Tweet;
import tech.buildrun.twitter.entities.User;
import tech.buildrun.twitter.repository.TweetRepository;
import tech.buildrun.twitter.repository.UserRepository;

class TweetServiceTest {

    private TweetRepository tweetRepository;
    private UserRepository userRepository;
    private TweetService tweetService;

    private final UUID userId = UUID.randomUUID();
    private final UUID otherUserId = UUID.randomUUID();
    private final Long tweetId = 1L;

    private JwtAuthenticationToken mockToken(String userIdStr) {
        JwtAuthenticationToken token = mock(JwtAuthenticationToken.class);
        when(token.getName()).thenReturn(userIdStr);
        return token;
    }

    private User buildUser(UUID id, Set<Role> roles) {
        User user = new User();
        user.setUserId(id);
        user.setRoles(roles);
        return user;
    }

    private Tweet buildTweet(User user) {
        Tweet tweet = new Tweet();
        tweet.setUser(user);
        return tweet;
    }

    @BeforeEach
    void setUp() {
        tweetRepository = mock(TweetRepository.class);
        userRepository = mock(UserRepository.class);
        tweetService = new TweetService(tweetRepository, userRepository);
    }

    @Test
    @DisplayName("Deve deletar o tweet quando o usuário for o dono")
    void deleteTweet_shouldDeleteIfOwner() {
        User user = buildUser(userId, Set.of());
        Tweet tweet = buildTweet(user);

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(tweetRepository.findById(tweetId)).thenReturn(Optional.of(tweet));

        JwtAuthenticationToken token = mockToken(userId.toString());

        tweetService.deleteTweet(tweetId, token);

        verify(tweetRepository).deleteById(eq(tweetId));
    }

    @Test
    @DisplayName("Deve deletar o tweet quando o usuário for admin")
    void deleteTweet_shouldDeleteIfAdmin() {
        Role adminRole = new Role();
        adminRole.setRoleName(Role.Values.ADMIN.name());

        User adminUser = buildUser(userId, Set.of(adminRole));
        User tweetOwner = buildUser(otherUserId, Set.of());
        Tweet tweet = buildTweet(tweetOwner);

        when(userRepository.findById(userId)).thenReturn(Optional.of(adminUser));
        when(tweetRepository.findById(tweetId)).thenReturn(Optional.of(tweet));

        JwtAuthenticationToken token = mockToken(userId.toString());

        tweetService.deleteTweet(tweetId, token);

        verify(tweetRepository).deleteById(eq(tweetId));
    }

    @Test
    @DisplayName("Deve lançar FORBIDDEN quando usuário não for dono nem admin")
    void deleteTweet_shouldThrowForbiddenIfNotOwnerOrAdmin() {
        User user = buildUser(userId, Set.of());
        User tweetOwner = buildUser(otherUserId, Set.of());
        Tweet tweet = buildTweet(tweetOwner);

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(tweetRepository.findById(tweetId)).thenReturn(Optional.of(tweet));

        JwtAuthenticationToken token = mockToken(userId.toString());

        ResponseStatusException ex = assertThrows(ResponseStatusException.class, () ->
            tweetService.deleteTweet(tweetId, token)
        );

        assertThat(ex.getStatusCode(), is(HttpStatus.FORBIDDEN));
        assertThat(ex.getReason(), containsString("not allowed"));

        verify(tweetRepository, never()).deleteById(any());
    }

    @Test
    @DisplayName("Deve lançar NOT_FOUND quando o tweet não existir")
    void deleteTweet_shouldThrowNotFoundIfTweetDoesNotExist() {
        User user = buildUser(userId, Set.of());

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(tweetRepository.findById(tweetId)).thenReturn(Optional.empty());

        JwtAuthenticationToken token = mockToken(userId.toString());

        ResponseStatusException ex = assertThrows(ResponseStatusException.class, () ->
            tweetService.deleteTweet(tweetId, token)
        );

        assertThat(ex.getStatusCode(), is(HttpStatus.NOT_FOUND));
        assertThat(ex.getReason(), containsString("Tweet not found"));

        verify(tweetRepository, never()).deleteById(any());
    }
}
