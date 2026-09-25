package br.com.scoreengine.infrastructure.web;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.TestPropertySource; // Import essencial
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;

@SpringBootTest
@AutoConfigureMockMvc
@TestPropertySource(properties = "app.security.jwt.enabled=true") // Força o JWT a ficar ativo nos testes
class ScoreControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void devePermitirAcessoComTokenJwtValido() throws Exception {
        String payloadValido = """
                {
                    "tipoPessoa": "PF",
                    "clienteId": "CLI-999",
                    "rendaMensalLiquida": 8500.00,
                    "quantidadeAtrasos": 0,
                    "endividamento": 0.20,
                    "tempoRelacionamentoMeses": 24,
                    "idade": 30,
                    "estadoCivil": "SOLTEIRO",
                    "tempoUltimoEmpregoMeses": 12,
                    "possuiAvalista": false
                }
                """;

        mockMvc.perform(post("/api/v1/score")
                .with(jwt())
                .contentType(MediaType.APPLICATION_JSON)
                .content(payloadValido))
                .andExpect(status().isOk());
    }

    @Test
    void deveNegarAcessoSemTokenJwt() throws Exception {
        String payloadValido = """
                {
                    "tipoPessoa": "PF",
                    "clienteId": "CLI-999",
                    "rendaMensalLiquida": 8500.00,
                    "quantidadeAtrasos": 0,
                    "endividamento": 0.20,
                    "tempoRelacionamentoMeses": 24,
                    "idade": 30,
                    "estadoCivil": "SOLTEIRO",
                    "tempoUltimoEmpregoMeses": 12,
                    "possuiAvalista": false
                }
                """;

        mockMvc.perform(post("/api/v1/score")
                .contentType(MediaType.APPLICATION_JSON)
                .content(payloadValido))
                .andExpect(status().isUnauthorized());
    }
}