package com.algoritmos.algorithms.search;
import com.algoritmos.domain.Metrics;
import com.algoritmos.domain.SearchAlgoritmo;
public final class BuscaBinaria implements SearchAlgoritmo {
    public String nome() { return "binaria"; }
    public boolean exigeOrdenacaoInput() { return true; }
    public int search(int[] a, int key, Metrics m) {
        int baixo = 0, alto = a.length - 1;
        while (baixo <= alto) {
            int mid = (baixo + alto) >>> 1; m.comparacao();
            if (a[mid] == key) return mid;
            if (a[mid] < key) baixo = mid + 1; else alto = mid - 1;
        }
        return -1;
    }
}
