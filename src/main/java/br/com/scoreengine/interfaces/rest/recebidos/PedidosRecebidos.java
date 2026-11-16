package br.com.scoreengine.interfaces.rest.recebidos;

import br.com.scoreengine.interfaces.rest.dto.request.UnifiedScoreRequestDTO;
import br.com.scoreengine.interfaces.rest.dto.response.UnifiedScoreResponseDTO;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.List;

/**
 * Guarda em memória os últimos pedidos recebidos em POST /api/v1/score/evaluate,
 * com os dados enviados e o resultado, para a tela do Score mostrar o que chegou
 * de outros sistemas (por exemplo, o AutoGiro).
 *
 * Fica só na memória: a lista é apagada quando o Score reinicia. O banco continua
 * guardando os resultados como antes.
 */
@Component
public class PedidosRecebidos {

    private static final int LIMITE = 50;

    private final Deque<PedidoRecebido> itens = new ArrayDeque<>();
    private long sequencia = 0;

    /**
     * Um pedido recebido.
     *
     * @param origem      cabeçalho Origin da chamada (endereço do site que enviou), ou null
     *                    quando a chamada não veio de um navegador
     * @param erroDecisao mensagem de erro do envio para a Decisão, ou null se não houve erro
     */
    public record PedidoRecebido(
            long id,
            Instant recebidoEm,
            String origem,
            UnifiedScoreRequestDTO pedido,
            UnifiedScoreResponseDTO resultado,
            String erroDecisao) {
    }

    public synchronized PedidoRecebido registrar(String origem, UnifiedScoreRequestDTO pedido,
                                                 UnifiedScoreResponseDTO resultado, String erroDecisao) {
        PedidoRecebido p = new PedidoRecebido(++sequencia, Instant.now(), origem, pedido, resultado, erroDecisao);
        itens.addFirst(p);
        while (itens.size() > LIMITE) {
            itens.removeLast();
        }
        return p;
    }

    /** Pedidos com id maior que {@code depoisDe}, do mais novo para o mais antigo. */
    public synchronized List<PedidoRecebido> listar(long depoisDe) {
        return itens.stream().filter(p -> p.id() > depoisDe).toList();
    }
}
