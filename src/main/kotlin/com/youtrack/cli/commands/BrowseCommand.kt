package com.youtrack.cli.commands

import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.parameters.arguments.argument
import com.youtrack.cli.client.t
import com.youtrack.cli.config.ConfigManager
import java.awt.Desktop
import java.net.URI

class BrowseCommand : CliktCommand(
    name = "browse",
    help = "Open an issue in the browser  (e.g. yt browse DEMO-1)"
) {
    private val issueId by argument("ISSUE_ID", help = "Issue ID (e.g. DEMO-1)")

    override fun run() {
        val cfg = ConfigManager.requireValid()
        val url = "${cfg.baseUrl.trimEnd('/')}/issue/$issueId"
        t.println("Opening $url")
        openBrowser(url)
    }

    private fun openBrowser(url: String) {
        val os = System.getProperty("os.name").lowercase()
        val cmd = when {
            os.contains("linux") -> arrayOf("xdg-open", url)
            os.contains("mac")   -> arrayOf("open", url)
            os.contains("win")   -> arrayOf("cmd", "/c", "start", url)
            else -> {
                if (Desktop.isDesktopSupported()) {
                    Desktop.getDesktop().browse(URI(url))
                    return
                }
                t.println("Cannot detect OS — open manually: $url")
                return
            }
        }
        ProcessBuilder(*cmd).inheritIO().start()
    }
}
