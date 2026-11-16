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

## Tecnologias

- Java 21
- Spring Boot 3.2.2
- Spring Web
- Spring Security
- Spring Data JPA
- PostgreSQL
- Lombok
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
```

Sem variáveis adicionais, o ambiente local utiliza PostgreSQL em `localhost:5432`, banco `score_engine_db`, usuário `postgres` e senha `root`, mantendo compatibilidade com a configuração existente. Em outro computador, os valores podem ser alterados sem modificar o código-fonte.

## Execução

Pré-requisitos:
- Java/JDK compatível com o `pom.xml`;
- Maven;
- PostgreSQL local disponível para o Score.

O projeto possui `pom.xml`, porém esta versão não possui Maven Wrapper. A aplicação Spring Boot deve ser executada como projeto Maven, e não compilando `ScoreEngineApplication.java` isoladamente.

Para a integração completa, primeiro execute o serviço de Decisão e depois o Score.

## Princípio da divisão de responsabilidades

**Score calcula. Decisão decide.**
