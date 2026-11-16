# Score Engine - Sistema de Avaliação de Risco de Crédito

Motor de cálculo de score de crédito desenvolvido em **Java 21** e **Spring Boot 3.2.2**, estruturado em camadas de domínio, aplicação e infraestrutura.

## Arquitetura atual

O Score Engine é responsável por calcular o score e produzir um resultado estruturado. Após o cálculo, o próprio serviço envia esse resultado por HTTP POST para o microsserviço de Decisão.

```text
Entrada do cliente
      ↓
Score Engine
      ↓ POST
http://localhost:8081/api/v1/decisao
      ↓
Decisão
      ↓
APROVADO / ANALISE_MANUAL / REPROVADO
```

Durante o desenvolvimento, os dois microsserviços podem ficar no mesmo notebook, em processos diferentes e com portas diferentes. Em uma implantação com computadores separados, a URL da Decisão pode ser alterada para o IP do computador correspondente.

### Contratos

Entrada do Score:

```text
POST /api/v1/score/evaluate
```

Saída do Score e payload enviado para a Decisão:
- clienteId
- tipoPessoa
- scoreFinal
- faixaRisco
- probabilidadeDefault
- modelo.codigo
- modelo.versao
- calculatedAt
- origem
- componentes
- fatoresImpacto

Entrada da Decisão:

```text
POST /api/v1/decisao
```

A Decisão não solicita novamente os dados utilizados para calcular o score.

## Estrutura de pacotes

```text
br.com.scoreengine
├── application
│   ├── dto            DTOs de entrada do endpoint legado e UnifiedScoreOutput
│   ├── port/out       Portas de saída (auditoria e envio para a Decisão)
│   ├── service        ScoreService e ScoreValidationService
│   └── usecase        EvaluateUnifiedScoreUseCase e CalculatePFScoreUseCase
├── domain
│   ├── calculator     Estratégias de cálculo PF e PJ
│   ├── exception      Exceções de negócio
│   ├── model          Perfis PF/PJ e objetos do resultado (ScoreResult, ScoreComponent...)
│   ├── repository     Contratos de persistência
│   └── validator      PerfilValidator
├── infrastructure
│   ├── config         Montagem dos beans sem anotação (ScoreConfig)
│   ├── integration    Cliente HTTP da Decisão
│   ├── persistence    Entidades JPA, repositórios Spring Data e adaptadores
│   ├── security       Segurança (JWT) e CORS
│   └── web            Filtro de Correlation ID
└── interfaces/rest
    ├── controller     Controllers REST
    ├── dto            DTOs de requisição e resposta
    ├── handler        Tratamento global de exceções
    └── mapper         Conversão entre DTOs REST e domínio
```

Endpoints expostos:

| Método | Caminho | Controller |
|---|---|---|
| POST | `/api/v1/score/evaluate` | `ScorePipelineController` (fluxo principal, usado pela tela) |
| POST | `/api/v1/score` | `ScoreController` |
| GET | `/api/v1/score/health` | `ScoreController` |
| POST | `/api/v1/score/pf` | `ScorePFController` |
| POST | `/api/v1/score/validation/metrics` | `ScoreValidationController` |

## Tecnologias

- Java 21
- Spring Boot 3.2.2
- Spring Web
- Spring Security
- Spring Data JPA
- PostgreSQL
- SpringDoc OpenAPI (Swagger)
- JUnit 5
- Maven

O projeto usa HTTP para a integração Score → Decisão. A configuração antiga de Kafka estava sem a dependência correspondente no `pom.xml` e foi retirada desta versão para evitar uma integração assíncrona não utilizada no fluxo definido pelo projeto.

## Banco de dados

O PostgreSQL continua sendo utilizado para persistência do Score, mas as configurações específicas de cada computador não ficam presas a um único ambiente.

Variáveis disponíveis:

```text
DB_URL
DB_USERNAME
DB_PASSWORD
SCORE_PORT
DECISION_URL
DECISION_ENABLED
JWT_ENABLED
JWT_ISSUER_URI
CORS_ALLOWED_ORIGINS
CORS_ALLOW_LOCAL_NETWORK
```

## Arquivo .env

Os valores dessas variáveis ficam no arquivo `.env`, que não é versionado (está no `.gitignore`). O repositório traz apenas o `.env.exemplo`, com os campos para preencher.

Para criar o seu `.env`:

```bash
cp .env.exemplo .env
```

Depois preencha os valores no `.env`, principalmente `DB_USERNAME` e `DB_PASSWORD`.

O `application.yml` importa esse arquivo automaticamente (`spring.config.import: optional:file:.env[.properties]`). O caminho é relativo à pasta de onde a aplicação é executada, então rode o projeto a partir da raiz (ex.: `mvn spring-boot:run`). Se o `.env` não existir, o Spring usa as variáveis de ambiente do sistema.

Valores padrão quando a variável não é informada:

| Variável | Padrão |
|---|---|
| `DB_URL` | `jdbc:postgresql://localhost:5432/score_engine_db` |
| `DB_USERNAME` | `postgres` |
| `DB_PASSWORD` | **nenhum** — obrigatório; sem ele a aplicação não sobe |

A senha não tem valor padrão de propósito, para que nenhuma credencial fique no repositório.

## Execução

Pré-requisitos:
- Java/JDK compatível com o `pom.xml`;
- Maven;
- PostgreSQL local disponível para o Score.

O projeto possui `pom.xml`, porém esta versão não possui Maven Wrapper. A aplicação Spring Boot deve ser executada como projeto Maven, e não compilando `ScoreEngineApplication.java` isoladamente.

Para a integração completa, primeiro execute o serviço de Decisão e depois o Score.

## Princípio da divisão de responsabilidades

**Score calcula. Decisão decide.**
