# YouTrack CLI

A fast, colorful command-line client for [JetBrains YouTrack](https://www.jetbrains.com/youtrack/) written in Kotlin.
Manage issues, search, browse agile boards, and view activity streams — all without leaving your terminal.

```
yt issue list --project DEMO
yt search "assignee: me #Unresolved Priority: Critical"
yt board sprint 112-1 201-2
yt activity user john.smith
```

---

## Features

| Area | Capabilities |
|------|-------------|
| **Issues** | Create, read, update, delete · Add comments · Apply YouTrack commands |
| **Issue Links** | List links · Link issues by relation type · View link types |
| **Work Items** | Log time · List & delete work items (time tracking) |
| **Browse** | Open any issue instantly in the browser (`yt b DEMO-1`) |
| **Search** | Full YouTrack query language · Pagination · Verbose mode |
| **Agile Boards** | List boards · View sprints with issues · Column layout |
| **Activity** | Recent activity per user · Full history per issue |
| **Users** | List users · View user details |
| **Tags** | List all tags |
| **Projects** | List all accessible projects |
| **Config** | Secure token storage · Default project · Masked display |

---

## Requirements

- JDK 17 or newer
- Gradle 8+ (wrapper included)
- A YouTrack instance (cloud or self-hosted) with a permanent API token

---

## Installation

### 1. Clone the repository

```bash
git clone https://github.com/yourorg/youtrack-cli.git
cd youtrack-cli
```

### 2. Build the fat JAR

```bash
./gradlew shadowJar
```

The self-contained JAR is produced at `build/libs/yt.jar`.

### 3. Install the `yt` wrapper script

**Linux / macOS:**
```bash
sudo cp scripts/yt /usr/local/bin/yt
sudo chmod +x /usr/local/bin/yt
```

Or create it manually:
```bash
cat > /usr/local/bin/yt << 'EOF'
#!/bin/sh
exec java -jar /path/to/youtrack-cli/build/libs/yt.jar "$@"
EOF
chmod +x /usr/local/bin/yt
```

**Windows (PowerShell):**
```powershell
# Create yt.bat somewhere on your PATH
@echo off
java -jar C:\path\to\youtrack-cli\build\libs\yt.jar %*
```

### 4. Configure credentials

Get your API token from YouTrack → **Profile → Account Security → Tokens → New token**.

```bash
yt config set --url https://company.youtrack.cloud --token perm:xxxxxxxx --project MYPROJ
```

### Verify

```bash
yt project list
```

---

## Quick Start

```bash
# 1. Configure
yt config set --url https://acme.youtrack.cloud --token perm:abc123 --project DEMO

# 2. List issues in default project
yt issue list

# 3. View a specific issue
yt issue get DEMO-42

# 4. Open an issue in the browser
yt b DEMO-42

# 5. Create an issue
yt issue create --summary "Login broken after update" --description "Returns 500 on /login"

# 6. Update an issue
yt issue update DEMO-55 --summary "Login broken on iOS 17"

# 7. Apply a command (state / priority / assignee)
yt issue command DEMO-55 "Priority Critical State In Progress"

# 8. Search
yt search "assignee: me #Unresolved"

# 9. Browse boards
yt board list
yt board sprint 112-1            # list sprints
yt board sprint 112-1 201-2      # issues in sprint

# 10. Activity
yt activity user john.smith
yt activity issue DEMO-42

# 11. Issue links
yt issue links DEMO-42                          # view all links
yt issue linktypes                              # see available relation names
yt issue link DEMO-42 "relates to DEMO-10"     # link two issues

# 12. Work items (time tracking)
yt issue workitem list DEMO-42
yt issue workitem add DEMO-42 --duration 1h30m --description "Fixed the bug"

# 13. Users & tags
yt user list
yt user get john.smith
yt tag list
```

---

## Command Reference

```
yt
├── config
│   ├── set       --url --token [--project]
│   ├── show
│   └── clear
├── project
│   └── list
├── issue
│   ├── list      [--project] [--top] [--skip]
│   ├── get       <ISSUE_ID>
│   ├── create    --summary [--project] [--description]
│   ├── update    <ISSUE_ID> [--summary] [--description]
│   ├── delete    <ISSUE_ID> [--yes]
│   ├── comment   <ISSUE_ID> --text
│   ├── command   <ISSUE_ID> <COMMAND>
│   ├── links     <ISSUE_ID>
│   ├── link      <ISSUE_ID> <LINK_COMMAND>
│   ├── linktypes
│   └── workitem
│       ├── list   <ISSUE_ID> [--top]
│       ├── add    <ISSUE_ID> --duration <DUR> [--description] [--date]
│       └── delete <ISSUE_ID> <WORK_ITEM_ID> [--yes]
├── browse (b)    <ISSUE_ID>
├── search        <QUERY> [--top] [--skip] [--verbose]
├── board
│   ├── list
│   ├── get       <BOARD_ID>
│   └── sprint    <BOARD_ID> [SPRINT_ID]
├── activity
│   ├── user      <LOGIN> [--top]
│   └── issue     <ISSUE_ID> [--top]
├── user
│   ├── list      [--top]
│   └── get       <LOGIN>
└── tag
    └── list      [--top]
```

Every command supports `--help` for detailed usage.

---

## Full Documentation

See **[DOCS.md](DOCS.md)** for:
- All commands with options and output examples
- YouTrack query language reference
- Pagination guide
- Exit codes

---

## Development

```bash
# Run directly with Gradle
./gradlew run --args="issue list --project DEMO"

# Run tests
./gradlew test

# Build fat JAR
./gradlew shadowJar
```

### Project layout

```
src/main/kotlin/com/youtrack/cli/
├── cli.kt                     Entry point & root command
├── config/
│   └── Config.kt              Config model + file persistence
├── client/
│   ├── YouTrackClient.kt      REST API client (OkHttp)
│   └── Printer.kt             Terminal output helpers (Mordant)
├── model/
│   └── Models.kt              Serializable data models
└── commands/
    ├── ConfigCommand.kt       yt config *
    ├── IssueCommands.kt       yt issue *
    ├── SearchCommand.kt       yt search
    ├── BoardCommands.kt       yt board *
    ├── ActivityCommands.kt    yt activity *
    ├── ProjectCommands.kt     yt project *
    ├── UserCommands.kt        yt user *
    ├── TagCommands.kt         yt tag *
    └── BrowseCommand.kt       yt browse / yt b
```

### Dependencies

| Library | Purpose |
|---------|---------|
| [Clikt](https://github.com/ajalt/clikt) | CLI argument parsing |
| [OkHttp](https://square.github.io/okhttp/) | HTTP client |
| [kotlinx.serialization](https://github.com/Kotlin/kotlinx.serialization) | JSON parsing |
| [Mordant](https://github.com/ajalt/mordant) | Colorful terminal output |

---

## License

[MIT](LICENSE)
