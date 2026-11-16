package br.com.scoreengine.infrastructure.security;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.cors.CorsConfigurationSource;

@Configuration
public class SecurityConfig {

    @Value("${app.security.jwt.enabled:true}")
    private boolean jwtEnabled;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http, CorsConfigurationSource corsConfigurationSource) throws Exception {
        http.csrf(csrf -> csrf.disable());

        // CORS: responde ao navegador (inclusive ao OPTIONS "preflight") antes da checagem de autenticação.
        // Não afeta chamadas entre servidores, como Score -> Decisão.
        http.cors(cors -> cors.configurationSource(corsConfigurationSource));

        if (jwtEnabled) {
            // Produção / Testes (Exige JWT)
            http.authorizeHttpRequests(auth -> auth
                    .requestMatchers("/api/v1/score/**").authenticated()
                    .anyRequest().permitAll())
                    .oauth2ResourceServer(oauth2 -> oauth2.jwt(Customizer.withDefaults()));
        } else {
            // Ambiente Local (Desativa a exigência de JWT)
            http.authorizeHttpRequests(auth -> auth
                    .anyRequest().permitAll());
        }

        return http.build();
    }
}