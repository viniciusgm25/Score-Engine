# Esteira de Crédito — Microsserviço de Decisão

O módulo de Decisão foi reestruturado para receber o resultado já calculado pelo Score Engine. A tela não solicita mais CPF, renda, valor de contrato, documentos ou outros dados adicionais para executar a decisão.

## Fluxo

```text
Score Engine
   │
   │ POST /api/v1/decisao
   ▼
Decisão
   │
   ├── APROVADO
   ├── ANALISE_MANUAL
   └── REPROVADO
```

## Endpoint receptor

```text
POST http://localhost:8081/api/v1/decisao
```

O payload esperado é o resultado unificado produzido pelo Score, contendo principalmente `clienteId`, `scoreFinal`, `faixaRisco`, `probabilidadeDefault`, `modelo`, `calculatedAt`, `origem`, `componentes` e `fatoresImpacto`.

## Política atual

A decisão preserva os limites existentes no módulo anterior:
- score abaixo de 400 → `REPROVADO`;
- score entre 400 e 499 → `ANALISE_MANUAL`;
- score a partir de 500 → `APROVADO`.

Esses limites são hipóteses de política acadêmica do projeto. O Score continua sendo responsável pelo cálculo e pela classificação de risco.

## Execução local

O serviço não precisa de banco de dados nem de Docker para executar.

Com Node.js instalado:

```text
node server.js
```

A interface ficará disponível em:

```text
http://localhost:8081
```

Health check:

```text
GET http://localhost:8081/api/v1/decisao/health
```

Última decisão recebida:

```text
GET http://localhost:8081/api/v1/decisao/latest
```

## Testes

Os testes automatizados ficam em `test/decisao.test.js` e podem ser executados com:

```text
npm install
npm test
```

## Teste de integração no mesmo notebook

1. Inicie a Decisão em `localhost:8081`.
2. Inicie o Score em `localhost:8080`.
3. Envie uma avaliação para `POST /api/v1/score/evaluate`.
4. O Score calcula e envia automaticamente o resultado para `POST /api/v1/decisao`.
5. A tela da Decisão passa a mostrar o resultado recebido.

Para executar em computadores diferentes, altere `DECISION_URL` no ambiente do Score para a URL do computador que hospeda a Decisão.
