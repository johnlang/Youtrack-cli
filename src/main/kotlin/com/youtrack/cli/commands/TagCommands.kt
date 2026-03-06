package com.youtrack.cli.commands

import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.core.subcommands
import com.github.ajalt.clikt.parameters.options.default
import com.github.ajalt.clikt.parameters.options.option
import com.github.ajalt.clikt.parameters.types.int
import com.github.ajalt.mordant.rendering.TextColors.*
import com.github.ajalt.mordant.rendering.TextStyles.*
import com.youtrack.cli.client.t

class TagCommand : CliktCommand(
    name = "tag",
    help = "Browse YouTrack tags"
) {
    init { subcommands(TagList()) }
    override fun run() = Unit
}

class TagList : CliktCommand(name = "list", help = "List all tags accessible with the configured token") {
    private val top by option("--top", "-n", help = "Max results (default 100)").int().default(100)

    override fun run() = withClient { client ->
        val tags = client.listTags(top)
        if (tags.isEmpty()) { t.println(yellow("No tags found.")); return@withClient }
        t.println(bold("Tags (${tags.size}):"))
        tags.forEach { t.println("  ${cyan(it.id)}  ${bold(it.name)}") }
    }
}
