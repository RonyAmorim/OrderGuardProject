package br.com.orderguard.screen.client_registration

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import br.com.orderguard.databinding.NotesFragmentBinding
import br.com.orderguard.model.Client

class NotesFragment(private val clientData: Client) : Fragment() {

    private var _binding: NotesFragmentBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = NotesFragmentBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    fun validateInputs(): Boolean {
        val notes = binding.notesInput.text.toString()
        clientData.notes = if (notes.isNotBlank()) {
            notes.split("\n").map { it.trim() }
        } else {
            emptyList()
        }
        return true
    }
}
