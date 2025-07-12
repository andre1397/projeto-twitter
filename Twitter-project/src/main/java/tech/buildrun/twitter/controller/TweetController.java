package tech.buildrun.twitter.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import tech.buildrun.twitter.dto.CreateTweetDto;
import tech.buildrun.twitter.dto.FeedDto;
import tech.buildrun.twitter.service.TweetService;

@RestController
public class TweetController {

    private final TweetService tweetService;

    public TweetController(TweetService tweetService) {
        this.tweetService = tweetService;
    }

    /**
     * Retorna o feed de tweets paginados.
     * @param page
     * @param pageSize
     * @return
     */
    @GetMapping("/feed")
    public ResponseEntity<FeedDto> feed(@RequestParam(value = "page", defaultValue = "0") int page,
                                        @RequestParam(value = "pageSize", defaultValue = "10") int pageSize) { 
        
        var feedDto = tweetService.getFeed(page, pageSize);
        return ResponseEntity.ok(feedDto);
    }

    /**
     * Cria um novo tweet.
     * @param createTweetDto
     * @param token
     * @return
     */
    @PostMapping("/tweets")
    public ResponseEntity<Void> createTweetDto(@RequestBody CreateTweetDto createTweetDto, JwtAuthenticationToken token){
        tweetService.createTweetDto(createTweetDto, token);
        return ResponseEntity.ok().build();
    }

    /**
     * Deleta um tweet.
     * @param tweetId
     * @param token
     * @return
     */
    @DeleteMapping("/tweets/{id}")
    public ResponseEntity<Void> deleteTweet(@PathVariable("id") Long tweetId, JwtAuthenticationToken token) {
        tweetService.deleteTweet(tweetId, token);
        return ResponseEntity.ok().build();
    }
}
