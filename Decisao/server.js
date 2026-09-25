const http = require("http");
const fs = require("fs");
const path = require("path");
const { processarResultadoScore } = require("./src/auditoria");

const PORT = Number(process.env.DECISION_PORT || 8081);
const HOST = process.env.DECISION_HOST || "0.0.0.0";
const PUBLIC_DIR = __dirname;

let ultimaDecisao = null;

function enviarJson(res, statusCode, payload) {
  const body = JSON.stringify(payload);

  res.writeHead(statusCode, {
    "Content-Type": "application/json; charset=utf-8",
    "Content-Length": Buffer.byteLength(body),
  });

  res.end(body);
}

function servirPagina(res) {
  const arquivo = path.join(PUBLIC_DIR, "index.html");

  if (!fs.existsSync(arquivo)) {
    return enviarJson(res, 404, {
      status: "NOT_FOUND",
      mensagem: "index.html não encontrado.",
    });
  }

  const html = fs.readFileSync(arquivo, "utf8");

  res.writeHead(200, {
    "Content-Type": "text/html; charset=utf-8",
  });

  res.end(html);
}

function servirArquivo(res, arquivo, contentType) {
  const caminho = path.join(PUBLIC_DIR, arquivo);

  if (!fs.existsSync(caminho)) {
    return enviarJson(res, 404, {
      status: "NOT_FOUND",
      mensagem: `Arquivo ${arquivo} não encontrado.`,
    });
  }

  const conteudo = fs.readFileSync(caminho);

  res.writeHead(200, {
    "Content-Type": contentType,
  });

  res.end(conteudo);
}

function lerJson(req) {
  return new Promise((resolve, reject) => {
    let body = "";

    req.on("data", (chunk) => {
      body += chunk;

      if (body.length > 1_000_000) {
        reject(new Error("Payload excede o limite de 1 MB."));
        req.destroy();
      }
    });

    req.on("end", () => {
      try {
        resolve(JSON.parse(body || "{}"));
      } catch {
        reject(new Error("Payload JSON inválido."));
      }
    });

    req.on("error", reject);
  });
}

const server = http.createServer(async (req, res) => {
  try {
    // Página principal
    if (req.method === "GET" && req.url === "/") {
      return servirPagina(res);
    }

    // CSS
    if (req.method === "GET" && req.url === "/styles.css") {
      return servirArquivo(
        res,
        "styles.css",
        "text/css; charset=utf-8"
      );
    }
    // Configuração da Swagger ui
    if (req.method === "GET" && req.url === "/swagger") {
      return servirArquivo(
          res,
          "swagger.html",
          "text/html; charset=utf-8"
      );
  }
  if (req.method === "GET" && req.url === "/openapi.yaml") {
      return servirArquivo(
          res,
          "openapi.yaml",
          "text/yaml; charset=utf-8"
      );
  }
    // JavaScript da interface
    if (req.method === "GET" && req.url === "/script.js") {
      return servirArquivo(
        res,
        "script.js",
        "application/javascript; charset=utf-8"
      );
    }

    // Health check
    if (
      req.method === "GET" &&
      req.url === "/api/v1/decisao/health"
    ) {
      return enviarJson(res, 200, {
        status: "UP",
        servico: "Decision",
        porta: PORT,
      });
    }

    // Última decisão recebida
    if (
      req.method === "GET" &&
      req.url === "/api/v1/decisao/latest"
    ) {
      return enviarJson(res, 200, {
        status: "OK",
        resultado: ultimaDecisao,
      });
    }

    // Resultado enviado pelo Score
    if (
      req.method === "POST" &&
      req.url === "/api/v1/decisao"
    ) {
      const scoreResult = await lerJson(req);

      ultimaDecisao = processarResultadoScore(scoreResult);

      console.log(
        `[DECISAO] Cliente ${ultimaDecisao.clienteId} | ` +
        `Score ${ultimaDecisao.scoreFinal} | ` +
        `${ultimaDecisao.decisao}`
      );

      return enviarJson(res, 200, ultimaDecisao);
    }

    // Rota inexistente
    return enviarJson(res, 404, {
      status: "NOT_FOUND",
      mensagem: "Rota não encontrada.",
    });

  } catch (error) {
    console.error("[DECISAO] Erro:", error.message);

    return enviarJson(res, 400, {
      status: "ERRO",
      mensagem: error.message,
    });
  }
});

server.listen(PORT, HOST, () => {
  console.log(
    `[DECISAO] Serviço iniciado em http://localhost:${PORT}`
  );

  console.log(
    `[DECISAO] Recebendo Score em POST /api/v1/decisao`
  );
});