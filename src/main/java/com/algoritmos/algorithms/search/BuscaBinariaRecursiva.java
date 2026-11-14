package com.algoritmos.algorithms.search;
import com.algoritmos.domain.Metrics;
import com.algoritmos.domain.SearchAlgoritmo;
public final class BuscaBinariaRecursiva implements SearchAlgoritmo {
    public String nome() { return "binaria_recursiva"; }
    public boolean exigeOrdenacaoInput() { return true; }
    public int search(int[] a, int key, Metrics m) { return search(a, key, 0, a.length - 1, m); }
    private int search(int[] a, int key, int baixo, int alto, Metrics m) {
        if (baixo > alto) return -1;
        int mid = (baixo + alto) >>> 1; m.comparacao();
        if (a[mid] == key) return mid;
        return a[mid] < key ? search(a, key, mid + 1, alto, m) : search(a, key, baixo, mid - 1, m);
    }
}
