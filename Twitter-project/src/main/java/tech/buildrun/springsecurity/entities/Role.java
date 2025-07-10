package tech.buildrun.springsecurity.entities;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "tb_roles")//Roles são os perfis de acesso no sistema, como por exemplo, ADMIN, USER, etc. Cada usuário pode ter um ou mais roles associados a ele. A tabela de roles é usada para armazenar esses perfis de acesso.
public class Role {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "role_id")
    private Long roleId;

    private String roleName;//Como é o hibernate que está criando a tabela,e não está sendo definido um name pro campo, o nome da coluna no banco de dados será role_name, pois o hibernate converte automaticamente os nomes dos campos para snake_case.

    public Role() {
    }

    public Role(String roleName) {
        this.roleName = roleName;
    }

    public String getRoleName() {
        return roleName;
    }

    public void setRoleName(String roleName) {
        this.roleName = roleName;
    }

    public Long getRoleId() {
        return roleId;
    }

    public void setRoleId(Long roleId) {
        this.roleId = roleId;
    }

    public enum Values{
        // Valores predefinidos para as roles, como ADMIN e BASIC. Esses valores podem ser usados para criar roles padrão no sistema.
        // O valor associado a cada role é um long que representa o ID da role no banco
        ADMIN(1L), BASIC(2L); // No banco ele cria a coluna com o nome das roles com letras minusculas e separadas por underline, ou seja, admin e basic

        long roleId;

        Values(long roleId){
            this.roleId = roleId;
        }

    }
}