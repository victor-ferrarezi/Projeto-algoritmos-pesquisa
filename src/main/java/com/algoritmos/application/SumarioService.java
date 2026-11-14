package com.algoritmos.application;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

//monta um resumo com os principais resultados dos testes
public final class SumarioService {
    public Path generate(Path resultsDir) throws IOException {
        Path searchFile = resultsDir.resolve("resultados_busca.csv");
        Path sortFile = resultsDir.resolve("resultados_ordenacao.csv");
        if (!Files.exists(searchFile) || !Files.exists(sortFile)) {
            throw new IOException("Ainda não existem resultados. Execute os experimentos primeiro.");
        }
        List<String> report = new ArrayList<>();
        report.add("RESUMO DOS EXPERIMENTOS — ANÁLISE DE ALGORITMOS");
        report.add("Resultados registrados nos arquivos CSV desta pasta.");
        report.add("");
        report.add("1. BUSCAS — base de 10.000 registros, chave fora da base, vetor ordenado");
        List<String[]> searchRows = rows(searchFile);
        for (String[] linha : searchRows) {
            if (linha.length > 9 && linha[1].equals("ordenada")
                    && linha[2].equals("10000") && linha[3].equals("fora")) {
                report.add(String.format("   %-20s | comparações: %s | tempo médio: %s ns | encontrado: %s",
                        linha[0], linha[8], linha[5], linha[9]));
            }
        }
        report.add("");
        report.add("2. ORDENAÇÃO — base de 10.000 registros, entrada aleatória");
        List<String[]> sortRows = rows(sortFile);
        String melhorAlgoritmo = "";
        String melhorTempo = "";
        double menorTempo = Double.POSITIVE_INFINITY;
        long skipped = 0;

        for (String[] linha : sortRows) {
            if (linha.length > 11 && linha[1].equals("10000")
                    && linha[2].equals("aleatorio") && linha[11].equals("executado")) {
                double tempo = parse(linha[5]);
                if (tempo < menorTempo) {
                    menorTempo = tempo;
                    melhorAlgoritmo = linha[0];
                    melhorTempo = linha[5];
                }
            }
            if (linha.length > 11 && linha[11].equals("pulado_custo_estimado")) {
                skipped++;
            }
        }

        if (!melhorAlgoritmo.isEmpty()) {
            report.add("   Menor tempo mediano observado: " + melhorAlgoritmo + " — " + melhorTempo + " ms.");
        }
        report.add("   Casos de ordenação quadrática não executados devido ao custo estimado: " + skipped + ".");
        report.add("   Os casos não executados não receberam tempos fictícios; os campos aparecem como NaN nos arquivos CSV.");
        report.add("");
        report.add("3. PONTO DE EQUILÍBRIO — sistema com várias consultas");
        Path point = resultsDir.resolve("resultados_ponto_equilibrio.csv");
        if (Files.exists(point)) {
            List<String[]> p = rows(point);
            if (!p.isEmpty() && p.get(0).length >= 4) report.add("   Número estimado de consultas para compensar o tempo de ordenação: " + p.get(0)[3] + ".");
        } else report.add("   Ainda não calculado.");
        report.add("");
        report.add("4. ARQUIVOS CRIADOS");
        try (java.util.stream.Stream<Path> files = Files.list(resultsDir)) {
            java.util.Iterator<Path> iterator = files.iterator();
            while (iterator.hasNext()) {
                Path arquivo = iterator.next();
                if (Files.isRegularFile(arquivo)) {
                    report.add("   - " + arquivo.getFileName());
                }
            }
        }
        Path target = resultsDir.resolve("resumo_experimentos.txt");
        Files.write(target, report, StandardCharsets.UTF_8);
        return target;
    }

    private List<String[]> rows(Path file) throws IOException {
        List<String> lines = Files.readAllLines(file, StandardCharsets.UTF_8);
        List<String[]> result = new ArrayList<>();
        for (int i = 1; i < lines.size(); i++) if (!lines.get(i).isBlank()) result.add(lines.get(i).split(",", -1));
        return result;
    }
    private double parse(String value) { try { return Double.parseDouble(value); } catch (Exception e) { return Double.POSITIVE_INFINITY; } }
}
