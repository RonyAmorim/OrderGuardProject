package br.com.orderguard.model

data class Order(
    var title: String = "",
    var description: String = "",
    var status: String = "",
    var createdAt: Long = System.currentTimeMillis(), // Adicionado o campo createdAt com valor padrão
    var deadline: String = "",
    var totalCost: Double = 0.0,
    var client: String = "",
    var serviceDetails: List<ServiceDetail> = emptyList(),
    var notes: List<String> = emptyList()
)
