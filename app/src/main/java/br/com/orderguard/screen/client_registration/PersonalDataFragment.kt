package br.com.orderguard.screen.client_registration

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import br.com.orderguard.databinding.PersonalDataFragmentBinding
import br.com.orderguard.model.Client
import okhttp3.*
import org.json.JSONObject
import java.io.IOException

class PersonalDataFragment(private val clientData: Client) : Fragment() {

    private var _binding: PersonalDataFragmentBinding? = null
    private val binding get() = _binding!!
    private val client = OkHttpClient()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = PersonalDataFragmentBinding.inflate(inflater, container, false)
        applyMasks()
        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun applyMasks() {
        applyCpfCnpjMask()
        applyPhoneMask()
    }

    private fun applyCpfCnpjMask() {
        binding.cpfInput.addTextChangedListener(object : TextWatcher {
            private var isUpdating = false
            private var oldText = ""

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
                oldText = s?.toString() ?: ""
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                if (isUpdating) return

                val currentText = s?.toString() ?: ""
                val unmasked = currentText.replace(Regex("[^\\d]"), "")

                // Verifica se é uma exclusão
                if (before > 0 && count == 0) {
                    // Permite exclusão sem aplicar máscara imediatamente
                    return
                }

                val mask = if (unmasked.length > 11) "##.###.###/####-##" else "###.###.###-##"

                var masked = ""
                var index = 0

                for (char in mask) {
                    if (char == '#') {
                        if (index >= unmasked.length) break
                        masked += unmasked[index]
                        index++
                    } else {
                        masked += char
                    }
                }

                isUpdating = true
                binding.cpfInput.setText(masked)
                binding.cpfInput.setSelection(masked.length)
                isUpdating = false
            }

            override fun afterTextChanged(s: Editable?) {
                if (isUpdating) return

                val unmasked = s?.toString()?.replace(Regex("[^\\d]"), "") ?: ""

                if (unmasked.length > 11) {
                    fetchCnpjDetails(unmasked)
                } else if (unmasked.length == 11) {
                    validateCpf(unmasked)
                }
            }
        })
    }

    private fun applyPhoneMask() {
        binding.phoneInput.addTextChangedListener(object : TextWatcher {
            private var isUpdating = false
            private var previousText = ""

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
                previousText = s?.toString() ?: ""
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                if (isUpdating) return

                val currentText = s?.toString() ?: ""
                val unmasked = currentText.replace(Regex("[^\\d]"), "")
                val previousUnmasked = previousText.replace(Regex("[^\\d]"), "")

                // Detecta exclusão para evitar reposicionamento indesejado
                if (unmasked == previousUnmasked) {
                    return
                }

                val mask = "(##) #####-####"
                var masked = ""
                var index = 0

                for (char in mask) {
                    if (char == '#') {
                        if (index >= unmasked.length) break
                        masked += unmasked[index]
                        index++
                    } else {
                        masked += char
                    }
                }

                isUpdating = true
                binding.phoneInput.setText(masked)
                // Garante que o cursor seja posicionado corretamente
                binding.phoneInput.setSelection(masked.length)
                isUpdating = false
            }

            override fun afterTextChanged(s: Editable?) {}
        })
    }

    private fun validateCpf(cpf: String): Boolean {
        if (cpf.length != 11 || cpf.all { it == cpf[0] }) {
            binding.cpfInput.error = "CPF inválido"
            return false
        }

        try {
            val numbers = cpf.map { it.toString().toInt() }

            val verifier1 = (0..8).sumOf { (10 - it) * numbers[it] } % 11
            val calculatedVerifier1 = if (verifier1 < 2) 0 else 11 - verifier1

            val verifier2 = (0..9).sumOf { (11 - it) * numbers[it] } % 11
            val calculatedVerifier2 = if (verifier2 < 2) 0 else 11 - verifier2

            return if (calculatedVerifier1 == numbers[9] && calculatedVerifier2 == numbers[10]) {
                true
            } else {
                binding.cpfInput.error = "CPF inválido"
                false
            }
        } catch (e: Exception) {
            binding.cpfInput.error = "CPF inválido"
            return false
        }
    }

    private fun fetchCnpjDetails(cnpj: String) {
        val url = "https://brasilapi.com.br/api/cnpj/v1/$cnpj"
        val request = Request.Builder().url(url).build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                requireActivity().runOnUiThread {
                    Toast.makeText(requireContext(), "Erro ao validar o CNPJ", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onResponse(call: Call, response: Response) {
                if (response.isSuccessful) {
                    val jsonResponse = response.body?.string()
                    val jsonObject = JSONObject(jsonResponse ?: "{}")

                    requireActivity().runOnUiThread {
                        if (jsonObject.has("error")) {
                            binding.cpfInput.error = "CNPJ inválido ou não encontrado"
                        } else {
                            Toast.makeText(requireContext(), "CNPJ válido", Toast.LENGTH_SHORT).show()
                        }
                    }
                } else {
                    requireActivity().runOnUiThread {
                        binding.cpfInput.error = "CNPJ inválido ou não encontrado"
                    }
                }
            }
        })
    }

    fun validateInputs(): Boolean {
        val name = binding.clientNameInput.text.toString()
        val cpfCnpj = binding.cpfInput.text.toString().replace(Regex("[^\\d]"), "")
        val email = binding.emailInput.text.toString()
        val phone = binding.phoneInput.text.toString()

        return when {
            name.isBlank() -> {
                Toast.makeText(requireContext(), "O nome é obrigatório!", Toast.LENGTH_SHORT).show()
                false
            }
            cpfCnpj.isBlank() -> {
                Toast.makeText(requireContext(), "O CPF/CNPJ é obrigatório!", Toast.LENGTH_SHORT).show()
                false
            }
            cpfCnpj.length <= 11 && !validateCpf(cpfCnpj) -> false
            !android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches() -> {
                Toast.makeText(requireContext(), "Digite um email válido!", Toast.LENGTH_SHORT).show()
                false
            }
            phone.isBlank() -> {
                Toast.makeText(requireContext(), "O telefone é obrigatório!", Toast.LENGTH_SHORT).show()
                false
            }
            else -> {
                clientData.fullName = name
                clientData.cpfCnpj = cpfCnpj
                clientData.email = email
                clientData.phone = phone
                true
            }
        }
    }
}
