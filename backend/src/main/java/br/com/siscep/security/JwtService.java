package br.com.siscep.security;

import br.com.siscep.domain.Usuario;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.time.Clock;
import java.time.Instant;
import java.util.Date;
import java.util.Optional;

/** Emissao e validacao do JWT (RNF05: expiracao maxima de 8 horas). */
@Service
public class JwtService {

    private static final long EXPIRACAO_MAXIMA_MINUTOS = 8 * 60;

    private final SecretKey chave;
    private final long expiracaoMinutos;
    private final Clock clock;

    public JwtService(JwtProperties props, Clock clock) {
        this.chave = Keys.hmacShaKeyFor(Decoders.BASE64.decode(props.secret()));
        this.expiracaoMinutos = Math.min(props.expiracaoMinutos(), EXPIRACAO_MAXIMA_MINUTOS);
        this.clock = clock;
    }

    public String gerarToken(Usuario usuario) {
        Instant agora = clock.instant();
        return Jwts.builder()
                .subject(String.valueOf(usuario.getId()))
                .claim("email", usuario.getEmail())
                .claim("perfil", usuario.getPerfil().name())
                .issuedAt(Date.from(agora))
                .expiration(Date.from(agora.plusSeconds(expiracaoMinutos * 60)))
                .signWith(chave)
                .compact();
    }

    public Optional<Long> validarEExtrairUsuarioId(String token) {
        try {
            Claims claims = Jwts.parser()
                    .verifyWith(chave)
                    .clock(() -> Date.from(clock.instant()))
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();
            return Optional.of(Long.valueOf(claims.getSubject()));
        } catch (JwtException | IllegalArgumentException e) {
            return Optional.empty();
        }
    }

    public long getExpiracaoSegundos() {
        return expiracaoMinutos * 60;
    }
}
