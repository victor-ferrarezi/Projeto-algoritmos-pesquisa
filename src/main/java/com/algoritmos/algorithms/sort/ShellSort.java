package com.algoritmos.algorithms.sort;
import com.algoritmos.domain.Metrics;
import com.algoritmos.domain.SortAlgoritmo;
public final class ShellSort implements SortAlgoritmo {
    public String name() { return "shell"; }
    public void sort(int[] a, Metrics m) {
        int gap = 1;
        while (gap < a.length / 3) gap = 3 * gap + 1;
        for (; gap >= 1; gap /= 3) {
            for (int i = gap; i < a.length; i++) {
                int key = a[i], j = i;
                while (j >= gap) { m.comparacao(); if (a[j - gap] > key) { a[j] = a[j - gap]; m.movement(); j -= gap; } else break; }
                if (j != i) { a[j] = key; m.movement(); }
            }
        }
    }
}
