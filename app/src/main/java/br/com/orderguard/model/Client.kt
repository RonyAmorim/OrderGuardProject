package br.com.orderguard.model

data class Client(
    var id: String? = null, // Adicione o campo de ID
    var fullName: String = "",
    var cpfCnpj: String = "",
    var email: String = "",
    var phone: String = "",
    var address: Address = Address(),
    var notes: List<String> = emptyList(),
    var createdAt: Long = 0L, // Representando a data de criação em milissegundos
    var updatedAt: Long = 0L  // Representando a data de atualização em milissegundos
)
