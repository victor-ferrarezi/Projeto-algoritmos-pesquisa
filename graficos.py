from pathlib import Path
import csv
import sys

try:
    import matplotlib.pyplot as plt
except ImportError:
    print("A biblioteca matplotlib não está instalada.")
    print("Instale com: python -m pip install matplotlib")
    sys.exit(1)

PASTA = Path(__file__).resolve().parent
RESULTADOS = PASTA / "resultados"
GRAFICOS = PASTA / "graficos"


def ler_csv(nome):
    caminho = RESULTADOS / nome
    if not caminho.exists():
        raise FileNotFoundError(f"Arquivo não encontrado: {caminho}")
    with caminho.open("r", encoding="utf-8-sig", newline="") as arquivo:
        return list(csv.DictReader(arquivo))


def salvar(figura, nome):
    GRAFICOS.mkdir(parents=True, exist_ok=True)
    figura.tight_layout()
    figura.savefig(GRAFICOS / nome, dpi=160, bbox_inches="tight")
    plt.close(figura)
    print(f"Gráfico criado: graficos/{nome}")


def numero(linha, campo):
    try:
        return float(linha[campo])
    except (KeyError, TypeError, ValueError):
        return float("nan")


def grafico_buscas():
    dados = ler_csv("resultados_busca.csv")
    fig, ax = plt.subplots(figsize=(10, 6))
    algoritmos = sorted({r["algoritmo"] for r in dados})
    for algoritmo in algoritmos:
        grupo = [r for r in dados if r["algoritmo"] == algoritmo
                 and r["posicao"] == "fora" and r["estado_base"] == "ordenada"]
        grupo.sort(key=lambda r: int(r["n"]))
        if grupo:
            ax.plot([int(r["n"]) for r in grupo],
                    [numero(r, "tempo_medio_ns") / 1_000_000 for r in grupo],
                    marker="o", label=algoritmo)
    ax.set_title("Tempo médio dos algoritmos de busca")
    ax.set_xlabel("Quantidade de registros")
    ax.set_ylabel("Tempo médio (ms)")
    ax.legend()
    ax.grid(True, alpha=0.3)
    salvar(fig, "01_tempo_buscas.png")


def grafico_comparacoes():
    dados = ler_csv("resultados_busca.csv")
    fig, ax = plt.subplots(figsize=(10, 6))
    algoritmos = sorted({r["algoritmo"] for r in dados})
    for algoritmo in algoritmos:
        grupo = [r for r in dados if r["algoritmo"] == algoritmo
                 and r["posicao"] == "fora" and r["estado_base"] == "ordenada"]
        grupo.sort(key=lambda r: int(r["n"]))
        if grupo:
            ax.plot([int(r["n"]) for r in grupo],
                    [numero(r, "comparacoes") for r in grupo],
                    marker="o", label=algoritmo)
    ax.set_title("Quantidade de comparações nas buscas")
    ax.set_xlabel("Quantidade de registros")
    ax.set_ylabel("Número de comparações")
    ax.legend()
    ax.grid(True, alpha=0.3)
    salvar(fig, "02_comparacoes_buscas.png")


def grafico_ordenacao():
    dados = ler_csv("resultados_ordenacao.csv")
    fig, ax = plt.subplots(figsize=(11, 6))
    for algoritmo in sorted({r["algoritmo"] for r in dados}):
        grupo = [r for r in dados if r["algoritmo"] == algoritmo
                 and r["estado_inicial"] == "aleatorio"
                 and r["status"] == "executado"]
        grupo.sort(key=lambda r: int(r["n"]))
        if grupo:
            ax.plot([int(r["n"]) for r in grupo],
                    [numero(r, "tempo_medio_ms") for r in grupo],
                    marker="o", label=algoritmo)
    ax.set_title("Tempo médio dos algoritmos de ordenação")
    ax.set_xlabel("Quantidade de registros")
    ax.set_ylabel("Tempo médio (ms)")
    ax.legend()
    ax.grid(True, alpha=0.3)
    salvar(fig, "03_tempo_ordenacao.png")


def grafico_consultas():
    dados = ler_csv("resultados_muitas_consultas.csv")
    dados.sort(key=lambda r: int(r["n_buscas"]))
    fig, ax = plt.subplots(figsize=(10, 6))
    x = [int(r["n_buscas"]) for r in dados]
    ax.plot(x, [numero(r, "sistemaA_media_ms") for r in dados],
            marker="o", label="Busca sequencial")
    ax.plot(x, [numero(r, "sistemaB_media_ms") for r in dados],
            marker="o", label="Ordenação + busca binária")
    ax.set_title("Tempo total conforme a quantidade de consultas")
    ax.set_xlabel("Quantidade de consultas")
    ax.set_ylabel("Tempo total (ms)")
    ax.legend()
    ax.grid(True, alpha=0.3)
    salvar(fig, "04_muitas_consultas.png")


def grafico_estados():
    dados = ler_csv("resultados_ordenacao.csv")
    algoritmos = sorted({r["algoritmo"] for r in dados})
    estados = ["aleatorio", "ordenado", "inverso"]
    fig, ax = plt.subplots(figsize=(11, 6))
    largura = 0.24
    posicoes = list(range(len(algoritmos)))
    for indice, estado in enumerate(estados):
        valores = []
        for algoritmo in algoritmos:
            linha = next((r for r in dados if r["algoritmo"] == algoritmo
                          and r["n"] == "10000"
                          and r["estado_inicial"] == estado
                          and r["status"] == "executado"), None)
            valores.append(numero(linha, "tempo_medio_ms") if linha else 0)
        deslocamento = (indice - 1) * largura
        ax.bar([p + deslocamento for p in posicoes], valores,
               width=largura, label=estado)
    ax.set_title("Ordenação conforme o estado inicial da base (10.000 registros)")
    ax.set_xlabel("Algoritmo")
    ax.set_ylabel("Tempo médio (ms)")
    ax.set_xticks(posicoes)
    ax.set_xticklabels(algoritmos, rotation=25, ha="right")
    ax.legend(title="Estado inicial")
    ax.grid(True, axis="y", alpha=0.3)
    salvar(fig, "05_estado_inicial.png")


def grafico_insercoes():
    dados = ler_csv("resultados_insercao_continua.csv")
    estrategias = sorted({r["estrategia"] for r in dados})
    fig, ax = plt.subplots(figsize=(10, 6))
    for estrategia in estrategias:
        grupo = [r for r in dados if r["estrategia"] == estrategia]
        grupo.sort(key=lambda r: int(r["insercoes"]))
        ax.plot([int(r["insercoes"]) for r in grupo],
                [numero(r, "tempo_medio_ms") for r in grupo],
                marker="o", label=estrategia.replace("_", " "))
    ax.set_title("Tempo de inserções contínuas")
    ax.set_xlabel("Quantidade de inserções")
    ax.set_ylabel("Tempo médio (ms)")
    ax.legend()
    ax.grid(True, alpha=0.3)
    salvar(fig, "06_insercoes_continuas.png")


def main():
    GRAFICOS.mkdir(parents=True, exist_ok=True)
    tarefas = [
        grafico_buscas,
        grafico_comparacoes,
        grafico_ordenacao,
        grafico_consultas,
        grafico_estados,
        grafico_insercoes,
    ]
    for tarefa in tarefas:
        tarefa()
    print("\nOs seis gráficos foram gerados com sucesso.")


if __name__ == "__main__":
    try:
        main()
    except (FileNotFoundError, KeyError) as erro:
        print(f"Não foi possível gerar os gráficos: {erro}")
        print("Execute primeiro os experimentos pelo menu do programa.")
        sys.exit(1)
