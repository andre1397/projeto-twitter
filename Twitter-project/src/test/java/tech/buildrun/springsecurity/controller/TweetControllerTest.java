package tech.buildrun.springsecurity.controller;

import java.util.Collections;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;

import tech.buildrun.springsecurity.dto.CreateTweetDto;
import tech.buildrun.springsecurity.dto.FeedDto;
import tech.buildrun.springsecurity.service.TweetService;

@ExtendWith(MockitoExtension.class)
@DisplayName("TweetController Unit Tests")
public class TweetControllerTest {

    @InjectMocks
    private TweetController tweetController;

    @Mock
    private TweetService tweetService;

    @Mock
    private JwtAuthenticationToken jwtAuthenticationToken;

    @Test
    @DisplayName("Deve retornar o feed com status OK")
    public void testFeed_ReturnsFeedDto() {
        int page = 0;
        int pageSize = 10;
        FeedDto mockFeed = new FeedDto(
                Collections.emptyList(),
                page,
                pageSize,
                1,
                0L);
        when(tweetService.getFeed(page, pageSize)).thenReturn(mockFeed);

        ResponseEntity<FeedDto> result = tweetController.feed(page, pageSize);

        assertEquals(HttpStatus.OK, result.getStatusCode());
        assertEquals(mockFeed, result.getBody());
        verify(tweetService, times(1)).getFeed(page, pageSize);
    }

    @Test
    @DisplayName("Deve retornar o feed com página e tamanho personalizados")
    public void testFeed_WithCustomPageAndSize() {
        int page = 2;
        int pageSize = 5;
        FeedDto mockFeed = new FeedDto(
                Collections.emptyList(),
                page,
                pageSize,
                3,
                15L);
        when(tweetService.getFeed(page, pageSize)).thenReturn(mockFeed);

        ResponseEntity<FeedDto> result = tweetController.feed(page, pageSize);

        assertEquals(HttpStatus.OK, result.getStatusCode());
        assertEquals(mockFeed, result.getBody());
        verify(tweetService, times(1)).getFeed(page, pageSize);
    }

    @Test
    @DisplayName("Deve criar um tweet e retornar status OK")
    public void testCreateTweetDto_CallsServiceAndReturnsOk() {
        CreateTweetDto createTweetDto = new CreateTweetDto("test content");
        doNothing().when(tweetService).createTweetDto(createTweetDto, jwtAuthenticationToken);

        ResponseEntity<Void> response = tweetController.createTweetDto(createTweetDto, jwtAuthenticationToken);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNull(response.getBody());
        verify(tweetService, times(1)).createTweetDto(createTweetDto, jwtAuthenticationToken);
    }

    @Test
    @DisplayName("Deve deletar um tweet e retornar status OK")
    public void testDeleteTweet_CallsServiceAndReturnsOk() {
        Long tweetId = 123L;
        doNothing().when(tweetService).deleteTweet(tweetId, jwtAuthenticationToken);

        ResponseEntity<Void> response = tweetController.deleteTweet(tweetId, jwtAuthenticationToken);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNull(response.getBody());
        verify(tweetService, times(1)).deleteTweet(tweetId, jwtAuthenticationToken);
    }
}
