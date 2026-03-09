# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Build & Run

**Must use Java 21** — the system default (Java 25) breaks Gradle's Kotlin DSL compilation:

```bash
# Build fat JAR
JAVA_HOME=/home/Evgenii.Pavelev/.sdkman/candidates/java/21.0.2-open PATH=$JAVA_HOME/bin:$PATH ./gradlew shadowJar

# Run directly via Gradle
JAVA_HOME=... PATH=... ./gradlew run --args="issue list --project DEMO"

# Run via wrapper script (requires JAR at build/libs/yt.jar)
scripts/yt <command>

# Compile only (fast check)
JAVA_HOME=... PATH=... ./gradlew compileKotlin
```

There are no tests in this project (`./gradlew test` will pass but run nothing).

## Architecture

The app is a Clikt-based CLI. Each command group lives in its own file under `commands/`. All commands share two helpers defined in `IssueCommands.kt`:

- `withClient { client -> ... }` — loads config, creates `YouTrackClient`, and catches all exceptions into a user-friendly error + exit code 1.
- `resolveProjectId(client, nameOrId)` — looks up a project by short name, full name, or ID.

**Data flow:** Command → `withClient` → `YouTrackClient` (HTTP) → deserialize into `Models.kt` types → `Printer.kt` (terminal output).

`YouTrackClient` makes all REST calls. JSON is built manually with `buildJsonObject`/`buildJsonArray` (no serialization of request payloads). Responses are deserialized via `kotlinx.serialization` into the model classes.

`Printer.kt` exposes top-level `print*` functions and a shared `val t = Terminal()`. The `displayValue()` extension on nullable `JsonElement?` handles custom field values which arrive as JSON objects like `{"name":"In Progress","$type":"..."}`.

## YouTrack API Quirks

- **Updates use POST, not PATCH** — `POST /api/issues/{id}` and `POST /api/articles/{id}`. PATCH returns 405.
- **Commands API needs internal DB ID** — `POST /api/commands` requires IDs like `2-32`, not `DEMO-32`. `applyCommand` auto-resolves readable IDs.
- **Issue activities require `categories` param** — `GET /api/issues/{id}/activities` must include `&categories=CommentsCategory,...`.
- **Custom field values are JSON objects** — `{"name":"Submitted","$type":"..."}`, not plain strings. Use `displayValue()` to extract `.name`.

## Adding a New Command

1. Create or extend a file in `commands/` following the existing pattern.
2. Register the new `CliktCommand` subclass in `cli.kt` inside `.subcommands(...)`.
3. Add any new API methods to `YouTrackClient.kt` and corresponding print functions to `Printer.kt`.
4. Add new model types to `model/Models.kt` with `@Serializable`.

## Sandbox for Testing

- URL: `https://johnlang-playground.youtrack.cloud`
- Token: `perm-YWRtaW4=.NDgtMQ==.jny6YRTq4ko6ELhM3EgpmLK8qC5JKa`
- Projects: DEMO, SB, SHMOSIP, SOMELONGNAME
