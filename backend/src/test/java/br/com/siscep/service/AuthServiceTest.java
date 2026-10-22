package br.com.siscep.service;

import br.com.siscep.domain.PerfilUsuario;
import br.com.siscep.domain.RegistroAcesso;
import br.com.siscep.domain.Usuario;
import br.com.siscep.dto.LoginResponse;
import br.com.siscep.dto.TrocaSenhaRequest;
import br.com.siscep.exception.AutenticacaoException;
import br.com.siscep.exception.RegraNegocioException;
import br.com.siscep.repository.RegistroAcessoRepository;
import br.com.siscep.repository.UsuarioRepository;
import br.com.siscep.security.JwtService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    private static final PasswordEncoder ENCODER = new BCryptPasswordEncoder(4);

    @Mock
    private UsuarioRepository usuarioRepository;
    @Mock
    private RegistroAcessoRepository registroAcessoRepository;
    @Mock
    private JwtService jwtService;

    private AuthService service;
    private Usuario usuario;

    @BeforeEach
    void setUp() {
        service = new AuthService(usuarioRepository, registroAcessoRepository, ENCODER, jwtService, Fixtures.CLOCK);
        usuario = Fixtures.usuario(1, PerfilUsuario.GESTOR);
        usuario.setSenhaHash(ENCODER.encode("Senha@123"));
    }

    @Test
    void loginValidoDevolveTokenERegistraAcesso() {
        when(usuarioRepository.findByEmailIgnoreCase("u1@dda.com.br")).thenReturn(Optional.of(usuario));
        when(jwtService.gerarToken(usuario)).thenReturn("token");
        when(jwtService.getExpiracaoSegundos()).thenReturn(28800L);

        LoginResponse r = service.login(" u1@dda.com.br ", "Senha@123", "127.0.0.1");

        assertThat(r.token()).isEqualTo("token");
        assertThat(r.usuario().perfil()).isEqualTo(PerfilUsuario.GESTOR);
        assertThat(usuario.getUltimoAcesso()).isEqualTo(LocalDateTime.now(Fixtures.CLOCK));
        ArgumentCaptor<RegistroAcesso> captor = ArgumentCaptor.forClass(RegistroAcesso.class);
        verify(registroAcessoRepository).save(captor.capture());
        assertThat(captor.getValue().isSucesso()).isTrue();
    }

    @Test
    void emailInexistenteUsaMensagemGenerica() {
        when(usuarioRepository.findByEmailIgnoreCase(any())).thenReturn(Optional.empty());
        assertThatThrownBy(() -> service.login("x@x.com", "a", null))
                .isInstanceOf(AutenticacaoException.class)
                .hasMessage(AuthService.CREDENCIAIS_INVALIDAS);
    }

    @Test
    void terceiraSenhaErradaBloqueiaAConta() {
        when(usuarioRepository.findByEmailIgnoreCase(any())).thenReturn(Optional.of(usuario));
        for (int i = 0; i < 2; i++) {
            assertThatThrownBy(() -> service.login("u1@dda.com.br", "errada", null))
                    .hasMessage(AuthService.CREDENCIAIS_INVALIDAS);
        }
        assertThatThrownBy(() -> service.login("u1@dda.com.br", "errada", null))
                .hasMessageContaining("bloqueada");
        assertThatThrownBy(() -> service.login("u1@dda.com.br", "Senha@123", null))
                .isInstanceOf(AutenticacaoException.class)
                .hasMessageContaining("bloqueada");
        verify(registroAcessoRepository, atLeastOnce()).save(any());
    }

    @Test
    void usuarioInativoNaoAutentica() {
        usuario.setAtivo(false);
        when(usuarioRepository.findByEmailIgnoreCase(any())).thenReturn(Optional.of(usuario));
        assertThatThrownBy(() -> service.login("u1@dda.com.br", "Senha@123", null))
                .hasMessageContaining("inativo");
    }

    @Test
    void trocaDeSenhaExigeSenhaAtualCorreta() {
        assertThatThrownBy(() -> service.trocarSenha(usuario, new TrocaSenhaRequest("errada", "NovaSenha@1")))
                .isInstanceOf(RegraNegocioException.class);
        assertThatThrownBy(() -> service.trocarSenha(usuario, new TrocaSenhaRequest("Senha@123", "Senha@123")))
                .isInstanceOf(RegraNegocioException.class);

        service.trocarSenha(usuario, new TrocaSenhaRequest("Senha@123", "NovaSenha@1"));
        assertThat(ENCODER.matches("NovaSenha@1", usuario.getSenhaHash())).isTrue();
    }
}
