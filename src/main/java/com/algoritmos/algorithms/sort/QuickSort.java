package com.algoritmos.algorithms.sort;
import com.algoritmos.domain.Metrics;
import com.algoritmos.domain.SortAlgoritmo;
public final class QuickSort implements SortAlgoritmo {
    public String name() { return "quick"; }
    public void sort(int[] a, Metrics m) { sort(a, 0, a.length - 1, m); }
    private void sort(int[] a, int baixo, int alto, Metrics m) {
        while (baixo < alto) {
            int pivot = partition(a, baixo, alto, m);
            if (pivot - baixo < alto - pivot) { sort(a, baixo, pivot - 1, m); baixo = pivot + 1; }
            else { sort(a, pivot + 1, alto, m); alto = pivot - 1; }
        }
    }
    private int partition(int[] a, int baixo, int alto, Metrics m) {
        int mid = (baixo + alto) >>> 1;
        m.comparacao(); if (a[mid] < a[baixo]) BubbleSort.swap(a, mid, baixo, m);
        m.comparacao(); if (a[alto] < a[baixo]) BubbleSort.swap(a, alto, baixo, m);
        m.comparacao(); if (a[alto] < a[mid]) BubbleSort.swap(a, alto, mid, m);
        BubbleSort.swap(a, mid, alto, m);
        int pivot = a[alto], i = baixo;
        for (int j = baixo; j < alto; j++) { m.comparacao(); if (a[j] < pivot) BubbleSort.swap(a, i++, j, m); }
        BubbleSort.swap(a, i, alto, m);
        return i;
    }
}
