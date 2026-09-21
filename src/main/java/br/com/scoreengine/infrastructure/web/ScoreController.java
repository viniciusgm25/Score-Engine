package br.com.scoreengine.infrastructure.web;

import br.com.scoreengine.application.dto.ClientePerfilDto;
import br.com.scoreengine.application.dto.ClientePerfilMapper;
import br.com.scoreengine.application.dto.PessoaFisicaDto;
import br.com.scoreengine.application.service.ScoreService;
import br.com.scoreengine.domain.model.common.CustomerType;
import br.com.scoreengine.domain.model.common.ScoreOrigin;
import br.com.scoreengine.domain.model.common.ScoreResult;
import br.com.scoreengine.domain.model.pf.CustomerPFProfile;
import br.com.scoreengine.domain.model.pj.CustomerPJProfile;
import br.com.scoreengine.interfaces.rest.dto.response.ModelInfoDTO;
import br.com.scoreengine.interfaces.rest.dto.response.ScoreComponentResponseDTO;
import br.com.scoreengine.interfaces.rest.dto.response.UnifiedScoreResponseDTO;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Objects;
import java.util.UUID;

/**
 * Ponto de Entrada REST para a Avaliação de Score de Crédito Corporativo e
 * Varejo.
 * Roteia requisições polimórficas (PF e PJ), delega ao ScoreService e projeta a
 * resposta
 * em conformidade com as diretrizes prudenciais do Bacen, Basileia e CDC.
 */
@RestController
@RequestMapping("/api/v1/score")
public class ScoreController {

    private static final Logger log = LoggerFactory.getLogger(ScoreController.class);
    private static final String CORRELATION_HEADER = "X-Correlation-Id";

    private final ScoreService scoreService;
    private final ClientePerfilMapper mapper;

    public ScoreController(ScoreService scoreService, ClientePerfilMapper mapper) {
        this.scoreService = Objects.requireNonNull(scoreService, "O ScoreService não pode ser nulo.");
        this.mapper = Objects.requireNonNull(mapper, "O ClientePerfilMapper não pode ser nulo.");
    }

    @GetMapping("/health")
    public ResponseEntity<String> healthCheck() {
        return ResponseEntity.ok("Score Engine operacional.");
    }

    @PostMapping
    public ResponseEntity<UnifiedScoreResponseDTO> calcularScore(
            @RequestHeader(value = CORRELATION_HEADER, required = false) String correlationId,
            @RequestBody @Valid ClientePerfilDto dto) {

        String traceId = (correlationId != null && !correlationId.isBlank())
                ? correlationId
                : UUID.randomUUID().toString();

        MDC.put("correlationId", traceId);

        try {
            log.info("Requisição recebida para cálculo de Score. Cliente ID: [{}], Tipo: [{}]",
                    dto.clienteId(), dto.tipoPessoa());

            ScoreResult resultado;
            if (dto.tipoPessoa() == CustomerType.PF || dto instanceof PessoaFisicaDto) {
                CustomerPFProfile profilePF = mapper.toDomainPF(dto);
                resultado = scoreService.processarScorePF(profilePF, traceId);
            } else {
                CustomerPJProfile profilePJ = mapper.toDomainPJ(dto);
                resultado = scoreService.processarScorePJ(profilePJ, traceId);
            }

            List<ScoreComponentResponseDTO> componentesDTO = resultado.componentes().stream()
                    .map(c -> new ScoreComponentResponseDTO(
                            c.nome(),
                            c.pontuacao(),
                            c.pontuacaoMaxima(),
                            c.pesoPonderado(),
                            c.motivo()))
                    .toList();

            String modelCode = (dto.tipoPessoa() == CustomerType.PF) ? "SCORE_PF" : "SCORE_PJ";

            UnifiedScoreResponseDTO responseDTO = new UnifiedScoreResponseDTO(
                    dto.clienteId(),
                    dto.tipoPessoa(),
                    resultado.scoreFinal(),
                    resultado.faixaRisco(),
                    resultado.probabilidadeDefault(),
                    new ModelInfoDTO(modelCode, resultado.modelVersion()),
                    resultado.calculatedAt(),
                    ScoreOrigin.CALCULO,
                    componentesDTO,
                    resultado.fatoresImpacto());

            return ResponseEntity.ok()
                    .header(CORRELATION_HEADER, traceId)
                    .body(responseDTO);

        } finally {
            MDC.remove("correlationId");
        }
    }
}