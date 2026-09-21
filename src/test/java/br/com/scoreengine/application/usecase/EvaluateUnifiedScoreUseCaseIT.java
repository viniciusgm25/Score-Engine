package br.com.scoreengine.application.usecase;

import br.com.scoreengine.application.dto.UnifiedScoreOutput;
import br.com.scoreengine.domain.model.common.CustomerType;
import br.com.scoreengine.domain.model.common.ScoreOrigin;
import br.com.scoreengine.domain.model.pf.CustomerPFProfile;
import br.com.scoreengine.domain.model.pj.CustomerPJProfile;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
class EvaluateUnifiedScoreUseCaseIT {

    @Autowired
    private EvaluateUnifiedScoreUseCase useCase;

    @Test
    @DisplayName("Deve calcular na primeira chamada e reutilizar o score no banco na segunda chamada (Idempotência)")
    void testCicloConsultaEReutilizacao() {
        CustomerPFProfile profile = new CustomerPFProfile();
        profile.setCpf("98765432100");
        profile.setRendaMensal(new BigDecimal("7000.00"));
        profile.setDividaTotal(new BigDecimal("800.00"));
        profile.setIdade(35);
        profile.setEstadoCivil("CASADO");
        profile.setNumeroDependentes(1);
        profile.setDiasAtrasoUltimos12Meses(0);
        profile.setLimiteRotativoUtilizado(new BigDecimal("100.00"));
        profile.setLimiteRotativoTotal(new BigDecimal("5000.00"));
        profile.setMesesNoEmpregoAtual(36);
        profile.setMesesRelacionamentoBanco(48);

        // 1ª Chamada: Não existe histórico prévio -> Executa cálculo (flag false para
        // default sem override)
        UnifiedScoreOutput primeira = useCase.executePF(profile, false);
        assertEquals(ScoreOrigin.CALCULO, primeira.origem());
        assertNotNull(primeira.scoreResult());

        // 2ª Chamada com os mesmos identificadores -> Recupera do banco dentro do TTL
        UnifiedScoreOutput segunda = useCase.executePF(profile, false);
        assertEquals(ScoreOrigin.BANCO, segunda.origem());
        assertEquals(primeira.scoreResult().scoreFinal(), segunda.scoreResult().scoreFinal());
    }

    @Test
    @DisplayName("Garante isolamento estrito: Busca PF não colide nem reaproveita registros PJ")
    void testIsolamentoPFePJ() {
        String docComum = "11122233344";

        CustomerPFProfile pf = new CustomerPFProfile();
        pf.setCpf(docComum);
        pf.setRendaMensal(new BigDecimal("5000.00"));
        pf.setDividaTotal(new BigDecimal("500.00"));
        pf.setIdade(29);
        pf.setEstadoCivil("SOLTEIRO");
        pf.setNumeroDependentes(0);
        pf.setDiasAtrasoUltimos12Meses(0);
        pf.setLimiteRotativoUtilizado(new BigDecimal("200.00"));
        pf.setLimiteRotativoTotal(new BigDecimal("2000.00"));
        pf.setMesesNoEmpregoAtual(24);
        pf.setMesesRelacionamentoBanco(24);

        CustomerPJProfile pj = new CustomerPJProfile();
        pj.setCnpj(docComum);
        pj.setRazaoSocial("Teste Isolamento");
        pj.setFaturamentoMensal(new BigDecimal("50000.00"));
        pj.setDespesasOperacionaisMensais(new BigDecimal("30000.00"));
        pj.setPassivoTotalBancario(new BigDecimal("10000.00"));
        pj.setFaturamentoBrutoAnual(new BigDecimal("600000.00"));
        pj.setDiasAtrasoUltimos12Meses(0);
        pj.setMesesConstituicao(24);
        pj.setSetorAtuacao("COMERCIO");

        UnifiedScoreOutput resPF = useCase.executePF(pf, false);
        assertEquals(CustomerType.PF, resPF.tipoPessoa());
        assertEquals("SCORE_PF", resPF.scoreResult().modelVersion().equals("v1.1.0") ? "SCORE_PF" : "");

        UnifiedScoreOutput resPJ = useCase.executePJ(pj, false);
        assertEquals(CustomerType.PJ, resPJ.tipoPessoa());
        assertEquals(ScoreOrigin.CALCULO, resPJ.origem(),
                "PJ com mesmo identificador deve disparar cálculo próprio sem reaproveitar PF.");
    }
}