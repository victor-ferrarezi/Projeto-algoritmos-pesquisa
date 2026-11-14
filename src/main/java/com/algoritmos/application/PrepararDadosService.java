package com.algoritmos.application;

import com.algoritmos.infrastructure.CsvDadosRepository;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;

// le as bases e cria uma cópia ordenada e outra embaralhada
public final class PrepararDadosService {
    public static final int[] SIZES = {1_000, 10_000};

    private final CsvDadosRepository csv;

    public PrepararDadosService(CsvDadosRepository csv) {
        this.csv = csv;
    }

    public void prepare(Path dataDirectory) throws IOException {
        Files.createDirectories(dataDirectory);

        for (int size : SIZES) {
            Path source = findSource(dataDirectory, size);
            PreparedData prepared = readAndValidate(source, size);

            // ordenação usa o ID e mantém os outros dados da linha juntos
            Row[] orderedRows = prepared.rows().toArray(new Row[0]);
            mergeSortRows(orderedRows, new Row[orderedRows.length], 0, orderedRows.length - 1);

            List<String> ordered = new ArrayList<>(orderedRows.length);
            for (Row row : orderedRows) ordered.add(row.text());

            List<String> shuffled = new ArrayList<>(ordered);
            Collections.shuffle(shuffled, new Random(42L + size));

            String header = String.join(String.valueOf(prepared.delimiter()), prepared.normalizedHeader());
            writeCsv(dataDirectory.resolve("base_" + size + "_ordenada.csv"), header, ordered);
            writeCsv(dataDirectory.resolve("base_" + size + "_desordenada.csv"), header, shuffled);

            System.out.printf("  OK: %s (%d registros) -> versões ordenada e desordenada%n",
                    source.getFileName(), size);

        }
    }

    private Path findSource(Path dataDirectory, int size) throws IOException {
        Path mockarooName = dataDirectory.resolve("clientes_" + size + ".csv");
        Path compatibleName = dataDirectory.resolve("base_" + size + ".csv");
        if (Files.exists(mockarooName)) return mockarooName;
        if (Files.exists(compatibleName)) return compatibleName;
        throw new IOException("Arquivo não encontrado: " + mockarooName.getFileName()
                + ". Exporte a base do Mockaroo com " + size + " registros e salve na pasta dados/.");
    }

    private PreparedData readAndValidate(Path source, int expectedSize) throws IOException {
        List<String> lines = Files.readAllLines(source, StandardCharsets.UTF_8);
        if (lines.size() < 2) throw new IOException("CSV sem registros: " + source.getFileName());

        String originalHeader = CsvDadosRepository.removeBom(lines.get(0));
        char delimiter = csv.detectDelimiter(originalHeader);
        List<String> columns = csv.parseLine(originalHeader, delimiter);
        List<String> normalized = normalizeHeader(columns);
        validateRequiredColumns(normalized, source);
        int idColumn = normalized.indexOf("id_cliente");

        List<Row> rows = new ArrayList<>(expectedSize);
        Set<Integer> uniqueIds = new HashSet<>(expectedSize * 2);
        for (int i = 1; i < lines.size(); i++) {
            String line = lines.get(i);
            if (line.isBlank()) continue;
            int id = csv.parseId(line, idColumn, delimiter);
            if (!uniqueIds.add(id)) {
                throw new IOException("ID de cliente repetido (" + id + ") no arquivo " + source.getFileName());
            }
            rows.add(new Row(line, id));
        }

        if (rows.size() != expectedSize) {
            throw new IOException("A base " + source.getFileName() + " possui " + rows.size()
                    + " registros; eram esperados " + expectedSize + ". Confira a quantidade exportada no Mockaroo.");
        }
        return new PreparedData(rows, normalized, delimiter);
    }

    private List<String> normalizeHeader(List<String> columns) {
        List<String> normalized = new ArrayList<>(columns.size());
        for (String column : columns) {
            String original = CsvDadosRepository.clean(column);
            String key = original.toLowerCase().replaceAll("[^a-z0-9]", "");
            switch (key) {
                case "id":
                case "idcliente":
                    normalized.add("id_cliente");
                    break;
                case "datacadastro":
                    normalized.add("data_cadastro");
                    break;
                case "numerocompras":
                    normalized.add("numero_compras");
                    break;
                case "valortotal":
                    normalized.add("valor_total");
                    break;
                default:
                    normalized.add(original.toLowerCase());
                    break;
            }
        }
        return normalized;
    }

    private void validateRequiredColumns(List<String> columns, Path source) throws IOException {
        String[] required = {"id_cliente", "nome", "idade", "cidade"};
        for (String field : required) {
            if (!columns.contains(field)) {
                throw new IOException("A coluna '" + field + "' não foi encontrada em " + source.getFileName()
                        + ". Confira os campos exportados pelo Mockaroo.");
            }
        }
    }

    private void mergeSortRows(Row[] rows, Row[] auxiliary, int left, int right) {
        if (left >= right) return;
        int middle = (left + right) >>> 1;
        mergeSortRows(rows, auxiliary, left, middle);
        mergeSortRows(rows, auxiliary, middle + 1, right);

        int i = left;
        int j = middle + 1;
        int k = left;
        while (i <= middle && j <= right) {
            if (rows[i].id() <= rows[j].id()) auxiliary[k++] = rows[i++];
            else auxiliary[k++] = rows[j++];
        }
        while (i <= middle) auxiliary[k++] = rows[i++];
        while (j <= right) auxiliary[k++] = rows[j++];
        for (int p = left; p <= right; p++) rows[p] = auxiliary[p];
    }

    private void writeCsv(Path target, String header, List<String> rows) throws IOException {
        List<String> output = new ArrayList<>(rows.size() + 1);
        output.add(header);
        output.addAll(rows);
        Files.write(target, output, StandardCharsets.UTF_8);
    }

    private static class Row {
        private String text;
        private int id;

        public Row(String text, int id) {
            this.text = text;
            this.id = id;
        }

        public String text() {
            return text;
        }

        public int id() {
            return id;
        }
    }

    private static class PreparedData {
        private List<Row> rows;
        private List<String> normalizedHeader;
        private char delimiter;

        public PreparedData(List<Row> rows, List<String> normalizedHeader, char delimiter) {
            this.rows = rows;
            this.normalizedHeader = normalizedHeader;
            this.delimiter = delimiter;
        }

        public List<Row> rows() {
            return rows;
        }

        public List<String> normalizedHeader() {
            return normalizedHeader;
        }

        public char delimiter() {
            return delimiter;
        }
    }
}
