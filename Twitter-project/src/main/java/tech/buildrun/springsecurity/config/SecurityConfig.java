package tech.buildrun.springsecurity.config;

import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtEncoder;
import org.springframework.security.web.SecurityFilterChain;

import com.nimbusds.jose.jwk.JWK;
import com.nimbusds.jose.jwk.JWKSet;
import com.nimbusds.jose.jwk.RSAKey;
import com.nimbusds.jose.jwk.source.ImmutableJWKSet;

@Configuration
@EnableWebSecurity //SpringSecutiry é uma biblioteca do Spring que fornece funcionalidades de segurança para aplicações web, como autenticação e autorização. Ela permite proteger endpoints, gerenciar usuários e roles, e configurar regras de acesso. Faz isso colocando "verificações" no caminho de todas as requisições antes delas chegarem aos controllers
@EnableMethodSecurity //A anotação @EnableMethodSecurity é usada para habilitar a segurança baseada em métodos no Spring Security. Isso permite que você aplique regras de segurança diretamente em métodos específicos, como por exemplo, usando anotações como @PreAuthorize, @PostAuthorize, @Secured, etc. Isso é útil para proteger métodos individuais com base nas roles ou permissões do usuário.
public class SecurityConfig {

    @Value("${jwt.public.key}") // A anotação @Value é usada para injetar valores de propriedades do arquivo application.properties ou de variáveis de ambiente. Aqui, ela está injetando o valor da chave pública RSA que será usada para verificar a assinatura dos tokens JWT.
    private RSAPublicKey publicKey; // Chave pública RSA usada para verificar a assinatura dos tokens JWT. Essa chave é necessária para validar a autenticidade dos tokens recebidos nas requisições.

    @Value("${jwt.private.key}")
    private RSAPrivateKey privateKey; // Chave privada RSA usada para assinar os tokens JWT. Essa chave é necessária para gerar tokens válidos que serão enviados aos clientes após a autenticação.

    @Bean //Bean é uma anotação do Spring que indica que o método deve ser gerenciado pelo container do Spring, ou seja, o Spring irá criar uma instância desse objeto e gerenciá-lo durante o ciclo de vida da aplicação.
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        // Aqui você pode configurar os filtros de segurança, como autenticação, autorização, etc.
        // Por exemplo, você pode usar o HttpSecurity para definir regras de acesso aos endpoints.
        http
            .authorizeHttpRequests(authorize -> authorize
                .requestMatchers(HttpMethod.POST, "/users").permitAll() // Permite que todos possam acessar a parte de criação de usuarios
                .requestMatchers(HttpMethod.POST, "/login").permitAll() // Permite que requisições POST para o endpoint /login sejam feitas sem autenticação, ou seja, qualquer usuário pode acessar esse endpoint para fazer login. Pois todo mundo precisa acessar o login pra preencher as credenciais e logar no sistema
                .anyRequest().authenticated()) // Qualquer requisição deve ser autenticada, ou seja, o usuário deve estar logado para acessar qualquer endpoint.
            .csrf(csrf -> csrf.disable())// Desabilita a proteção CSRF (Cross-Site Request Forgery), que é uma medida de segurança para proteger contra ataques de falsificação de requisições entre sites. É comum desabilitar essa proteção em APIs REST, mas deve ser usada com cautela e nunca em ambiente de produção, somente para testes pois gera uma falha na segurança da aplicação
            .oauth2ResourceServer(oauth2 -> oauth2.jwt(Customizer.withDefaults()))// Configura o servidor de recursos OAuth2 para usar JWT (JSON Web Tokens) como método de autenticação. Isso significa que as requisições serão autenticadas usando tokens JWT, que são passados no cabeçalho Authorization das requisições HTTP. O Customizer.withDefaults() aplica as configurações padrão para o uso de JWT, tirando a necessidade de configurá-las manualmente
            .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS)); // Configura a política de criação de sessões para Stateless, o que significa que o servidor não manterá estado entre as requisições. Isso é comum em APIs REST, onde cada requisição é independente e não depende de uma sessão mantida pelo servidor. Isso também ajuda a melhorar a escalabilidade da aplicação, pois não há necessidade de armazenar informações de sessão no servidor.
        
        return http.build(); // O método build() cria uma instância de SecurityFilterChain, que é a cadeia de filtros de segurança que será aplicada às requisições HTTP.
    }

    /*
     * Essa parte abaixo não precisa memorizar, pois será igual em praticamente todas a aplicações feitas com Spring Security, então basta copiar como está aqui em qualquer app feito com Spring Security.
     */

    @Bean
    public JwtEncoder jwtEncoder() {
        JWK jwk = new RSAKey.Builder(this.publicKey).privateKey(privateKey).build();// O JwtEncoder é responsável por criar tokens JWT. Ele usa a chave privada para assinar os tokens, garantindo que eles sejam válidos e possam ser verificados pelo servidor. O RSAKey.Builder é usado para construir um JWK (JSON Web Key) que contém a chave pública e privada RSA, que será usada pelo JwtEncoder para assinar os tokens.
        var jwks = new ImmutableJWKSet<>(new JWKSet(jwk));// O ImmutableJWKSet é uma coleção imutável de JWKs (JSON Web Keys) que contém a chave pública e privada RSA. Essa coleção é usada pelo JwtEncoder para assinar os tokens JWT. O JWKSet é uma representação JSON das chaves, que pode ser usada para compartilhar as chaves entre diferentes serviços ou aplicações.
        return new NimbusJwtEncoder(jwks);// O NimbusJwtEncoder é uma implementação do JwtEncoder que usa a biblioteca Nimbus para criar e assinar tokens JWT. Ele usa o JWKSet contendo a chave pública e privada RSA para assinar os tokens, garantindo que eles sejam válidos e possam ser verificados pelo servidor.
    }

    @Bean
    public JwtDecoder jwtDecoder() {
        return NimbusJwtDecoder.withPublicKey(publicKey).build();// O JwtDecoder é responsável por decodificar e validar os tokens JWT recebidos nas requisições. Ele usa a chave pública para verificar a assinatura dos tokens, garantindo que eles foram emitidos pelo servidor e não foram alterados. O NimbusJwtDecoder é uma implementação do JwtDecoder que usa a biblioteca Nimbus
    }

    @Bean
    public BCryptPasswordEncoder bCryptPasswordEncoder() {
        return new BCryptPasswordEncoder(); // O BCryptPasswordEncoder é usado para codificar senhas de forma segura. Ele usa o algoritmo BCrypt, que é um algoritmo de hash seguro e resistente a ataques de força bruta. É recomendado usar o BCryptPasswordEncoder para armazenar senhas de usuários no banco de dados, pois ele adiciona uma camada extra de segurança ao processo de autenticação.
    }
}
