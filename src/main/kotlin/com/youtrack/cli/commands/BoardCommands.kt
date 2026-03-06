package com.youtrack.cli.commands

import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.core.subcommands
import com.github.ajalt.clikt.parameters.arguments.argument
import com.github.ajalt.clikt.parameters.arguments.optional
import com.github.ajalt.mordant.rendering.TextColors.*
import com.github.ajalt.mordant.rendering.TextStyles.*
import com.youtrack.cli.client.*

class BoardCommand : CliktCommand(
    name = "board",
    help = "View and interact with agile boards"
) {
    init { subcommands(BoardList(), BoardGet(), BoardSprint()) }
    override fun run() = Unit
}

class BoardList : CliktCommand(name = "list", help = "List all agile boards") {
    override fun run() = withClient { client ->
        val boards = client.listBoards()
        if (boards.isEmpty()) { t.println(yellow("No boards found.")); return@withClient }
        t.println(bold("Agile Boards (${boards.size}):"))
        boards.forEach { printBoardShort(it) }
    }
}

class BoardGet : CliktCommand(name = "get", help = "Show board details including sprints and columns") {
    private val boardId by argument("BOARD_ID", help = "Board ID")

    override fun run() = withClient { client ->
        val board = client.getBoard(boardId)
        printBoardFull(board)
    }
}

class BoardSprint : CliktCommand(
    name = "sprint",
    help = "Show issues in a sprint. If SPRINT_ID is omitted, lists all sprints."
) {
    private val boardId by argument("BOARD_ID", help = "Board ID")
    private val sprintId by argument("SPRINT_ID", help = "Sprint ID (optional)").optional()

    override fun run() = withClient { client ->
        if (sprintId == null) {
            val board = client.getBoard(boardId)
            if (board.sprints.isNullOrEmpty()) {
                t.println(yellow("No sprints found on board ${board.name}."))
                return@withClient
            }
            t.println(bold("Sprints on board '${board.name}':"))
            board.sprints.forEach { s ->
                val active = if (s.isDefault) green(" [active]") else ""
                val archived = if (s.archived) red(" [archived]") else ""
                t.println("  ${cyan(s.id)}  ${bold(s.name)}$active$archived")
            }
        } else {
            val sprint = client.getBoardSprint(boardId, sprintId!!)
            printSprintFull(sprint)
        }
    }
}
