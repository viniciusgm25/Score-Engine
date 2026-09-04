function alternarCampos() {
    const tipo = document.getElementById('tipoPessoa').value;
    document.getElementById('camposPF').style.display = tipo === 'PF' ? 'block' : 'none';
    document.getElementById('camposPJ').style.display = tipo === 'PJ' ? 'block' : 'none';
}

function calcularScore() {
    const tipoPessoa = document.getElementById('tipoPessoa').value;
    const clienteId = document.getElementById('clienteId').value;

    let payload = { clienteId, tipoPessoa };
    console.log("Iniciando cálculo...", payload);

    const parseVal = (id) => parseFloat(document.getElementById(id).value.replace(',', '.')) || 0.0;

    if (tipoPessoa === 'PF') {
        payload.renda = parseVal('renda');
        payload.endividamento = parseVal('endividamento');
        payload.atrasos = parseInt(document.getElementById('atrasos').value) || 0;
        payload.tempoRelacionamento = parseInt(document.getElementById('tempoRelacionamento').value) || 0;
    } else {
        payload.faturamento = parseVal('faturamento');
        payload.endividamento = parseVal('endividamentoPj');
        payload.tempoAtividade = parseInt(document.getElementById('tempoAtividade').value) || 0;
    }

    document.getElementById('jsonRequest').innerText = JSON.stringify(payload, null, 2);

    const token = document.getElementById('token').value;
    const headers = { 'Content-Type': 'application/json' };
    if (token) headers['Authorization'] = 'Bearer ' + token;

    fetch('http://localhost:8080/api/v1/score', {
        method: 'POST',
        headers: headers,
        body: JSON.stringify(payload)
    })
        .then(async response => {
            document.getElementById('httpStatus').innerText = response.status + ' ' + response.statusText;
            const text = await response.text();
            try {
                const json = JSON.parse(text);
                document.getElementById('jsonResponse').innerText = JSON.stringify(json, null, 2);
            } catch (e) {
                document.getElementById('jsonResponse').innerText = text;
            }
        })
        .catch(err => {
            document.getElementById('httpStatus').innerText = 'ERROR';
            document.getElementById('jsonResponse').innerText = err.message;
            console.error("Erro na requisição:", err);
        });
}