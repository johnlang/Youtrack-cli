package com.youtrack.cli.commands

import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.core.subcommands
import com.github.ajalt.clikt.parameters.arguments.argument
import com.github.ajalt.clikt.parameters.options.default
import com.github.ajalt.clikt.parameters.options.option
import com.github.ajalt.clikt.parameters.types.int
import com.github.ajalt.mordant.rendering.TextColors.*
import com.github.ajalt.mordant.rendering.TextStyles.*
import com.youtrack.cli.client.printUserFull
import com.youtrack.cli.client.printUserShort
import com.youtrack.cli.client.t

class UserCommand : CliktCommand(
    name = "user",
    help = "Browse YouTrack users"
) {
    init { subcommands(UserList(), UserGet(), UserMe()) }
    override fun run() = Unit
}

class UserList : CliktCommand(name = "list", help = "List users visible to the configured token") {
    private val top by option("--top", "-n", help = "Max results (default 50)").int().default(50)

    override fun run() = withClient { client ->
        val users = client.listUsers(top)
        if (users.isEmpty()) { t.println(yellow("No users found.")); return@withClient }
        t.println(bold("Users (${users.size}):"))
        users.forEach { printUserShort(it) }
    }
}

class UserGet : CliktCommand(name = "get", help = "Show details for a user by login or ID") {
    private val loginOrId by argument("LOGIN", help = "User login or ID")

    override fun run() = withClient { client ->
        val user = client.getUser(loginOrId)
        printUserFull(user)
    }
}

class UserMe : CliktCommand(name = "me", help = "Show the currently authenticated user's profile") {
    override fun run() = withClient { client ->
        val user = client.getCurrentUser()
        printUserFull(user)
    }
}
