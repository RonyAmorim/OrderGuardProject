package br.com.orderguard.screen.login

import android.content.Intent
import android.os.Bundle
import android.text.InputType
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import br.com.orderguard.R
import br.com.orderguard.databinding.LoginScreenBinding
import br.com.orderguard.screen.MainScreen
import br.com.orderguard.screen.singup.SingUpScreen
import com.google.firebase.auth.FirebaseAuth

class LoginScreen : AppCompatActivity() {
    private lateinit var auth: FirebaseAuth
    private lateinit var binding: LoginScreenBinding
    private var isPasswordVisible = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = LoginScreenBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Configuração dos Window Insets
        ViewCompat.setOnApplyWindowInsetsListener(binding.mainLayout) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // Inicialize o FirebaseAuth
        auth = FirebaseAuth.getInstance()

        // Configuração da visibilidade da senha
        binding.togglePasswordVisibility.setOnClickListener {
            if (isPasswordVisible) {
                binding.passwordEditText.inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD
                binding.togglePasswordVisibility.setImageResource(R.drawable.baseline_visibility_off_24)
            } else {
                binding.passwordEditText.inputType = InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD
                binding.togglePasswordVisibility.setImageResource(R.drawable.baseline_visibility_24)
            }
            // Mantenha o cursor no final do texto
            binding.passwordEditText.setSelection(binding.passwordEditText.text.length)
            isPasswordVisible = !isPasswordVisible
        }

        // Configuração do Listener de Clique para o botão de login
        binding.loginButton.setOnClickListener {
            val email = binding.emailEditText.text.toString().trim()
            val password = binding.passwordEditText.text.toString().trim()

            // Verificar se os campos estão preenchidos
            if (email.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "Por favor, preencha todos os campos", Toast.LENGTH_SHORT).show()
            } else {
                loginUser(email, password)
            }
        }

        // Configuração do Listener de Clique para "Esqueceu a senha?"
        binding.forgotPasswordTextView.setOnClickListener {
            val email = binding.emailEditText.text.toString().trim()
            if (email.isEmpty()) {
                Toast.makeText(this, "Por favor, insira seu email para redefinir a senha", Toast.LENGTH_SHORT).show()
            } else {
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

        // Listener para redirecionar ao SingUpScreen
        binding.registerTextView.setOnClickListener {
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
                    val intent = Intent(this, MainScreen::class.java)
                    startActivity(intent)
                    finish() // Finaliza a atividade atual para não voltar ao login
                } else {
                    // Exiba a mensagem de erro
                    Toast.makeText(this, "Falha na autenticação: ${task.exception?.message}", Toast.LENGTH_SHORT).show()
                }
            }
    }
}
