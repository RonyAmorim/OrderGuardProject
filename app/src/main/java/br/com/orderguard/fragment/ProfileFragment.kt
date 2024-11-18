package br.com.orderguard.fragment

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import br.com.orderguard.R
import br.com.orderguard.databinding.ProfileSettingsFragmentBinding
import br.com.orderguard.screen.login.LoginScreen
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class ProfileFragment : Fragment() {

    // Variável para o View Binding
    private var _binding: ProfileSettingsFragmentBinding? = null
    private val binding get() = _binding!!
    private val auth = FirebaseAuth.getInstance()
    private val db = FirebaseFirestore.getInstance()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inicializar o View Binding
        _binding = ProfileSettingsFragmentBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Carregar os dados do usuário autenticado
        val currentUser = auth.currentUser
        if (currentUser != null) {
            val userId = currentUser.uid
            // Buscar os dados do usuário no Firestore
            db.collection("Users").document(userId).get().addOnSuccessListener { document ->
                if (document != null && document.exists()) {
                    // Atualizar o nome e as informações pessoais
                    val name = document.getString("companyName") ?: "Nome não encontrado"
                    val phone = document.getString("phone") ?: "Telefone não encontrado"
                    val email = document.getString("email") ?: "Email não encontrado"

                    // Preencher os TextViews com os dados
                    binding.userName.text = name
                    binding.telefone.text = phone
                    binding.email.text = email
                } else {
                    // Tratar o caso em que o documento não existe
                    binding.userName.text = "Usuário não encontrado"
                }
            }.addOnFailureListener { e ->
                // Tratar erros ao buscar os dados
                binding.userName.text = "Erro ao buscar dados: ${e.message}"
            }
        }

        // Configuração do botão de logout
        binding.logout.setOnClickListener {
            FirebaseAuth.getInstance().signOut() // Fazer logout do Firebase
            val intent = Intent(requireContext(), LoginScreen::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent) // Redirecionar para a tela de login
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null // Limpar o binding
    }
}
