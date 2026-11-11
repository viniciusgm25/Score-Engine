package br.com.scoreengine.infrastructure.security;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;

import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * Configuração de CORS do Score.
 *
 * CORS é uma regra do NAVEGADOR: só entra em ação quando uma página aberta em um
 * endereço (origem) chama este serviço a partir de outro endereço.
 * Chamadas entre servidores (ex.: Score -> Decisão, Financiamento -> Score feito pelo
 * backend) NÃO passam por CORS, então esta configuração não afeta a integração com a Decisão.
 *
 * Quem é liberado:
 *  - origens listadas em app.cors.allowed-origins (variável CORS_ALLOWED_ORIGINS, separadas por vírgula);
 *  - localhost e IPs de rede local (192.168.x.x, 10.x.x.x, 172.16-31.x.x), com qualquer porta,
 *    quando app.cors.allow-local-network=true (variável CORS_ALLOW_LOCAL_NETWORK).
 * Qualquer outra origem não recebe os cabeçalhos de CORS e o navegador bloqueia a chamada.
 */
@Configuration
public class CorsConfig {

    /** localhost, 127.0.0.1 e faixas de IP privado, com porta opcional. Os números ficam limitados a dígitos. */
    private static final Pattern ORIGEM_REDE_LOCAL = Pattern.compile(
            "^https?://(localhost|127\\.0\\.0\\.1"
                    + "|192\\.168\\.\\d{1,3}\\.\\d{1,3}"
                    + "|10\\.\\d{1,3}\\.\\d{1,3}\\.\\d{1,3}"
                    + "|172\\.(1[6-9]|2\\d|3[01])\\.\\d{1,3}\\.\\d{1,3})"
                    + "(:\\d{1,5})?$");

    @Value("${app.cors.allowed-origins:}")
    private String origensPermitidasConfig;

    @Value("${app.cors.allow-local-network:true}")
    private boolean permitirRedeLocal;

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        Set<String> origensPermitidas = lerOrigens(origensPermitidasConfig);

        return (HttpServletRequest request) -> {
            String origem = request.getHeader(HttpHeaders.ORIGIN);
            if (!origemPermitida(origem, origensPermitidas, permitirRedeLocal)) {
                return null; // sem cabeçalhos de CORS: o navegador bloqueia
            }

            CorsConfiguration config = new CorsConfiguration();
            config.setAllowedOrigins(List.of(origem));
            config.setAllowedMethods(List.of("GET", "POST", "OPTIONS"));
            config.setAllowedHeaders(List.of("Content-Type", "Accept", "Authorization"));
            config.setMaxAge(3600L);
            return config;
        };
    }

    static boolean origemPermitida(String origem, Set<String> origensPermitidas, boolean permitirRedeLocal) {
        if (origem == null || origem.isBlank()) {
            return false;
        }
        if (origensPermitidas.contains(origem)) {
            return true;
        }
        return permitirRedeLocal && ORIGEM_REDE_LOCAL.matcher(origem).matches();
    }

    static Set<String> lerOrigens(String valor) {
        if (valor == null || valor.isBlank()) {
            return Set.of();
        }
        return Arrays.stream(valor.split(","))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .map(s -> s.endsWith("/") ? s.substring(0, s.length() - 1) : s)
                .collect(Collectors.toSet());
    }
}
