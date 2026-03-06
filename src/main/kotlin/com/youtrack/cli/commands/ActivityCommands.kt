package com.youtrack.cli.commands

import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.core.subcommands
import com.github.ajalt.clikt.parameters.arguments.argument
import com.github.ajalt.clikt.parameters.options.default
import com.github.ajalt.clikt.parameters.options.option
import com.github.ajalt.clikt.parameters.types.int
import com.github.ajalt.mordant.rendering.TextColors.*
import com.github.ajalt.mordant.rendering.TextStyles.*
import com.youtrack.cli.client.printActivity
import com.youtrack.cli.client.t

class ActivityCommand : CliktCommand(
    name = "activity",
    help = "View activity streams"
) {
    init { subcommands(ActivityUser(), ActivityIssue()) }
    override fun run() = Unit
}

class ActivityUser : CliktCommand(
    name = "user",
    help = "Show recent activity for a user (login name)"
) {
    private val login by argument("LOGIN", help = "User login name (or 'me')")
    private val top by option("--top", "-n", help = "Max results (default 30)").int().default(30)

    override fun run() = withClient { client ->
        val items = client.getUserActivity(login, top)
        if (items.isEmpty()) {
            t.println(yellow("No activity found for user '$login'."))
            return@withClient
        }
        t.println(bold("Activity for '$login' (${items.size} events):"))
        items.forEach { printActivity(it) }
    }
}

class ActivityIssue : CliktCommand(
    name = "issue",
    help = "Show activity history for a specific issue"
) {
    private val issueId by argument("ISSUE_ID", help = "Issue ID (e.g. PROJ-42)")
    private val top by option("--top", "-n", help = "Max results (default 50)").int().default(50)

    override fun run() = withClient { client ->
        val items = client.getIssueActivity(issueId, top)
        if (items.isEmpty()) {
            t.println(yellow("No activity found for issue '$issueId'."))
            return@withClient
        }
        t.println(bold("Activity for '$issueId' (${items.size} events):"))
        items.forEach { printActivity(it) }
    }
}
