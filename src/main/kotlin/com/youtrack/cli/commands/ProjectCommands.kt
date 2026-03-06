package com.youtrack.cli.commands

import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.core.subcommands
import com.github.ajalt.mordant.rendering.TextColors.*
import com.github.ajalt.mordant.rendering.TextStyles.*
import com.youtrack.cli.client.printProjectShort
import com.youtrack.cli.client.t

class ProjectCommand : CliktCommand(
    name = "project",
    help = "List available projects"
) {
    init { subcommands(ProjectList()) }
    override fun run() = Unit
}

class ProjectList : CliktCommand(name = "list", help = "List all accessible projects") {
    override fun run() = withClient { client ->
        val projects = client.listProjects()
        if (projects.isEmpty()) { t.println(yellow("No projects found.")); return@withClient }
        t.println(bold("Projects (${projects.size}):"))
        projects.forEach { printProjectShort(it) }
    }
}
