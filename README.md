<<<<<<< HEAD
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
=======
# Esteira de Crédito — Score + Decisão

Projeto acadêmico da Esteira de Crédito, com integração entre os módulos **Score** e **Decisão**.

## Arquitetura atual

```text
Score Engine :8080
      |
      | HTTP POST
      v
Decisão :8081
      |
      v
Resultado da decisão / auditoria
```

O Score calcula o resultado. A Decisão recebe esse resultado e aplica a lógica correspondente.

---

## 1. Estrutura

```text
/
├── Score-Engine/
│   ├── pom.xml
│   └── src/
├── Decisao/
│   ├── server.js
│   ├── index.html
│   ├── styles.css
│   ├── script.js
│   └── src/
└── README.md
```

### Responsabilidades

**Score**
- receber os dados necessários;
- calcular o score;
- classificar o risco;
- enviar o resultado para a Decisão.

**Decisão**
- receber o resultado do Score;
- processar a decisão;
- disponibilizar o resultado para consulta e auditoria.

---

## 2. Pré-requisitos

Verifique se a máquina possui:

- Java;
- Node.js;
- PostgreSQL para o Score;
- VS Code recomendado para executar o projeto Java.

Verificar Java:

```powershell
java -version
javac -version
```

Verificar Node.js:

```powershell
node -v
```

O Score utiliza PostgreSQL localmente por padrão:

```text
Host: localhost
Porta: 5432
Banco: score_engine_db
Usuário: postgres
```

Os valores podem ser alterados pelas variáveis de ambiente utilizadas no `application.yml`.

> Este README não inclui configuração global do Maven. O Score deve ser executado como aplicação Java/Spring Boot pelo ambiente de desenvolvimento no VS Code.

---

## 3. Executar a Decisão

Entre na pasta:

```powershell
cd Decisao
```

Inicie o serviço:

```powershell
node server.js
```

A saída deverá indicar a porta 8081, por exemplo:

```text
[DECISAO] Serviço iniciado em http://localhost:8081
[DECISAO] Recebendo Score em POST /api/v1/decisao
```

Mantenha esse terminal aberto.

Abra no navegador:

```text
http://localhost:8081
```

---

## 4. Verificar a Decisão

Health check:

```text
http://localhost:8081/api/v1/decisao/health
```

Consulta do último resultado:

```text
http://localhost:8081/api/v1/decisao/latest
```

---

## 5. Executar o Score

Abra a pasta `Score-Engine` no VS Code.

Abra:

```text
ScoreEngineApplication.java
```

Execute a aplicação pelo suporte Java/Spring Boot do VS Code.

**Não execute o arquivo Java diretamente pelo Code Runner.**

O Score deverá ficar disponível na porta:

```text
http://localhost:8080
```

---

## 6. Configuração da comunicação

O `application.yml` do Score utiliza, por padrão:

```text
Score:
http://localhost:8080

Decisão:
http://localhost:8081
```

O endereço da Decisão pode ser alterado pela variável:

```text
DECISION_URL
```

Valor padrão:

```text
http://localhost:8081
```

---

## 7. Ordem recomendada

1. Inicie o PostgreSQL.
2. Inicie a Decisão.
3. Inicie o Score.
4. Envie uma avaliação para o Score.
5. O Score calcula o resultado.
6. O Score envia o resultado para a Decisão por POST.
7. A Decisão processa o resultado.

Fluxo:

```text
Dados do cliente
      ↓
Score
      ↓
POST /api/v1/score/evaluate
      ↓
Resultado calculado
      ↓
POST /api/v1/decisao
      ↓
Decisão
      ↓
Resultado / auditoria
```

---

## 8. Endpoint do Score

Endpoint principal:

```text
POST /api/v1/score/evaluate
```

URL local:

```text
http://localhost:8080/api/v1/score/evaluate
```

Exemplo de payload:

```json
{
  "tipoPessoa": "PF",
  "forcarRecalculo": true,
  "perfilPF": {
    "cpf": "11122233344",
    "rendaMensal": 1000,
    "dividaTotal": 25000,
    "idade": 19,
    "estadoCivil": "SOLTEIRO",
    "numeroDependentes": 5,
    "diasAtrasoUltimos12Meses": 365,
    "limiteRotativoUtilizado": 4000,
    "limiteRotativoTotal": 4000,
    "mesesNoEmpregoAtual": 1,
    "mesesRelacionamentoBanco": 1
  }
}
```

Esse exemplo representa um cenário deliberadamente muito ruim para testes.

---

## 9. Endpoint da Decisão

A Decisão recebe o resultado do Score em:

```text
POST /api/v1/decisao
```

URL local:

```text
http://localhost:8081/api/v1/decisao
```

A Decisão não deve exigir o preenchimento manual dos dados que já foram produzidos pelo Score.

---

## 10. Testar a Decisão isoladamente

Também é possível enviar diretamente um resultado para a Decisão, sem executar o Score:

```powershell
$body = @{
    clienteId = "TESTE-001"
    tipoPessoa = "PF"
    scoreFinal = 120
    faixaRisco = "MUITO_RUIM"
    probabilidadeDefault = 0.85
    modelo = @{
        codigo = "SCORE_PF"
        versao = "v1.1.0"
    }
    calculatedAt = (Get-Date).ToUniversalTime().ToString("o")
    origem = "CALCULO"
    componentes = @()
    fatoresImpacto = @(
        "Histórico de pagamento muito ruim.",
        "Alta probabilidade de inadimplência.",
        "Baixa capacidade de pagamento."
    )
} | ConvertTo-Json -Depth 5

Invoke-RestMethod `
    -Uri "http://localhost:8081/api/v1/decisao" `
    -Method POST `
    -ContentType "application/json" `
    -Body $body
```

Depois, consulte:

```text
http://localhost:8081/api/v1/decisao/latest
>>>>>>> outro-repo/main
```

---

<<<<<<< HEAD
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
=======
## 11. Execução em computadores diferentes

Os serviços também podem ficar em máquinas diferentes.

Exemplo:

```text
Máquina A
Decisão
192.168.0.10:8081

Máquina B
Score
192.168.0.20:8080
```

Nesse caso, o Score deve utilizar:

```text
http://192.168.0.10:8081
```

O IP na URL é o IP da máquina que **recebe** a requisição.

Portanto:

```text
Score → Decisão
```

usa o IP da máquina da **Decisão**.

---

## 12. Problemas comuns

### Decisão não inicia

Verifique:

```powershell
node -v
```

Depois:

```powershell
cd Decisao
node server.js
```

### A página abre, mas o CSS não aparece

Confirme a existência de:

```text
index.html
styles.css
script.js
```

O `server.js` disponibiliza esses arquivos.

### O POST aparece no terminal, mas a interface não atualiza

Consulte:

```text
http://localhost:8081/api/v1/decisao/latest
```

Se houver um resultado, a Decisão recebeu o POST.

### Score não inicia por problema no banco

Verifique se o PostgreSQL está disponível em:

```text
localhost:5432
```

e se o banco configurado está correto.

### Score não consegue enviar para a Decisão

Verifique:

```text
http://localhost:8081/api/v1/decisao/health
```

Se estiverem em computadores diferentes, confira o valor de `DECISION_URL` e use o IP da máquina da Decisão.

---

## 13. Fluxo completo

```text
                 ESTEIRA DE CRÉDITO

              ┌──────────────────┐
              │   Dados cliente  │
              └────────┬─────────┘
                       │
                       v
              ┌──────────────────┐
              │   SCORE ENGINE   │
              │     :8080        │
              └────────┬─────────┘
                       │
                       │ HTTP POST
                       v
              ┌──────────────────┐
              │     DECISÃO      │
              │     :8081        │
              └────────┬─────────┘
                       │
                       v
              ┌──────────────────┐
              │ Resultado /      │
              │ Auditoria        │
              └──────────────────┘
```

---
## 14. Exemplos de Status na Tela Decisão

### Aprovado
![Status Aprovado](https://github.com/AsebiCode/Projeto-APS/blob/c2bd2561253393ad7a0df88ff08d3a3a92050fb0/status_aprovado.jpg)

### Análise Manual
![Status Análise Manual](https://github.com/AsebiCode/Projeto-APS/blob/c2bd2561253393ad7a0df88ff08d3a3a92050fb0/status_analisemanual.jpg)

### Reprovado
![Status Reprovado](https://github.com/AsebiCode/Projeto-APS/blob/c2bd2561253393ad7a0df88ff08d3a3a92050fb0/status_reprovado.jpg)

---

## 15. Evolução futura

Uma evolução prevista é a inclusão do cálculo de juros.

A intenção é manter as responsabilidades separadas, podendo ampliar o fluxo para:

```text
Score
  ↓
Decisão
  ↓
Cálculo de juros
  ↓
Demais etapas da operação
```

O cálculo de juros poderá ser incorporado posteriormente sem colocar essa responsabilidade diretamente no Score ou na Decisão.

---

## Status atual

A integração entre **Score** e **Decisão** foi estruturada para funcionar por HTTP POST.

A Decisão deixou de depender do preenchimento manual dos dados principais e passou a receber o resultado produzido pelo Score.

O projeto permanece aberto para novas integrações e funcionalidades.
>>>>>>> outro-repo/main
