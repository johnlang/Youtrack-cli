package com.youtrack.cli.client

import com.youtrack.cli.config.Config
import com.youtrack.cli.model.*
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.*
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.Response
import java.util.concurrent.TimeUnit

class YouTrackClient(private val config: Config) {

    private val json = Json {
        ignoreUnknownKeys = true
        explicitNulls = false
        coerceInputValues = true
    }

    private val http = OkHttpClient.Builder()
        .connectTimeout(15, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .addInterceptor { chain ->
            val req = chain.request().newBuilder()
                .header("Authorization", "Bearer ${config.token}")
                .header("Accept", "application/json")
                .header("Content-Type", "application/json")
                .build()
            chain.proceed(req)
        }
        .build()

    private val base get() = config.baseUrl.trimEnd('/')

    // ── Low-level HTTP ────────────────────────────────────────────────────────

    private fun get(path: String): Response =
        http.newCall(Request.Builder().url("$base$path").get().build()).execute()

    private fun post(path: String, body: String): Response =
        http.newCall(
            Request.Builder().url("$base$path")
                .post(body.toRequestBody("application/json".toMediaType()))
                .build()
        ).execute()

    private fun patch(path: String, body: String): Response =
        http.newCall(
            Request.Builder().url("$base$path")
                .patch(body.toRequestBody("application/json".toMediaType()))
                .build()
        ).execute()

    private fun delete(path: String): Response =
        http.newCall(Request.Builder().url("$base$path").delete().build()).execute()

    private fun Response.requireSuccess(): String {
        val body = body?.string() ?: ""
        if (!isSuccessful) {
            val msg = try {
                json.parseToJsonElement(body).jsonObject["error_description"]?.jsonPrimitive?.content
                    ?: json.parseToJsonElement(body).jsonObject["error"]?.jsonPrimitive?.content
                    ?: "HTTP $code"
            } catch (_: Exception) { "HTTP $code: $body" }
            error(msg)
        }
        return body
    }

    // ── Issues ────────────────────────────────────────────────────────────────

    private val issueFields = "id,idReadable,summary,description,created,updated,resolved," +
        "project(id,name,shortName),reporter(id,login,fullName)," +
        "customFields(name,value(name)),tags(id,name)"

    fun listIssues(projectId: String, top: Int = 20, skip: Int = 0): List<Issue> {
        val raw = get("/api/issues?fields=$issueFields&query=project:$projectId&\$top=$top&\$skip=$skip")
            .requireSuccess()
        return json.decodeFromString(raw)
    }

    fun getIssue(issueId: String): Issue {
        val raw = get("/api/issues/$issueId?fields=$issueFields,comments(id,text,author(login,fullName),created)")
            .requireSuccess()
        return json.decodeFromString(raw)
    }

    fun createIssue(projectId: String, summary: String, description: String?): Issue {
        val payload = buildJsonObject {
            put("summary", summary)
            if (description != null) put("description", description)
            put("project", buildJsonObject { put("id", projectId) })
        }
        val raw = post("/api/issues?fields=$issueFields", payload.toString()).requireSuccess()
        return json.decodeFromString(raw)
    }

    fun updateIssue(issueId: String, summary: String?, description: String?): Issue {
        val payload = buildJsonObject {
            if (summary != null) put("summary", summary)
            if (description != null) put("description", description)
        }
        val raw = patch("/api/issues/$issueId?fields=$issueFields", payload.toString()).requireSuccess()
        return json.decodeFromString(raw)
    }

    fun deleteIssue(issueId: String) {
        delete("/api/issues/$issueId").requireSuccess()
    }

    fun addComment(issueId: String, text: String): Comment {
        val payload = buildJsonObject { put("text", text) }
        val raw = post(
            "/api/issues/$issueId/comments?fields=id,text,author(login,fullName),created",
            payload.toString()
        ).requireSuccess()
        return json.decodeFromString(raw)
    }

    fun applyCommand(issueId: String, command: String) {
        val payload = buildJsonObject {
            put("query", command)
            put("issues", buildJsonArray { add(buildJsonObject { put("id", issueId) }) })
        }
        post("/api/commands", payload.toString()).requireSuccess()
    }

    // ── Search ────────────────────────────────────────────────────────────────

    fun searchIssues(query: String, top: Int = 20, skip: Int = 0): List<Issue> {
        val encoded = java.net.URLEncoder.encode(query, "UTF-8")
        val raw = get("/api/issues?fields=$issueFields&query=$encoded&\$top=$top&\$skip=$skip")
            .requireSuccess()
        return json.decodeFromString(raw)
    }

    // ── Projects ──────────────────────────────────────────────────────────────

    fun listProjects(): List<Project> {
        val raw = get("/api/admin/projects?fields=id,name,shortName,description&\$top=100")
            .requireSuccess()
        return json.decodeFromString(raw)
    }

    // ── Agile Boards ──────────────────────────────────────────────────────────

    fun listBoards(): List<AgileBoard> {
        val raw = get("/api/agiles?fields=id,name&\$top=100").requireSuccess()
        return json.decodeFromString(raw)
    }

    fun getBoard(boardId: String): AgileBoard {
        val raw = get(
            "/api/agiles/$boardId?fields=id,name," +
                "sprints(id,name,goal,start,finish,archived,isDefault)," +
                "columnSettings(columns(id,presentation))"
        ).requireSuccess()
        return json.decodeFromString(raw)
    }

    fun getBoardSprint(boardId: String, sprintId: String): Sprint {
        val raw = get(
            "/api/agiles/$boardId/sprints/$sprintId?fields=id,name,goal,start,finish,archived,isDefault," +
                "issues($issueFields)&\$top=100"
        ).requireSuccess()
        return json.decodeFromString(raw)
    }

    // ── Activity ──────────────────────────────────────────────────────────────

    fun getUserActivity(login: String, top: Int = 30): List<ActivityItem> {
        val encoded = java.net.URLEncoder.encode("author: $login", "UTF-8")
        val raw = get(
            "/api/activities?fields=id,timestamp,author(id,login,fullName)," +
                "target(id,idReadable,summary),category(id),\$type,added,removed" +
                "&query=$encoded&\$top=$top&categories=CommentsCategory,CustomFieldCategory," +
                "IssueCreatedCategory,IssueResolvedCategory,LinksCategory,AttachmentCategory"
        ).requireSuccess()
        return try { json.decodeFromString(raw) } catch (_: Exception) { emptyList() }
    }

    fun getIssueActivity(issueId: String, top: Int = 50): List<ActivityItem> {
        val raw = get(
            "/api/issues/$issueId/activities?fields=id,timestamp,author(id,login,fullName)," +
                "target(id,idReadable,summary),category(id),\$type,added,removed&\$top=$top"
        ).requireSuccess()
        return try { json.decodeFromString(raw) } catch (_: Exception) { emptyList() }
    }
}
