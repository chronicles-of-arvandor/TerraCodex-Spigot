package me.numilani.terracodex.data

data class Page(
    val id: String,
    var categoryId: String,
    var name: String,
    var contents: String,
    var tags: String,
    var revealedTo: String,
    var pageStatus: String
)
