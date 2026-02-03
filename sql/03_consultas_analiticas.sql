/*
 * SCRIPT DQL - CONSULTAS ANALÍTICAS AVANÇADAS (Conforme Teste 3.4)
 * Autor: Gustavo Caldeira
 *
 * Objetivo: Responder às perguntas complexas de negócio solicitadas.
 */

USE teste_intuitive_care;

-- ==============================================================================
-- QUERY 1: Quais as 5 operadoras com maior crescimento percentual de despesas 
--          entre o primeiro e o último trimestre analisado?
--
-- Desafio: Tratamento de dados faltantes (Operadoras sem dados no 1º ou último tri).
-- Solução: Utilizamos INNER JOIN para garantir que só calculamos quem tem dados 
--          em AMBOS os períodos (consistência).
-- ==============================================================================
WITH limites_temporais AS (
    SELECT MIN(data_referencia) as primeiro_tri, MAX(data_referencia) as ultimo_tri 
    FROM demonstracoes_contabeis
),
despesas_inicio AS (
    SELECT registro_ans, SUM(saldo_final) as valor_inicial
    FROM demonstracoes_contabeis
    WHERE data_referencia = (SELECT primeiro_tri FROM limites_temporais)
    GROUP BY registro_ans
),
despesas_fim AS (
    SELECT registro_ans, SUM(saldo_final) as valor_final
    FROM demonstracoes_contabeis
    WHERE data_referencia = (SELECT ultimo_tri FROM limites_temporais)
    GROUP BY registro_ans
)
SELECT 
    o.registro_ans,
    o.razao_social,
    FORMAT(i.valor_inicial, 2, 'pt_BR') as despesa_inicial,
    FORMAT(f.valor_final, 2, 'pt_BR') as despesa_final,
    -- Cálculo: ((Final - Inicial) / Inicial) * 100
    CONCAT(ROUND(((f.valor_final - i.valor_inicial) / i.valor_inicial) * 100, 2), '%') as crescimento_pct
FROM operadoras o
JOIN despesas_inicio i ON o.registro_ans = i.registro_ans
JOIN despesas_fim f ON o.registro_ans = f.registro_ans
ORDER BY ((f.valor_final - i.valor_inicial) / i.valor_inicial) DESC
LIMIT 5;

-- ==============================================================================
-- QUERY 2: Qual a distribuição de despesas por UF? Liste os 5 estados com 
--          maiores despesas totais.
--
-- Desafio Adicional: Calcular também a média de despesas por operadora em cada UF.
-- ==============================================================================
SELECT 
    o.uf,
    -- Total de despesas no estado
    FORMAT(SUM(d.saldo_final), 2, 'pt_BR') AS despesa_total_estado,
    -- Média de despesas por operadora naquele estado
    FORMAT(AVG(d.saldo_final), 2, 'pt_BR') AS media_por_operadora,
    -- Contagem de operadoras distintas para auditoria
    COUNT(DISTINCT o.registro_ans) as qtd_operadoras
FROM demonstracoes_contabeis d
JOIN operadoras o ON d.registro_ans = o.registro_ans
WHERE o.uf IS NOT NULL AND o.uf != '' -- Filtra estados inválidos
GROUP BY o.uf
ORDER BY SUM(d.saldo_final) DESC
LIMIT 5;

-- ==============================================================================
-- QUERY 3: Quantas operadoras tiveram despesas acima da média geral em 
--          pelo menos 2 dos 3 trimestres analisados?
--
-- Trade-off Técnico: Uso de CTEs (Common Table Expressions) para legibilidade 
-- e manutenção, em vez de subqueries aninhadas complexas.
-- ==============================================================================
WITH media_geral_por_trimestre AS (
    -- 1. Calcula a média geral de despesas de TODAS as operadoras por trimestre
    SELECT data_referencia, AVG(saldo_final) as media_geral
    FROM demonstracoes_contabeis
    GROUP BY data_referencia
),
performance_operadoras AS (
    -- 2. Cruza as despesas da operadora com a média daquele trimestre
    SELECT 
        d.registro_ans,
        d.data_referencia,
        CASE 
            WHEN d.saldo_final > m.media_geral THEN 1 
            ELSE 0 
        END as acima_da_media
    FROM demonstracoes_contabeis d
    JOIN media_geral_por_trimestre m ON d.data_referencia = m.data_referencia
),
contagem_performance AS (
    -- 3. Soma quantas vezes cada operadora ficou acima da média
    SELECT registro_ans, SUM(acima_da_media) as trimestres_acima
    FROM performance_operadoras
    GROUP BY registro_ans
)
-- 4. Resultado Final: Conta quantas operadoras cumpriram o requisito (>= 2 trimestres)
SELECT COUNT(*) as qtd_operadoras_consistentes
FROM contagem_performance
WHERE trimestres_acima >= 2;