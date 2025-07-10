package tech.buildrun.springsecurity.entities;

import java.time.Instant;

import org.hibernate.annotations.CreationTimestamp;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "tb_tweets")
public class Tweet {

    @Id
    @GeneratedValue(strategy = jakarta.persistence.GenerationType.SEQUENCE)//SEQUENCE incrementa o id conforme forem entrando mais itens (tweets), ou seja, o id é auto incrementável.
    @Column(name = "tweet_id")//O nome da coluna no banco de dados será "tweet_id". caso não especifique, o nome será o mesmo que está no código java
    private Long tweetId;

    @ManyToOne//ManyToOne indica que um tweet pertence a um usuário, ou seja, um usuário pode ter vários tweets, mas um tweet só pode pertencer a um usuário.
    @JoinColumn(name = "user_id")//O nome da coluna no banco de dados será "user_id"
    private User user;//Criador do tweet
    private String content;//Conteúdo do tweet

    @CreationTimestamp//Faz com que essa informação seja inserida de forma automatica no banco de dados, ou seja, não é necessário informar essa informação manualmente.
    private Instant creationTimestamp;//Registra a hora em que o tweet foi criado

    public User getUser() {
        return user;
    }

    public Long getTweetId() {
        return tweetId;
    }

    public void setTweetId(Long tweetId) {
        this.tweetId = tweetId;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public Instant getCreationTimestamp() {
        return creationTimestamp;
    }

    public void setCreationTimestamp(Instant creationTimestamp) {
        this.creationTimestamp = creationTimestamp;
    }
}
