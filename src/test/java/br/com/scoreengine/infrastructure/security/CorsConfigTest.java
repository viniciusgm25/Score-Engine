package br.com.scoreengine.infrastructure.security;

import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CorsConfigTest {

    private static final Set<String> LISTA = Set.of("https://credcasa-esteira-credito-1.onrender.com");

    @Test
    void liberaOrigemDaLista() {
        assertTrue(CorsConfig.origemPermitida("https://credcasa-esteira-credito-1.onrender.com", LISTA, false));
    }

    @Test
    void liberaLocalhostEIpsDeRedeLocalQuandoHabilitado() {
        assertTrue(CorsConfig.origemPermitida("http://localhost:8081", Set.of(), true));
        assertTrue(CorsConfig.origemPermitida("http://127.0.0.1:8080", Set.of(), true));
        assertTrue(CorsConfig.origemPermitida("http://192.168.0.25:8080", Set.of(), true));
        assertTrue(CorsConfig.origemPermitida("http://10.114.35.11:5000", Set.of(), true));
        assertTrue(CorsConfig.origemPermitida("http://172.20.1.5:3000", Set.of(), true));
    }

    @Test
    void bloqueiaRedeLocalQuandoDesabilitado() {
        assertFalse(CorsConfig.origemPermitida("http://192.168.0.25:8080", Set.of(), false));
    }

    @Test
    void bloqueiaOrigensDesconhecidasEImitacoes() {
        assertFalse(CorsConfig.origemPermitida("https://site-qualquer.com", LISTA, true));
        assertFalse(CorsConfig.origemPermitida("http://192.168.0.25.site-malicioso.com", LISTA, true));
        assertFalse(CorsConfig.origemPermitida("http://localhost.site-malicioso.com", LISTA, true));
        assertFalse(CorsConfig.origemPermitida("http://172.40.1.5:8080", LISTA, true));
        assertFalse(CorsConfig.origemPermitida(null, LISTA, true));
    }

    @Test
    void leOrigensSeparadasPorVirgulaIgnorandoBarraFinalEEspacos() {
        Set<String> origens = CorsConfig.lerOrigens(" https://a.com/ , https://b.com ,, ");
        assertEquals(Set.of("https://a.com", "https://b.com"), origens);
        assertEquals(Set.of(), CorsConfig.lerOrigens(""));
        assertEquals(Set.of(), CorsConfig.lerOrigens(null));
    }

    @Test
    void fonteDeCorsDevolveConfiguracaoSoParaOrigemPermitida() {
        CorsConfig config = new CorsConfig();
        ReflectionTestUtils.setField(config, "origensPermitidasConfig", "https://credcasa-esteira-credito-1.onrender.com");
        ReflectionTestUtils.setField(config, "permitirRedeLocal", true);
        CorsConfigurationSource fonte = config.corsConfigurationSource();

        MockHttpServletRequest permitida = new MockHttpServletRequest("POST", "/api/v1/score/evaluate");
        permitida.addHeader("Origin", "http://192.168.0.25:8080");
        CorsConfiguration cors = fonte.getCorsConfiguration(permitida);
        assertNotNull(cors);
        assertEquals(java.util.List.of("http://192.168.0.25:8080"), cors.getAllowedOrigins());
        assertTrue(cors.getAllowedMethods().contains("POST"));

        MockHttpServletRequest negada = new MockHttpServletRequest("POST", "/api/v1/score/evaluate");
        negada.addHeader("Origin", "https://site-qualquer.com");
        assertNull(fonte.getCorsConfiguration(negada));

        // Chamada servidor -> servidor (sem cabeçalho Origin), como Score -> Decisão
        assertNull(fonte.getCorsConfiguration(new MockHttpServletRequest("POST", "/api/v1/score/evaluate")));
    }
}
