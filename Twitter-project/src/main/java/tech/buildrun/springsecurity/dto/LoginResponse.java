package tech.buildrun.springsecurity.dto;

public record LoginResponse(String accessToken, Long expiresIn){
}
