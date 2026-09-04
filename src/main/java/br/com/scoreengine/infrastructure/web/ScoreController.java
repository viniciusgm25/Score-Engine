package br.com.scoreengine.infrastructure.web;

import br.com.scoreengine.application.dto.ClientePerfilDto;
import br.com.scoreengine.application.dto.ClientePerfilMapper;
import br.com.scoreengine.application.service.ScoreService;
import br.com.scoreengine.domain.model.ClientePerfil;
import br.com.scoreengine.domain.model.ScoreResultado;
import jakarta.validation.Valid;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@RestController
@RequestMapping("/api/v1/score")
public class ScoreController {

    private final ScoreService scoreService;
    private final ClientePerfilMapper mapper;
    private static final Logger log = LoggerFactory.getLogger(ScoreController.class);

    public ScoreController(ScoreService scoreService, ClientePerfilMapper mapper) {
        this.scoreService = scoreService;
        this.mapper = mapper;
    }

    @GetMapping
    public String getPoint() {
        return "EndPoint do GET";
    }

    @PostMapping
    public ResponseEntity<ScoreResultado> calcularScore(
            @RequestHeader(value = "X-Correlation-Id", required = false) String correlationId,
            @RequestBody @Valid ClientePerfilDto dto) {

        log.info("1. JSON recebido do formulário (Instância de {}): {}",
                dto != null ? dto.getClass().getSimpleName() : "null", dto);

        if (correlationId == null || correlationId.isBlank()) {
            correlationId = UUID.randomUUID().toString();
        }

        ClientePerfil perfil = mapper.toDomain(dto);
        ScoreResultado resultado = scoreService.processarScore(perfil, correlationId);

        log.info("3. Resposta pronta para devolução: {}", resultado);

        return ResponseEntity.ok(resultado);
    }
}