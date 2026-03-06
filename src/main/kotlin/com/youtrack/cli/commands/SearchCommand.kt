package com.youtrack.cli.commands

import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.parameters.arguments.argument
import com.github.ajalt.clikt.parameters.options.default
import com.github.ajalt.clikt.parameters.options.flag
import com.github.ajalt.clikt.parameters.options.option
import com.github.ajalt.clikt.parameters.types.int
import com.github.ajalt.mordant.rendering.TextColors.*
import com.github.ajalt.mordant.rendering.TextStyles.*
import com.youtrack.cli.client.printIssueFull
import com.youtrack.cli.client.printIssueShort
import com.youtrack.cli.client.t

class SearchCommand : CliktCommand(
    name = "search",
    help = "Search issues using YouTrack query language"
) {
    private val query by argument("QUERY", help = "YouTrack search query (e.g. 'project: DEMO assignee: me #Unresolved')")
    private val top by option("--top", "-n", help = "Max results (default 20)").int().default(20)
    private val skip by option("--skip", help = "Skip N results").int().default(0)
    private val verbose by option("--verbose", "-v", help = "Show full issue details").flag()

    override fun run() = withClient { client ->
        val issues = client.searchIssues(query, top, skip)
        if (issues.isEmpty()) {
            t.println(yellow("No issues found for query: $query"))
            return@withClient
        }
        t.println(bold("Search results for \"$query\" (${issues.size}):"))
        if (verbose) {
            issues.forEach { printIssueFull(it) }
        } else {
            issues.forEach { printIssueShort(it) }
        }
    }
}
