package com.algoritmos.application;

import com.algoritmos.AlgoritmoCatalogo;
import com.algoritmos.domain.Metrics;
import com.algoritmos.domain.SearchAlgoritmo;
import com.algoritmos.domain.SortAlgoritmo;
import java.util.Arrays;
import java.util.Random;

// Faz alguns testes para conferir se as buscas e ordenações estão corretas
public final class TestService {
    public boolean run() {
        boolean allOk = true;
        Random random = new Random(42);
        int[] sizes = {0, 1, 2, 3, 10, 257, 2_000};
        for (SortAlgoritmo algorithm : AlgoritmoCatalogo.ordenacoes()) {
            boolean ok = true;
            for (int size : sizes) for (int mode = 0; mode < 4; mode++) {
                int[] data = new int[size];
                for (int i = 0; i < size; i++) data[i] = random.nextInt(mode == 3 ? 12 : 100_000);
                if (mode == 1 || mode == 2) Arrays.sort(data);
                if (mode == 2) reverse(data);
                int[] expected = data.clone(); Arrays.sort(expected);
                algorithm.sort(data, new Metrics());
                ok &= Arrays.equals(data, expected);
            }
            System.out.println((ok ? "[OK]     " : "[FALHOU] ") + algorithm.name());
            allOk &= ok;
        }
        int[] sorted = new int[1_000];
        for (int i = 0; i < sorted.length; i++) sorted[i] = i * 3 + 1;
        boolean searchOk = true;
        for (SearchAlgoritmo algorithm : AlgoritmoCatalogo.buscas()) {
            for (int i = 0; i < sorted.length; i++) searchOk &= algorithm.search(sorted, sorted[i], new Metrics()) == i;
            for (int key : new int[]{-5, 0, 2, 3, 100_000}) searchOk &= algorithm.search(sorted, key, new Metrics()) == -1;
        }
        System.out.println((searchOk ? "[OK]     " : "[FALHOU] ") + "buscas (4 algoritmos)");
        return allOk && searchOk;
    }
    private void reverse(int[] data) { for (int i = 0; i < data.length / 2; i++) { int j = data.length - i - 1, t = data[i]; data[i] = data[j]; data[j] = t; } }
}
