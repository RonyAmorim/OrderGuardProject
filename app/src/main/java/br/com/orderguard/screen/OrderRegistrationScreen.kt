package br.com.orderguard.screen

import android.content.Intent
import android.os.Bundle
import android.widget.ImageButton
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.graphics.Insets
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import br.com.orderguard.R

class OrderRegistrationScreen : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge() // Habilita o modo EdgeToEdge
        setContentView(R.layout.order_registration_screen)

        // Configura o listener para aplicar os WindowInsets
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
                val systemBars: Insets = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // Referência ao textView8 para redirecionar ao FromSignUpActivity
        val textView8: ImageButton = findViewById(R.id.btn_back)

        // Configuração do Listener de Clique para textView8
        textView8.setOnClickListener {
            val intent = Intent(this, MainScreen::class.java)
            startActivity(intent)
            // Aplica animação de transição ao clicar
            overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right)
            finish()
        }
    }
}
