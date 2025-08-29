
# Projeto Twitter - Spring Security API com JWT + MySQL + Docker

Este projeto é uma API REST segura, desenvolvida com Java 21 e Spring Boot, utilizando autenticação via JWT com chaves RSA, controle de acesso por roles e persistência de dados com MySQL em container Docker.

Repositório: [https://github.com/andre1397/projeto-twitter.git]

---

## Tecnologias utilizadas

- Java 21
- Spring Boot
- Spring Security (com JWT RSA)
- Spring Data JPA
- Maven
- MySQL
- Docker
- JUnit 5 + Mockito

---

## Como executar o projeto

### 1. Clonar o repositório

```bash
git clone https://github.com/andre1397/Projeto-Twitter.git
cd Projeto-Twitter
```

---

### 2. Subir o MySQL com Docker

Certifique-se de que o Docker esteja instalado e rodando. Execute:

```bash
docker-compose up -d
```

O banco será iniciado com:
- Porta: `3306`
- Banco de dados: `mydb`
- Usuário: `admin`
- Senha: `123`

---

### 3. Configurar o `application.properties`

```properties
# JWT com chaves RSA
jwt.public.key=classpath:app.pub
jwt.private.key=classpath:app.key

# Inicialização de dados
spring.sql.init.mode=always
spring.jpa.defer-datasource-initialization=true

# Hibernate
spring.jpa.hibernate.ddl-auto=update

# Conexão com MySQL
spring.datasource.url=jdbc:mysql://localhost:3306/mydb
spring.datasource.username=admin
spring.datasource.password=123
spring.datasource.driver-class-name=com.mysql.cj.jdbc.Driver

# Mostrar queries no console
spring.jpa.show-sql=true
```

> As chaves `app.key` e `app.pub` devem estar presentes na pasta `resources` para validar os tokens JWT.

---

### 4. Executar a aplicação

#### Via terminal (Maven)

```bash
./mvnw spring-boot:run
```

#### Ou via IDE

Execute a classe com `@SpringBootApplication`.

A aplicação estará disponível em:

```
http://localhost:8080
```

---

## Usuário Administrador Padrão

Na inicialização da aplicação, um usuário `admin` será automaticamente criado se ainda não existir:

- **Usuário:** `admin`
- **Senha:** `123`
- **Permissão:** `ROLE_ADMIN`

Esse usuário pode acessar endpoints restritos à role ADMIN.

---

## Autenticação

A autenticação da API é feita via **JWT com chave pública/privada RSA**.

### Endpoints públicos

| Método | Endpoint   | Descrição                                                                 |
|--------|------------|---------------------------------------------------------------------------|
| POST   | `/login`   | Gera o token JWT ao fornecer `username` e `password`.                     |
| POST   | `/users`   | Cria um novo usuário com os dados no corpo da requisição (sem autenticação). |

> Exemplo de requisição para criar usuário:

```json
{
  "username": "usuario",
  "password": "senha123"
}
```

---

### Endpoints protegidos (JWT obrigatório)

Esses endpoints exigem um token JWT válido no header da requisição:

```
Authorization: Bearer SEU_TOKEN_AQUI
```

| Método | Endpoint         | Descrição                                                                        | Permissão Necessária         |
|--------|------------------|----------------------------------------------------------------------------------|------------------------------|
| GET    | `/users`         | Lista todos os usuários cadastrados.                                             | `ROLE_ADMIN`                 |
| DELETE | `/users/{id}`    | Deleta um usuário. Pode ser feito por um `ROLE_ADMIN` ou pelo próprio usuário.   | `ROLE_ADMIN` ou o próprio id |
| GET    | `/feed`          | Retorna uma lista paginada de tweets (feed).                                     | Autenticado                  |
| POST   | `/tweets`        | Cria um novo tweet com base no corpo da requisição.                              | Autenticado                  |
| DELETE | `/tweets/{id}`   | Deleta um tweet específico. Somente o autor pode deletar seu próprio tweet.      | Autenticado (proprietário)   |

---

## Rodar os testes

```bash
./mvnw test
```

---

## Encerrar o ambiente

```bash
docker-compose down
```

---

## Acesso manual ao banco (opcional)

Você pode conectar ao MySQL com qualquer cliente (ex: DBeaver, MySQL Workbench):

- Host: `localhost`
- Porta: `3306`
- Usuário: `admin`
- Senha: `123`
- Banco: `mydb`

---

## Observações

- As tabelas são criadas automaticamente via JPA.
- O projeto suporta múltiplas roles por usuário.
- Os tokens JWT são validados com chave pública.
- A aplicação popula o banco com um usuário admin na inicialização.
