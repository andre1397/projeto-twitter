package tech.buildrun.springsecurity.dto;

public record LoginRequest(String username, String password){//Como é um record, não é necessário criar um construtor, pois o record já gera automaticamente um construtor com os parâmetros fornecidos. Além disso, o record também gera automaticamente os métodos equals(), hashCode() e toString() com base nos campos fornecidos. Isso torna o código mais conciso e legível, evitando a necessidade de escrever esses métodos manualmente.

}
