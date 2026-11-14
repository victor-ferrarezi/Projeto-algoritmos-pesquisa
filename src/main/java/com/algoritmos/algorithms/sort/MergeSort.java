package com.algoritmos.algorithms.sort;
import com.algoritmos.domain.Metrics;
import com.algoritmos.domain.SortAlgoritmo;
public final class MergeSort implements SortAlgoritmo {
    public String name() { return "intercalacao"; }
    public void sort(int[] a, Metrics m) { if (a.length > 1) sort(a, new int[a.length], 0, a.length - 1, m); }
    public void sort(int[] a, int length, Metrics m) { if (length > 1) sort(a, new int[length], 0, length - 1, m); }
    private void sort(int[] a, int[] aux, int baixo, int alto, Metrics m) {
        if (baixo >= alto) return;
        int mid = (baixo + alto) >>> 1;
        sort(a, aux, baixo, mid, m); sort(a, aux, mid + 1, alto, m);
        for (int p = baixo; p <= alto; p++) {
            aux[p] = a[p];
            m.movement();
        }
        int i = baixo, j = mid + 1;
        for (int k = baixo; k <= alto; k++) {
            if (i > mid) a[k] = aux[j++];
            else if (j > alto) a[k] = aux[i++];
            else { m.comparacao(); if (aux[j] < aux[i]) a[k] = aux[j++]; else a[k] = aux[i++]; }
            m.movement();
        }
    }
}
