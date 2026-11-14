# Campos provisórios da solicitação (Financiamento)

Branch: `feature/campos-solicitacao-provisorio`

O grupo de Financiamento ainda não envia os dados da solicitação, então o Score
aceita e repassa à Decisão estes campos (todos opcionais, o Score NÃO os usa no cálculo):

| Campo | Tipo | Exemplo |
|---|---|---|
| identificador | texto | "OP-2026-000123" |
| valor (valor solicitado) | número | 10000.00 |
| modalidade | texto (um dos 6 valores abaixo) | "CREDITO_PESSOAL" |
| prazoMeses | inteiro | 24 |
| dataLiberacao | data (AAAA-MM-DD) | "2026-11-01" |
| primeiroRelacionamento | true/false | false |
| amortizacao | texto (`PRICE` ou `SAC`) | "SAC" |

Modalidades aceitas: `CONSIGNADO_INSS`, `CONSIGNADO_PRIVADO`, `CONSIGNADO_PUBLICO`,
`CREDITO_PESSOAL`, `OUTROS_BENS`, `VEICULOS` (outro valor retorna erro 400; `null` é aceito pela API).

Na tela, todos os campos da solicitação são obrigatórios (não envia vazio).

O campo `amortizacao` é opcional na API (`null` é aceito; outro valor além de `PRICE`/`SAC` retorna 400).
O Score apenas o repassa no corpo enviado à Decisão; a Decisão só encaminha ao Juros os campos que ela mapeia.

Fluxo: tela (Score) -> `POST /api/v1/score/evaluate` -> resposta/`POST` para a Decisão
com esses 6 campos junto do score.

Arquivos alterados: `UnifiedScoreRequestDTO`, `UnifiedScoreResponseDTO`,
`UnifiedScoreDtoMapper`, `ScorePipelineController`, `static/index.html`, `static/script.js`.

Quando o Financiamento integrar de verdade: apagar esses campos e o bloco
"Dados da solicitação" do `index.html`/`script.js`.
