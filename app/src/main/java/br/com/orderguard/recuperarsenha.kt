package br.com.orderguard

import android.os.Bundle
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.firebase.auth.FirebaseAuth

class RecuperarSenha : AppCompatActivity() {

    // Inicializa a instância do FirebaseAuth
    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()  // Para usar modo edge-to-edge (barra de status e navegação)
        setContentView(R.layout.activity_recuperarsenha)

        // Inicializa o FirebaseAuth
        auth = FirebaseAuth.getInstance()

        // Configura os elementos da tela
        val forgotPasswordTextView: TextView = findViewById(R.id.forgotPasswordTextView)
        val emailEditText: EditText = findViewById(R.id.emailEditText)

        // Listener para "Esqueceu a senha?"
        forgotPasswordTextView.setOnClickListener {
            val email = emailEditText.text.toString().trim()

            // Verifica se o campo de email não está vazio
            if (email.isEmpty()) {
                Toast.makeText(this, "Por favor, insira seu email para redefinir a senha", Toast.LENGTH_SHORT).show()
            } else {
                // Envia o e-mail de redefinição de senha através do Firebase Authentication
                auth.sendPasswordResetEmail(email)
                    .addOnCompleteListener { task ->
                        if (task.isSuccessful) {
                            // Sucesso no envio do e-mail
                            Toast.makeText(this, "Email de redefinição de senha enviado!", Toast.LENGTH_SHORT).show()
                        } else {
                            // Caso ocorra um erro
                            Toast.makeText(this, "Erro ao enviar o email: ${task.exception?.message}", Toast.LENGTH_SHORT).show()
                        }
                    }
            }
        }

        // Ajusta o layout para o modo de barras de sistema (opcional, dependendo do seu design)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.mainLayout)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
    }
}
