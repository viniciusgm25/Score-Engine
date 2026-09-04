package br.com.scoreengine.infrastructure.security;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
public class SecurityConfig {

    @Value("${app.security.jwt.enabled:true}")
    private boolean jwtEnabled;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http.csrf(csrf -> csrf.disable());

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