package com.algoritmos;

import com.algoritmos.algorithms.sort.*;
import com.algoritmos.algorithms.search.BuscaBinaria;
import com.algoritmos.algorithms.search.BuscaSequencial;
import com.algoritmos.algorithms.search.BuscaBinariaRecursiva;
import com.algoritmos.algorithms.search.BuscaSequencialRecursiva;
import com.algoritmos.domain.SearchAlgoritmo;
import com.algoritmos.domain.SortAlgoritmo;
import java.util.List;

// Lista os algoritmos usados nos testes.
public final class AlgoritmoCatalogo {
    private AlgoritmoCatalogo() {}
    public static List<SearchAlgoritmo> buscas() {
        return List.of(new BuscaSequencial(), new BuscaSequencialRecursiva(), new BuscaBinaria(), new BuscaBinariaRecursiva());
    }
    public static List<SortAlgoritmo> ordenacoes() {
        return List.of(new BubbleSort(), new SelectionSort(), new InsertionSort(), new ShellSort(), new MergeSort(), new QuickSort(), new HeapSort());
    }
}
