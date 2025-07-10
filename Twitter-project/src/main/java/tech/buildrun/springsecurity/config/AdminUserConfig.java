package tech.buildrun.springsecurity.config;

import java.util.Set;

import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import jakarta.transaction.Transactional;
import tech.buildrun.springsecurity.entities.Role;
import tech.buildrun.springsecurity.entities.User;
import tech.buildrun.springsecurity.repository.RoleRepository;
import tech.buildrun.springsecurity.repository.UserRepository;

@Configuration
public class AdminUserConfig implements CommandLineRunner{ // CommandLineRunner é uma interface do Spring que permite executar o código após a inicialização da aplicação. É útil para executar tarefas de inicialização, como criar usuários, carregar dados iniciais, etc. O método run() será chamado automaticamente pelo Spring após a aplicação ser iniciada, permitindo que você execute o código necessário para configurar o usuário administrador.

    private RoleRepository roleRepository;

    private UserRepository userRepository;

    private BCryptPasswordEncoder passwordEncoder;

    public AdminUserConfig(BCryptPasswordEncoder passwordEncoder, RoleRepository roleRepository, UserRepository userRepository) {
        this.passwordEncoder = passwordEncoder;
        this.roleRepository = roleRepository;
        this.userRepository = userRepository;
    }

    @Override
    @Transactional // A anotação @Transactional é usada para indicar que o método deve ser executado dentro de uma transação. Isso significa que todas as operações de banco de dados realizadas dentro deste método serão tratadas como uma única transação, garantindo consistência e integridade dos dados. Se ocorrer algum erro durante a execução do método, todas as alterações feitas no banco de dados serão revertidas.
    public void run(String... args) throws Exception {
        // Aqui você pode implementar a lógica para criar o usuário administrador, como verificar se ele já existe no banco de dados e, se não existir, criá-lo com as credenciais desejadas.
        // Por exemplo, você pode usar um UserRepository para verificar e criar o usuário administrador.
        // Exemplo:
        // if (!userRepository.existsByUsername("admin")) {
        //     User adminUser = new User("admin", passwordEncoder.encode("admin123"), "ROLE_ADMIN");
        //     userRepository.save(adminUser);
        // }

        var roleAdmin = roleRepository.findByRoleName(Role.Values.ADMIN.name());// Busca a role de administrador no banco de dados. Se não existir, você pode criar uma nova role de administrador.

        var userAdmin = userRepository.findByUsername("admin");// Busca o usuário administrador no banco de dados.

        userAdmin.ifPresentOrElse(
            user -> System.out.println("Admin user already exists!"), //Caso o usuario ja exista, print que o usuário já existe no BD e não cria nada
            () -> { //Caso não exista ele cria o usuario com os dados abaixo
                var user = new User();
                user.setUsername("admin");
                user.setPassword(passwordEncoder.encode("123")); // A senha é criptografada usando BCryptPasswordEncoder antes de ser salva no banco de dados.
                user.setRoles(Set.of(roleAdmin));// Define as roles do usuário, neste caso, apenas a role de administrador.
                userRepository.save(user);
                }
            );
                
                
    }
}
    
