package com.youtrack.cli.client

import com.github.ajalt.mordant.rendering.TextColors.*
import com.github.ajalt.mordant.rendering.TextStyles.*
import com.github.ajalt.mordant.terminal.Terminal
import com.youtrack.cli.model.*
import kotlinx.serialization.json.*
import java.text.SimpleDateFormat
import java.util.Date

val t = Terminal()

private val dateFmt = SimpleDateFormat("yyyy-MM-dd HH:mm")

fun Long.fmtDate(): String = dateFmt.format(Date(this))

/** Extract a human-readable string from a custom-field value JsonElement.
 *  Enum/state values come as {"name":"Submitted","$type":"..."} — return the name.
 *  Plain string primitives are returned as-is.
 *  Arrays (multi-value fields) are joined with ", ".
 */
private fun JsonElement?.displayValue(): String = when (this) {
    null, JsonNull -> ""
    is JsonPrimitive -> content
    is JsonObject -> get("name")?.jsonPrimitive?.contentOrNull ?: toString()
    is JsonArray -> joinToString(", ") { it.displayValue() }
}

fun printIssueShort(issue: Issue) {
    val id = cyan(issue.idReadable.ifBlank { issue.id })
    val state = issue.customFields.firstOrNull { it.name == "State" }?.value.displayValue()
    val priority = issue.customFields.firstOrNull { it.name == "Priority" }?.value.displayValue()
    val stateTag = if (state.isNotBlank()) " [${yellow(state)}]" else ""
    val priorityTag = if (priority.isNotBlank()) " (${green(priority)})" else ""
    t.println("  $id$stateTag$priorityTag  ${bold(issue.summary)}")
}

fun printIssueFull(issue: Issue) {
    t.println()
    t.println(bold("─".repeat(70)))
    t.println("${bold("ID:")}          ${cyan(issue.idReadable.ifBlank { issue.id })}")
    t.println("${bold("Summary:")}     ${bold(issue.summary)}")
    t.println("${bold("Project:")}     ${issue.project?.name ?: "—"} (${issue.project?.shortName ?: ""})")
    t.println("${bold("Reporter:")}    ${issue.reporter?.fullName ?: issue.reporter?.login ?: "—"}")

    val assignee = issue.customFields.firstOrNull { it.name == "Assignee" }?.value.displayValue().ifBlank { "—" }
    t.println("${bold("Assignee:")}    $assignee")

    val state = issue.customFields.firstOrNull { it.name == "State" }?.value.displayValue().ifBlank { "—" }
    t.println("${bold("State:")}       ${yellow(state)}")

    val priority = issue.customFields.firstOrNull { it.name == "Priority" }?.value.displayValue().ifBlank { "—" }
    t.println("${bold("Priority:")}    ${green(priority)}")

    val type = issue.customFields.firstOrNull { it.name == "Type" }?.value.displayValue().ifBlank { "—" }
    t.println("${bold("Type:")}        $type")

    if (!issue.tags.isNullOrEmpty()) {
        t.println("${bold("Tags:")}        ${issue.tags.joinToString(", ") { it.name }}")
    }
    if (issue.created != null) t.println("${bold("Created:")}     ${issue.created.fmtDate()}")
    if (issue.updated != null) t.println("${bold("Updated:")}     ${issue.updated.fmtDate()}")
    if (issue.resolved != null) t.println("${bold("Resolved:")}    ${issue.resolved.fmtDate()}")
    if (!issue.description.isNullOrBlank()) {
        t.println()
        t.println(bold("Description:"))
        t.println(issue.description)
    }
    if (!issue.comments.isNullOrEmpty()) {
        t.println()
        t.println(bold("Comments (${issue.comments.size}):"))
        for (c in issue.comments) {
            val who = c.author?.fullName ?: c.author?.login ?: "?"
            val when_ = c.created?.fmtDate() ?: ""
            t.println("  ${cyan(who)} at $when_:")
            c.text?.lines()?.forEach { t.println("    $it") }
        }
    }
    t.println(bold("─".repeat(70)))
}

fun printBoardShort(board: AgileBoard) {
    t.println("  ${cyan(board.id)}  ${bold(board.name)}")
}

fun printBoardFull(board: AgileBoard) {
    t.println()
    t.println(bold("─".repeat(70)))
    t.println("${bold("Board:")}  ${bold(board.name)}  (${cyan(board.id)})")
    if (!board.sprints.isNullOrEmpty()) {
        t.println()
        t.println(bold("Sprints:"))
        for (s in board.sprints) {
            val active = if (s.isDefault) green(" [active]") else ""
            val archived = if (s.archived) red(" [archived]") else ""
            val range = buildString {
                if (s.start != null) append(" ${s.start.fmtDate()}")
                if (s.finish != null) append(" → ${s.finish.fmtDate()}")
            }
            t.println("  ${cyan(s.id)}  ${bold(s.name)}$active$archived$range")
            if (!s.goal.isNullOrBlank()) t.println("       Goal: ${s.goal}")
        }
    }
    if (board.columnSettings?.columns != null) {
        t.println()
        t.println(bold("Columns:") + " " + board.columnSettings.columns.joinToString(", ") { it.presentation })
    }
    t.println(bold("─".repeat(70)))
}

fun printSprintFull(sprint: Sprint) {
    t.println()
    t.println(bold("─".repeat(70)))
    val active = if (sprint.isDefault) green(" [active]") else ""
    t.println("${bold("Sprint:")}  ${bold(sprint.name)}$active  (${cyan(sprint.id)})")
    if (!sprint.goal.isNullOrBlank()) t.println("${bold("Goal:")}    ${sprint.goal}")
    if (sprint.start != null) t.println("${bold("Start:")}   ${sprint.start.fmtDate()}")
    if (sprint.finish != null) t.println("${bold("End:")}     ${sprint.finish.fmtDate()}")
    if (!sprint.issues.isNullOrEmpty()) {
        t.println()
        t.println(bold("Issues (${sprint.issues.size}):"))
        sprint.issues.forEach { printIssueShort(it) }
    } else {
        t.println("  (no issues)")
    }
    t.println(bold("─".repeat(70)))
}

fun printActivity(item: ActivityItem) {
    val who = item.author?.fullName ?: item.author?.login ?: "?"
    val when_ = item.timestamp?.fmtDate() ?: ""
    val target = item.target?.let { "${it.idReadable ?: it.id}: ${it.summary ?: ""}" } ?: ""
    val category = item.category?.id?.removeSuffix("Category") ?: item.type.removePrefix("jetbrains.jetpad.model..")

    val added = item.added?.let { " → ${it.toString().take(80)}" } ?: ""
    val removed = item.removed?.let { " (was: ${it.toString().take(40)})" } ?: ""

    t.println("  ${cyan(when_)}  ${bold(who)}  [${yellow(category)}]  $target$removed$added")
}

fun printProjectShort(p: com.youtrack.cli.model.Project) {
    t.println("  ${cyan(p.shortName)}  ${bold(p.name)}  (id: ${p.id})")
    if (!p.description.isNullOrBlank()) t.println("       ${p.description}")
}

fun printIssueLinks(issueId: String, links: List<com.youtrack.cli.model.IssueLink>) {
    t.println()
    t.println(bold("Links for $issueId:"))
    for (link in links) {
        if (link.issues.isEmpty()) continue
        val lt = link.linkType
        val label = when (link.direction) {
            "OUTWARD" -> lt?.sourceToTarget ?: lt?.name ?: "linked"
            "INWARD"  -> lt?.targetToSource ?: lt?.name ?: "linked"
            else      -> lt?.name ?: "linked"
        }
        t.println("  ${yellow(label)}:")
        for (issue in link.issues) {
            val resolved = if (issue.resolved != null) red(" [resolved]") else ""
            t.println("    ${cyan(issue.idReadable.ifBlank { issue.id })}$resolved  ${issue.summary}")
        }
    }
}

fun printLinkType(lt: com.youtrack.cli.model.IssueLinkType) {
    val direction = if (lt.directed) {
        "${yellow(lt.sourceToTarget)} / ${yellow(lt.targetToSource)}"
    } else {
        yellow(lt.name)
    }
    t.println("  ${bold(lt.name)}  ($direction)")
}

fun printWorkItem(item: com.youtrack.cli.model.WorkItem) {
    val who = item.author?.fullName ?: item.author?.login ?: "?"
    val when_ = item.date?.fmtDate() ?: ""
    val dur = item.duration?.presentation ?: "?"
    val desc = if (!item.text.isNullOrBlank()) "  — ${item.text}" else ""
    t.println("  ${cyan(item.id)}  ${bold(dur)}  $who  $when_$desc")
}

fun printUserShort(u: com.youtrack.cli.model.User) {
    val email = if (!u.email.isNullOrBlank()) "  <${u.email}>" else ""
    t.println("  ${cyan(u.login)}  ${bold(u.fullName ?: u.login)}$email")
}

fun printUserFull(u: com.youtrack.cli.model.User) {
    t.println()
    t.println(bold("─".repeat(50)))
    t.println("${bold("Login:")}     ${cyan(u.login)}")
    t.println("${bold("Name:")}      ${u.fullName ?: "—"}")
    t.println("${bold("Email:")}     ${u.email ?: "—"}")
    t.println("${bold("ID:")}        ${u.id}")
    if (!u.groups.isNullOrEmpty()) {
        t.println("${bold("Groups:")}    ${u.groups.joinToString(", ") { it.name }}")
    }
    t.println(bold("─".repeat(50)))
}

fun printArticleShort(a: com.youtrack.cli.model.Article) {
    val id = cyan(a.idReadable.ifBlank { a.id })
    val proj = a.project?.shortName?.let { " [$it]" } ?: ""
    t.println("  $id$proj  ${bold(a.summary)}")
}

fun printArticleFull(a: com.youtrack.cli.model.Article) {
    t.println()
    t.println(bold("─".repeat(70)))
    t.println("${bold("ID:")}          ${cyan(a.idReadable.ifBlank { a.id })}")
    t.println("${bold("Title:")}       ${bold(a.summary)}")
    t.println("${bold("Project:")}     ${a.project?.name ?: "—"} (${a.project?.shortName ?: ""})")
    t.println("${bold("Author:")}      ${a.author?.fullName ?: a.author?.login ?: "—"}")
    if (a.parentArticle != null) {
        t.println("${bold("Parent:")}      ${cyan(a.parentArticle.idReadable.ifBlank { a.parentArticle.id })}  ${a.parentArticle.summary}")
    }
    if (!a.tags.isNullOrEmpty()) {
        t.println("${bold("Tags:")}        ${a.tags.joinToString(", ") { it.name }}")
    }
    if (a.created != null) t.println("${bold("Created:")}     ${a.created.fmtDate()}")
    if (a.updated != null) t.println("${bold("Updated:")}     ${a.updated.fmtDate()}")
    if (!a.childArticles.isNullOrEmpty()) {
        t.println()
        t.println(bold("Sub-articles (${a.childArticles.size}):"))
        a.childArticles.forEach { t.println("  ${cyan(it.idReadable.ifBlank { it.id })}  ${it.summary}") }
    }
    if (!a.content.isNullOrBlank()) {
        t.println()
        t.println(bold("Content:"))
        t.println(a.content)
    }
    t.println(bold("─".repeat(70)))
}
