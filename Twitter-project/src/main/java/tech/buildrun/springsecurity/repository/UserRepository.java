package tech.buildrun.springsecurity.repository;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import tech.buildrun.springsecurity.entities.User;

@Repository
public interface UserRepository extends JpaRepository<User, UUID> {

    // Este repositório é usado para acessar os dados da tabela de usuários no banco de dados.
    // Ele estende JpaRepository, que fornece métodos para operações CRUD (Create, Read, Update, Delete).
    // O tipo de entidade é Role e o tipo de ID é Long.
    
    // Aqui você pode adicionar métodos personalizados se necessário, por exemplo:
    // Optional<Role> findByRoleName(String roleName);

    Optional<User> findByUsername(String username); // Método para encontrar um usuário pelo nome de usuário. Isso é útil para autenticação e verificação de existência de usuários no sistema.
}
