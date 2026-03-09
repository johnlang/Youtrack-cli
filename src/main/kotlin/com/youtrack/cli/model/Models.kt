package com.youtrack.cli.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

// ── Issue ────────────────────────────────────────────────────────────────────

@Serializable
data class Issue(
    val id: String = "",
    val idReadable: String = "",
    val summary: String = "",
    val description: String? = null,
    val created: Long? = null,
    val updated: Long? = null,
    val resolved: Long? = null,
    val project: ProjectRef? = null,
    val reporter: UserRef? = null,
    val assignee: UserRef? = null,
    val priority: FieldValue? = null,
    val type: FieldValue? = null,
    val state: FieldValue? = null,
    val customFields: List<CustomField> = emptyList(),
    val comments: List<Comment>? = null,
    val tags: List<Tag>? = null
)

@Serializable
data class ProjectRef(
    val id: String = "",
    val name: String = "",
    val shortName: String = ""
)

@Serializable
data class UserRef(
    val id: String = "",
    val login: String = "",
    val fullName: String? = null,
    val email: String? = null
)

@Serializable
data class FieldValue(
    val name: String = ""
)

@Serializable
data class CustomField(
    val name: String = "",
    val value: kotlinx.serialization.json.JsonElement? = null
)

@Serializable
data class Comment(
    val id: String = "",
    val text: String? = null,
    val author: UserRef? = null,
    val created: Long? = null,
    val updated: Long? = null
)

@Serializable
data class Tag(
    val id: String = "",
    val name: String = ""
)

// ── Agile Boards ─────────────────────────────────────────────────────────────

@Serializable
data class AgileBoard(
    val id: String = "",
    val name: String = "",
    val sprints: List<Sprint>? = null,
    val columnSettings: ColumnSettings? = null
)

@Serializable
data class Sprint(
    val id: String = "",
    val name: String = "",
    val goal: String? = null,
    val start: Long? = null,
    val finish: Long? = null,
    val archived: Boolean = false,
    val isDefault: Boolean = false,
    val issues: List<Issue>? = null
)

@Serializable
data class ColumnSettings(
    val columns: List<BoardColumn>? = null
)

@Serializable
data class BoardColumn(
    val id: String = "",
    val presentation: String = ""
)

// ── Activity ─────────────────────────────────────────────────────────────────

@Serializable
data class ActivityItem(
    val id: String = "",
    val timestamp: Long? = null,
    val author: UserRef? = null,
    val target: ActivityTarget? = null,
    val category: ActivityCategory? = null,
    val added: kotlinx.serialization.json.JsonElement? = null,
    val removed: kotlinx.serialization.json.JsonElement? = null,
    @SerialName("\$type") val type: String = ""
)

@Serializable
data class ActivityTarget(
    val id: String = "",
    val idReadable: String? = null,
    val summary: String? = null
)

@Serializable
data class ActivityCategory(
    val id: String = ""
)

// ── Issue Links ───────────────────────────────────────────────────────────────

@Serializable
data class IssueLinkType(
    val id: String = "",
    val name: String = "",
    val directed: Boolean = false,
    val sourceToTarget: String = "",
    val targetToSource: String = ""
)

@Serializable
data class IssueLink(
    val id: String = "",
    val direction: String = "",   // "OUTWARD", "INWARD", "BOTH"
    val linkType: IssueLinkType? = null,
    val issues: List<Issue> = emptyList()
)

// ── Work Items ────────────────────────────────────────────────────────────────

@Serializable
data class WorkItemDuration(
    val minutes: Int = 0,
    val presentation: String = ""
)

@Serializable
data class WorkItem(
    val id: String = "",
    val date: Long? = null,
    val duration: WorkItemDuration? = null,
    val text: String? = null,
    val author: UserRef? = null
)

// ── Users ─────────────────────────────────────────────────────────────────────

@Serializable
data class User(
    val id: String = "",
    val login: String = "",
    val fullName: String? = null,
    val email: String? = null,
    val groups: List<UserGroup>? = null
)

@Serializable
data class UserGroup(
    val id: String = "",
    val name: String = ""
)

// ── Articles ──────────────────────────────────────────────────────────────────

@Serializable
data class Article(
    val id: String = "",
    val idReadable: String = "",
    val summary: String = "",
    val content: String? = null,
    val created: Long? = null,
    val updated: Long? = null,
    val project: ProjectRef? = null,
    val author: UserRef? = null,
    val tags: List<Tag>? = null,
    val childArticles: List<Article>? = null,
    val parentArticle: Article? = null
)

// ── Project ───────────────────────────────────────────────────────────────────

@Serializable
data class Project(
    val id: String = "",
    val name: String = "",
    val shortName: String = "",
    val description: String? = null
)
