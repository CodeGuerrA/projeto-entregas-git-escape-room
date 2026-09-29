package br.edu.entregas.service;

public class LoginService {
    private static final String USUARIO = "admin";
    private static final String SENHA = "12345678";

    public boolean autenticar(String usuario, String senha) {
        return USUARIO.equals(usuario) && SENHA.equals(senha);
    }
}
