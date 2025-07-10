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
                        PageRequest.of(page, pageSize, Sort.Direction.DESC, "creationTimestamp")) // Aqui está sendo usado o método findAll do TweetRepository, que retorna uma lista de tweets paginada. O PageRequest.of(page, pageSize, Sort.Direction.DESC, "creationTimestamp") cria um objeto PageRequest que define a página atual, o tamanho da página e a ordenação dos tweets por data de criação em ordem decrescente.
                        .map(tweet ->
                            new FeedItemDto( // Aqui está sendo usado o método map do Stream para transformar cada Tweet em um FeedItemDto, que é um objeto que contém as informações necessárias para o feed.
                                tweet.getTweetId(),
                                tweet.getContent(),
                                tweet.getUser().getUsername()
                            )
                        );

        var feedDto = new FeedDto(tweets.getContent(), page, pageSize, tweets.getTotalPages(), tweets.getTotalElements());
        
        return feedDto;
    }

    public void createTweetDto(CreateTweetDto createTweetDto, JwtAuthenticationToken token) {
        //Pega o usuario logado pra ser o dono do tweet
        var user = userRepository.findById(UUID.fromString(token.getName())); // Aqui está sendo pego o id do token pelo getName(), pois na criação do token no meu código estou passando o id do usuário como nome(subject) do token. O getName() é um método do JwtAuthenticationToken que pega o subject do token, que nesse caso é o id do usuário. Além disso foi convertido para UUID pois o UserRepository está usando UUID como tipo de ID.

        var tweet = new Tweet();// o var funcion aqui pois está sendo criada a variavel tweet e sendo inicializada como do tipo Tweet então o var sabe que é um Tweet sem precisar especificar o seu tipo manualmente
        tweet.setUser(user.get());
        tweet.setContent(createTweetDto.content());

        tweetRepository.save(tweet);
    }

    public void deleteTweet(Long tweetId, JwtAuthenticationToken token) {
        var user = userRepository.findById(UUID.fromString(token.getName()));

        var tweet = tweetRepository.findById(tweetId)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Tweet not found!")); //Com o Optional do método do TweetRepository é possível colocar um orElse que caso não encontre o item desejado (nesse caso aqui o tweet pertencente aquele usuario) ele lança uma exception.

        var isAdmin = user.get().getRoles().stream().anyMatch(role -> role.getRoleName().equalsIgnoreCase(Role.Values.ADMIN.name()));//Verifica se o user donno do tweet tem a role admin, se tiver, ele pode deletar tweets de outros usuarios tambem.

        // Verifica se o tweet em questão pertece ao usuário logado ou se o usuario em questão é admin, se for, ele podera deletar tweets de outros usuario alem dos proprios, se não for admin e nem pertencer ao usuario ele dá um forbidden(proibido). Ele faz essa verficação de acordo com o JWT que foi passado no token, que contém o id do usuário logado.
        if (isAdmin || tweet.getUser().getUserId().equals(UUID.fromString(token.getName()))) {
            tweetRepository.deleteById(tweetId);
        } else {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "You are not allowed to delete this tweet.");
        }
    }

}
