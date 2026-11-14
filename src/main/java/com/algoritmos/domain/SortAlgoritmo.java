package com.algoritmos.domain;

// Métodos comuns aos algoritmos de ordenação.
public interface SortAlgoritmo {
    String name();
    void sort(int[] values, Metrics metrics);

    // Ordena o trecho do vetor informado.
    default void sort(int[] values, int length, Metrics metrics) {
        if (length < 0 || length > values.length) throw new IllegalArgumentException("Tamanho inválido: " + length);
        if (length == values.length) { sort(values, metrics); return; }
        int[] prefix = java.util.Arrays.copyOf(values, length);
        sort(prefix, metrics);
        System.arraycopy(prefix, 0, values, 0, length);
    }
}
