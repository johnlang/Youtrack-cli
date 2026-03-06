package com.youtrack.cli.commands

import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.core.subcommands
import com.github.ajalt.clikt.parameters.arguments.argument
import com.github.ajalt.clikt.parameters.options.*
import com.github.ajalt.clikt.parameters.types.int
import com.github.ajalt.mordant.rendering.TextColors.*
import com.github.ajalt.mordant.rendering.TextStyles.*
import com.youtrack.cli.client.printArticleFull
import com.youtrack.cli.client.printArticleShort
import com.youtrack.cli.client.t
import com.youtrack.cli.config.ConfigManager

class ArticleCommand : CliktCommand(
    name = "article",
    help = "Browse and manage YouTrack knowledge base articles"
) {
    init { subcommands(ArticleList(), ArticleGet(), ArticleCreate(), ArticleUpdate(), ArticleDelete()) }
    override fun run() = Unit
}

class ArticleList : CliktCommand(name = "list", help = "List knowledge base articles") {
    private val project by option("--project", "-p", help = "Filter by project short name")
    private val top by option("--top", "-n", help = "Max results (default 25)").int().default(25)
    private val skip by option("--skip", help = "Skip N results").int().default(0)

    override fun run() = withClient { client ->
        val projectId = if (project != null) resolveProjectId(client, project!!) else null
        val articles = client.listArticles(projectId, top, skip)
        if (articles.isEmpty()) { t.println(yellow("No articles found.")); return@withClient }
        val label = if (project != null) "Articles in '$project'" else "Articles"
        t.println(bold("$label (${articles.size}):"))
        articles.forEach { printArticleShort(it) }
    }
}

class ArticleGet : CliktCommand(name = "get", help = "Show full content of a knowledge base article") {
    private val articleId by argument("ARTICLE_ID", help = "Article ID (e.g. DEMO-A-1)")

    override fun run() = withClient { client ->
        val article = client.getArticle(articleId)
        printArticleFull(article)
    }
}

class ArticleCreate : CliktCommand(name = "create", help = "Create a new knowledge base article") {
    private val project by option("--project", "-p", help = "Project short name or ID").defaultLazy {
        ConfigManager.load().defaultProject.ifBlank { error("Provide --project or set a default project") }
    }
    private val summary by option("--summary", "-s", help = "Article title").required()
    private val content by option("--content", "-c", help = "Article body text")

    override fun run() = withClient { client ->
        val projectId = resolveProjectId(client, project)
        val article = client.createArticle(projectId, summary, content)
        t.println(green("✓ Created article ${bold(article.idReadable.ifBlank { article.id })}: ${article.summary}"))
    }
}

class ArticleUpdate : CliktCommand(name = "update", help = "Update the title or content of an article") {
    private val articleId by argument("ARTICLE_ID", help = "Article ID")
    private val summary by option("--summary", "-s", help = "New title")
    private val content by option("--content", "-c", help = "New content")

    override fun run() {
        if (summary == null && content == null) {
            t.println(yellow("Nothing to update. Provide --summary or --content."))
            return
        }
        withClient { client ->
            val article = client.updateArticle(articleId, summary, content)
            t.println(green("✓ Updated ${bold(article.idReadable.ifBlank { article.id })}"))
        }
    }
}

class ArticleDelete : CliktCommand(name = "delete", help = "Delete a knowledge base article (irreversible)") {
    private val articleId by argument("ARTICLE_ID", help = "Article ID")
    private val yes by option("--yes", "-y", help = "Skip confirmation prompt").flag()

    override fun run() {
        if (!yes) {
            print("Delete article $articleId? This cannot be undone. [y/N] ")
            val answer = readLine()?.trim()?.lowercase()
            if (answer != "y" && answer != "yes") { t.println(yellow("Cancelled.")); return }
        }
        withClient { client ->
            client.deleteArticle(articleId)
            t.println(green("✓ Deleted article $articleId"))
        }
    }
}
