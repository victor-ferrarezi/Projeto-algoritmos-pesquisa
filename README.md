# Projeto de análise de algoritmos

Este projeto compara algoritmos de busca e ordenação usando bases de clientes em arquivos CSV. Os testes são feitos em Java e os gráficos são gerados com Python.

## O que precisa estar instalado

- JDK 17 ou mais recente.
- Python 3.
- Biblioteca `matplotlib` do Python.

Para instalar a biblioteca, use:

```bash
python -m pip install matplotlib
```

No Windows, também pode ser necessário usar `py` no lugar de `python`.

## Bases de dados

A pasta `dados/` contém as duas bases usadas no trabalho: `base_1000.csv` (1.000 registros) e `base_10000.csv` (10.000 registros). Cada arquivo possui as colunas `id`, `nome`, `idade` e `cidade`. O campo `id` deve ser inteiro e não pode se repetir.

Na preparação, o programa reconhece a coluna `id` e cria versões ordenadas e embaralhadas de cada base. As demais colunas são mantidas no CSV, mas os algoritmos de busca e ordenação trabalham com os IDs.

## Como executar

### Windows

1. Extraia o ZIP para uma pasta.
2. Abra a pasta e execute `executar.bat`.
3. Quando o menu aparecer, escolha a opção desejada. Para rodar os experimentos completos, use a opção 5.

### Linux ou macOS

No terminal, entre na pasta do projeto e execute:

```bash
./executar.sh
```

O script compila os arquivos Java e inicia o menu. É necessário ter o JDK configurado no PATH.

## Experimentos e resultados

A opção de execução rápida serve para conferir se o programa está funcionando. Para coletar os dados do trabalho, use a execução completa.

Os resultados são gravados em CSV na pasta `resultados/`. Depois de executar os testes, use a opção de resumo para gerar `resumo_experimentos.txt` e a opção de gráficos para criar as imagens na pasta `graficos/`.

São gerados gráficos de tempo e número de comparações das buscas, tempo das ordenações, custo de várias consultas, influência da ordem inicial da base e estratégias de inserção.

Alguns algoritmos de ordenação mais lentos não são executados com as bases maiores, pois o tempo poderia ficar muito alto. Esses casos são marcados como não executados nos resultados; o programa não inventa tempos para eles.

Os tempos dependem do computador e das condições em que os testes forem executados. Por isso, os valores obtidos na apresentação podem ser diferentes dos resultados de outra máquina.

## Comandos pelo terminal

Depois de compilar, também é possível chamar o programa diretamente:

```bash
java -cp bin com.algoritmos.App --testar
java -cp bin com.algoritmos.App --preparar
java -cp bin com.algoritmos.App --rapido
java -cp bin com.algoritmos.App --completo
java -cp bin com.algoritmos.App --resumo
java -cp bin com.algoritmos.App --graficos
```
