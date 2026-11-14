package com.algoritmos;

import com.algoritmos.ui.ConsoleMenu;

//ponto de entrada do programa
public final class App {
    private App() {
    }

    public static void main(String[] args) {
        try {
            ConsoleMenu menu = new ConsoleMenu();
            if (args.length == 0) {
                menu.start();
            } else {
                menu.executeCommand(args[0]);
            }
        } catch (Exception erro) {
            System.err.println("Erro ao executar o programa: " + erro.getMessage());
            erro.printStackTrace(System.err);
            System.exit(1);
        }
    }
}
