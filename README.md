# Score Engine — Sistema de Avaliação de Risco de Crédito

Motor de cálculo de escore de crédito em **Java 21** + **Spring Boot 3.2.2**, estruturado sob os princípios de **Clean Architecture** / **Ports & Adapters** e **Domain-Driven Design (DDD)**. Calcula score de crédito para clientes **Pessoa Física (PF)** e **Pessoa Jurídica (PJ)**, com auditoria persistida e conformidade com as diretrizes do Banco Central do Brasil (Resoluções CMN nº 4.557/2017 e 2.682/1999).

---

## Arquitetura

```
interfaces / infrastructure (REST, Kafka, JPA, Security)
                 ↓
        application (use cases, ScoreService)
                 ↓
           domain (modelos, validadores, ScoreCalculatorStrategy<T>)
```

- **domain** — regras de negócio puras: `CustomerPFProfile`/`CustomerPJProfile`, `PerfilValidator`, `ScoreCalculatorStrategy<T>` (Strategy Pattern com `PFScoreCalculatorStrategy` e `PJScoreCalculatorStrategy`), `ScoreResult`.
- **application** — orquestração: `ScoreService` (validação → cálculo → auditoria) e `EvaluateUnifiedScoreUseCase` (com cache de reavaliação e persistência de histórico).
- **infrastructure** — adaptadores de entrada (REST, `ScoreKafkaConsumer`) e saída (JPA/Postgres via `ScoreAuditPort`, `ScoreEvaluationRepository`), segurança (`SecurityConfig`, JWT/OAuth2 Resource Server), observabilidade (`CorrelationIdFilter` + MDC).

---

## Tecnologias

| Componente     | Versão / Detalhe                       |
| -------------- | -------------------------------------- |
| Java           | 21                                     |
| Spring Boot    | 3.2.2 (Web, Security, Data JPA, Kafka) |
| Banco de dados | PostgreSQL 15 (via `docker-compose`)   |
| Mensageria     | Apache Kafka (via `docker-compose`)    |
| Testes         | JUnit 5 + Mockito                      |
| Build          | Maven                                  |

---

## Pré-requisitos

- Java 21 (JDK)
- Maven
- Docker + Docker Compose (para Postgres e Kafka locais)

---

## Como executar localmente

### 1. Subir a infraestrutura (Postgres + Kafka)

```bash
docker compose up -d
```

> ⚠️ **Atenção à porta do Postgres:** o `docker-compose.yml` expõe o Postgres do container na porta **5433** do host (`"5433:5432"`), não na 5432 padrão — isso evita conflito com uma instância local de Postgres que você já possa ter rodando. Se for conectar com `psql` ou outro client manualmente, use `-p 5433`.

Para resetar o banco do zero (útil se o schema ficar desalinhado — veja nota sobre `ddl-auto` mais abaixo):

```bash
docker compose down -v
docker compose up -d
```

### 2. Compilar e rodar os testes

```bash
mvn clean compile test-compile
mvn test
```

### 3. Subir a aplicação

```bash
mvn spring-boot:run
```

A aplicação sobe em `http://localhost:8080`. Uma interface HTML simples de testes fica disponível em `http://localhost:8080/index.html`.

---

## Configuração

Principais chaves em `application.yml`:

| Chave                            | O que controla                                                                                                                              |
| -------------------------------- | ------------------------------------------------------------------------------------------------------------------------------------------- |
| `spring.datasource.url`          | `jdbc:postgresql://localhost:5433/score_engine_db` — porta 5433, ver nota acima                                                             |
| `app.security.jwt.enabled`       | `true`/`false` — liga/desliga exigência de JWT nas rotas `/api/v1/score/**`. Em `false`, todas as rotas ficam abertas (útil para dev local) |
| `spring.kafka.consumer.group-id` | `score-engine-group`                                                                                                                        |
| `spring.jpa.hibernate.ddl-auto`  | `update`                                                                                                                                    |

**Nota sobre `ddl-auto: update`:** ele cria tabelas e colunas novas automaticamente, mas **não altera** o tipo/tamanho de uma coluna já existente (ex.: ampliar um `varchar`). Se uma entidade mudar um `length` ou uma constraint de coluna existente, o schema local pode ficar desalinhado silenciosamente (o Hibernate só emite `WARN`, não erro). Nesses casos, resete o volume do Postgres (`docker compose down -v && docker compose up -d`) ou aplique o `ALTER TABLE` manualmente. Para produção, migrar para Flyway/Liquibase é recomendado.

---

## Endpoints REST

Base path: `/api/v1/score`. Autenticação JWT (Bearer token) exigida quando `app.security.jwt.enabled=true`.

| Método | Rota                               | Controller                  | Request                                                                           | Response                    | Descrição                                                                                                                                                       |
| ------ | ---------------------------------- | --------------------------- | --------------------------------------------------------------------------------- | --------------------------- | --------------------------------------------------------------------------------------------------------------------------------------------------------------- |
| `GET`  | `/api/v1/score/health`             | `ScoreController`           | —                                                                                 | `200 OK` texto simples      | Health check                                                                                                                                                    |
| `POST` | `/api/v1/score`                    | `ScoreController`           | `ClientePerfilDto` (polimórfico PF/PJ via `tipoPessoa`)                           | `UnifiedScoreResponseDTO`   | Cálculo direto, sem checagem de cache/histórico                                                                                                                 |
| `POST` | `/api/v1/score/evaluate`           | `ScorePipelineController`   | `UnifiedScoreRequestDTO` (`tipoPessoa`, `forcarRecalculo`, `perfilPF`/`perfilPJ`) | `UnifiedScoreResponseDTO`   | **Endpoint principal.** Consulta histórico (TTL de 30 dias) antes de recalcular, salvo se `forcarRecalculo=true`. É o que a UI de testes (`index.html`) consome |
| `POST` | `/api/v1/score/pf`                 | `ScorePFController`         | `ScorePFRequestDTO`                                                               | `ScoreResponseDTO`          | Cálculo PF isolado, sem checagem de cache                                                                                                                       |
| `POST` | `/api/v1/score/validation/metrics` | `ScoreValidationController` | `{ scores: number[], defaults: boolean[] }`                                       | `{ ascRoc, razaoAcuracia }` | Backtesting / poder discriminatório do modelo                                                                                                                   |

**Formato de `UnifiedScoreResponseDTO`** (retorno do endpoint principal):

```json
{
  "clienteId": "string",
  "tipoPessoa": "PF | PJ",
  "scoreFinal": 0,
  "faixaRisco": "EXCELENTE | BOM | REGULAR | CRITICO",
  "probabilidadeDefault": 0.0,
  "modelo": { "codigo": "string", "versao": "string" },
  "calculatedAt": "2026-01-01T00:00:00Z",
  "origem": "CALCULO | BANCO",
  "componentes": [
    {
      "nome": "string",
      "pontuacao": 0.0,
      "pontuacaoMaxima": 0.0,
      "pesoPonderado": 0.0,
      "motivo": "string"
    }
  ],
  "fatoresImpacto": ["string"]
}
```

> Ao consumir esse endpoint de um front-end, use exatamente esses nomes de campo (`scoreFinal`, `faixaRisco`, `calculatedAt`, `pesoPonderado`) — não há campos `score`, `classificacaoRisco`, `calculadoEm` ou `peso`/`impacto` por componente na resposta real.

**Se houver mais de um controller respondendo à mesma rota**, o Spring falha o boot com `Ambiguous mapping` — antes de adicionar um novo endpoint, confira se já não existe um equivalente nos controllers acima.

---

## Kafka

|                  |                                                                                                      |
| ---------------- | ---------------------------------------------------------------------------------------------------- |
| Tópico consumido | `score-evaluation-requests`                                                                          |
| Consumer group   | `score-engine-group`                                                                                 |
| Consumer         | `ScoreKafkaConsumer` — roteia para PF/PJ pelo campo `tipoPessoa` do payload, delega a `ScoreService` |
| Payload esperado | `ClientePerfilDto` (mesmo formato polimórfico do endpoint `POST /api/v1/score`)                      |

⚠️ Sem Dead Letter Queue configurada ainda: falhas de desserialização ou de processamento são logadas e relançadas para a política padrão de retry do container Kafka do Spring, mas não há tópico de DLQ dedicado.

---

## Estrutura de pacotes

```
br.com.scoreengine
├── domain/            # modelos, validadores, calculadoras (Strategy), regras de negócio puras
├── application/       # use cases, ScoreService, DTOs de aplicação
├── infrastructure/
│   ├── web/           # filtros, exception handlers de infra
│   ├── messaging/      # ScoreKafkaConsumer
│   ├── persistence/    # entidades JPA, adapters, repositórios Spring Data
│   ├── security/       # SecurityConfig (JWT/OAuth2)
│   └── config/          # ScoreConfig (wiring manual de beans)
└── interfaces/rest/    # controllers, DTOs de request/response, mappers
```

---

## Testes

```bash
mvn test
```

Cobertura principal:

- `PFScoreCalculatorSensitivityTest` / `PJScoreCalculatorSensitivityTest` — comportamento das estratégias de cálculo PF/PJ
- `PerfilValidatorTest` — validação de perfis PF
- `ScoreServiceTest` — orquestração (validação → cálculo → auditoria)
- `EvaluateUnifiedScoreUseCaseIT` — fluxo completo com banco (idempotência e isolamento PF/PJ)
- `ScoreKafkaConsumerTest` — roteamento PF/PJ via Kafka
- `ScorePFControllerIT` / `ScoreControllerIntegrationTest` — contrato HTTP e segurança

---

## Notas para quem for mexer no projeto

- Antes de criar uma classe nova, confira se já não existe uma com o mesmo nome/responsabilidade em outro pacote — o projeto já teve duplicatas de `GlobalExceptionHandler`, `ScoreAuditRepository` e de controllers mapeando a mesma rota, resultado de refactors anteriores que não removeram a versão antiga. Um jeito rápido de checar:
  ```bash
  find src -name "*.java" | xargs -n1 basename | sort | uniq -d
  ```
- Ao adicionar ou alterar uma coluna em uma entidade JPA, confirme no banco (`\d nome_da_tabela` no `psql`) se a alteração realmente foi aplicada — `ddl-auto: update` não garante isso para colunas existentes (ver seção de configuração acima).
