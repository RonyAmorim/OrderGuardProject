package br.com.orderguard.model

data class Address(
    var street: String = "",
    var number: String = "",
    var neighborhood: String = "",
    var city: String = "",
    var state: String = "",
    var zipCode: String = ""
)
