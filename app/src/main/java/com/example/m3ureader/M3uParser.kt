package com.example.m3ureader

/**
 * Parser simples de listas M3U / M3U8 (formato estendido usado por IPTV).
 *
 * Cada canal é descrito por uma linha `#EXTINF:...,Nome do canal` seguida da
 * linha com a URL do stream. Atributos comuns como `tvg-logo` e `group-title`
 * são extraídos quando presentes.
 */
object M3uParser {

    private val attrRegex = Regex("([A-Za-z0-9_-]+)=\"([^\"]*)\"")

    fun parse(content: String): List<Channel> {
        val channels = mutableListOf<Channel>()
        var pendingName: String? = null
        var pendingLogo: String? = null
        var pendingGroup: String? = null

        content.lineSequence().forEach { rawLine ->
            val line = rawLine.trim()
            when {
                line.isEmpty() -> Unit
                line.startsWith("#EXTM3U", ignoreCase = true) -> Unit
                line.startsWith("#EXTINF", ignoreCase = true) -> {
                    val attrs = attrRegex.findAll(line)
                        .associate { it.groupValues[1].lowercase() to it.groupValues[2] }
                    pendingLogo = attrs["tvg-logo"]
                    pendingGroup = attrs["group-title"]
                    val commaIdx = line.indexOf(',')
                    pendingName = if (commaIdx in 0 until line.length - 1) {
                        line.substring(commaIdx + 1).trim()
                    } else {
                        attrs["tvg-name"]
                    }
                }
                line.startsWith("#") -> Unit
                else -> {
                    val name = pendingName?.takeIf { it.isNotBlank() } ?: line
                    channels.add(
                        Channel(
                            name = name,
                            url = line,
                            logo = pendingLogo?.takeIf { it.isNotBlank() },
                            group = pendingGroup?.takeIf { it.isNotBlank() }
                        )
                    )
                    pendingName = null
                    pendingLogo = null
                    pendingGroup = null
                }
            }
        }
        return channels
    }
}
