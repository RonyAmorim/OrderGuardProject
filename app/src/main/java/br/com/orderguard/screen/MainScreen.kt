package br.com.orderguard.screen

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import br.com.orderguard.fragment.ClientsFragment
import br.com.orderguard.fragment.HomeFragment
import br.com.orderguard.fragment.OrdersFragment
import br.com.orderguard.fragment.ProfileFragment
import br.com.orderguard.R
import com.google.android.material.bottomnavigation.BottomNavigationView

class MainScreen : AppCompatActivity() {

    // Variável para rastrear o fragmento atual
    private var currentFragmentTag: String = "home"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.main_screen)

        // Carregar o fragmento inicial (Home)
        loadFragment(HomeFragment(), "home")

        // Configurar o BottomNavigationView
        val bottomNavigationView = findViewById<BottomNavigationView>(R.id.bottomNavigation)
        bottomNavigationView.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_home -> loadFragment(HomeFragment(), "home")
                R.id.nav_business -> loadFragment(ClientsFragment(), "clients")
                R.id.nav_orders -> loadFragment(OrdersFragment(), "orders")
                R.id.nav_settings -> loadFragment(ProfileFragment(), "settings")
            }
            true
        }
    }

    // Função para carregar o fragmento com animação baseada no fragmento atual e destino
    private fun loadFragment(fragment: Fragment, newFragmentTag: String) {
        val transaction = supportFragmentManager.beginTransaction()

        // Definir as animações baseadas no fragmento atual e no destino
        when (currentFragmentTag) {
            "home" -> {
                if (newFragmentTag == "clients" || newFragmentTag == "orders" || newFragmentTag == "settings") {
                    // Animações para sair da Home para a direita
                    transaction.setCustomAnimations(R.anim.slide_in_right, R.anim.slide_out_left)
                }
            }
            "clients" -> {
                if (newFragmentTag == "home") {
                    // Animação para voltar ao Home para a esquerda
                    transaction.setCustomAnimations(R.anim.slide_in_left, R.anim.slide_out_right)
                } else {
                    // Animação para sair dos Clientes para a direita
                    transaction.setCustomAnimations(R.anim.slide_in_right, R.anim.slide_out_left)
                }
            }
            "orders" -> {
                if (newFragmentTag == "settings") {
                    // Animação para ir das Ordens para Configurações para a direita
                    transaction.setCustomAnimations(R.anim.slide_in_right, R.anim.slide_out_left)
                } else {
                    // Animação para sair das Ordens para a esquerda
                    transaction.setCustomAnimations(R.anim.slide_in_left, R.anim.slide_out_right)
                }
            }
            "settings" -> {
                // Animação para voltar às Ordens ou qualquer outro fragmento para a esquerda
                transaction.setCustomAnimations(R.anim.slide_in_left, R.anim.slide_out_right)
            }
        }

        // Atualizar o fragmento atual
        currentFragmentTag = newFragmentTag

        // Substituir o fragmento e adicionar à pilha de volta
        transaction.replace(R.id.mainLayout, fragment)
        transaction.addToBackStack(null)
        transaction.commit()
    }
}
