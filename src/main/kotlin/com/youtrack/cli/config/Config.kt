package com.youtrack.cli.config

import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.io.File

@Serializable
data class Config(
    val baseUrl: String = "",
    val token: String = "",
    val defaultProject: String = ""
)

object ConfigManager {
    private val json = Json { prettyPrint = true; ignoreUnknownKeys = true }
    private val configDir: File get() = File(System.getProperty("user.home"), ".youtrack-cli")
    private val configFile: File get() = File(configDir, "config.json")

    fun load(): Config {
        if (!configFile.exists()) return Config()
        return try {
            json.decodeFromString<Config>(configFile.readText())
        } catch (_: Exception) {
            Config()
        }
    }

    fun save(config: Config) {
        configDir.mkdirs()
        configFile.writeText(json.encodeToString(config))
        configFile.setReadable(false, false)
        configFile.setReadable(true, true) // owner-only read
    }

    fun requireValid(): Config {
        val cfg = load()
        if (cfg.baseUrl.isBlank()) error("YouTrack URL not configured. Run: yt config set --url <URL> --token <TOKEN>")
        if (cfg.token.isBlank()) error("API token not configured. Run: yt config set --url <URL> --token <TOKEN>")
        return cfg
    }
}
