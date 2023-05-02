package me.numilani.terracodex.data

data class Page(
    val Id: String,
    var CategoryId: String,
    var Contents: String,
    var Tags: String,
    var RevealedTo: String,
    var PageStatus: String
)
