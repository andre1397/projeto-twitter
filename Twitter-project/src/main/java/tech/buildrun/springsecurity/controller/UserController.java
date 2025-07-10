package tech.buildrun.springsecurity.controller;

import java.util.List;
import java.util.UUID;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import jakarta.transaction.Transactional;
import tech.buildrun.springsecurity.dto.CreateUserDto;
import tech.buildrun.springsecurity.entities.User;
import tech.buildrun.springsecurity.repository.UserRepository;
import tech.buildrun.springsecurity.service.UserService;

@RestController
public class UserController {

    private final UserService userService;

    public UserController(UserService userService, UserRepository userRepository) {
        this.userService = userService;
    }

    /**
     * Cria um novo usuário com base nos dados fornecidos no DTO.
     * @param userDto
     * @return
     */
    @PostMapping("/users")
    @Transactional
    public ResponseEntity<Void> newUser(@RequestBody CreateUserDto userDto) {
        userService.newUser(userDto);
        return ResponseEntity.ok().build();
    }

    /**
     * Lista todos os usuários cadastrados no sistema.
     * @return
     */
    @GetMapping("/users")
    @PreAuthorize("hasAuthority('SCOPE_admin')") 
    public ResponseEntity<List<User>> listUsers() {
        var users = userService.listUsers();
        return ResponseEntity.ok(users);
    }

    /**
     * Deleta um usuário com base no ID fornecido.
     * Apenas usuários com a role ADMIN ou o próprio usuário autenticado podem deletar sua conta
     * @param id
     * @param token
     * @return
     */
    @DeleteMapping("/users/{id}")
    @PreAuthorize("hasAuthority('SCOPE_admin') or #id.toString() == authentication.token.subject")
    public ResponseEntity<Void> deleteUser(@PathVariable UUID id, JwtAuthenticationToken token) {
        userService.deleteUser(id, token);
        return ResponseEntity.ok().build();
    }
        
}
