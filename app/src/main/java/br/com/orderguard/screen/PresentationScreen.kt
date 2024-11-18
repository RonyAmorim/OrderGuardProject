package br.com.orderguard.screen

import android.content.Intent
import android.os.Bundle
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import br.com.orderguard.R
import br.com.orderguard.screen.login.LoginScreen
import br.com.orderguard.screen.singup.SingUpScreen

class PresentationScreen : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.presentation_screen)

        // Configuração dos Window Insets para ajustar o layout às barras do sistema
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.mainLayout)) { view, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            view.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // Referência ao loginButton
        val loginButton: TextView = findViewById(R.id.loginButton)

        // Configuração do Listener de Clique para loginButton
        loginButton.setOnClickListener {
            navigateToFormLoginActivity()
        }

        // Referência ao registerTextView para redirecionar ao FormSignUpActivity
        val registerTextView: TextView = findViewById(R.id.registerTextView)

        // Configuração do Listener de Clique para registerTextView
        registerTextView.setOnClickListener {
            navigateToFormSignUpActivity()
        }
    }

    // Método para navegar para a FormLoginActivity com animações
    private fun navigateToFormLoginActivity() {
        val intent = Intent(this, LoginScreen::class.java)
        startActivity(intent)
        overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left)
        finish()
    }

    // Método para navegar para a FormSignUpActivity com animações
    private fun navigateToFormSignUpActivity() {
        val intent = Intent(this, SingUpScreen::class.java)
        startActivity(intent)
        overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left)
        finish()
    }
}
