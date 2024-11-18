package br.com.orderguard.fragment

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import br.com.orderguard.screen.ClientRegistrationScreen
import br.com.orderguard.R

class ClientsFragment : Fragment() {
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.list_clients_fragment, container, false)

        // Adiciona o listener de clique no botão "addButton"
        val addButton: View = view.findViewById(R.id.addButton)
        addButton.setOnClickListener {
            // Navega para a tela ClientRegistrationScreen
            val intent = Intent(activity, ClientRegistrationScreen::class.java)
            startActivity(intent)
        }

        return view
    }
}
