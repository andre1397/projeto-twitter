package tech.buildrun.springsecurity.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.server.ResponseStatusException;

import tech.buildrun.springsecurity.dto.ErrorResponse;

@RestControllerAdvice//Informa que essa classe deve interceptar as exceptions lançadas pelos controllers da aplicação, permitindo que você trate essas exceções de forma centralizada. Pra funcionar, essas exceptions não devem ser tratadas em try/catch das suas classes e em nenhum outro lugar, pois o @RestControllerAdvice vai interceptar essas exceptions e tratá-las aqui, retornando uma resposta adequada para o cliente. Se você tratar as exceptions em outros lugares, o @RestControllerAdvice não será acionado.
public class GlobalExceptionHandler {

    @ExceptionHandler(BadCredentialsException.class)//Informa qual tipo de exception esse método vai tratar. Nesse caso, ele vai tratar as exceptions do tipo BadCredentialsException, que são lançadas quando as credenciais do usuário (como nome de usuário ou senha) são inválidas. Qualquer exception desse tipo que estourar no sistema será capturada por esse método, e a resposta adequada será retornada para o cliente.
    public ResponseEntity<ErrorResponse> handleBadCredentialsException(BadCredentialsException ex) {
        ErrorResponse errorResponse = new ErrorResponse("401 UNAUTHORIZED", ex.getMessage());//ex.getMessage é pra pegar a mensagem personalizada que está dentro da exception criada na classe, nesse caso aqui é no UserService, a mensagem está sendo setada lá.
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(errorResponse);
    }

    @ExceptionHandler(ResponseStatusException.class)
    public ResponseEntity<ErrorResponse> handleResponseStatusException(ResponseStatusException ex) {
        String reason = ex.getReason() != null ? ex.getReason() : "";
        ErrorResponse errorResponse = new ErrorResponse(ex.getStatusCode().toString(), reason);
        return ResponseEntity.status(ex.getStatusCode()).body(errorResponse);
    }

}
