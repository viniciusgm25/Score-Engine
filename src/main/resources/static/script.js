function toggleProfileForms() {
    const isPF = document.querySelector('input[name="tipoPessoa"]:checked').value === 'PF';
    document.getElementById('pfFields').style.display = isPF ? 'block' : 'none';
    document.getElementById('pjFields').style.display = isPF ? 'none' : 'block';
}

document.getElementById('scoreForm').addEventListener('submit', async function (e) {
    e.preventDefault();

    const tipoPessoa = document.querySelector('input[name="tipoPessoa"]:checked').value;
    const forcarRecalculo = document.getElementById('chkForcarRecalculo').checked;

    // A flag forcarRecalculo é injetada na raiz do JSON para o UnifiedScoreRequestDTO
    const payload = {
        tipoPessoa: tipoPessoa,
        forcarRecalculo: forcarRecalculo
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
    document.getElementById('traceStatus').textContent = "Transmitindo POST /api/v1/score/evaluate...";

    const headers = { 'Content-Type': 'application/json' };
    const token = document.getElementById('jwtToken').value.trim();
    if (token) headers['Authorization'] = 'Bearer ' + token;

    try {
        const response = await fetch('/api/v1/score/evaluate', {
            method: 'POST',
            headers: headers,
            body: jsonStr
        });

        document.getElementById('traceStatus').textContent = `Status: ${response.status} ${response.statusText}`;
        const data = await response.json();
        document.getElementById('traceResponse').textContent = JSON.stringify(data, null, 2);

        if (response.ok) {
            document.getElementById('cardResultado').style.display = 'block';

            // Campos alinhados ao UnifiedScoreResponseDTO real:
            // clienteId, tipoPessoa, scoreFinal, faixaRisco, probabilidadeDefault,
            // modelo{codigo,versao}, calculatedAt, origem, componentes[], fatoresImpacto[]
            document.getElementById('outCliente').textContent = data.clienteId;
            document.getElementById('outTipo').textContent = data.tipoPessoa;
            document.getElementById('outScore').textContent = data.scoreFinal;
            document.getElementById('outRating').textContent = data.faixaRisco;

            const elemOrigem = document.getElementById('outOrigem');
            elemOrigem.textContent = data.origem;
            elemOrigem.className = `badge-origem origem-${data.origem}`;

            document.getElementById('outModelo').textContent = `${data.modelo.codigo} (${data.modelo.versao})`;
            document.getElementById('outData').textContent = new Date(data.calculatedAt).toLocaleString();

            const compContainer = document.getElementById('outComponentes');
            compContainer.innerHTML = '';
            if (data.componentes) {
                data.componentes.forEach(c => {
                    // O DTO não traz um campo de "impacto" por componente,
                    // então a classe/rótulo de impacto foi removida daqui.
                    const item = document.createElement('div');
                    item.className = 'component-item';
                    item.innerHTML = `<strong>${c.nome}</strong>: ${c.pontuacao} / ${c.pontuacaoMaxima} pts (Peso: ${c.pesoPonderado})<br/><span class="hint">${c.motivo}</span>`;
                    compContainer.appendChild(item);
                });
            }

            if (data.fatoresImpacto && data.fatoresImpacto.length) {
                const fatoresTitle = document.createElement('h4');
                fatoresTitle.textContent = 'Fatores de Impacto';
                compContainer.appendChild(fatoresTitle);

                const fatoresList = document.createElement('ul');
                data.fatoresImpacto.forEach(f => {
                    const li = document.createElement('li');
                    li.textContent = f;
                    fatoresList.appendChild(li);
                });
                compContainer.appendChild(fatoresList);
            }
        } else {
            document.getElementById('cardResultado').style.display = 'none';
        }
    } catch (err) {
        document.getElementById('traceStatus').textContent = "Falha de rede ou timeout de comunicação.";
        document.getElementById('traceResponse').textContent = String(err);
    }
});