from fastapi import FastAPI, Query
from fastapi.middleware.cors import CORSMiddleware
import pandas as pd

app = FastAPI()

# Configuração de CORS: Essencial para que o Frontend (Vue) consiga 
# acessar os dados deste servidor Python sem ser bloqueado pelo navegador.
app.add_middleware(
    CORSMiddleware,
    allow_origins=["*"],
    allow_methods=["*"],
    allow_headers=["*"],
)

def carregar_csv(nome_arquivo):
    # Tenta ler com ';' primeiro, se der erro tenta ',' 
    try:
        df = pd.read_csv(nome_arquivo, sep=";", encoding="latin-1")
    except:
        df = pd.read_csv(nome_arquivo, sep=",", encoding="latin-1")
    return df

@app.get("/api/operadoras")
def listar_operadoras(page: int = 1, limit: int = 10):
    df = carregar_csv("Relatorio_Cadop.csv")
    start = (page - 1) * limit
    end = start + limit
    # Retorna dados + metadados (Questão 4.2.4 - Opção B)
    return {
        "data": df.iloc[start:end].fillna("").to_dict(orient="records"),
        "total": len(df),
        "page": page,
        "limit": limit
    }

@app.get("/api/operadoras/{cnpj}/despesas")
def historico_despesas(cnpj: str):
    # Lendo o arquivo consolidado que foi movido para a pasta
    df_despesas = carregar_csv("consolidado.csv")
    # Filtra onde o CNPJ contém o valor digitado
    resultado = df_despesas[df_despesas.astype(str).apply(lambda x: x.str.contains(cnpj)).any(axis=1)]
    return resultado.fillna("").to_dict(orient="records")

@app.get("/api/estatisticas")
def obter_estatisticas():
    df = carregar_csv("Relatorio_Cadop.csv")
    return {
        "total_operadoras": len(df),
        "modalidades": df['Modalidade'].value_counts().to_dict(),
        "top_5_estados": df['UF'].value_counts().head(5).to_dict()
    }