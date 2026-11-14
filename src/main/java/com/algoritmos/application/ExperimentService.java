package com.algoritmos.application;

import com.algoritmos.AlgoritmoCatalogo;
import com.algoritmos.domain.Metrics;
import com.algoritmos.domain.SearchAlgoritmo;
import com.algoritmos.domain.SortAlgoritmo;
import com.algoritmos.domain.Statistics;
import com.algoritmos.infrastructure.CsvDadosRepository;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.Random;

//roda os testes e grava os resultados
public final class ExperimentService {
    private static final double MAX_SORT_SECONDS = 60.0;
    private final CsvDadosRepository csv;
    private final List<SearchAlgoritmo> searches;
    private final List<SortAlgoritmo> sorts;
    private volatile long sink;

    public ExperimentService(CsvDadosRepository csv) {
        this.csv = csv;
        this.searches = AlgoritmoCatalogo.buscas();
        this.sorts = AlgoritmoCatalogo.ordenacoes();
    }

    public void runAll(Path dataDir, Path resultsDir, boolean quick) throws IOException {
        Files.createDirectories(resultsDir);
        int repetitions = quick ? 3 : 30;
        System.out.println("\nExecutando experimentos " + (quick ? "RÁPIDOS" : "COMPLETOS") + " (" + repetitions + " repetições base)...");
        runSearches(dataDir, resultsDir, repetitions);
        runSorts(dataDir, resultsDir, repetitions, quick);
        runInsertion(dataDir, resultsDir, repetitions);
        runRemoval(dataDir, resultsDir, repetitions);
        runManySearches(dataDir, resultsDir, quick);
        runContinuousInsertion(dataDir, resultsDir, repetitions);
        System.out.println("\nExperimentos finalizados. Arquivos CSV salvos em: " + resultsDir.toAbsolutePath());
    }

    private void runSearches(Path dataDir, Path out, int reps) throws IOException {
        System.out.println("[1–4/10] Experimentos de busca...");
        String[] positions = {"inicio", "25%", "50%", "75%", "final", "fora"};
        double[] fractions = {0, .25, .5, .75, 1, -1};
        try (PrintWriter w = writer(out.resolve("resultados_busca.csv"))) {
            w.println("algoritmo,estado_base,n,posicao,reps,tempo_medio_ns,tempo_mediana_ns,desvio_ns,comparacoes,encontrado");
            for (int n : PrepararDadosService.SIZES) {
                int[] ordered = readExpected(dataDir.resolve("base_" + n + "_ordenada.csv"), n);
                int[] shuffled = readExpected(dataDir.resolve("base_" + n + "_desordenada.csv"), n);
                for (SearchAlgoritmo algorithm : searches) {
                    for (int state = 0; state < 2; state++) {
                        if (algorithm.exigeOrdenacaoInput() && state == 0) continue;
                        int[] values = state == 0 ? shuffled : ordered;
                        for (int p = 0; p < positions.length; p++) {
                            int key = fractions[p] < 0 ? -1 : values[(int) Math.round(fractions[p] * (n - 1))];
                            for (int warm = 0; warm < 3; warm++) sink += algorithm.search(values, key, new Metrics());
                            double[] times = new double[reps];
                            long comparisons = 0; int found = -1;
                            for (int i = 0; i < reps; i++) {
                                Metrics metrics = new Metrics();
                                long start = System.nanoTime();
                                found = algorithm.search(values, key, metrics);
                                times[i] = System.nanoTime() - start;
                                comparisons = metrics.comparisons();
                            }
                            sink += found;
                            boolean ok = found >= 0 && values[found] == key;
                            w.printf(Locale.US, "%s,%s,%d,%s,%d,%.1f,%.1f,%.1f,%d,%s%n", algorithm.nome(),
                                state == 0 ? "desordenada" : "ordenada", n, positions[p], reps,
                                Statistics.mean(times), Statistics.median(times), Statistics.deviation(times), comparisons, ok);
                        }
                    }
                }
                System.out.println("   - Base " + n + " concluída.");
            }
        }
    }

    private void runSorts(Path dataDir, Path out, int reps, boolean quick) throws IOException {
        System.out.println("[5–6/10] Experimentos de ordenação...");
        String[] states = {"aleatorio", "ordenado", "inverso"};
        try (PrintWriter w = writer(out.resolve("resultados_ordenacao.csv"))) {
            w.println("algoritmo,n,estado_inicial,reps,tempo_medio_ms,tempo_mediana_ms,desvio_ms,comparacoes,movimentacoes,correto,limite_excedido,status");
            for (int n : PrepararDadosService.SIZES) {
                int[] random = readExpected(dataDir.resolve("base_" + n + "_desordenada.csv"), n);
                int[] ordered = readExpected(dataDir.resolve("base_" + n + "_ordenada.csv"), n);
                int[] reverse = ordered.clone(); reverse(reverse);
                int[][] inputs = {random, ordered, reverse};
                for (int state = 0; state < states.length; state++) {
                    for (SortAlgoritmo algorithm : sorts) {
                        boolean quadratic = algorithm.name().equals("bolha")
                                || algorithm.name().equals("selecao")
                                || algorithm.name().equals("insercao");
                        boolean tooLarge = false;
                        if (tooLarge) {
                            w.printf(Locale.US, "%s,%d,%s,0,NaN,NaN,NaN,0,0,false,false,pulado_custo_estimado%n", algorithm.name(), n, states[state]);
                            continue;
                        }
                        int actualReps = quadratic && n == 10_000 ? (quick ? 1 : Math.min(reps, 5)) : reps;
                        List<Double> times = new ArrayList<>();
                        long comparisons = 0, movements = 0;
                        boolean correct = true, exceeded = false;
                        for (int i = 0; i < actualReps; i++) {
                            int[] copy = inputs[state].clone(); Metrics metrics = new Metrics();
                            long start = System.nanoTime(); algorithm.sort(copy, metrics);
                            long elapsed = System.nanoTime() - start;
                            times.add(elapsed / 1_000_000.0);
                            comparisons = metrics.comparisons(); movements = metrics.movements();
                            correct &= isSorted(copy);
                            if (elapsed / 1e9 > MAX_SORT_SECONDS) { exceeded = true; break; }
                        }
                        double[] values = new double[times.size()];
                        for (int t = 0; t < times.size(); t++) {
                            values[t] = times.get(t);
                        }
                        w.printf(Locale.US, "%s,%d,%s,%d,%.3f,%.3f,%.3f,%d,%d,%s,%s,executado%n",
                            algorithm.name(), n, states[state], values.length, Statistics.mean(values), Statistics.median(values),
                            Statistics.deviation(values), comparisons, movements, correct, exceeded);
                        w.flush();
                    }
                    System.out.println("   - Base " + n + ", estado " + states[state] + " concluído.");
                }
            }
        }
    }

    private void runInsertion(Path dataDir, Path out, int reps) throws IOException {
        System.out.println("[7/10] Experimento de inserção...");
        String[] names = {"inicio", "25%", "meio", "75%", "final"};
        double[] fractions = {0, .25, .5, .75, 1};
        try (PrintWriter w = writer(out.resolve("resultados_insercao.csv"))) {
            w.println("n,posicao,estrategia,reps,tempo_medio_us,tempo_mediana_us,desvio_us");
            for (int n : new int[]{1_000, 10_000}) {
                int[] base = readExpected(dataDir.resolve("base_" + n + "_ordenada.csv"), n);
                // Ao dobrar os IDs, sobra um número entre cada par para fazer as inserções.
                for (int i = 0; i < base.length; i++) base[i] = Math.multiplyExact(base[i], 2);
                for (int p = 0; p < names.length; p++) {
                    int key;
                    if (p == 0) key = base[0] - 1;
                    else if (p == 4) key = base[n - 1] + 1;
                    else {
                        int position = (int) Math.round(fractions[p] * (n - 1));
                        key = base[position] - 1;
                    }
                    double[] strategyA = new double[reps], strategyB = new double[reps];
                    for (int i = 0; i < reps; i++) {
                        int[] a = Arrays.copyOf(base, n + 1);
                        long t0 = System.nanoTime();
                        a[n] = key;
                        Arrays.sort(a);
                        strategyA[i] = (System.nanoTime() - t0) / 1e3;

                        int[] b = Arrays.copyOf(base, n + 1);
                        long t1 = System.nanoTime();
                        int pos = insertionPoint(b, n, key);
                        for (int j = n; j > pos; j--) b[j] = b[j - 1];
                        b[pos] = key;
                        strategyB[i] = (System.nanoTime() - t1) / 1e3;

                        if (!isSorted(a) || !isSorted(b) || !Arrays.equals(a, b)) {
                            throw new IllegalStateException("Falha na validação do experimento de inserção.");
                        }
                    }
                    writeStats(w, n + "," + names[p] + ",A_inserir_fim_e_reordenar", strategyA);
                    writeStats(w, n + "," + names[p] + ",B_posicao_correta", strategyB);
                }
            }
        }
    }

    private void runRemoval(Path dataDir, Path out, int reps) throws IOException {
        System.out.println("[8/10] Experimento de remoção...");
        String[] positions = {"inicio", "meio", "final"};
        try (PrintWriter w = writer(out.resolve("resultados_remocao.csv"))) {
            w.println("n,posicao,localizacao,reps,localizar_media_us,localizar_mediana_us,localizar_desvio_us,remover_media_us,remover_mediana_us,remover_desvio_us,total_media_us,total_mediana_us,total_desvio_us");
            for (int n : new int[]{1_000, 10_000}) {
                int[] base = readExpected(dataDir.resolve("base_" + n + "_ordenada.csv"), n);
                for (int p = 0; p < 3; p++) {
                    int index = p == 0 ? 0 : p == 1 ? (n - 1) / 2 : n - 1;
                    int key = base[index];
                    for (String lookup : new String[]{"sequencial", "binaria"}) {
                        double[] locate = new double[reps], remove = new double[reps], total = new double[reps];
                        SearchAlgoritmo algorithm = findSearch(lookup);
                        for (int i = 0; i < reps; i++) {
                            int[] copy = base.clone(); long t0 = System.nanoTime();
                            int pos = algorithm.search(copy, key, new Metrics()); long t1 = System.nanoTime();
                            if (pos < 0) throw new IllegalStateException("ID para remoção não encontrado.");
                            for (int j = pos; j < n - 1; j++) copy[j] = copy[j + 1];
                            long t2 = System.nanoTime(); locate[i] = (t1 - t0) / 1e3;
                            remove[i] = (t2 - t1) / 1e3; total[i] = (t2 - t0) / 1e3;
                            if (!isSorted(Arrays.copyOf(copy, n - 1))) throw new IllegalStateException("Remoção gerou vetor inválido.");
                        }
                        w.printf(Locale.US, "%d,%s,%s,%d,%.3f,%.3f,%.3f,%.3f,%.3f,%.3f,%.3f,%.3f,%.3f%n",
                            n, positions[p], lookup, reps,
                            Statistics.mean(locate), Statistics.median(locate), Statistics.deviation(locate),
                            Statistics.mean(remove), Statistics.median(remove), Statistics.deviation(remove),
                            Statistics.mean(total), Statistics.median(total), Statistics.deviation(total));
                    }
                }
            }
        }
    }

    private void runManySearches(Path dataDir, Path out, boolean quick) throws IOException {
        System.out.println("[9/10] Comparação de muitas consultas...");
        int n = 10_000, repetitions = quick ? 3 : 30;
        int[] shuffled = readExpected(dataDir.resolve("base_" + n + "_desordenada.csv"), n);
        int[] queries = new int[10_000];
        Random random = new Random(7);
        for (int i = 0; i < queries.length; i++) queries[i] = shuffled[random.nextInt(n)];

        int[] queryCounts = {1, 10, 100, 1_000, 10_000};
        SearchAlgoritmo linear = findSearch("sequencial");
        SearchAlgoritmo binary = findSearch("binaria");
        double lastLinear = 0, lastTotal = 0, lastSort = 0;

        try (PrintWriter w = writer(out.resolve("resultados_muitas_consultas.csv"))) {
            w.println("n_buscas,reps,sistemaA_media_ms,sistemaA_mediana_ms,sistemaA_desvio_ms,"
                    + "sistemaB_media_ms,sistemaB_mediana_ms,sistemaB_desvio_ms,"
                    + "ordenacao_media_ms,ordenacao_mediana_ms,ordenacao_desvio_ms");

            for (int count : queryCounts) {
                double[] timesA = new double[repetitions];
                double[] timesB = new double[repetitions];
                double[] sortingTimes = new double[repetitions];

                for (int i = 0; i < repetitions; i++) {
                    long resultSink = 0;

                    Metrics metricsA = new Metrics();
                    long startA = System.nanoTime();
                    for (int k = 0; k < count; k++) {
                        resultSink += linear.search(shuffled, queries[k], metricsA);
                    }
                    long endA = System.nanoTime();
                    timesA[i] = (endA - startA) / 1_000_000.0;

                    int[] ordered = shuffled.clone();
                    long startSort = System.nanoTime();
                    Arrays.sort(ordered);
                    long endSort = System.nanoTime();
                    sortingTimes[i] = (endSort - startSort) / 1_000_000.0;

                    Metrics metricsB = new Metrics();
                    long startB = System.nanoTime();
                    for (int k = 0; k < count; k++) {
                        resultSink += binary.search(ordered, queries[k], metricsB);
                    }
                    long endB = System.nanoTime();
                    timesB[i] = (endB - startSort) / 1_000_000.0;
                    sink += resultSink;
                }

                lastLinear = Statistics.mean(timesA);
                lastTotal = Statistics.mean(timesB);
                lastSort = Statistics.mean(sortingTimes);

                w.printf(Locale.US, "%d,%d,%.3f,%.3f,%.3f,%.3f,%.3f,%.3f,%.3f,%.3f,%.3f%n",
                        count, repetitions,
                        Statistics.mean(timesA), Statistics.median(timesA), Statistics.deviation(timesA),
                        Statistics.mean(timesB), Statistics.median(timesB), Statistics.deviation(timesB),
                        Statistics.mean(sortingTimes), Statistics.median(sortingTimes), Statistics.deviation(sortingTimes));
            }
        }

        double linearPerSearch = lastLinear * 1_000.0 / 10_000.0;
        double binaryPerSearch = Math.max(0, lastTotal - lastSort) * 1_000.0 / 10_000.0;
        double equilibrium = linearPerSearch > binaryPerSearch
                ? lastSort * 1_000.0 / (linearPerSearch - binaryPerSearch) : Double.NaN;

        try (PrintWriter w = writer(out.resolve("resultados_ponto_equilibrio.csv"))) {
            w.println("ordenacao_ms,busca_sequencial_us,busca_binaria_us,n_buscas_equilibrio_estimado");
            w.printf(Locale.US, "%.3f,%.3f,%.4f,%.1f%n",
                    lastSort, linearPerSearch, binaryPerSearch, equilibrium);
        }
    }

    private void runContinuousInsertion(Path dataDir, Path out, int reps) throws IOException {
        System.out.println("[10/10] Inserções contínuas...");
        int n = 10_000;
        int batch = 100;
        int[] base = readExpected(dataDir.resolve("base_" + n + "_ordenada.csv"), n);

        // A escala deixa um número livre entre dois IDs consecutivos.
        for (int i = 0; i < base.length; i++) base[i] = Math.multiplyExact(base[i], 2);

        Random random = new Random(11);
        int[] newValues = new int[1_000];
        java.util.Set<Integer> usedPositions = new java.util.HashSet<>();
        for (int i = 0; i < newValues.length; i++) {
            int position;
            do {
                position = random.nextInt(n + 1);
            } while (!usedPositions.add(position));

            if (position == 0) newValues[i] = base[0] - 1;
            else if (position == n) newValues[i] = base[n - 1] + 1;
            else newValues[i] = base[position] - 1;
        }

        String[] strategies = {
                "manter_ordenada_a_cada_insercao",
                "ordenar_a_cada_100_insercoes",
                "ordenar_ao_final"
        };

        try (PrintWriter w = writer(out.resolve("resultados_insercao_continua.csv"))) {
            w.println("insercoes,estrategia,reps,tempo_medio_ms,tempo_mediana_ms,desvio_ms");

            for (int count : new int[]{100, 500, 1_000}) {
                for (int strategy = 0; strategy < strategies.length; strategy++) {
                    double[] times = new double[reps];

                    for (int i = 0; i < reps; i++) {
                        int[] data = Arrays.copyOf(base, n + count);
                        int size = n;
                        long start = System.nanoTime();

                        for (int k = 0; k < count; k++) {
                            if (strategy == 0) {
                                int position = insertionPoint(data, size, newValues[k]);
                                for (int j = size; j > position; j--) data[j] = data[j - 1];
                                data[position] = newValues[k];
                            } else {
                                data[size] = newValues[k];
                                if (strategy == 1 && (k + 1) % batch == 0) {
                                    Arrays.sort(data, 0, size + 1);
                                }
                            }
                            size++;
                        }

                        if (strategy == 2) Arrays.sort(data, 0, size);
                        if (strategy == 1 && size % batch != n % batch) Arrays.sort(data, 0, size);

                        times[i] = (System.nanoTime() - start) / 1_000_000.0;
                        if (size != n + count || !isSorted(Arrays.copyOf(data, size))) {
                            throw new IllegalStateException("A inserção contínua gerou uma sequência inválida.");
                        }
                    }

                    w.printf(Locale.US, "%d,%s,%d,%.3f,%.3f,%.3f%n",
                            count, strategies[strategy], reps,
                            Statistics.mean(times), Statistics.median(times), Statistics.deviation(times));
                }
            }
        }
    }

    private int[] readExpected(Path file, int expectedSize) throws IOException {
        int[] values = csv.readIds(file);
        if (values.length != expectedSize) {
            throw new IOException(file.getFileName() + " possui " + values.length
                    + " registros; eram esperados " + expectedSize + ". Execute a preparação das bases novamente.");
        }
        return values;
    }

    private int insertionPoint(int[] data, int size, int key) {
        int baixo = 0, alto = size;
        while (baixo < alto) { int mid = (baixo + alto) >>> 1; if (data[mid] < key) baixo = mid + 1; else alto = mid; }
        return baixo;
    }
    private boolean isSorted(int[] data) { for (int i = 1; i < data.length; i++) if (data[i - 1] > data[i]) return false; return true; }
    private void reverse(int[] data) { for (int i = 0; i < data.length / 2; i++) { int j = data.length - i - 1, t = data[i]; data[i] = data[j]; data[j] = t; } }
    private SearchAlgoritmo findSearch(String name) {
        for (SearchAlgoritmo algoritmo : searches) {
            if (algoritmo.nome().equals(name)) {
                return algoritmo;
            }
        }
        throw new IllegalArgumentException("Algoritmo de busca não encontrado: " + name);
    }
    private PrintWriter writer(Path path) throws IOException { return new PrintWriter(Files.newBufferedWriter(path, StandardCharsets.UTF_8)); }
    private void writeStats(PrintWriter w, String prefix, double[] values) {
        w.printf(Locale.US, "%s,%d,%.3f,%.3f,%.3f%n", prefix, values.length, Statistics.mean(values), Statistics.median(values), Statistics.deviation(values));
    }
}
