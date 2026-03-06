package com.youtrack.cli.commands

import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.core.subcommands
import com.github.ajalt.clikt.parameters.options.option
import com.github.ajalt.clikt.parameters.options.required
import com.youtrack.cli.client.t
import com.youtrack.cli.config.Config
import com.youtrack.cli.config.ConfigManager
import com.github.ajalt.mordant.rendering.TextColors.*
import com.github.ajalt.mordant.rendering.TextStyles.*

class ConfigCommand : CliktCommand(
    name = "config",
    help = "Manage YouTrack CLI configuration"
) {
    init {
        subcommands(ConfigSet(), ConfigShow(), ConfigClear())
    }

    override fun run() = Unit
}

class ConfigSet : CliktCommand(
    name = "set",
    help = "Set YouTrack server URL, API token, and optional default project"
) {
    private val url by option("--url", help = "YouTrack base URL (e.g. https://company.youtrack.cloud)").required()
    private val token by option("--token", help = "Permanent API token").required()
    private val project by option("--project", help = "Default project short name")

    override fun run() {
        val cfg = ConfigManager.load().copy(
            baseUrl = url.trimEnd('/'),
            token = token,
            defaultProject = project ?: ConfigManager.load().defaultProject
        )
        ConfigManager.save(cfg)
        t.println(green("✓ Configuration saved."))
    }
}

class ConfigShow : CliktCommand(
    name = "show",
    help = "Display current configuration (token is masked)"
) {
    override fun run() {
        val cfg = ConfigManager.load()
        if (cfg.baseUrl.isBlank()) {
            t.println(yellow("No configuration found. Run: yt config set --url <URL> --token <TOKEN>"))
            return
        }
        val maskedToken = if (cfg.token.length > 8)
            cfg.token.take(4) + "****" + cfg.token.takeLast(4)
        else "****"
        t.println(bold("YouTrack CLI Configuration"))
        t.println("  ${bold("URL:")}             ${cyan(cfg.baseUrl)}")
        t.println("  ${bold("Token:")}           $maskedToken")
        t.println("  ${bold("Default project:")} ${cfg.defaultProject.ifBlank { "(none)" }}")
    }
}

class ConfigClear : CliktCommand(
    name = "clear",
    help = "Remove stored configuration"
) {
    override fun run() {
        ConfigManager.save(Config())
        t.println(yellow("Configuration cleared."))
    }
}
