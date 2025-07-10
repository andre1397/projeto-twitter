package tech.buildrun.springsecurity.service;

import java.util.UUID;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import tech.buildrun.springsecurity.dto.CreateTweetDto;
import tech.buildrun.springsecurity.dto.FeedDto;
import tech.buildrun.springsecurity.dto.FeedItemDto;
import tech.buildrun.springsecurity.entities.Role;
import tech.buildrun.springsecurity.entities.Tweet;
import tech.buildrun.springsecurity.repository.TweetRepository;
import tech.buildrun.springsecurity.repository.UserRepository;

@Service
public class TweetService {

    UserRepository userRepository;
    TweetRepository tweetRepository;

    public TweetService(TweetRepository tweetRepository, UserRepository userRepository) {
        this.tweetRepository = tweetRepository;
        this.userRepository = userRepository;
    }

    public FeedDto getFeed(int page, int pageSize) {
        var tweets = tweetRepository.findAll(
                        PageRequest.of(page, pageSize, Sort.Direction.DESC, "creationTimestamp")) 
                        .map(tweet ->
                            new FeedItemDto(
                                tweet.getTweetId(),
                                tweet.getContent(),
                                tweet.getUser().getUsername()
                            )
                        );

        var feedDto = new FeedDto(tweets.getContent(), page, pageSize, tweets.getTotalPages(), tweets.getTotalElements());
        
        return feedDto;
    }

    public void createTweetDto(CreateTweetDto createTweetDto, JwtAuthenticationToken token) {
        var user = userRepository.findById(UUID.fromString(token.getName()));

        var tweet = new Tweet();
        tweet.setUser(user.get());
        tweet.setContent(createTweetDto.content());

        tweetRepository.save(tweet);
    }

    public void deleteTweet(Long tweetId, JwtAuthenticationToken token) {
        var user = userRepository.findById(UUID.fromString(token.getName()));

        var tweet = tweetRepository.findById(tweetId)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Tweet not found!"));

        var isAdmin = user.get().getRoles().stream().anyMatch(role -> role.getRoleName().equalsIgnoreCase(Role.Values.ADMIN.name()));

        if (isAdmin || tweet.getUser().getUserId().equals(UUID.fromString(token.getName()))) {
            tweetRepository.deleteById(tweetId);
        } else {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "You are not allowed to delete this tweet.");
        }
    }

}
