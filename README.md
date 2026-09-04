# Score Engine - Sistema de Avaliação de Risco de Crédito

Motor de cálculo de escore de crédito desenvolvido em **Java 21** e **Spring Boot 3.2.2**, estruturado sob os princípios de **Clean Architecture** e **Domain-Driven Design (DDD)**. O sistema atende aos rigorosos padrões de governança, segurança e gerenciamento de risco exigidos pelo Banco Central do Brasil (Resoluções CMN nº 4.557/2017 e 2.682/1999)[cite: 8, 9].

## 🏗️ Arquitetura do Projeto

O projeto é dividido em camadas estritamente isoladas:

- **Domain:** Núcleo de negócio puro, contendo as regras matemáticas e estratégias de cálculo para Pessoa Física (PF) e Pessoa Jurídica (PJ) (Strategy Pattern).
- **Application:** Orquestração de fluxos de negócio (`ScoreService`) e serviços estatísticos de _backtesting_ (ROC/CAP).
- **Infrastructure:** Adaptadores de entrada (REST Controllers, Consumidor Kafka) e de saída (Repositórios JPA de Auditoria).
- **Security:** Configuração de segurança baseada em Spring Security OAuth2 Resource Server (JWT) com sessões _Stateless_.
- **Observability:** Propagação automática de rastreabilidade transversal utilizando `CorrelationIdFilter` e MDC (SLF4J).

## 🚀 Tecnologias Utilizadas

- Java 21
- Spring Boot 3.2.2 (Web, Security, Data JPA, Kafka)
- PostgreSQL
- JUnit 5 & Mockito
- Maven

## 📋 Como Executar

1. **Pré-requisitos:**
   - Java 21 instalado
   - Maven configurado
   - Instância do PostgreSQL rodando localmente (porta 5432)
   - Servidor Apache Kafka ativo (porta 9092, opcional para mensageria assíncrona)

2. **Compilação e Testes:**
   Na raiz do módulo `score-engine`, execute:
   ```bash
   mvn clean install
   ```
