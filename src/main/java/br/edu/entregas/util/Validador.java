package br.edu.entregas.util;

public final class Validador {
    private Validador() {}

    public static void textoObrigatorio(String valor, String campo) {
        if (valor == null || valor.isBlank()) {
            throw new IllegalArgumentException(campo + " é obrigatório.");
        }
    }

    public static void numeroPositivo(double valor, String campo) {
        if (valor <= 0) throw new IllegalArgumentException(campo + " deve ser positivo.");
    }
}
