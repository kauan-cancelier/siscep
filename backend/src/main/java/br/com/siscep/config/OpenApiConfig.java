package br.com.siscep.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/** Documentacao automatica da API (RNF10). */
@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI siscepOpenApi() {
        String esquema = "bearer-jwt";
        return new OpenAPI()
                .info(new Info()
                        .title("SISCEP API")
                        .version("1.0.0")
                        .description("API REST do Sistema de Controle de Estoque de Pastilhas Industriais - DDA Usinagem")
                        .contact(new Contact().name("Kauan Mello Cancelier")))
                .addSecurityItem(new SecurityRequirement().addList(esquema))
                .components(new Components().addSecuritySchemes(esquema, new SecurityScheme()
                        .type(SecurityScheme.Type.HTTP)
                        .scheme("bearer")
                        .bearerFormat("JWT")));
    }
}
