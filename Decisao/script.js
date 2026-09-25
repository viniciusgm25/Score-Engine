const ICONES = {
  aprovado: '<svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.2" stroke-linecap="round" stroke-linejoin="round"><path d="M20 6 9 17l-5-5"/></svg>',
  manual: '<svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.2" stroke-linecap="round" stroke-linejoin="round"><path d="M12 9v4"/><path d="M12 17h.01"/><path d="M10.3 3.9 1.9 18a2 2 0 0 0 1.7 3h16.8a2 2 0 0 0 1.7-3L14.7 3.9a2 2 0 0 0-3.4 0Z"/></svg>',
  reprovado: '<svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.2" stroke-linecap="round" stroke-linejoin="round"><path d="M18 6 6 18"/><path d="m6 6 12 12"/></svg>'
};

const STATUS = {
  APROVADO: { classe: "aprovado", titulo: "APROVADO", icone: ICONES.aprovado },
  ANALISE_MANUAL: { classe: "manual", titulo: "ANÁLISE MANUAL", icone: ICONES.manual },
  REPROVADO: { classe: "reprovado", titulo: "REPROVADO", icone: ICONES.reprovado }
};

function escapeHtml(value) {
  return String(value ?? "")
    .replaceAll("&", "&amp;")
    .replaceAll("<", "&lt;")
    .replaceAll(">", "&gt;")
    .replaceAll('"', "&quot;")
    .replaceAll("'", "&#039;");
}

function formatarPD(value) {
  const number = Number(value);
  return Number.isFinite(number) ? `${(number * 100).toFixed(2)}%` : "—";
}

function formatarData(value) {
  if (!value) return "—";
  const data = new Date(value);
  return Number.isNaN(data.getTime()) ? value : data.toLocaleString("pt-BR");
}

function renderResultado(resultado) {
  const status = STATUS[resultado.decisao];
  const veredito = document.getElementById("veredito");

  if (!status) {
    veredito.className = "verdict-banner reprovado";
    veredito.innerHTML = `<span class="verdict-texto"><div class="titulo">DECISÃO INVÁLIDA</div><div class="detalhe">O serviço retornou um estado desconhecido.</div></span>`;
    return;
  }

  veredito.className = `verdict-banner ${status.classe}`;
  veredito.innerHTML = `
    <span class="verdict-icone">${status.icone}</span>
    <span class="verdict-texto">
      <div class="titulo">${status.titulo}</div>
      <div class="detalhe">${escapeHtml(resultado.motivo)}</div>
    </span>
  `;

  document.getElementById("cards").hidden = false;
  document.getElementById("metadata").hidden = false;
  document.getElementById("componentesPanel").hidden = false;
  document.getElementById("jsonLog").hidden = false;

  document.getElementById("clienteId").textContent = resultado.clienteId ?? "—";
  document.getElementById("scoreFinal").textContent = resultado.scoreFinal ?? "—";
  document.getElementById("faixaRisco").textContent = resultado.faixaRisco ?? "—";
  document.getElementById("pd").textContent = formatarPD(resultado.probabilidadeDefault);
  document.getElementById("modelo").textContent = resultado.modelo?.codigo ?? "—";
  document.getElementById("versao").textContent = resultado.modelo?.versao ?? "—";
  document.getElementById("origem").textContent = resultado.origem ?? "—";
  document.getElementById("calculatedAt").textContent = formatarData(resultado.calculatedAt);

  const fatores = Array.isArray(resultado.fatoresImpacto) ? resultado.fatoresImpacto : [];
  document.getElementById("fatoresImpacto").innerHTML = fatores.length
    ? fatores.map(fator => `<li>${escapeHtml(fator)}</li>`).join("")
    : "<li>Nenhum fator de impacto informado.</li>";

  const componentes = Array.isArray(resultado.componentes) ? resultado.componentes : [];
  document.getElementById("componentes").innerHTML = componentes.length
    ? componentes.map(item => `
        <tr>
          <td>${escapeHtml(item.nome)}</td>
          <td class="mono">${escapeHtml(item.pontuacao)} / ${escapeHtml(item.pontuacaoMaxima)}</td>
          <td class="mono">${escapeHtml(Number(item.pesoPonderado ?? 0) * 100)}%</td>
          <td>${escapeHtml(item.motivo)}</td>
        </tr>
      `).join("")
    : '<tr><td colspan="4">Nenhum componente informado.</td></tr>';

  document.getElementById("jsonLogConteudo").textContent = JSON.stringify(resultado, null, 2);
}

async function carregarUltimaDecisao() {
  const connectionStatus = document.getElementById("connectionStatus");

  try {
    const response = await fetch("/api/v1/decisao/latest", { cache: "no-store" });
    if (!response.ok) throw new Error(`HTTP ${response.status}`);

    const data = await response.json();
    if (data.resultado) {
      connectionStatus.textContent = "Score recebido";
      connectionStatus.className = "connection ativo";
      renderResultado(data.resultado);
    } else {
      connectionStatus.textContent = "Aguardando Score";
      connectionStatus.className = "connection";
    }
  } catch (error) {
    connectionStatus.textContent = "Serviço indisponível";
    connectionStatus.className = "connection erro";
    console.error(error);
  }
}

document.getElementById("btnAtualizar").addEventListener("click", carregarUltimaDecisao);
carregarUltimaDecisao();
setInterval(carregarUltimaDecisao, 2000);
