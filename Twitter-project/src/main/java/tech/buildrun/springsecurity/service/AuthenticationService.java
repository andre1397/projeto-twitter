package tech.buildrun.springsecurity.service;

import java.time.Instant;
import java.util.stream.Collectors;

import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.stereotype.Service;

import tech.buildrun.springsecurity.dto.LoginRequest;
import tech.buildrun.springsecurity.dto.LoginResponse;
import tech.buildrun.springsecurity.entities.Role;
import tech.buildrun.springsecurity.repository.UserRepository;

@Service
public class AuthenticationService {

    private final UserRepository userRepository;
    private final BCryptPasswordEncoder passwordEncoder;
    private final JwtEncoder jwtEncoder;

    public AuthenticationService(JwtEncoder jwtEncoder, BCryptPasswordEncoder passwordEncoder, UserRepository userRepository) {
        this.jwtEncoder = jwtEncoder;
        this.passwordEncoder = passwordEncoder;
        this.userRepository = userRepository;
    }

    public LoginResponse login(LoginRequest loginRequest) {
        var user = userRepository.findByUsername(loginRequest.username());

        if (user.isEmpty() || !user.get().isLogincorrect(loginRequest, passwordEncoder)) {//Se o usuário estiver vazio ou a senha estiver incorreta, estoura uma exception
            throw new BadCredentialsException("user or password is invalid!");
        }

        var now = Instant.now(); // Obtém o instante atual.
        var expiresIn = 3600L; // Define o tempo de expiração do token em segundos (1 hora neste caso).

        var scopes = user.get().getRoles().stream() // Obtém as roles do usuário autenticado, que são as permissões ou grupos aos quais o usuário pertence. O stream serve pra permitir que você processe as roles do usuário de forma funcional, usando operações como map, filter, etc.
                .map(Role::getRoleName) // Mapeia as roles do usuário para seus nomes, que serão usados como escopos no token JWT. Permitindo que as roles de cada usuario logado sejam inserida direto no token JWT gerado para ele
                .collect(Collectors.joining(" ")); // Junta os nomes das roles em uma única string, separada por espaços. Isso é útil para definir os escopos do token JWT, que são as permissões concedidas ao usuário.

        //claims são informações adicionais que podem ser incluídas no token JWT. Elas podem conter dados como o ID do usuário, roles, permissões, etc. Essas informações são úteis para identificar o usuário e suas permissões quando ele faz requisições autenticadas.
        var claims = JwtClaimsSet.builder()
                .issuer("mybackend") // O emissor do token, que pode ser o nome da sua aplicação ou serviço.
                .subject(user.get().getUserId().toString()) // O assunto do token, que geralmente é o nome de usuário ou ID do usuário autenticado.
                .issuedAt(now)
                .expiresAt(now.plusSeconds(expiresIn)) // Define a data de expiração do token, que é o instante atual mais o tempo de expiração definido anteriormente.
                .claim("roles", user.get().getRoles()) // As roles do usuário, que são as permissões ou grupos aos quais o usuário pertence.
                .claim("scope", scopes) // Insre as roles do usuario no token JWT gerado para ele na hora que fez o login
                .build();

        var jwtValue = jwtEncoder.encode(JwtEncoderParameters.from(claims)).getTokenValue(); // Cria o token JWT usando o JwtEncoder e os parâmetros definidos anteriormente.

        return new LoginResponse(jwtValue, expiresIn); // Retornar uma resposta adequada
    }

}
