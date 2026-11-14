package com.algoritmos.algorithms.sort;
import com.algoritmos.domain.Metrics;
import com.algoritmos.domain.SortAlgoritmo;
public final class BubbleSort implements SortAlgoritmo {
    public String name() { return "bubble"; }
    public void sort(int[] a, Metrics m) {
        for (int end = a.length - 1; end > 0; end--) {
            boolean changed = false;
            for (int i = 0; i < end; i++) { m.comparacao(); if (a[i] > a[i + 1]) { swap(a, i, i + 1, m); changed = true; } }
            if (!changed) break;
        }
    }
    static void swap(int[] a, int i, int j, Metrics m) {
        if (i == j) return;
        int t = a[i]; a[i] = a[j]; a[j] = t; m.movement(); m.movement();
    }
}
