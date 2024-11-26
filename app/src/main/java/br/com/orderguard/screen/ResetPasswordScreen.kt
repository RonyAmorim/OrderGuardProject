package br.com.orderguard.screen

import android.content.Intent
import android.os.Bundle
import android.text.InputType
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import br.com.orderguard.R
import br.com.orderguard.databinding.ResetPasswordScreenBinding
import br.com.orderguard.screen.home.MainScreen
import com.google.firebase.auth.EmailAuthProvider
import com.google.firebase.auth.FirebaseAuth

class ResetPasswordScreen : AppCompatActivity() {

    private lateinit var binding: ResetPasswordScreenBinding
    private var isCurrentPasswordVisible = false
    private var isPasswordVisible = false
    private var isConfirmPasswordVisible = false
    private val auth = FirebaseAuth.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ResetPasswordScreenBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Configura ajustes de layout para insets
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // Configura os botões de visibilidade das senhas
        togglePasswordVisibility(binding.currentPasswordInput, binding.toggleCurrentPasswordVisibility) {
            isCurrentPasswordVisible = !isCurrentPasswordVisible
        }
        togglePasswordVisibility(binding.passwordEditText, binding.togglePasswordVisibility) {
            isPasswordVisible = !isPasswordVisible
        }
        togglePasswordVisibility(binding.confirmaSenhaEditText, binding.toggleConfirmPasswordVisibility) {
            isConfirmPasswordVisible = !isConfirmPasswordVisible
        }

        // Botão de voltar
        binding.btnBack.setOnClickListener {
            navigateToMainScreen()
        }

        // Botão para salvar nova senha
        binding.btnSavePassword.setOnClickListener {
            changePassword()
        }
    }

    private fun togglePasswordVisibility(editText: android.widget.EditText, toggleButton: android.widget.ImageView, toggleState: () -> Unit) {
        toggleButton.setOnClickListener {
            toggleState()
            if (editText.inputType == InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD) {
                editText.inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD
                toggleButton.setImageResource(R.drawable.baseline_visibility_off_24)
            } else {
                editText.inputType = InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD
                toggleButton.setImageResource(R.drawable.baseline_visibility_24)
            }
            editText.setSelection(editText.text.length) // Move o cursor para o final
        }
    }

    private fun changePassword() {
        val currentPassword = binding.currentPasswordInput.text.toString().trim()
        val newPassword = binding.passwordEditText.text.toString().trim()
        val confirmPassword = binding.confirmaSenhaEditText.text.toString().trim()

        // Validação de entrada
        if (currentPassword.isEmpty() || newPassword.isEmpty() || confirmPassword.isEmpty()) {
            showToast("Por favor, preencha todos os campos.")
            return
        }

        if (newPassword != confirmPassword) {
            showToast("A nova senha e a confirmação não correspondem.")
            return
        }

        val user = auth.currentUser
        if (user != null && user.email != null) {
            val credential = EmailAuthProvider.getCredential(user.email!!, currentPassword)

            // Reautentica o usuário
            user.reauthenticate(credential).addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    // Atualiza a senha no Firebase Authentication
                    user.updatePassword(newPassword).addOnCompleteListener { updateTask ->
                        if (updateTask.isSuccessful) {
                            showToast("Senha alterada com sucesso.")
                            navigateToMainScreen()
                        } else {
                            showToast("Erro ao atualizar a senha: ${updateTask.exception?.message}")
                        }
                    }
                } else {
                    showToast("Senha atual incorreta.")
                }
            }
        }
    }

    private fun navigateToMainScreen() {
        val intent = Intent(this, MainScreen::class.java)
        intent.putExtra("navigateTo", "settings")
        startActivity(intent)
        finish() // Fecha a tela atual
    }

    private fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }
}
