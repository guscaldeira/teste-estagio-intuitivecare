# Teste Técnico - Intuitive Care

Este repositório contém a solução para o teste técnico de desenvolvimento Java. O projeto foi estruturado para resolver as questões propostas de forma modular, limpa e escalável.

##  Índice
- [Questão 1: Web Scraping e ETL da ANS](#-questão-1-web-scraping-e-etl-da-ans)
- [Questão 2: Transformação de Dados e Teste de Desempenho](#questão-2-transformação-de-dados-e-teste-de-desempenho)
- [Questão 3: Banco de Dados e Análise de Dados](#questão-3-banco-de-dados-e-análise-de-dados)
- [Questão 4: Aplicação Full Stack (Dashboard ANS)](#-questão-4-aplicação-full-stack-dashboard-ans)

---

##  Questão 1: Web Scraping e ETL da ANS

### Objetivo
Desenvolver uma solução que acesse o site da ANS, identifique os arquivos de "Demonstrações Contábeis" mais recentes, realize o download, extração e transformação dos dados (ETL).

### Funcionalidades Implementadas
1.  **Busca Dinâmica (Scraping):** O sistema não possui anos "chumbados" no código. Ele varre o diretório da ANS e identifica automaticamente os anos e trimestres mais recentes.
2.  **Download Resiliente:** Uso de timeouts e tratamento de erros para baixar os arquivos `.zip`.
3.  **Processamento Otimizado (Stream):** Leitura dos arquivos CSV linha a linha para evitar estouro de memória (OutOfMemory), tratando encoding `ISO-8859-1` e removendo caracteres BOM.
4.  **Consolidação:** Filtra apenas as despesas de "Eventos/Sinistros" e gera um arquivo único `consolidado.csv`.
5.  **Entrega:** Compacta o resultado final em `consolidado_despesas.zip`.

###  Tecnologias e Bibliotecas
* **Java 11+**
* **Jsoup:** Para navegação e parsing HTML.
* **Apache Commons IO:** Para gerenciamento de arquivos e streams.
* **Apache Commons CSV:** Para leitura e escrita robusta de arquivos CSV.

###  Como Executar
Execute a classe `Main.java` localizada no pacote `com.intuitivecare.questao1`.
O fluxo será:
1.  Console exibirá o progresso da busca e download.
2.  Uma pasta `downloads/` será criada na raiz.
3.  O arquivo final `consolidado_despesas.zip` será gerado na raiz do projeto.

---

## Questão 2: Transformação de Dados e Teste de Desempenho

###  Objetivo
Transformar os dados consolidados da etapa anterior, enriquecendo-os com informações cadastrais oficiais da ANS, realizando cálculos estatísticos complexos e gerando uma saída estruturada e compactada.

###  Funcionalidades Implementadas
1.  **Validação Cruzada:** Valida as operadoras não apenas matematicamente, mas cruzando contra a base oficial de operadoras ativas da ANS (`Relatorio_Cadop.csv`).
2.  **Enriquecimento de Dados (Join):** Adiciona `RegistroANS`, `Modalidade` e `UF` ao CSV original.
3.  **Cálculos Estatísticos:** Calcula Soma Total, Média Trimestral e Desvio Padrão das despesas.
4.  **Ordenação Eficiente:** Ordena os resultados do maior custo para o menor.
5.  **Compactação Automática:** Gera o arquivo final `.zip` conforme solicitado.

###  Decisões Técnicas e Trade-offs (Justificativas)

O desafio exigiu escolhas arquiteturais específicas. Abaixo, detalho as decisões tomadas conforme solicitado no enunciado:

> **1. Estratégia de Validação de CNPJs/Operadoras**
> * **Decisão:** Utilizar validação por **Lookup na Base Oficial** (Join) em vez de apenas validação algorítmica de CNPJ.
> * **Justificativa:** Um CNPJ pode ser matematicamente válido (dígitos verificadores corretos) mas pertencer a uma operadora falida ou suspensa. Ao cruzar os dados com o `Relatorio_Cadop.csv` (Operadoras Ativas), garantimos a integridade do negócio, descartando registros que não possuem correspondência ativa na ANS. Isso é mais robusto para o contexto regulatório.

> **2. Estratégia de Processamento e Join**
> * **Decisão:** Join em Memória via `HashMap`.
> * **Justificativa:** O arquivo de cadastro (`Relatorio_Cadop.csv`) é pequeno (1.200 linhas). Carregá-lo inteiramente em um `HashMap<String, String[]>` consome memória insignificante e permite acesso O(1) instantâneo.
> * Comparado a um banco de dados (que adicionaria latência de I/O) ou loops aninhados (que teria complexidade O(N*M)), o uso de HashMaps permitiu processar as 170.000 linhas do arquivo principal em milissegundos.

> **3. Estratégia de Ordenação**
> * **Decisão:** Ordenação em Memória (`Collections.sort`) pós-agregação.
> * **Justificativa:** A ordenação foi solicitada no resultado **agregado** (agrupado por Operadora). Mesmo processando milhões de linhas de despesas, o resultado final (número de operadoras únicas) é pequeno (< 2.000 registros). Ordenar uma lista desse tamanho em memória é computacionalmente barato e não justifica o uso de algoritmos de ordenação externa (External Merge Sort).

###  Como Executar
1.  Certifique-se de que o arquivo `consolidado.csv` (gerado na Questão 1) e o arquivo `Relatorio_Cadop.csv` (baixado da ANS) estejam na raiz do projeto.
2.  Execute a classe `Main.java` localizada no pacote `com.intuitivecare.questao2`.
3.  O sistema irá processar os dados e gerar:
    * `despesas_agregadas.csv`: Relatório detalhado.
    * `Teste_{Gustavo_Caldeira}.zip`: Arquivo final para entrega.

## Questão 3: Banco de Dados e Análise de Dados

###  Objetivo
Estruturar os dados processados nas etapas anteriores em um banco de dados relacional e desenvolver queries analíticas complexas para extração de insights.

###  Decisões Técnicas e Trade-offs (Justificativas)

> **1. Normalização de Tabelas (Opção B)**
> * **Decisão:** Optei por tabelas normalizadas separadas (Operadoras, Despesas Consolidadas e Despesas Agregadas).
> * **Justificativa:** Dado o volume de dados e a baixa frequência de atualização cadastral vs. alta frequência de lançamentos financeiros, a normalização evita redundância, garante a integridade referencial e facilita a manutenção do esquema a longo prazo.

> **2. Tipos de Dados para Valores Monetários**
> * **Decisão:** Uso de `DECIMAL(15,2)` para campos de custo.
> * **Justificativa:** Evita erros de arredondamento comuns em tipos de ponto flutuante (`FLOAT`), garantindo precisão absoluta para cálculos financeiros e auditoria.

###  Consultas Analíticas Implementadas
As queries foram desenvolvidas para resolver os seguintes desafios de negócio:
1. **Crescimento Percentual:** Identificação das 5 operadoras com maior variação de despesas entre o primeiro e o último trimestre, tratando casos de operadoras sem dados em todos os períodos para evitar divisões por zero ou resultados nulos.
2. **Distribuição Geográfica:** Listagem dos 5 estados com maiores despesas totais, incluindo o cálculo da média de gastos por operadora em cada UF.
3. **Análise de Performance:** Filtro de operadoras que mantiveram despesas acima da média geral em pelo menos 2 dos 3 trimestres analisados.

###  Como Executar
1. **Ambiente:** Certifique-se de ter um servidor **MySQL (8.0+)** ou **PostgreSQL (10+)** instalado.
2. **Importação:**
   - Crie um banco de dados chamado `ans_despesas`.
   - Execute o script DDL localizado em `/sql/setup_tabelas.sql` para criar a estrutura.
   - Utilize o comando `LOAD DATA INFILE` ou a ferramenta de importação da sua IDE (DBeaver/Workbench) para carregar os arquivos CSV gerados nas Questões 1 e 2.
3. **Análise:** Execute o arquivo `/sql/queries_analiticas.sql` para visualizar os resultados dos desafios de crescimento percentual e médias por UF.

---

## Questão 4: Aplicação Full Stack (Dashboard ANS)

### ### Objetivo
Desenvolver uma interface web para visualização dos dados das operadoras, permitindo busca, paginação e análise visual por meio de gráficos.

### ### Funcionalidades Implementadas
1. **API RESTful:** Backend em Python para fornecimento de dados em tempo real.
2. **Dashboard Dinâmico:** Interface Vue.js com filtragem instantânea por CNPJ ou Razão Social.
3. **Visualização Analítica:** Gráfico de pizza/barras mostrando a distribuição de operadoras por estado (UF).
4. **Paginação Inteligente:** Navegação de 10 em 10 registros para otimizar o carregamento.

### ### Decisões Técnicas e Trade-offs (Justificativas)

> **1. Framework Backend: FastAPI**
> * **Decisão:** Uso do FastAPI em vez de Flask ou Django.
> * **Justificativa:** O FastAPI oferece validação automática de dados e performance superior, além de simplificar a integração com o Frontend através do suporte nativo a CORS.

> **2. Estratégia de Busca: Filtro no Cliente**
> * **Decisão:** A busca é processada no Frontend após o carregamento da lista.
> * **Justificativa:** Para um volume de ~1.100 registros, o processamento no navegador é instantâneo, reduzindo a latência e o número de requisições desnecessárias ao servidor.

> **3. Paginação: Offset-based**
> * **Decisão:** Uso de parâmetros `page` e `limit`.
> * **Justificativa:** É a forma mais robusta de garantir que o usuário consiga navegar por grandes datasets sem comprometer a memória do navegador.

### ### Como Executar

**Backend (Python):**
1. Acesse a pasta `cd backend`.
2. Ative o ambiente virtual e execute: `python -m uvicorn main:app --reload`.

**Frontend (Vue.js):**
1. Acesse a pasta `cd frontend`.
2. Instale as dependências: `npm install`.
3. Inicie o servidor: `npm run dev`.

---

## 👨‍💻 Autor
Desenvolvido por **Gustavo Caldeira** como parte do processo seletivo da **Intuitive Care**.