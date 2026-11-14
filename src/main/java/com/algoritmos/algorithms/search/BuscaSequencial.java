package com.algoritmos.algorithms.search;
import com.algoritmos.domain.Metrics;
import com.algoritmos.domain.SearchAlgoritmo;
public final class BuscaSequencial implements SearchAlgoritmo {
    public String nome() { return "sequencial"; }
    public boolean exigeOrdenacaoInput() { return false; }
    public int search(int[] a, int key, Metrics m) {
        for (int i = 0; i < a.length; i++) { m.comparacao(); if (a[i] == key) return i; }
        return -1;
    }
}
