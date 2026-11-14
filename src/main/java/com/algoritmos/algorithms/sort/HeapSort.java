package com.algoritmos.algorithms.sort;
import com.algoritmos.domain.Metrics;
import com.algoritmos.domain.SortAlgoritmo;
public final class HeapSort implements SortAlgoritmo {
    public String name() { return "heap"; }
    public void sort(int[] a, Metrics m) {
        for (int i = a.length / 2 - 1; i >= 0; i--) sift(a, i, a.length, m);
        for (int end = a.length - 1; end > 0; end--) { BubbleSort.swap(a, 0, end, m); sift(a, 0, end, m); }
    }
    private void sift(int[] a, int root, int size, Metrics m) {
        while (true) {
            int child = root * 2 + 1;
            if (child >= size) return;
            if (child + 1 < size) { m.comparacao(); if (a[child + 1] > a[child]) child++; }
            m.comparacao();
            if (a[child] > a[root]) { BubbleSort.swap(a, root, child, m); root = child; }
            else return;
        }
    }
}
