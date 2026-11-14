package com.algoritmos.domain;

// Métodos que todos os algoritmos de busca precisam implementar.
public interface SearchAlgoritmo {
    String nome();
    boolean exigeOrdenacaoInput();
    int search(int[] values, int key, Metrics metrics);
}
