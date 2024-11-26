package br.com.orderguard.model

data class ServiceDetail(
    val serviceName: String, // Nome do serviço
    val cost: Double, // Custo do serviço
    val quantity: Int // Quantidade de serviços realizados
)