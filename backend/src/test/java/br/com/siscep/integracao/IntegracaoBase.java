package br.com.siscep.integracao;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.testcontainers.containers.PostgreSQLContainer;

import java.util.Map;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/** Base dos testes de integracao com PostgreSQL real via Testcontainers (container compartilhado). */
@SpringBootTest
@AutoConfigureMockMvc
abstract class IntegracaoBase {

    @ServiceConnection
    static final PostgreSQLContainer<?> POSTGRES = new PostgreSQLContainer<>("postgres:16-alpine");

    static {
        POSTGRES.start();
    }

    static final String ADMIN_EMAIL = "admin@siscep.com.br";
    static final String ADMIN_SENHA = "Admin@123";

    @Autowired
    protected MockMvc mvc;
    @Autowired
    protected ObjectMapper json;

    protected String token(String email, String senha) throws Exception {
        String body = mvc.perform(post("/api/auth/login").contentType(MediaType.APPLICATION_JSON)
                        .content(json.writeValueAsString(Map.of("email", email, "senha", senha))))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        return "Bearer " + json.readTree(body).get("token").asText();
    }

    protected String tokenAdmin() throws Exception {
        return token(ADMIN_EMAIL, ADMIN_SENHA);
    }

    protected JsonNode postJson(String url, String token, Object corpo, int statusEsperado) throws Exception {
        String body = mvc.perform(post(url).header("Authorization", token).contentType(MediaType.APPLICATION_JSON)
                        .content(json.writeValueAsString(corpo)))
                .andExpect(status().is(statusEsperado))
                .andReturn().getResponse().getContentAsString();
        return body.isEmpty() ? null : json.readTree(body);
    }

    /** Cria um usuario com o perfil informado e devolve o token dele. */
    protected String criarUsuario(String tokenAdmin, String email, String perfil) throws Exception {
        postJson("/api/usuarios", tokenAdmin,
                Map.of("nome", "Teste " + perfil, "email", email, "senha", "Senha@1234", "perfil", perfil), 201);
        return token(email, "Senha@1234");
    }
}
