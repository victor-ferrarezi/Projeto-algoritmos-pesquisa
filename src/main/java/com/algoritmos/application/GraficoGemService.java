package com.algoritmos.application;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

// script Python que monta os gráficos
public final class GraficoGemService {
    public void generate(Path projectDirectory) throws IOException, InterruptedException {
        Path script = projectDirectory.resolve("graficos.py");
        if (!Files.exists(script)) {
            throw new IOException("O arquivo graficos.py não foi encontrado na pasta do projeto.");
        }

        String[] interpreters = {"python", "py", "python3"};
        IOException lastError = null;
        Process process = null;

        for (String interpreter : interpreters) {
            try {
                process = new ProcessBuilder(interpreter, script.toString())
                        .directory(projectDirectory.toFile())
                        .inheritIO()
                        .start();
                break;
            } catch (IOException error) {
                lastError = error;
            }
        }

        if (process == null) {
            throw new IOException("Não foi possível iniciar o Python. Instale o Python e tente novamente.",
                    lastError);
        }

        int exitCode = process.waitFor();
        if (exitCode != 0) {
            throw new IOException("A criação dos gráficos terminou com erro. "
                    + "Confira se a biblioteca matplotlib está instalada.");
        }
    }
}
