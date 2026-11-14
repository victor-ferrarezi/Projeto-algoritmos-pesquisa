package com.algoritmos.algorithms.search;

import com.algoritmos.domain.Metrics;
import com.algoritmos.domain.SearchAlgoritmo;

// busca percorre um bloco por vez
public final class BuscaSequencialRecursiva implements SearchAlgoritmo {
    private static final int TAMANHO_BLOCO = 500;

    @Override
    public String nome() {
        return "sequencial_recursiva";
    }

    @Override
    public boolean exigeOrdenacaoInput() {
        return false;
    }

    @Override
    public int search(int[] valores, int chave, Metrics metricas) {
        return searchBloco(valores, chave, 0, metricas);
    }

    private int searchBloco(int[] valores, int chave, int inicio, Metrics metricas) {
        if (inicio >= valores.length) {
            return -1;
        }

        int fim = Math.min(inicio + TAMANHO_BLOCO, valores.length);
        for (int indice = inicio; indice < fim; indice++) {
            metricas.comparacao();
            if (valores[indice] == chave) {
                return indice;
            }
        }

        return searchBloco(valores, chave, fim, metricas);
    }
}
