package com.example.m3ureader

/** Um canal/entrada de uma lista M3U. */
data class Channel(
    val name: String,
    val url: String,
    val logo: String? = null,
    val group: String? = null
)
