package tech.buildrun.springsecurity.dto;

// Record quer dizer que LoginResponse é uma classe imutável, ou seja, uma vez criada, seus valores não podem ser alterados. Isso é útil para garantir que os dados de resposta de login não sejam modificados acidentalmente após serem criados.
public record LoginResponse(String accessToken, Long expiresIn){//accessToken seria o token de acesso gerado após o login bem-sucedido, e ExpiresIn seria o tempo de expiração do token em segundos. Esses valores são retornados como parte da resposta de login para que o cliente possa usá-los para autenticação em requisições futuras.
}