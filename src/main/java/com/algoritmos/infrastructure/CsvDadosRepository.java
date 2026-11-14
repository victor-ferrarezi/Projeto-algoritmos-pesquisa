package com.algoritmos.infrastructure;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

// Leitura dos arquivos CSV usados pelo programa.
public final class CsvDadosRepository {
    public int idColumn(String header, char delimiter) throws IOException {
        List<String> columns = parseLine(header, delimiter);
        for (int i = 0; i < columns.size(); i++) {
            String name = clean(columns.get(i));
            if (name.equalsIgnoreCase("id") || name.equalsIgnoreCase("id_cliente")) return i;
        }
        throw new IOException("Não encontrei uma coluna 'id' ou 'id_cliente' no cabeçalho.");
    }

    public char detectDelimiter(String header) {
        int semicolons = 0, commas = 0;
        boolean quoted = false;
        for (int i = 0; i < header.length(); i++) {
            char c = header.charAt(i);
            if (c == '"') quoted = !quoted;
            else if (!quoted && c == ';') semicolons++;
            else if (!quoted && c == ',') commas++;
        }
        return semicolons > commas ? ';' : ',';
    }

    public List<String> parseLine(String line, char delimiter) {
        List<String> cells = new ArrayList<>();
        StringBuilder cell = new StringBuilder();
        boolean quoted = false;
        for (int i = 0; i < line.length(); i++) {
            char c = line.charAt(i);
            if (c == '"') {
                if (quoted && i + 1 < line.length() && line.charAt(i + 1) == '"') { cell.append('"'); i++; }
                else quoted = !quoted;
            } else if (c == delimiter && !quoted) {
                cells.add(cell.toString()); cell.setLength(0);
            } else cell.append(c);
        }
        cells.add(cell.toString());
        return cells;
    }

    public int parseId(String row, int column, char delimiter) throws IOException {
        List<String> cells = parseLine(row, delimiter);
        if (column >= cells.size()) throw new IOException("Linha CSV sem a coluna de ID: " + row);
        try { return Integer.parseInt(clean(cells.get(column))); }
        catch (NumberFormatException e) { throw new IOException("ID inválido na linha: " + row, e); }
    }

    public int[] readIds(Path file) throws IOException {
        List<String> lines = Files.readAllLines(file, StandardCharsets.UTF_8);
        if (lines.size() < 2) throw new IOException("CSV sem registros: " + file);
        String header = removeBom(lines.get(0));
        char delimiter = detectDelimiter(header);
        int idColumn = idColumn(header, delimiter);
        int quantidade = 0;
        for (int i = 1; i < lines.size(); i++) {
            if (!lines.get(i).isBlank()) {
                quantidade++;
            }
        }

        int[] ids = new int[quantidade];
        int index = 0;
        for (int i = 1; i < lines.size(); i++) {
            if (!lines.get(i).isBlank()) {
                ids[index] = parseId(lines.get(i), idColumn, delimiter);
                index++;
            }
        }
        return ids;
    }

    public static String clean(String value) {
        String s = value == null ? "" : value.trim();
        if (s.length() >= 2 && s.startsWith("\"") && s.endsWith("\"")) s = s.substring(1, s.length() - 1);
        return s.replace("\uFEFF", "").trim();
    }
    public static String removeBom(String value) { return value == null ? "" : value.replace("\uFEFF", ""); }
}
