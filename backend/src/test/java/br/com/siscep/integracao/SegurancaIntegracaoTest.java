package br.com.siscep.integracao;

import com.fasterxml.jackson.databind.JsonNode;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;

import java.util.Map;
import java.util.UUID;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.not;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class SegurancaIntegracaoTest extends IntegracaoBase {

    private void loginInvalido(String email, int statusEsperado) throws Exception {
        mvc.perform(post("/api/auth/login").contentType(MediaType.APPLICATION_JSON)
                        .content(json.writeValueAsString(Map.of("email", email, "senha", "errada"))))
                .andExpect(status().is(statusEsperado));
    }

    @Test
    void contaEhBloqueadaAposTresTentativasEAdminDesbloqueia() throws Exception {
        String admin = tokenAdmin();
        String email = "bloq" + UUID.randomUUID().toString().substring(0, 6) + "@dda.com.br";
        JsonNode u = postJson("/api/usuarios", admin,
                Map.of("nome", "Bloq", "email", email, "senha", "Senha@1234", "perfil", "OPERADOR"), 201);
        for (int i = 0; i < 3; i++) {
            loginInvalido(email, 401);
        }
        mvc.perform(post("/api/auth/login").contentType(MediaType.APPLICATION_JSON)
                        .content(json.writeValueAsString(Map.of("email", email, "senha", "Senha@1234"))))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.mensagem", containsString("bloqueada")));
        postJson("/api/usuarios/" + u.get("id").asLong() + "/desbloquear", admin, Map.of(), 200);
        token(email, "Senha@1234");
    }

    @Test
    void inativarUsuarioRevogaTokenImediatamente() throws Exception {
        String admin = tokenAdmin();
        String email = "inat" + UUID.randomUUID().toString().substring(0, 6) + "@dda.com.br";
        String tokenUsuario = criarUsuario(admin, email, "GESTOR");
        mvc.perform(get("/api/auth/me").header("Authorization", tokenUsuario)).andExpect(status().isOk());
        long id = json.readTree(mvc.perform(get("/api/auth/me").header("Authorization", tokenUsuario))
                .andReturn().getResponse().getContentAsString()).get("id").asLong();
        mvc.perform(patch("/api/usuarios/" + id + "/status").header("Authorization", admin)
                        .contentType(MediaType.APPLICATION_JSON).content("{\"ativo\":false}"))
                .andExpect(status().isOk());
        mvc.perform(get("/api/auth/me").header("Authorization", tokenUsuario)).andExpect(status().isUnauthorized());
    }

    @Test
    void tokenInvalidoEhRecusadoSemExporDetalhes() throws Exception {
        mvc.perform(get("/api/pastilhas").header("Authorization", "Bearer abc.def.ghi"))
                .andExpect(status().isUnauthorized())
                .andExpect(content().string(not(containsString("Exception"))));
    }

    @Test
    void validacaoRetornaMensagensEmPortugues() throws Exception {
        String admin = tokenAdmin();
        mvc.perform(post("/api/fabricantes").header("Authorization", admin).contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isBadRequest())
                .andExpect(content().string(containsString("informe o nome")));
    }

    @Test
    void swaggerEHealthSaoPublicos() throws Exception {
        mvc.perform(get("/v3/api-docs")).andExpect(status().isOk());
        mvc.perform(get("/actuator/health")).andExpect(status().isOk());
    }
}
