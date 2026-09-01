package br.com.scoreengine;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Ponto de entrada oficial do microsserviço Score Engine.
 */
@SpringBootApplication
public class ScoreEngineApplication {
    public static void main(String[] args) {
        SpringApplication.run(ScoreEngineApplication.class, args);
    }
}