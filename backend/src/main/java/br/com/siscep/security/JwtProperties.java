package br.com.siscep.security;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "siscep.jwt")
public record JwtProperties(String secret, long expiracaoMinutos) {
}
