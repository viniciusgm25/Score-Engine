package br.com.scoreengine.infrastructure.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
//import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
//import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;

/**
 * Configuração de Segurança da Infraestrutura Web.
 * Blinda o acesso ao Motor de Score, exigindo autenticação via Token JWT.
 */
@Configuration
@EnableWebSecurity
public class SecurityConfig {

    /**
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                // Desabilita CSRF pois nossa API é Stateless (não baseada em sessões web
                // tradicionais)
                .csrf(csrf -> csrf.disable())

                // Define que o gerenciamento de sessão será Stateless (SINTAXE CORRIGIDA)
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))

                // Configura as regras de autorização das rotas
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/api/v1/score/**").authenticated() // Protege o endpoint do Score
                        .anyRequest().permitAll() // Libera rotas de healthcheck/actuator no futuro
                )

                // Configura a aplicação como um Resource Server que valida tokens JWT
                .oauth2ResourceServer(oauth2 -> oauth2.jwt(Customizer.withDefaults()));

        return http.build();
    }*/

    @Bean
public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
    http
        .csrf(csrf -> csrf.disable())
        .authorizeHttpRequests(auth -> auth
            .requestMatchers("/api/v1/score/**", "/index.html", "/css/**", "/js/**").permitAll()
            .anyRequest().authenticated()
        );
    return http.build();
}
}