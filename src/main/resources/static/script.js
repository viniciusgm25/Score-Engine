function toggleProfileForms() {
    const isPF = document.querySelector('input[name="tipoPessoa"]:checked').value === 'PF';
    document.getElementById('pfFields').style.display = isPF ? 'block' : 'none';
    document.getElementById('pjFields').style.display = isPF ? 'none' : 'block';
}

function formatarPD(value) {
    const number = Number(value);
    return Number.isFinite(number) ? `${(number * 100).toFixed(2)}%` : '—';
}

function escapeHtml(value) {
    return String(value ?? '')
        .replaceAll('&', '&amp;')
        .replaceAll('<', '&lt;')
        .replaceAll('>', '&gt;')
        .replaceAll('"', '&quot;')
        .replaceAll("'", '&#039;');
}

// Data de liberação começa com a data de hoje (pode ser alterada)
const _dl = document.getElementById('solDataLiberacao');
if (_dl && !_dl.value) _dl.value = new Date().toISOString().slice(0, 10);

document.getElementById('scoreForm').addEventListener('submit', async function (e) {
    e.preventDefault();

    const tipoPessoa = document.querySelector('input[name="tipoPessoa"]:checked').value;
    const forcarRecalculo = document.getElementById('chkForcarRecalculo').checked;

    const payload = {
        tipoPessoa,
        forcarRecalculo
    };

    if (tipoPessoa === 'PF') {
        payload.perfilPF = {
            cpf: document.getElementById('pfCpf').value.trim(),
            rendaMensal: parseFloat(document.getElementById('pfRenda').value),
            dividaTotal: parseFloat(document.getElementById('pfDivida').value),
            idade: parseInt(document.getElementById('pfIdade').value, 10),
            estadoCivil: document.getElementById('pfEstadoCivil').value,
            numeroDependentes: parseInt(document.getElementById('pfDependentes').value, 10),
            diasAtrasoUltimos12Meses: parseInt(document.getElementById('pfAtrasos').value, 10),
            limiteRotativoUtilizado: parseFloat(document.getElementById('pfRotUtil').value),
            limiteRotativoTotal: parseFloat(document.getElementById('pfRotTotal').value),
            mesesNoEmpregoAtual: parseInt(document.getElementById('pfMesesEmp').value, 10),
            mesesRelacionamentoBanco: parseInt(document.getElementById('pfMesesBanco').value, 10)
        };
    } else {
        payload.perfilPJ = {
            cnpj: document.getElementById('pjCnpj').value.trim(),
            razaoSocial: document.getElementById('pjRazao').value.trim(),
            faturamentoMensal: parseFloat(document.getElementById('pjFatMensal').value),
            despesasOperacionaisMensais: parseFloat(document.getElementById('pjDespMensal').value),
            passivoTotalBancario: parseFloat(document.getElementById('pjPassivo').value),
            faturamentoBrutoAnual: parseFloat(document.getElementById('pjFatAnual').value),
            diasAtrasoUltimos12Meses: parseInt(document.getElementById('pjAtrasos').value, 10),
            mesesConstituicao: parseInt(document.getElementById('pjMesesConst').value, 10),
            setorAtuacao: document.getElementById('pjSetor').value
        };
    }

    // Dados da solicitação (provisório). Campo vazio vira null.
    const txt = id => document.getElementById(id).value.trim() || null;
    const num = id => { const v = document.getElementById(id).value; return v === '' ? null : parseFloat(v); };
    const primeiro = document.getElementById('solPrimeiro').value;
    payload.identificador = txt('solIdentificador');
    payload.valor = num('solValor');
    payload.modalidade = txt('solModalidade');
    payload.prazoMeses = num('solPrazo');
    payload.dataLiberacao = txt('solDataLiberacao');
    payload.primeiroRelacionamento = primeiro === '' ? null : primeiro === 'true';
    payload.amortizacao = txt('solAmortizacao');

    const jsonStr = JSON.stringify(payload, null, 2);
    document.getElementById('traceRequest').textContent = jsonStr;
    document.getElementById('traceStatus').textContent = 'Calculando Score e enviando resultado para a Decisão...';

    const headers = { 'Content-Type': 'application/json' };
    const token = document.getElementById('jwtToken').value.trim();
    if (token) headers['Authorization'] = 'Bearer ' + token;

    try {
        const response = await fetch('/api/v1/score/evaluate', {
            method: 'POST',
            headers,
            body: jsonStr
        });

        const data = await response.json();
        document.getElementById('traceStatus').textContent = `Status: ${response.status} ${response.statusText}`;
        document.getElementById('traceResponse').textContent = JSON.stringify(data, null, 2);

        if (!response.ok) {
            document.getElementById('cardResultado').style.display = 'none';
            return;
        }

        document.getElementById('cardResultado').style.display = 'block';
        document.getElementById('outCliente').textContent = data.clienteId ?? '—';
        document.getElementById('outTipo').textContent = data.tipoPessoa ?? '—';
        document.getElementById('outScore').textContent = data.scoreFinal ?? '—';
        document.getElementById('outRating').textContent = data.faixaRisco ?? '—';
        document.getElementById('outPd').textContent = formatarPD(data.probabilidadeDefault);
        document.getElementById('outOrigem').textContent = data.origem ?? '—';
        document.getElementById('outModelo').textContent = data.modelo
            ? `${data.modelo.codigo} (${data.modelo.versao})`
            : '—';
        document.getElementById('outData').textContent = data.calculatedAt
            ? new Date(data.calculatedAt).toLocaleString('pt-BR')
            : '—';

        const componentes = Array.isArray(data.componentes) ? data.componentes : [];
        const compContainer = document.getElementById('outComponentes');
        compContainer.innerHTML = componentes.length
            ? componentes.map(c => `
                <div class="component-item">
                    <strong>${escapeHtml(c.nome)}</strong>: ${escapeHtml(c.pontuacao)} / ${escapeHtml(c.pontuacaoMaxima)}
                    (Peso: ${escapeHtml(Number(c.pesoPonderado ?? 0) * 100)}%)<br/>
                    <span class="hint">${escapeHtml(c.motivo)}</span>
                </div>
              `).join('')
            : '<div class="component-item">Nenhum componente informado.</div>';

        document.getElementById('outDecision').textContent =
            'Resultado enviado automaticamente para o microsserviço de Decisão.';
    } catch (err) {
        document.getElementById('traceStatus').textContent = 'Falha de comunicação com o Score Engine ou com a Decisão.';
        document.getElementById('traceResponse').textContent = String(err);
        document.getElementById('cardResultado').style.display = 'none';
    }
});


// ---- Pedidos recebidos: lista o que chegou ao Score, inclusive de outros sistemas ----
const ROTULOS_RECEBIDOS = {
    cpf: 'CPF', cnpj: 'CNPJ', razaoSocial: 'Razão social', rendaMensal: 'Renda mensal', dividaTotal: 'Dívida total',
    idade: 'Idade', estadoCivil: 'Estado civil', numeroDependentes: 'Dependentes',
    diasAtrasoUltimos12Meses: 'Dias de atraso (12 meses)', limiteRotativoUtilizado: 'Limite rotativo usado',
    limiteRotativoTotal: 'Limite rotativo total', mesesNoEmpregoAtual: 'Meses no emprego',
    mesesRelacionamentoBanco: 'Meses de banco', faturamentoMensal: 'Faturamento mensal',
    despesasOperacionaisMensais: 'Despesas mensais', passivoTotalBancario: 'Passivo bancário',
    faturamentoBrutoAnual: 'Faturamento anual', mesesConstituicao: 'Meses de empresa', setorAtuacao: 'Setor',
    identificador: 'Identificador', valor: 'Valor', modalidade: 'Modalidade', prazoMeses: 'Prazo (meses)',
    dataLiberacao: 'Data de liberação', primeiroRelacionamento: 'Primeiro relacionamento', amortizacao: 'Amortização'
};
const CAMPOS_EM_REAIS = /renda|divida|limite|valor|faturamento|despesas|passivo/i;
const CAMPOS_SOLICITACAO = ['identificador', 'valor', 'modalidade', 'prazoMeses', 'dataLiberacao', 'primeiroRelacionamento', 'amortizacao'];

const pedidosRecebidos = [];
const recebidosAbertos = new Set();
let ultimoRecebido = 0;
let primeiraConsulta = true;

function valorRecebido(chave, v) {
    if (v === null || v === undefined || v === '') return '—';
    if (typeof v === 'boolean') return v ? 'Sim' : 'Não';
    if (CAMPOS_EM_REAIS.test(chave) && Number.isFinite(Number(v))) {
        return Number(v).toLocaleString('pt-BR', { style: 'currency', currency: 'BRL' });
    }
    if (chave === 'dataLiberacao') return new Date(v + 'T12:00:00').toLocaleDateString('pt-BR');
    return String(v);
}

function listaRecebida(obj, chaves) {
    const itens = (chaves ?? Object.keys(obj ?? {})).filter(k => obj && obj[k] !== undefined);
    if (!itens.length) return '<p class="hint">Nada informado.</p>';
    return '<dl>' + itens.map(k =>
        `<dt>${escapeHtml(ROTULOS_RECEBIDOS[k] ?? k)}</dt><dd>${escapeHtml(valorRecebido(k, obj[k]))}</dd>`
    ).join('') + '</dl>';
}

function origemRecebida(origem) {
    if (!origem) return 'outro serviço (sem navegador)';
    if (origem === window.location.origin) return 'esta tela';
    return origem;
}

function renderizarRecebidos(novosIds) {
    const lista = document.getElementById('recebidosLista');
    if (!pedidosRecebidos.length) {
        lista.innerHTML = '<p class="hint">Nenhum pedido recebido desde que o Score iniciou.</p>';
        return;
    }
    lista.innerHTML = pedidosRecebidos.map(p => {
        const pedido = p.pedido ?? {};
        const r = p.resultado ?? {};
        const perfil = pedido.perfilPF ?? pedido.perfilPJ ?? {};
        const cliente = perfil.cpf ?? perfil.cnpj ?? r.clienteId ?? '—';
        const classes = ['recebido', p.erroDecisao ? 'com-erro' : '', novosIds.has(p.id) ? 'novo' : ''].join(' ');
        const componentes = Array.isArray(r.componentes) ? r.componentes.map(c =>
            `<dt>${escapeHtml(c.nome)}</dt><dd>${escapeHtml(c.pontuacao)} / ${escapeHtml(c.pontuacaoMaxima)}</dd>`).join('') : '';
        return `
            <details class="${classes}" data-id="${p.id}" ${recebidosAbertos.has(p.id) ? 'open' : ''}>
                <summary>
                    <strong>Score ${escapeHtml(r.scoreFinal ?? '—')} · ${escapeHtml(r.faixaRisco ?? '—')}</strong>
                    <span>${escapeHtml(pedido.tipoPessoa ?? '')} ${escapeHtml(cliente)}</span>
                    <span>${escapeHtml(pedido.identificador ?? '')}</span>
                    <span>${pedido.valor != null ? escapeHtml(valorRecebido('valor', pedido.valor)) : ''}${pedido.prazoMeses ? ' em ' + escapeHtml(pedido.prazoMeses) + 'x' : ''}</span>
                    <span class="hint">${new Date(p.recebidoEm).toLocaleTimeString('pt-BR')} · de ${escapeHtml(origemRecebida(p.origem))}</span>
                </summary>
                <div class="recebido-corpo">
                    <div><h4>Dados do cliente</h4>${listaRecebida(perfil)}</div>
                    <div><h4>Solicitação</h4>${listaRecebida(pedido, CAMPOS_SOLICITACAO)}</div>
                    <div><h4>Resultado</h4><dl>
                        <dt>Score</dt><dd>${escapeHtml(r.scoreFinal ?? '—')}</dd>
                        <dt>Faixa de risco</dt><dd>${escapeHtml(r.faixaRisco ?? '—')}</dd>
                        <dt>PD</dt><dd>${escapeHtml(formatarPD(r.probabilidadeDefault))}</dd>
                        <dt>Origem do cálculo</dt><dd>${escapeHtml(r.origem ?? '—')}</dd>
                        ${componentes}
                    </dl></div>
                    ${p.erroDecisao
                        ? `<p class="recebido-erro">Falha ao enviar para a Decisão: ${escapeHtml(p.erroDecisao)}</p>`
                        : '<p class="hint recebido-erro" style="color:inherit">Enviado para a Decisão (ou envio desligado na configuração).</p>'}
                </div>
            </details>`;
    }).join('');
}

document.getElementById('recebidosLista').addEventListener('toggle', (e) => {
    const id = Number(e.target.dataset?.id);
    if (!id) return;
    if (e.target.open) recebidosAbertos.add(id); else recebidosAbertos.delete(id);
}, true);

async function buscarRecebidos() {
    const status = document.getElementById('recebidosStatus');
    const headers = {};
    const token = document.getElementById('jwtToken').value.trim();
    if (token) headers['Authorization'] = 'Bearer ' + token;
    try {
        const response = await fetch('/api/v1/score/recebidos?depoisDe=' + ultimoRecebido, { headers });
        if (response.status === 401 || response.status === 403) {
            status.textContent = 'Para listar os pedidos, informe o token JWT acima (ou rode com a segurança desligada no ambiente local).';
            return;
        }
        if (!response.ok) {
            status.textContent = `Não foi possível listar os pedidos (erro ${response.status}).`;
            return;
        }
        const novos = await response.json();
        const novosIds = new Set(primeiraConsulta ? [] : novos.map(p => p.id));
        if (novos.length || primeiraConsulta) {
            pedidosRecebidos.unshift(...novos);
            pedidosRecebidos.splice(50);
            if (novos.length) ultimoRecebido = Math.max(ultimoRecebido, ...novos.map(p => p.id));
            renderizarRecebidos(novosIds);
        }
        primeiraConsulta = false;
        status.textContent = `${pedidosRecebidos.length} pedido(s). Última consulta às ${new Date().toLocaleTimeString('pt-BR')}.`;
    } catch (err) {
        status.textContent = 'Sem resposta do Score. Ele está rodando?';
    }
}

buscarRecebidos();
setInterval(buscarRecebidos, 3000);
