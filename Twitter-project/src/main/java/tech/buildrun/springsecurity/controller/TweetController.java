package tech.buildrun.springsecurity.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import tech.buildrun.springsecurity.dto.CreateTweetDto;
import tech.buildrun.springsecurity.dto.FeedDto;
import tech.buildrun.springsecurity.service.TweetService;

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
    public ResponseEntity<FeedDto> feed(@RequestParam(value = "page", defaultValue = "0") int page, //Indica a página atual, começando do zero.
                                        @RequestParam(value = "pageSize", defaultValue = "10") int pageSize) { //Indica que o tamanho da página é 10 tweets por padrão, mas pode ser alterado pelo usuário. As paginas vem de acordo com o numero inserido na url da requisicao. Ex.: pageSize=1&page=0 traz a primeira pagina com 1 tweet(o mais recente pois eles estao vindo na ordem descrescente), pageSize=10&page=1 traz a segunda pagina com 10 tweets, e assim por diante.
        
        var feedDto = tweetService.getFeed(page, pageSize);
        return ResponseEntity.ok(feedDto);//Devolve um json com todos os tweets do usuario de acordo com o numero de tweets por pagina e o numero de paginas inseridos na URL da requisicao
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
    public ResponseEntity<Void> deleteTweet(@PathVariable("id") Long tweetId, JwtAuthenticationToken token) { // ResponseEntity<Void> é usado para retornar uma resposta sem corpo, apenas com o status HTTP.
        tweetService.deleteTweet(tweetId, token);
        return ResponseEntity.ok().build();
    }
}
