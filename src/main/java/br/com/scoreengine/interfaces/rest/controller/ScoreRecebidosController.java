package br.com.scoreengine.interfaces.rest.controller;

import br.com.scoreengine.interfaces.rest.recebidos.PedidosRecebidos;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Tag(
    name = "Pedidos recebidos",
    description = "Últimos pedidos que chegaram ao Score, com os dados enviados e o resultado"
)
@RestController
@RequestMapping("/api/v1/score/recebidos")
public class ScoreRecebidosController {

    private final PedidosRecebidos recebidos;

    public ScoreRecebidosController(PedidosRecebidos recebidos) {
        this.recebidos = recebidos;
    }

    @Operation(
        summary = "Lista os últimos pedidos recebidos",
        description = "Devolve até 50 pedidos, do mais novo para o mais antigo. Com depoisDe, devolve só os que chegaram depois desse id. A lista fica em memória e é apagada quando o Score reinicia."
    )
    @GetMapping
    public List<PedidosRecebidos.PedidoRecebido> listar(@RequestParam(defaultValue = "0") long depoisDe) {
        return recebidos.listar(depoisDe);
    }
}
