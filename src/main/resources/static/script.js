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
