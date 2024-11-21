package br.com.orderguard.screen.client_registration

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import br.com.orderguard.databinding.AddressFragmentBinding
import br.com.orderguard.model.Address
import br.com.orderguard.model.Client
import okhttp3.*
import org.json.JSONObject
import java.io.IOException

class AddressFragment(private val clientData: Client) : Fragment() {

    private var _binding: AddressFragmentBinding? = null
    private val binding get() = _binding!!

    private val client = OkHttpClient()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = AddressFragmentBinding.inflate(inflater, container, false)
        applyZipCodeMask()
        setupZipCodeListener()
        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun applyZipCodeMask() {
        binding.zipCodeInput.addTextChangedListener(object : TextWatcher {
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

                val mask = "#####-###"
                var masked = ""
                var index = 0

                for (char in mask) {
                    if (char == '#') {
                        if (index >= unmasked.length) break
                        masked += unmasked[index]
                        index++
                    } else {
                        if (index >= unmasked.length) break
                        masked += char
                    }
                }

                isUpdating = true
                binding.zipCodeInput.setText(masked)
                // Garante que o cursor não salte para posições inválidas
                binding.zipCodeInput.setSelection(masked.length)
                isUpdating = false
            }

            override fun afterTextChanged(s: Editable?) {}
        })
    }

    private fun setupZipCodeListener() {
        binding.zipCodeInput.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                val zipCode = s?.toString()?.replace("-", "") ?: ""
                if (zipCode.length == 8) {
                    fetchAddressByZipCode(zipCode)
                }
            }
        })
    }

    private fun fetchAddressByZipCode(zipCode: String) {
        val url = "https://brasilapi.com.br/api/cep/v1/$zipCode"
        val request = Request.Builder().url(url).build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                requireActivity().runOnUiThread {
                    Toast.makeText(requireContext(), "Erro ao buscar o CEP.", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onResponse(call: Call, response: Response) {
                if (response.isSuccessful) {
                    val jsonResponse = response.body?.string()
                    val jsonObject = JSONObject(jsonResponse ?: "{}")

                    requireActivity().runOnUiThread {
                        binding.streetInput.setText(jsonObject.optString("street", ""))
                        binding.neighborhoodInput.setText(jsonObject.optString("neighborhood", ""))
                        binding.cityInput.setText(jsonObject.optString("city", ""))
                        binding.stateInput.setText(jsonObject.optString("state", ""))
                    }
                } else {
                    requireActivity().runOnUiThread {
                        Toast.makeText(requireContext(), "CEP não encontrado.", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        })
    }

    fun validateInputs(): Boolean {
        val street = binding.streetInput.text.toString()
        val number = binding.numberInput.text.toString()
        val neighborhood = binding.neighborhoodInput.text.toString()
        val city = binding.cityInput.text.toString()
        val state = binding.stateInput.text.toString()
        val zipCode = binding.zipCodeInput.text.toString()

        return when {
            street.isBlank() -> {
                Toast.makeText(requireContext(), "A rua é obrigatória!", Toast.LENGTH_SHORT).show()
                false
            }
            number.isBlank() -> {
                Toast.makeText(requireContext(), "O número é obrigatório!", Toast.LENGTH_SHORT).show()
                false
            }
            city.isBlank() -> {
                Toast.makeText(requireContext(), "A cidade é obrigatória!", Toast.LENGTH_SHORT).show()
                false
            }
            state.isBlank() -> {
                Toast.makeText(requireContext(), "O estado é obrigatório!", Toast.LENGTH_SHORT).show()
                false
            }
            zipCode.isBlank() -> {
                Toast.makeText(requireContext(), "O CEP é obrigatório!", Toast.LENGTH_SHORT).show()
                false
            }
            else -> {
                clientData.address = Address(
                    street = street,
                    number = number,
                    neighborhood = neighborhood,
                    city = city,
                    state = state,
                    zipCode = zipCode
                )
                true
            }
        }
    }
}
