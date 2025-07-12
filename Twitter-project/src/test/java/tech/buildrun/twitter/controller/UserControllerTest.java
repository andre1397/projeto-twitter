package tech.buildrun.twitter.controller;

import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
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
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;

import tech.buildrun.twitter.dto.CreateUserDto;
import tech.buildrun.twitter.entities.User;
import tech.buildrun.twitter.service.UserService;

@ExtendWith(MockitoExtension.class)
@DisplayName("UserController Unit Tests")
public class UserControllerTest {

    @Mock
    private UserService userService;

    @Mock
    private JwtAuthenticationToken jwtAuthenticationToken;

    @InjectMocks
    private UserController userController;

    @Test
    @DisplayName("Deve criar um novo usuário e retornar 200 OK")
    void testNewUser_ReturnsOk() {
        CreateUserDto dto = new CreateUserDto("usuarioTeste", "senha123");

        ResponseEntity<Void> response = userController.newUser(dto);

        verify(userService).newUser(dto);
        assertEquals(200, response.getStatusCodeValue());
    }

    @Test
    @DisplayName("Deve chamar o serviço userService.newUser com o DTO correto")
    void testNewUser_CallsServiceWithCorrectDto() {
        CreateUserDto dto = new CreateUserDto("usuarioTeste", "senha123");

        userController.newUser(dto);

        ArgumentCaptor<CreateUserDto> captor = ArgumentCaptor.forClass(CreateUserDto.class);
        verify(userService).newUser(captor.capture());
        assertSame(dto, captor.getValue());
    }

    @Test
    @DisplayName("Deve listar todos os usuários e retorná-los na resposta")
    void testListUsers_ReturnsListOfUsers() {
        User user1 = new User();
        User user2 = new User();
        List<User> users = Arrays.asList(user1, user2);
        when(userService.listUsers()).thenReturn(users);

        ResponseEntity<List<User>> response = userController.listUsers();

        verify(userService).listUsers();
        assertEquals(200, response.getStatusCodeValue());
        MatcherAssert.assertThat(response.getBody(), Matchers.contains(user1, user2));
    }

    @Test
    @DisplayName("Deve deletar o usuário e retornar 200 OK")
    void testDeleteUser_ReturnsOk() {
        UUID id = UUID.randomUUID();

        ResponseEntity<Void> response = userController.deleteUser(id, jwtAuthenticationToken);

        verify(userService).deleteUser(id, jwtAuthenticationToken);
        assertEquals(200, response.getStatusCodeValue());
    }

    @Test
    @DisplayName("Deve chamar o serviço userService.deleteUser com os argumentos corretos")
    void testDeleteUser_CallsServiceWithCorrectArguments() {
        UUID id = UUID.randomUUID();

        userController.deleteUser(id, jwtAuthenticationToken);

        verify(userService).deleteUser(id, jwtAuthenticationToken);
    }
}
