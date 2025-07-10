package tech.buildrun.springsecurity.service;

import java.util.List;
import java.util.Set;
import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import tech.buildrun.springsecurity.dto.CreateUserDto;
import tech.buildrun.springsecurity.entities.Role;
import tech.buildrun.springsecurity.entities.User;
import tech.buildrun.springsecurity.repository.RoleRepository;
import tech.buildrun.springsecurity.repository.UserRepository;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final BCryptPasswordEncoder passwordEncoder;

    public UserService(BCryptPasswordEncoder passwordEncoder, RoleRepository roleRepository, UserRepository userRepository) {
        this.passwordEncoder = passwordEncoder;
        this.roleRepository = roleRepository;
        this.userRepository = userRepository;
    }

    public void newUser(CreateUserDto userDto) {
        var basicRole = roleRepository.findByRoleName(Role.Values.BASIC.name());// Busca o papel (role) de usuário básico no repositório de roles para criar um usuario comum

        if (userDto.username() == null || userDto.username().isBlank() || userDto.username().isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Username is required!");
        }
        if (userDto.password() == null || userDto.password().isBlank() || userDto.password().isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Password is required!");
        }

        var userFromDb = userRepository.findByUsername(userDto.username());
        if (userFromDb.isPresent()) {// O isPresent() verifica se algum usuario foi encontrado, isso é possível pois o método findByUsername retorna um Optional<User>
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Username already exists!");// Se o usuário já existir, lança uma exceção com o status HTTP 422 (Unprocessable Entity) e uma mensagem de erro indicando que o nome de usuário já existe.
        }

        var user = new User();
        //Pega os dados presentes no DTO depois da verificação e cria um User com os dados presentes no DTO
        user.setUsername(userDto.username());
        user.setPassword(passwordEncoder.encode(userDto.password()));// Codifica a senha e então a salva no User criado
        user.setRoles(Set.of(basicRole));

        userRepository.save(user);
    }

    public List<User> listUsers() {
        return userRepository.findAll();
    }

    public void deleteUser(UUID id, JwtAuthenticationToken token) {
        var userToDelete = userRepository.findById(id)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found!"));

        var authUser = userRepository.findById(UUID.fromString(token.getName()))
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Unauthorized user!"));

        var isAdmin = authUser.getRoles().stream().anyMatch(role -> role.getRoleName().equalsIgnoreCase(Role.Values.ADMIN.name()));

        if (isAdmin || authUser.getUserId().equals(id)) {// Verifica se o usuario e um administrador ou se e o proprio usuario que esta tentando deletar sua conta
            userToDelete.getRoles().clear();
            userRepository.save(userToDelete);
            userRepository.deleteById(id);
        } else {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "You are not allowed to delete this user.");
        }
    }
}
