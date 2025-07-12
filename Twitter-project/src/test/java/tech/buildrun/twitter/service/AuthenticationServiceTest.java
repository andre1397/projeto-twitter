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
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import static org.mockito.ArgumentMatchers.any;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;

import tech.buildrun.twitter.dto.LoginRequest;
import tech.buildrun.twitter.dto.LoginResponse;
import tech.buildrun.twitter.entities.Role;
import tech.buildrun.twitter.entities.User;
import tech.buildrun.twitter.repository.UserRepository;

@ExtendWith(MockitoExtension.class)
@DisplayName("AuthenticationService Unit Tests")
class AuthenticationServiceTest {

    @Mock
    private JwtEncoder jwtEncoder;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private AuthenticationService authenticationService;

    private LoginRequest loginRequest;
    private User user;
    private Role role;

    @BeforeEach
    void setUp() {
        loginRequest = new LoginRequest("usernameTest", "senha123");

        role = new Role();
        role.setRoleName("ROLE_USER");

        user = mock(User.class);
        lenient().when(user.getUserId()).thenReturn(UUID.randomUUID());
        lenient().when(user.getRoles()).thenReturn(Set.of(role));
        lenient().when(user.isLogincorrect(any(), any())).thenReturn(true);
    }

    @Test
    @DisplayName("Deve retornar LoginResponse quando as credenciais são válidas")
    void login_shouldReturnLoginResponse_whenCredentialsAreValid() {

        when(userRepository.findByUsername("usernameTest")).thenReturn(Optional.of(user));

        var jwt = org.mockito.Mockito.mock(Jwt.class);
        when(jwt.getTokenValue()).thenReturn("jwt-token");
        when(jwtEncoder.encode(any(JwtEncoderParameters.class))).thenReturn(jwt);

        LoginResponse response = authenticationService.login(loginRequest);

        assertThat(response.accessToken(), is("jwt-token"));
        assertThat(response.expiresIn(), is(3600L));
    }

    @Test
    @DisplayName("Deve lançar BadCredentialsException quando o usuário não é encontrado")
    void login_shouldThrowBadCredentialsException_whenUserNotFound() {

        when(userRepository.findByUsername("usernameTest")).thenReturn(Optional.empty());

        assertThrows(BadCredentialsException.class, () -> authenticationService.login(loginRequest));
    }

    @Test
    @DisplayName("Deve incluir roles e scope nas JWT claims")
    void login_shouldIncludeRolesAndScopeInJwtClaims() {
        when(userRepository.findByUsername("usernameTest")).thenReturn(Optional.of(user));

        var jwt = org.mockito.Mockito.mock(Jwt.class);
        when(jwt.getTokenValue()).thenReturn("jwt-token");

        ArgumentCaptor<JwtEncoderParameters> captor = ArgumentCaptor.forClass(JwtEncoderParameters.class);
        when(jwtEncoder.encode(captor.capture())).thenReturn(jwt);

        authenticationService.login(loginRequest);

        JwtEncoderParameters params = captor.getValue();
        JwtClaimsSet claims = params.getClaims();

        assertThat(claims.getClaim("roles"), is(user.getRoles()));
        assertThat((String) claims.getClaim("scope"), containsString("ROLE_USER"));
    }


}
