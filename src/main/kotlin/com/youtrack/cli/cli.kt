package com.youtrack.cli

import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.core.subcommands
import com.youtrack.cli.commands.*

class YouTrackCli : CliktCommand(
    name = "yt",
    help = """
        YouTrack CLI — manage issues, boards, and activity from your terminal.

        Configure first:

            yt config set --url https://company.youtrack.cloud --token perm:xxxx

        Then use subcommands to work with YouTrack.
    """.trimIndent(),
    invokeWithoutSubcommand = true,
    printHelpOnEmptyArgs = true
) {
    override fun run() = Unit
}

fun main(args: Array<String>) {
    YouTrackCli()
        .subcommands(
            ConfigCommand(),
            IssueCommand(),
            SearchCommand(),
            BoardCommand(),
            ActivityCommand(),
            ProjectCommand(),
            UserCommand(),
            TagCommand(),
            BrowseCommand()
        )
        .main(args)
}
