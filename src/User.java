public class User {
    private String email;
    private String password;
    private String token;

    private static void validate_email() {
        // Faz a validação do email

        // Caso invalido, avisa o usuario
        // Permanece na primeira etapa

        // Caso valido mas bloqueado, avisa o usuario
        // Permanece na primeira etapa

        // Caso valido, vai para segunda etapa
    }

    private static void validate_password() {
        // Se negativa, avisa o usuario
        // Contabiliza um erro de verificacao de senha pessoal

        // Se houver 3 erros consecutivos, volta para a primeira etapa
        // O acesso desse usuario fica bloqueado por 2 minutos 

        // Se positiva, vai para terceira etapa
    }

    private static validate_token() { // Google Authenticator
        // Se negativa, avisa o usuario
        // Contabiliza um erro de verificacao de token

        // Se houver 3 erros consecutivos, volta para a primeira etapa
        // O acesso desse usuario fica bloqueado por 2 minutos 

        // Se positiva, permite acesso desse usuario ao sistema 
    }
}