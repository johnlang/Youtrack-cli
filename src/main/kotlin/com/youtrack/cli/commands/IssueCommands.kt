package com.youtrack.cli.commands

import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.core.ProgramResult
import com.github.ajalt.clikt.core.subcommands
import com.github.ajalt.clikt.parameters.arguments.argument
import com.github.ajalt.clikt.parameters.options.*
import com.github.ajalt.clikt.parameters.types.int
import com.github.ajalt.mordant.rendering.TextColors.*
import com.github.ajalt.mordant.rendering.TextStyles.*
import com.youtrack.cli.client.YouTrackClient
import com.youtrack.cli.client.printIssueFull
import com.youtrack.cli.client.printIssueLinks
import com.youtrack.cli.client.printIssueShort
import com.youtrack.cli.client.printLinkType
import com.youtrack.cli.client.printWorkItem
import com.youtrack.cli.client.t
import com.youtrack.cli.config.ConfigManager

class IssueCommand : CliktCommand(
    name = "issue",
    help = "Manage YouTrack issues (CRUD + comments + commands + links + work items)"
) {
    init {
        subcommands(
            IssueList(), IssueGet(), IssueCreate(), IssueUpdate(),
            IssueDelete(), IssueComment(), IssueApplyCommand(),
            IssueLinks(), IssueLinkAdd(), IssueLinkTypes(),
            WorkItemCommand()
        )
    }

    override fun run() = Unit
}

class IssueList : CliktCommand(name = "list", help = "List issues in a project") {
    private val project by option("--project", "-p", help = "Project short name or ID").defaultLazy {
        ConfigManager.load().defaultProject.ifBlank { error("Provide --project or set a default project") }
    }
    private val top by option("--top", "-n", help = "Max results (default 20)").int().default(20)
    private val skip by option("--skip", help = "Skip N results").int().default(0)

    override fun run() = withClient { client ->
        val issues = client.listIssues(project, top, skip)
        if (issues.isEmpty()) { t.println(yellow("No issues found.")); return@withClient }
        t.println(bold("Issues in project '$project' (${issues.size}):"))
        issues.forEach { printIssueShort(it) }
    }
}

class IssueGet : CliktCommand(name = "get", help = "Show full details for an issue") {
    private val issueId by argument("ISSUE_ID", help = "Issue ID (e.g. PROJ-42)")

    override fun run() = withClient { client ->
        val issue = client.getIssue(issueId)
        printIssueFull(issue)
    }
}

class IssueCreate : CliktCommand(name = "create", help = "Create a new issue") {
    private val project by option("--project", "-p", help = "Project short name or ID").defaultLazy {
        ConfigManager.load().defaultProject.ifBlank { error("Provide --project or set a default project") }
    }
    private val summary by option("--summary", "-s", help = "Issue summary").required()
    private val description by option("--description", "-d", help = "Issue description")

    override fun run() = withClient { client ->
        // Resolve project ID if shortName given
        val projectId = resolveProjectId(client, project)
        val issue = client.createIssue(projectId, summary, description)
        t.println(green("✓ Created issue ${bold(issue.idReadable.ifBlank { issue.id })}: ${issue.summary}"))
    }
}

class IssueUpdate : CliktCommand(name = "update", help = "Update an existing issue") {
    private val issueId by argument("ISSUE_ID", help = "Issue ID (e.g. PROJ-42)")
    private val summary by option("--summary", "-s", help = "New summary")
    private val description by option("--description", "-d", help = "New description")

    override fun run() {
        if (summary == null && description == null) {
            t.println(yellow("Nothing to update. Provide --summary or --description."))
            return
        }
        withClient { client ->
            val issue = client.updateIssue(issueId, summary, description)
            t.println(green("✓ Updated ${bold(issue.idReadable.ifBlank { issue.id })}"))
        }
    }
}

class IssueDelete : CliktCommand(name = "delete", help = "Delete an issue (irreversible)") {
    private val issueId by argument("ISSUE_ID", help = "Issue ID to delete")
    private val yes by option("--yes", "-y", help = "Skip confirmation prompt").flag()

    override fun run() {
        if (!yes) {
            print("Delete issue $issueId? This cannot be undone. [y/N] ")
            val answer = readLine()?.trim()?.lowercase()
            if (answer != "y" && answer != "yes") {
                t.println(yellow("Cancelled."))
                return
            }
        }
        withClient { client ->
            client.deleteIssue(issueId)
            t.println(green("✓ Deleted $issueId"))
        }
    }
}

class IssueComment : CliktCommand(name = "comment", help = "Add a comment to an issue") {
    private val issueId by argument("ISSUE_ID", help = "Issue ID")
    private val text by option("--text", "-t", help = "Comment text").required()

    override fun run() = withClient { client ->
        val comment = client.addComment(issueId, text)
        t.println(green("✓ Comment added (id: ${comment.id})"))
    }
}

class IssueApplyCommand : CliktCommand(
    name = "command",
    help = "Apply a YouTrack command to an issue (e.g. 'Priority Critical', 'State In Progress')"
) {
    private val issueId by argument("ISSUE_ID", help = "Issue ID")
    private val command by argument("COMMAND", help = "Command string to apply")

    override fun run() = withClient { client ->
        client.applyCommand(issueId, command)
        t.println(green("✓ Command '$command' applied to $issueId"))
    }
}

// ── Issue Links ───────────────────────────────────────────────────────────────

class IssueLinks : CliktCommand(name = "links", help = "List all links for an issue") {
    private val issueId by argument("ISSUE_ID", help = "Issue ID (e.g. PROJ-42)")

    override fun run() = withClient { client ->
        val links = client.getIssueLinks(issueId)
        if (links.isEmpty()) { t.println(yellow("No links found for $issueId.")); return@withClient }
        printIssueLinks(issueId, links)
    }
}

class IssueLinkAdd : CliktCommand(
    name = "link",
    help = "Link an issue to another issue.\n\n" +
        "LINK_COMMAND is a YouTrack command string combining the relation and target,\n" +
        "e.g. \"relates to DEMO-2\", \"subtask of BACK-1\", \"duplicates MOB-5\".\n" +
        "Run 'yt issue linktypes' to see available relation names."
) {
    private val issueId by argument("ISSUE_ID", help = "Source issue ID")
    private val linkCommand by argument("LINK_COMMAND", help = "Link command, e.g. \"relates to DEMO-2\"")

    override fun run() = withClient { client ->
        client.linkIssue(issueId, linkCommand)
        t.println(green("✓ Link applied to $issueId: $linkCommand"))
    }
}

class IssueLinkTypes : CliktCommand(name = "linktypes", help = "List available issue link types") {
    override fun run() = withClient { client ->
        val types = client.getLinkTypes()
        if (types.isEmpty()) { t.println(yellow("No link types found.")); return@withClient }
        t.println(bold("Issue Link Types (${types.size}):"))
        types.forEach { printLinkType(it) }
    }
}

// ── Work Items ────────────────────────────────────────────────────────────────

class WorkItemCommand : CliktCommand(
    name = "workitem",
    help = "Manage work items (time tracking) on an issue"
) {
    init { subcommands(WorkItemList(), WorkItemAdd(), WorkItemDelete()) }
    override fun run() = Unit
}

class WorkItemList : CliktCommand(name = "list", help = "List work items logged on an issue") {
    private val issueId by argument("ISSUE_ID", help = "Issue ID")
    private val top by option("--top", "-n", help = "Max results (default 50)").int().default(50)

    override fun run() = withClient { client ->
        val items = client.listWorkItems(issueId, top)
        if (items.isEmpty()) { t.println(yellow("No work items logged on $issueId.")); return@withClient }
        t.println(bold("Work items for $issueId (${items.size}):"))
        items.forEach { printWorkItem(it) }
    }
}

class WorkItemAdd : CliktCommand(name = "add", help = "Log a work item (time entry) on an issue") {
    private val issueId by argument("ISSUE_ID", help = "Issue ID")
    private val duration by option("--duration", "-d", help = "Duration, e.g. 1h30m, 2h, 45m").required()
    private val description by option("--description", "--desc", help = "Optional work description")
    private val date by option("--date", help = "Date as YYYY-MM-DD (defaults to today)")

    override fun run() = withClient { client ->
        val dateMs: Long? = date?.let {
            java.text.SimpleDateFormat("yyyy-MM-dd").parse(it)?.time
        }
        val item = client.addWorkItem(issueId, duration, description, dateMs)
        t.println(green("✓ Work item logged on $issueId (id: ${item.id}): ${item.duration?.presentation ?: duration}"))
    }
}

class WorkItemDelete : CliktCommand(name = "delete", help = "Delete a work item from an issue") {
    private val issueId by argument("ISSUE_ID", help = "Issue ID")
    private val workItemId by argument("WORK_ITEM_ID", help = "Work item ID")
    private val yes by option("--yes", "-y", help = "Skip confirmation prompt").flag()

    override fun run() {
        if (!yes) {
            print("Delete work item $workItemId from $issueId? [y/N] ")
            val answer = readLine()?.trim()?.lowercase()
            if (answer != "y" && answer != "yes") { t.println(yellow("Cancelled.")); return }
        }
        withClient { client ->
            client.deleteWorkItem(issueId, workItemId)
            t.println(green("✓ Deleted work item $workItemId from $issueId"))
        }
    }
}

// ── Helpers ───────────────────────────────────────────────────────────────────

internal fun withClient(block: (YouTrackClient) -> Unit) {
    try {
        val cfg = ConfigManager.requireValid()
        block(YouTrackClient(cfg))
    } catch (e: Exception) {
        t.println(red("Error: ${e.message}"))
        throw ProgramResult(1)
    }
}

internal fun resolveProjectId(client: YouTrackClient, nameOrId: String): String {
    // If already looks like an ID (starts with 0- or is numeric prefix) return as-is
    // Otherwise look up by shortName
    val projects = try { client.listProjects() } catch (_: Exception) { return nameOrId }
    return projects.firstOrNull {
        it.shortName.equals(nameOrId, ignoreCase = true) || it.id == nameOrId || it.name.equals(nameOrId, ignoreCase = true)
    }?.id ?: nameOrId
}
