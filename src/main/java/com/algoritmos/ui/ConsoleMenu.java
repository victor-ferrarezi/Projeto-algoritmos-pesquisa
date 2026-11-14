package com.algoritmos.ui;

import com.algoritmos.application.*;
import com.algoritmos.infrastructure.CsvDadosRepository;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Scanner;

// Exibe o menu e lê a opção escolhida.
public final class ConsoleMenu {
    private final Path project = Path.of(".").toAbsolutePath().normalize();
    private final Path data = project.resolve("dados");
    private final Path results = project.resolve("resultados");
    private final CsvDadosRepository csv = new CsvDadosRepository();
    private final PrepararDadosService preparation = new PrepararDadosService(csv);
    private final TestService tests = new TestService();
    private final ExperimentService experiments = new ExperimentService(csv);
    private final SumarioService summary = new SumarioService();
    private final GraficoGemService graphs = new GraficoGemService();

    public void start() {
        try (Scanner input = new Scanner(System.in)) {
            boolean running = true;
            while (running) {
                showMenu();
                System.out.print("Escolha uma opção: ");
                String option = input.nextLine().trim();
                try {
                    switch (option) {
                        case "1":
                            executarFluxoCompleto(true);
                            break;
                        case "2":
                            showTestResult();
                            break;
                        case "3":
                            prepareBases();
                            break;
                        case "4":
                            executarExperimentos(true);
                            break;
                        case "5":
                            executarExperimentos(false);
                            break;
                        case "6":
                            mostrarResumo();
                            break;
                        case "7":
                            gerarGraficos();
                            break;
                        case "0":
                            running = false;
                            break;
                        default:
                            System.out.println("Opção inválida. Escolha um número do menu.");
                            break;
                    }
                } catch (Exception e) {
                    System.out.println("\nNão foi possível concluir a operação: " + e.getMessage());
                    System.out.println("Confira os arquivos em dados/ e tente novamente.");
                }
                if (running) { System.out.println("\nPressione ENTER para voltar ao menu..."); input.nextLine(); }
            }
        }
        System.out.println("Programa encerrado.");
    }

    public void executeCommand(String command) throws Exception {
        switch (command) {
            case "--testar":
                showTestResult();
                break;
            case "--preparar":
                prepareBases();
                break;
            case "--rapido":
                executarFluxoCompleto(true);
                break;
            case "--completo":
                executarFluxoCompleto(false);
                break;
            case "--resumo":
                mostrarResumo();
                break;
            case "--graficos":
                gerarGraficos();
                break;
            default:
                System.out.println("Comandos: --testar, --preparar, --rapido, --completo, --resumo, --graficos");
                start();
                break;
        }
    }

    private void showMenu() {
        System.out.println("\n====================================================");
        System.out.println("  ANÁLISE DE ALGORITMOS");
        System.out.println("====================================================");
        System.out.println("1. Verificar, preparar e executar testes rápidos");
        System.out.println("2. Verificar os algoritmos");
        System.out.println("3. Preparar os arquivos de dados");
        System.out.println("4. Fazer testes rápidos (3 repetições)");
        System.out.println("5. Fazer testes completos (30 repetições nos cenários principais)");
        System.out.println("6. Ver resumo dos resultados");
        System.out.println("7. Criar gráficos para o relatório");
        System.out.println("0. Sair");
        System.out.println("----------------------------------------------------");
    }

    private void executarFluxoCompleto(boolean quick) throws Exception {
        System.out.println("\nETAPA 1/4 — verificando os algoritmos");
        if (!tests.run()) throw new IllegalStateException("Um ou mais testes falharam. Os experimentos foram interrompidos.");
        System.out.println("\nETAPA 2/4 — preparando os dados");
        preparation.prepare(data);
        System.out.println("\nETAPA 3/4 — fazendo os testes");
        experiments.runAll(data, results, quick);
        System.out.println("\nETAPA 4/4 — preparando o resumo");
        mostrarResumo();
        System.out.println("\nPronto! Os arquivos estão na pasta resultados/.");
    }

    private void showTestResult() {
        System.out.println("\nVALIDAÇÃO DOS ALGORITMOS");
        boolean ok = tests.run();
        System.out.println(ok ? "\nResultado: todos os testes passaram." : "\nResultado: existem falhas; não execute os experimentos até corrigi-las.");
    }

    private void prepareBases() throws IOException { preparation.prepare(data); }

    private void executarExperimentos(boolean quick) throws Exception {
        if (!tests.run()) throw new IllegalStateException("Um ou mais algoritmos falharam na validação.");
        preparation.prepare(data);
        experiments.runAll(data, results, quick);
        mostrarResumo();
    }

    private void mostrarResumo() throws IOException {
        Path file = summary.generate(results);
        System.out.println("\n" + Files.readString(file));
    }

    private void gerarGraficos() throws Exception {
        if (!Files.exists(results.resolve("resultados_busca.csv"))) throw new IOException("Execute os experimentos antes de gerar os gráficos.");
        graphs.generate(project);
        System.out.println("Gráficos salvos na pasta graficos/.");
    }
}
