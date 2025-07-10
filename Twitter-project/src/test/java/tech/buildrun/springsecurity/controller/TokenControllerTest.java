package tech.buildrun.springsecurity.controller;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertEquals;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;

import tech.buildrun.springsecurity.dto.LoginRequest;
import tech.buildrun.springsecurity.dto.LoginResponse;
import tech.buildrun.springsecurity.service.AuthenticationService;

@ExtendWith(MockitoExtension.class)
@DisplayName("TokenController Unit Tests")
public class TokenControllerTest {

    @Mock
    private AuthenticationService authenticationService;

    @InjectMocks
    private TokenController tokenController;

    @Test
    @DisplayName("Deve retornar 200 OK com o token de login")
    void testLogin_ReturnsOkWithLoginResponse() {
        // Arrange
        LoginRequest loginRequest = new LoginRequest("usernameTeste", "senha123");
        LoginResponse loginResponse = new LoginResponse("testeToken123", 3600L);
        when(authenticationService.login(loginRequest)).thenReturn(loginResponse);

        // Act
        ResponseEntity<LoginResponse> response = tokenController.login(loginRequest);

        // Assert
        assertEquals(200, response.getStatusCodeValue());
        assertThat(response.getBody(), is(loginResponse));

        ArgumentCaptor<LoginRequest> captor = ArgumentCaptor.forClass(LoginRequest.class);
        verify(authenticationService).login(captor.capture());
        assertThat(captor.getValue(), is(loginRequest));
    }
}
