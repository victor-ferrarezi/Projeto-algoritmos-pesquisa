package com.algoritmos.algorithms.sort;
import com.algoritmos.domain.Metrics;
import com.algoritmos.domain.SortAlgoritmo;
public final class InsertionSort implements SortAlgoritmo {
    public String name() { return "insert"; }
    public void sort(int[] a, Metrics m) {
        for (int i = 1; i < a.length; i++) {
            int key = a[i], j = i - 1;
            while (j >= 0) { m.comparacao(); if (a[j] > key) { a[j + 1] = a[j]; m.movement(); j--; } else break; }
            if (j + 1 != i) { a[j + 1] = key; m.movement(); }
        }
    }
}
