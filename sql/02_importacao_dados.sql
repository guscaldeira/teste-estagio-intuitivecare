/*
 * SCRIPT DML - IMPORTAÇÃO DE DADOS
 * Autor: Gustavo Caldeira
 *
 * INSTRUÇÕES PARA EXECUÇÃO:
 * 1. Certifique-se de que o 'local_infile' está ativado no seu servidor MySQL.
 * 2. Atualize os caminhos dos arquivos .csv abaixo ('C:/Users/...') para o diretório local onde os arquivos se encontram.
 */

USE teste_intuitive_care;

-- ==============================================================================
-- 1. IMPORTAR OPERADORAS (Relatorio_Cadop.csv)
-- ==============================================================================
LOAD DATA LOCAL INFILE 'C:/Users/PC/OneDrive/Documentos/MeusProjetos/teste-estagio-intuitivecare/Relatorio_Cadop.csv'
INTO TABLE operadoras
CHARACTER SET latin1
FIELDS TERMINATED BY ';'
ENCLOSED BY '"'
LINES TERMINATED BY '\r\n'
IGNORE 1 ROWS
(registro_ans, cnpj, razao_social, nome_fantasia, modalidade, logradouro, numero, complemento, bairro, cidade, uf, cep, ddd, telefone, fax, endereco_eletronico, representante, cargo_representante, @data_reg_temp)
SET 
    -- Converte a data de "dd/mm/aaaa" para "aaaa-mm-dd"
    data_registro_ans = STR_TO_DATE(@data_reg_temp, '%d/%m/%Y'),
    -- Remove pontos, barras e traços do CNPJ para deixar apenas números
    cnpj = REPLACE(REPLACE(REPLACE(cnpj, '.', ''), '/', ''), '-', '');

-- ==============================================================================
-- 2. IMPORTAR DEMONSTRAÇÕES CONTÁBEIS (consolidado.csv)
-- ==============================================================================
LOAD DATA LOCAL INFILE 'C:/Users/PC/OneDrive/Documentos/MeusProjetos/teste-estagio-intuitivecare/consolidado.csv'
INTO TABLE demonstracoes_contabeis
CHARACTER SET utf8mb4
FIELDS TERMINATED BY ';'
LINES TERMINATED BY '\n'
IGNORE 1 ROWS
(@razao_social_dummy, registro_ans, @trimestre_temp, @ano_temp, @valor_despesas)
SET
    -- Cria a data de referência baseada no Trimestre e Ano (Ex: 1T2023 -> 2023-03-31)
    data_referencia = STR_TO_DATE(
        CONCAT(
            @ano_temp, '-',
            CASE 
                WHEN @trimestre_temp LIKE '1%' THEN '03-31'
                WHEN @trimestre_temp LIKE '2%' THEN '06-30'
                WHEN @trimestre_temp LIKE '3%' THEN '09-30'
                WHEN @trimestre_temp LIKE '4%' THEN '12-31'
                ELSE '01-01'
            END
        ), '%Y-%m-%d'
    ),
    
    -- Define valores padrão para as colunas que não existem neste CSV específico
    cd_conta_contabil = 'EVENTOS_SINISTROS',
    descricao = 'Despesas Assistenciais Consolidadas',
    saldo_inicial = 0,
    
    -- Trata o formato do dinheiro (Troca vírgula por ponto para o padrão Decimal SQL)
    saldo_final = CAST(REPLACE(@valor_despesas, ',', '.') AS DECIMAL(15,2));

/* * NOTA: A importação da tabela 'despesas_agregadas' foi removida pois optou-se
 * pela normalização dos dados e execução das agregações via Query SQL (Arquivo 03).
 */