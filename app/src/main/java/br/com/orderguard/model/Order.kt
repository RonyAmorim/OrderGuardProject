package br.com.orderguard.model

data class Order(
    val title: String, // Título da ordem
    val description: String, // Descrição detalhada
    val status: String, // Status da ordem ("Pending", "Completed", etc.)
    val deadline: String, // Prazo para entrega ou realização (Data de Criação)
    val totalCost: Double, // Custo total da ordem
    val serviceDetails: List<ServiceDetail>, // Lista de detalhes dos serviços
    val notes: List<String> // Lista de notas adicionais
)

