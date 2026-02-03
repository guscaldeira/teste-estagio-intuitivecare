/*
 * SCRIPT DDL - CRIAÇÃO DA ESTRUTURA DO BANCO DE DADOS
 * Banco de Dados: MySQL 8.0+
 * Autor: Gustavo Caldeira (Candidato)
 *
 * DECISÕES TÉCNICAS E TRADE-OFFS (Conforme Solicitado):
 *
 * 1. Normalização (Opção B - Tabelas Normalizadas):
 * - Decisão: Separei os dados em 'operadoras' (Dimensão) e 'demonstracoes_contabeis' (Fato).
 * - Justificativa: Embora o volume de dados seja médio, a normalização garante integridade.
 * Se o endereço ou a modalidade de uma operadora mudar, atualizamos em apenas um lugar.
 * Isso evita anomalias de atualização e reduz redundância de texto (Strings).
 *
 * 2. Tipos de Dados (Dinheiro e Datas):
 * - Monetário: Uso de DECIMAL(15,2).
 * - Justificativa: FLOAT e DOUBLE possuem imprecisão de ponto flutuante. Para dados financeiros,
 * a precisão exata dos centavos é crítica.
 * - Datas: Uso de DATE.
 * Justificativa: Permite o uso de funções nativas de banco (YEAR, QUARTER, DATEDIFF) que
 * seriam impossíveis ou lentas se armazenadas como VARCHAR.
 */

-- Criação do Banco (Idempotente)
CREATE DATABASE IF NOT EXISTS teste_intuitive_care
CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

USE teste_intuitive_care;

-- 1. Tabela de Operadoras (Dados Cadastrais - Fonte: Relatorio_Cadop.csv)
-- Esta tabela armazena a entidade "Operadora" de forma única.
CREATE TABLE IF NOT EXISTS operadoras (
    registro_ans INT PRIMARY KEY,       -- Chave Primária (Identificador Único)
    cnpj VARCHAR(20) NOT NULL,          -- CNPJ formatado ou limpo
    razao_social VARCHAR(255) NOT NULL,
    nome_fantasia VARCHAR(255),
    modalidade VARCHAR(100),
    logradouro VARCHAR(255),
    numero VARCHAR(50),
    complemento VARCHAR(150),
    bairro VARCHAR(100),
    cidade VARCHAR(100),
    uf CHAR(2),
    cep VARCHAR(10),
    ddd VARCHAR(5),
    telefone VARCHAR(20),
    fax VARCHAR(20),                    -- Adicionado para compatibilidade com o CSV
    endereco_eletronico VARCHAR(150),
    representante VARCHAR(150),
    cargo_representante VARCHAR(100),
    data_registro_ans DATE,             
    
    INDEX idx_cnpj (cnpj)               -- Índice para buscas rápidas por CNPJ
);

-- 2. Tabela de Demonstrações Contábeis (Dados Financeiros - Fonte: consolidado.csv)
-- Esta é a tabela Fato, que contém os valores e movimentações.
CREATE TABLE IF NOT EXISTS demonstracoes_contabeis (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    registro_ans INT NOT NULL,
    data_referencia DATE NOT NULL,      -- Essencial para análises temporais (Trimestres)
    cd_conta_contabil VARCHAR(50) NOT NULL,
    descricao VARCHAR(255) NOT NULL,
    saldo_inicial DECIMAL(15,2),        -- DECIMAL para precisão financeira
    saldo_final DECIMAL(15,2) NOT NULL, -- DECIMAL para precisão financeira

    -- Chave Estrangeira para garantir integridade (Só aceita despesa de operadora que existe)
    CONSTRAINT fk_contabil_operadora
        FOREIGN KEY (registro_ans) REFERENCES operadoras(registro_ans)
        ON DELETE CASCADE,

    -- Índices para otimizar as queries analíticas solicitadas (Agrupamento por Data e Operadora)
    INDEX idx_data_ref (data_referencia),
    INDEX idx_reg_ans_data (registro_ans, data_referencia)
);