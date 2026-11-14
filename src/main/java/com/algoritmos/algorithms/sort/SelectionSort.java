package com.algoritmos.algorithms.sort;
import com.algoritmos.domain.Metrics;
import com.algoritmos.domain.SortAlgoritmo;
public final class SelectionSort implements SortAlgoritmo {
    public String name() { return "selection"; }
    public void sort(int[] a, Metrics m) {
        for (int i = 0; i < a.length - 1; i++) {
            int min = i;
            for (int j = i + 1; j < a.length; j++) { m.comparacao(); if (a[j] < a[min]) min = j; }
            BubbleSort.swap(a, i, min, m);
        }
    }
}
