package tech.buildrun.springsecurity.entities;

import java.util.Set;
import java.util.UUID;

import org.springframework.security.crypto.password.PasswordEncoder;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.Table;
import tech.buildrun.springsecurity.dto.LoginRequest;

@Entity
@Table(name = "tb_users")
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "user_id")// O nome da coluna no banco de dados será "user_id". caso não especifique, o nome será o mesmo que está no código java
    private UUID userId;

    @Column(unique = true, nullable = false)// unique = true garante que o username não será duplicado no banco de dados.
    private String username;

    private String password;

    @ManyToMany(fetch=FetchType.EAGER)//ManyToMany indica que um usuário pode ter várias roles e uma role pode pertencer a vários usuários. E o cascade = CascadeType.ALL indica que todas as operações (como persistir, remover, etc.) serão aplicadas às roles associadas ao usuário. EAGER significa que as roles serão carregadas imediatamente quando o usuário for carregado do banco de dados, ou seja, não será necessário fazer uma nova consulta ao banco de dados para obter as roles do usuário. Isso é útil para evitar consultas adicionais quando você sabe que precisará das roles do usuário imediatamente.
    @JoinTable(//Indica que as tabelas de usuario e de roles terão uma tabela de junção para armazenar a relação entre elas. por exemplo o id do usuário e os ids de cada role pertencente a ele.
        name = "tb_user_roles", // Nome da tabela de junção
        joinColumns = @jakarta.persistence.JoinColumn(name = "user_id"), // Coluna que a tabela de junção pegara para referenciar o usuário em questão
        inverseJoinColumns = @jakarta.persistence.JoinColumn(name = "role_id") // Coluna que a tabela de junção pegara para referenciar a role em questão.
    )
    private Set<Role> roles;//Set é um tipo de array que possui algumas diferenças que o array normal, como por exemplo, não aceitar valores duplicados.

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public Set<Role> getRoles() {
        return roles;
    }

    public void setRoles(Set<Role> roles) {
        this.roles = roles;
    }

    public User() {
    }

    public UUID getUserId() {
        return userId;
    }

    public void setUserId(UUID userId) {
        this.userId = userId;
    }

    public boolean isLogincorrect(LoginRequest loginRequest, PasswordEncoder passwordEncoder) {
        return passwordEncoder.matches(loginRequest.password(), this.password);// O método matches compara a senha fornecida no login com a senha armazenada no banco de dados, usando o PasswordEncoder para verificar se elas correspondem. Se as senhas corresponderem, o usuário é considerado autenticado com sucesso.
    }
}