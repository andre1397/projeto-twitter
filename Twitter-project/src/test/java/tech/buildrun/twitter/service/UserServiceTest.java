package tech.buildrun.twitter.service;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThrows;
import org.junit.Before;
import org.junit.Test;
import org.junit.jupiter.api.DisplayName;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.mockito.MockitoAnnotations;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.web.server.ResponseStatusException;

import tech.buildrun.twitter.dto.CreateUserDto;
import tech.buildrun.twitter.entities.Role;
import tech.buildrun.twitter.entities.User;
import tech.buildrun.twitter.repository.RoleRepository;
import tech.buildrun.twitter.repository.UserRepository;

public class UserServiceTest {

    @Mock private UserRepository userRepository;
    @Mock private RoleRepository roleRepository;
    @Mock private BCryptPasswordEncoder passwordEncoder;

    @InjectMocks private UserService userService;

    @Before
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        userService = new UserService(passwordEncoder, roleRepository, userRepository);
    }

    @Test
    @DisplayName("Deve criar novo usuário com sucesso")
    public void testNewUser_Success() {
        CreateUserDto dto = new CreateUserDto("user", "senha123");
        Role basicRole = new Role();
        basicRole.setRoleName(Role.Values.BASIC.name());

        when(roleRepository.findByRoleName(Role.Values.BASIC.name())).thenReturn(basicRole);
        when(userRepository.findByUsername("user")).thenReturn(Optional.empty());
        when(passwordEncoder.encode("senha123")).thenReturn("encodedsenha123");

        userService.newUser(dto);

        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(userCaptor.capture());
        User savedUser = userCaptor.getValue();

        assertThat(savedUser.getUsername(), is("user"));
        assertThat(savedUser.getPassword(), is("encodedsenha123"));
        assertThat(savedUser.getRoles(), contains(basicRole));
    }

    @Test
    @DisplayName("Deve lançar exceção quando o nome de usuário for null")
    public void testNewUser_UsernameNull() {
        CreateUserDto dto = new CreateUserDto(null, "senha123");

        ResponseStatusException ex = assertThrows(ResponseStatusException.class, () ->
                userService.newUser(dto));
        assertThat(ex.getStatusCode(), is(HttpStatus.BAD_REQUEST));
    }

    @Test
    @DisplayName("Deve lançar exceção quando o nome de usuário for em branco")
    public void testNewUser_UsernameBlank() {
        CreateUserDto dto = new CreateUserDto("   ", "senha123");

        ResponseStatusException ex = assertThrows(ResponseStatusException.class, () ->
                userService.newUser(dto));
        assertThat(ex.getStatusCode(), is(HttpStatus.BAD_REQUEST));
    }

    @Test
    @DisplayName("Deve lançar exceção quando a senha for null")
    public void testNewUser_senha123wordNull() {
        CreateUserDto dto = new CreateUserDto("user", null);

        ResponseStatusException ex = assertThrows(ResponseStatusException.class, () ->
                userService.newUser(dto));
        assertThat(ex.getStatusCode(), is(HttpStatus.BAD_REQUEST));
    }

    @Test
    @DisplayName("Deve lançar exceção quando a senha for em branco")
    public void testNewUser_senha123wordBlank() {
        CreateUserDto dto = new CreateUserDto("user", "   ");

        ResponseStatusException ex = assertThrows(ResponseStatusException.class, () ->
                userService.newUser(dto));
        assertThat(ex.getStatusCode(), is(HttpStatus.BAD_REQUEST));
    }

    @Test
    @DisplayName("Deve lançar exceção quando nome de usuário já existir")
    public void testNewUser_UsernameExists() {
        CreateUserDto dto = new CreateUserDto("user", "senha123");

        when(userRepository.findByUsername("user")).thenReturn(Optional.of(new User()));

        ResponseStatusException ex = assertThrows(ResponseStatusException.class, () ->
                userService.newUser(dto));
        assertThat(ex.getStatusCode(), is(HttpStatus.CONFLICT));
    }

    @Test
    @DisplayName("Deve retornar lista de usuários")
    public void testListUsers() {
        List<User> users = Arrays.asList(new User(), new User());

        when(userRepository.findAll()).thenReturn(users);

        List<User> result = userService.listUsers();

        assertThat(result, hasSize(2));
    }

    @Test
    @DisplayName("Deve deletar usuário se for admin")
    public void testDeleteUser_AsAdmin_Success() {
        UUID userId = UUID.randomUUID();

        User userToDelete = new User();
        userToDelete.setUserId(userId);
        userToDelete.setRoles(new HashSet<>(Set.of(new Role())));

        Role adminRole = new Role();
        adminRole.setRoleName(Role.Values.ADMIN.name());

        User adminUser = new User();
        adminUser.setUserId(UUID.randomUUID());
        adminUser.setRoles(Set.of(adminRole));

        JwtAuthenticationToken token = mock(JwtAuthenticationToken.class);
        when(token.getName()).thenReturn(adminUser.getUserId().toString());

        when(userRepository.findById(userId)).thenReturn(Optional.of(userToDelete));
        when(userRepository.findById(adminUser.getUserId())).thenReturn(Optional.of(adminUser));

        userService.deleteUser(userId, token);

        verify(userRepository).save(userToDelete);
        verify(userRepository).deleteById(userId);
        assertThat(userToDelete.getRoles(), empty());
    }

    @Test
    @DisplayName("Deve deletar usuário se for ele mesmo")
    public void testDeleteUser_AsSelf_Success() {
        UUID userId = UUID.randomUUID();
        User user = new User();
        user.setUserId(userId);
        Role role = new Role();
        role.setRoleName("BASIC");
        user.setRoles(new HashSet<>(Set.of(role)));

        JwtAuthenticationToken token = mock(JwtAuthenticationToken.class);
        when(token.getName()).thenReturn(userId.toString());
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));

        userService.deleteUser(userId, token);

        verify(userRepository).save(user);
        verify(userRepository).deleteById(userId);
        assertThat(user.getRoles(), empty());
    }

    @Test
    @DisplayName("Deve lançar FORBIDDEN se usuário não for admin nem ele mesmo")
    public void testDeleteUser_NotAllowed() {
        UUID userId = UUID.randomUUID();

        User userToDelete = new User();
        userToDelete.setUserId(userId);
        Role basicRole = new Role();
        basicRole.setRoleName("BASIC"); // Definido corretamente
        userToDelete.setRoles(Set.of(basicRole));

        User otherUser = new User();
        otherUser.setUserId(UUID.randomUUID());
        Role anotherBasicRole = new Role();
        anotherBasicRole.setRoleName("BASIC"); // Também definido corretamente
        otherUser.setRoles(Set.of(anotherBasicRole));

        JwtAuthenticationToken token = mock(JwtAuthenticationToken.class);
        when(token.getName()).thenReturn(otherUser.getUserId().toString());
        when(userRepository.findById(userId)).thenReturn(Optional.of(userToDelete));
        when(userRepository.findById(otherUser.getUserId())).thenReturn(Optional.of(otherUser));

        ResponseStatusException ex = assertThrows(ResponseStatusException.class, () ->
                userService.deleteUser(userId, token));
        assertThat(ex.getStatusCode(), is(HttpStatus.FORBIDDEN));
        assertThat(ex.getReason(), containsString("not allowed"));
    }

    @Test
    @DisplayName("Deve lançar NOT_FOUND se usuário não for encontrado")
    public void testDeleteUser_UserNotFound() {
        UUID userId = UUID.randomUUID();

        JwtAuthenticationToken token = mock(JwtAuthenticationToken.class);
        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        ResponseStatusException ex = assertThrows(ResponseStatusException.class, () ->
                userService.deleteUser(userId, token));
        assertThat(ex.getStatusCode(), is(HttpStatus.NOT_FOUND));
    }

    @Test
    @DisplayName("Deve lançar UNAUTHORIZED se usuário autenticado não for encontrado")
    public void testDeleteUser_AuthUserNotFound() {
        UUID userId = UUID.randomUUID();

        User userToDelete = new User();
        userToDelete.setUserId(userId);

        UUID randomAuthId = UUID.randomUUID();

        JwtAuthenticationToken token = mock(JwtAuthenticationToken.class);
        when(token.getName()).thenReturn(randomAuthId.toString());

        when(userRepository.findById(userId)).thenReturn(Optional.of(userToDelete));
        when(userRepository.findById(randomAuthId)).thenReturn(Optional.empty());

        ResponseStatusException ex = assertThrows(ResponseStatusException.class, () ->
                userService.deleteUser(userId, token));
        assertThat(ex.getStatusCode(), is(HttpStatus.UNAUTHORIZED));
    }
}
