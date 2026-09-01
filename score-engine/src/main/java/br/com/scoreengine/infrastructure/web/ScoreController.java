package br.com.scoreengine.infrastructure.web;

import br.com.scoreengine.application.dto.ClientePerfilDto;
import br.com.scoreengine.application.dto.ClientePerfilMapper;
import br.com.scoreengine.application.service.ScoreService;
import br.com.scoreengine.domain.model.ClientePerfil;
import br.com.scoreengine.domain.model.ScoreResultado;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/score")
public class ScoreController {

    private final ScoreService scoreService;
    private final ClientePerfilMapper mapper;

    public ScoreController(ScoreService scoreService, ClientePerfilMapper mapper) {
        this.scoreService = scoreService;
        this.mapper = mapper;
    }

    @GetMapping
    public String getPoint(){
        return "EndPoint do GET";
    }

    @PostMapping
    public ResponseEntity<ScoreResultado> calcularScore(
            @RequestHeader(value = "X-Correlation-Id", required = false) String correlationId,
            @RequestBody ClientePerfilDto dto) {

        if (correlationId == null || correlationId.isBlank()) {
            correlationId = UUID.randomUUID().toString();
        }

        ClientePerfil perfil = mapper.toDomain(dto);
        ScoreResultado resultado = scoreService.processarScore(perfil, correlationId);

        return ResponseEntity.ok(resultado);
    }
}