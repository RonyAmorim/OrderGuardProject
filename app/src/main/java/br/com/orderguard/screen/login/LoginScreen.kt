package br.com.orderguard.screen.login

import android.content.Intent
import android.os.Bundle
import android.text.InputType
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import br.com.orderguard.R
import br.com.orderguard.screen.MainScreen
import br.com.orderguard.screen.singup.SingUpScreen
import com.google.firebase.auth.FirebaseAuth

class LoginScreen : AppCompatActivity() {
    private lateinit var auth: FirebaseAuth
    private var isPasswordVisible = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.login_screen)

        // Configuração dos Window Insets
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.mainLayout)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // Inicialize o FirebaseAuth
        auth = FirebaseAuth.getInstance()

        // Referência aos campos de entrada de email e senha
        val emailEditText: EditText = findViewById(R.id.emailEditText)
        val passwordEditText: EditText = findViewById(R.id.passwordEditText)
        val togglePasswordVisibility: ImageView = findViewById(R.id.togglePasswordVisibility)
        val loginButton: TextView = findViewById(R.id.loginButton)
        val forgotPasswordTextView: TextView = findViewById(R.id.forgotPasswordTextView)

        // Configuração da visibilidade da senha
        togglePasswordVisibility.setOnClickListener {
            if (isPasswordVisible) {
                // Se a senha estiver visível, oculte-a
                passwordEditText.inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD
                togglePasswordVisibility.setImageResource(R.drawable.baseline_visibility_off_24)
            } else {
                // Se a senha estiver oculta, mostre-a
                passwordEditText.inputType = InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD
                togglePasswordVisibility.setImageResource(R.drawable.baseline_visibility_24)
            }
            // Mantenha o cursor no final do texto
            passwordEditText.setSelection(passwordEditText.text.length)
            isPasswordVisible = !isPasswordVisible
        }

        // Configuração do Listener de Clique para o botão de login
        loginButton.setOnClickListener {
            val email = emailEditText.text.toString().trim()
            val password = passwordEditText.text.toString().trim()

            // Verificar se os campos estão preenchidos
            if (email.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "Por favor, preencha todos os campos", Toast.LENGTH_SHORT).show()
            } else {
                loginUser(email, password)
            }
        }

        // Configuração do Listener de Clique para "Esqueceu a senha?"
        forgotPasswordTextView.setOnClickListener {
            val email = emailEditText.text.toString().trim()
            if (email.isEmpty()) {
                Toast.makeText(this, "Por favor, insira seu email para redefinir a senha", Toast.LENGTH_SHORT).show()
            } else {
                // Envia um email de redefinição de senha
                auth.sendPasswordResetEmail(email)
                    .addOnCompleteListener { task ->
                        if (task.isSuccessful) {
                            Toast.makeText(this, "Email de redefinição de senha enviado!", Toast.LENGTH_SHORT).show()
                        } else {
                            Toast.makeText(this, "Erro ao enviar email: ${task.exception?.message}", Toast.LENGTH_SHORT).show()
                        }
                    }
            }
        }

        // Referência ao TextView para redirecionar ao FormSignUpActivity
        val registerTextView: TextView = findViewById(R.id.registerTextView)
        registerTextView.setOnClickListener {
            val intent = Intent(this, SingUpScreen::class.java)
            startActivity(intent)
            overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left)
        }
    }

    // Método para autenticar o usuário
    private fun loginUser(email: String, password: String) {
        auth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    // Login bem-sucedido, navegue para a próxima página
                    val intent = Intent(this, MainScreen::class.java) // Substitua "MainActivity" pela sua próxima Activity
                    startActivity(intent)
                    finish() // Finaliza a atividade atual para não voltar ao login
                } else {
                    // Exiba a mensagem de erro
                    Toast.makeText(this, "Falha na autenticação: ${task.exception?.message}", Toast.LENGTH_SHORT).show()
                }
            }
    }
}
