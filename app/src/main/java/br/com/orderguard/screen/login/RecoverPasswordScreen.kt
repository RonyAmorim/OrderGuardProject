package br.com.orderguard.screen.login

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import br.com.orderguard.R
import br.com.orderguard.databinding.RecoverPasswordScreenBinding
import com.google.firebase.auth.FirebaseAuth

class RecoverPasswordScreen : AppCompatActivity() {

    private lateinit var binding: RecoverPasswordScreenBinding
    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Configuração do View Binding
        binding = RecoverPasswordScreenBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Inicializa o FirebaseAuth
        auth = FirebaseAuth.getInstance()

        // Configuração do botão "Voltar"
        binding.btnBackRec.setOnClickListener {
            navigateToLoginScreen()
        }

        // Configuração do botão "Redefinir Senha"
        binding.resetPasswordButton.setOnClickListener {
            val email = binding.emailEditText.text.toString().trim()

            if (email.isEmpty()) {
                Toast.makeText(this, "Por favor, insira seu email para redefinir a senha", Toast.LENGTH_SHORT).show()
            } else {
                // Envia o e-mail de redefinição de senha
                auth.sendPasswordResetEmail(email)
                    .addOnCompleteListener { task ->
                        if (task.isSuccessful) {
                            // Exibe mensagem de sucesso e redireciona para a tela de login
                            Toast.makeText(this, "Verifique seu email para redefinir a senha.", Toast.LENGTH_LONG).show()
                            navigateToLoginScreen()
                        } else {
                            // Exibe mensagem de erro
                            Toast.makeText(
                                this,
                                "Erro ao enviar o email: ${task.exception?.message}",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    }
            }
        }
    }

    // Função para navegar para a tela de login
    private fun navigateToLoginScreen() {
        val intent = Intent(this, LoginScreen::class.java)
        startActivity(intent)
        finish() // Finaliza a activity atual
    }
}
