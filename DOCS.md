# YouTrack CLI — Full Documentation

A command-line client for [JetBrains YouTrack](https://www.jetbrains.com/youtrack/) written in Kotlin.
The binary is invoked as `yt`.

---

## Table of Contents

1. [Configuration](#configuration)
2. [Projects](#projects)
3. [Issues (CRUD)](#issues-crud)
   - [List issues](#list-issues)
   - [Get issue](#get-issue)
   - [Create issue](#create-issue)
   - [Update issue](#update-issue)
   - [Delete issue](#delete-issue)
   - [Add comment](#add-comment)
   - [Apply command](#apply-command)
4. [Search](#search)
5. [Agile Boards](#agile-boards)
   - [List boards](#list-boards)
   - [Get board](#get-board)
   - [View sprint](#view-sprint)
6. [Activity](#activity)
   - [User activity](#user-activity)
   - [Issue activity](#issue-activity)
7. [Exit Codes](#exit-codes)
8. [YouTrack Query Language](#youtrack-query-language)

---

## Configuration

All commands require an API token and the base URL of your YouTrack instance.
Credentials are stored in `~/.youtrack-cli/config.json` (file is owner-read-only).

### `yt config set`

Store connection credentials.

```
yt config set --url <URL> --token <TOKEN> [--project <SHORT_NAME>]
```

| Option | Description |
|--------|-------------|
| `--url` | Base URL of your YouTrack server, e.g. `https://company.youtrack.cloud` |
| `--token` | Permanent API token (create in YouTrack → Profile → Account Security → Tokens) |
| `--project` | Optional default project short name used when `--project` is omitted |

**Example:**
```bash
yt config set --url https://acme.youtrack.cloud --token perm:abc123def456 --project DEMO
```

---

### `yt config show`

Display the current configuration. The token is partially masked.

```bash
yt config show
```

**Output:**
```
YouTrack CLI Configuration
  URL:             https://acme.youtrack.cloud
  Token:           perm****456
  Default project: DEMO
```

---

### `yt config clear`

Remove all stored credentials.

```bash
yt config clear
```

---

## Projects

### `yt project list`

List all projects accessible with the configured token.

```bash
yt project list
```

**Output:**
```
Projects (3):
  DEMO  Demo Project  (id: 0-1)
  BACK  Backend       (id: 0-2)
       Core backend services
  MOB   Mobile App    (id: 0-3)
```

The `shortName` (first column) is used as the project identifier in all other commands.

---

## Issues (CRUD)

### List Issues

```
yt issue list [--project <PROJECT>] [--top <N>] [--skip <N>]
```

| Option | Default | Description |
|--------|---------|-------------|
| `--project`, `-p` | default project | Project short name or ID |
| `--top`, `-n` | 20 | Maximum number of issues to return |
| `--skip` | 0 | Number of issues to skip (for pagination) |

**Examples:**
```bash
# List the first 20 issues in the default project
yt issue list

# List 50 issues in project BACK
yt issue list --project BACK --top 50

# Paginate: second page of 20
yt issue list --project DEMO --skip 20
```

**Output:**
```
Issues in project 'DEMO' (5):
  DEMO-1 [Open] (Normal)  Login page broken
  DEMO-2 [In Progress] (Critical)  API timeout errors
  DEMO-3 [Fixed] (Minor)  Typo in footer
```

---

### Get Issue

Show full details for a single issue, including description, custom fields, tags, and comments.

```
yt issue get <ISSUE_ID>
```

**Example:**
```bash
yt issue get DEMO-42
```

**Output:**
```
──────────────────────────────────────────────────────────────────────
ID:          DEMO-42
Summary:     Login page broken after SSO update
Project:     Demo Project (DEMO)
Reporter:    Jane Doe
Assignee:    John Smith
State:       In Progress
Priority:    Critical
Type:        Bug
Tags:        regression, auth
Created:     2024-01-15 09:32
Updated:     2024-01-16 14:10

Description:
After updating the SSO provider the login page returns 500.
Steps to reproduce: ...

Comments (2):
  Jane Doe at 2024-01-15 10:00:
    Confirmed on staging too.
  John Smith at 2024-01-16 14:10:
    Fix is in review, see PR #234.
──────────────────────────────────────────────────────────────────────
```

---

### Create Issue

Create a new issue in a project.

```
yt issue create --project <PROJECT> --summary <SUMMARY> [--description <TEXT>]
```

| Option | Required | Description |
|--------|----------|-------------|
| `--project`, `-p` | yes (or default) | Target project |
| `--summary`, `-s` | yes | Issue title |
| `--description`, `-d` | no | Issue body / description |

**Examples:**
```bash
# Minimal
yt issue create --project DEMO --summary "Button misaligned on mobile"

# With description
yt issue create -p BACK -s "Add rate limiting" -d "Implement per-user rate limit of 100 req/min on /api/data"
```

**Output:**
```
✓ Created issue DEMO-55: Button misaligned on mobile
```

---

### Update Issue

Update the summary or description of an existing issue.

```
yt issue update <ISSUE_ID> [--summary <TEXT>] [--description <TEXT>]
```

**Examples:**
```bash
# Update summary only
yt issue update DEMO-55 --summary "Button misaligned on mobile (iOS only)"

# Update description
yt issue update DEMO-55 --description "Affects iPhone 14 and newer in portrait mode."

# Update both
yt issue update DEMO-55 -s "New title" -d "New description"
```

**Output:**
```
✓ Updated DEMO-55
```

---

### Delete Issue

Permanently delete an issue. Prompts for confirmation unless `--yes` is passed.

```
yt issue delete <ISSUE_ID> [--yes]
```

**Examples:**
```bash
# With confirmation prompt
yt issue delete DEMO-99

# Skip prompt
yt issue delete DEMO-99 --yes
```

**Output:**
```
Delete issue DEMO-99? This cannot be undone. [y/N] y
✓ Deleted DEMO-99
```

---

### Add Comment

Add a text comment to an issue.

```
yt issue comment <ISSUE_ID> --text <TEXT>
```

**Example:**
```bash
yt issue comment DEMO-42 --text "Verified fix works on production."
```

**Output:**
```
✓ Comment added (id: 2-150)
```

---

### Apply Command

Apply a [YouTrack command](https://www.jetbrains.com/help/youtrack/server/Commands.html) to an issue.
Commands are the same strings used in the command window inside YouTrack.

```
yt issue command <ISSUE_ID> <COMMAND>
```

**Examples:**
```bash
# Change state
yt issue command DEMO-42 "State Fixed"

# Set priority
yt issue command DEMO-42 "Priority Critical"

# Assign to a user
yt issue command DEMO-42 "assignee john.smith"

# Add tag
yt issue command DEMO-42 "tag regression"

# Multiple fields at once
yt issue command DEMO-42 "Priority Major State In Progress assignee me"
```

**Output:**
```
✓ Command 'Priority Critical' applied to DEMO-42
```

---

## Search

Search issues using the full [YouTrack query language](https://www.jetbrains.com/help/youtrack/server/Search-and-Command-Attributes.html).

```
yt search <QUERY> [--top <N>] [--skip <N>] [--verbose]
```

| Option | Default | Description |
|--------|---------|-------------|
| `--top`, `-n` | 20 | Maximum results |
| `--skip` | 0 | Skip N results |
| `--verbose`, `-v` | false | Show full issue details instead of one-liners |

**Examples:**
```bash
# All unresolved bugs in DEMO
yt search "project: DEMO #Unresolved type: Bug"

# Issues assigned to me updated in the last week
yt search "assignee: me updated: -1w .. today"

# Critical issues across all projects
yt search "Priority: Critical #Unresolved"

# Issues with a specific text in summary
yt search "summary: {login page}"

# Full details for matching issues
yt search "project: BACK assignee: jane" --verbose

# Paginate
yt search "#Unresolved" --top 10 --skip 10
```

**Output (default):**
```
Search results for "project: DEMO #Unresolved type: Bug" (3):
  DEMO-2 [In Progress] (Critical)  API timeout errors
  DEMO-8 [Open] (Normal)  CSV export breaks on empty data
  DEMO-12 [Open] (Minor)  Tooltip flickers on hover
```

---

## Agile Boards

### List Boards

```bash
yt board list
```

**Output:**
```
Agile Boards (2):
  112-1  Backend Sprint Board
  112-2  Mobile Kanban
```

---

### Get Board

Show board details: columns and all sprints.

```
yt board get <BOARD_ID>
```

**Example:**
```bash
yt board get 112-1
```

**Output:**
```
──────────────────────────────────────────────────────────────────────
Board:  Backend Sprint Board  (112-1)

Sprints:
  201-1  Sprint 1  2024-01-01 → 2024-01-14  [archived]
  201-2  Sprint 2  2024-01-15 → 2024-01-28  [active]
       Goal: Finish authentication module
  201-3  Sprint 3  2024-01-29 → 2024-02-11

Columns: Open, In Progress, In Review, Fixed
──────────────────────────────────────────────────────────────────────
```

---

### View Sprint

List all issues in a sprint. When `SPRINT_ID` is omitted, all sprints for the board are listed.

```
yt board sprint <BOARD_ID> [SPRINT_ID]
```

**Examples:**
```bash
# List sprints
yt board sprint 112-1

# View issues in a specific sprint
yt board sprint 112-1 201-2
```

**Output (sprint view):**
```
──────────────────────────────────────────────────────────────────────
Sprint:  Sprint 2  [active]  (201-2)
Goal:    Finish authentication module
Start:   2024-01-15 00:00
End:     2024-01-28 00:00

Issues (4):
  BACK-10 [In Progress] (Critical)  Implement JWT refresh tokens
  BACK-11 [Open] (Normal)  Add OAuth2 callback handler
  BACK-12 [In Review] (Normal)  Unit tests for auth service
  BACK-13 [Fixed] (Minor)  Fix redirect after logout
──────────────────────────────────────────────────────────────────────
```

---

## Activity

### User Activity

Show the recent activity performed by a specific user across all issues.

```
yt activity user <LOGIN> [--top <N>]
```

| Option | Default | Description |
|--------|---------|-------------|
| `--top`, `-n` | 30 | Maximum events to show |

**Examples:**
```bash
# Activity for user john.smith
yt activity user john.smith

# My own activity (use your login)
yt activity user jane.doe --top 50
```

**Output:**
```
Activity for 'john.smith' (5 events):
  2024-01-16 14:10  John Smith  [CustomField]  BACK-10: JWT refresh  (was: Open) → In Progress
  2024-01-16 13:45  John Smith  [Comment]      BACK-10: JWT refresh  → "Working on it"
  2024-01-15 17:00  John Smith  [IssueCreated] BACK-13: Fix redirect after logout
  2024-01-15 16:30  John Smith  [CustomField]  BACK-11: OAuth handler  → Normal
  2024-01-15 09:00  John Smith  [CustomField]  DEMO-42: Login broken  (was: Open) → In Progress
```

---

### Issue Activity

Show the full activity history for a specific issue.

```
yt activity issue <ISSUE_ID> [--top <N>]
```

| Option | Default | Description |
|--------|---------|-------------|
| `--top`, `-n` | 50 | Maximum events to show |

**Examples:**
```bash
yt activity issue DEMO-42
yt activity issue BACK-10 --top 100
```

**Output:**
```
Activity for 'DEMO-42' (4 events):
  2024-01-15 09:32  Jane Doe    [IssueCreated] DEMO-42: Login page broken
  2024-01-15 10:00  Jane Doe    [Comment]      DEMO-42: Login page broken → "Confirmed on staging"
  2024-01-16 14:10  John Smith  [CustomField]  DEMO-42: Login page broken  (was: Open) → In Progress
  2024-01-16 14:10  John Smith  [Links]        DEMO-42: Login page broken → PR #234
```

---

## Exit Codes

| Code | Meaning |
|------|---------|
| 0 | Success |
| 1 | Error (API error, config missing, validation failure) |

---

## YouTrack Query Language

The `search` command and `issue list` accept YouTrack's query language. Key syntax:

```
# Filter by project
project: DEMO

# Filter by state
#Unresolved     (shorthand for State: -Resolved)
State: {In Progress}

# Filter by assignee
assignee: me
assignee: john.smith

# Filter by priority
Priority: Critical

# Filter by type
Type: Bug

# Filter by tag
tag: regression

# Date ranges (relative)
created: -1w .. today
updated: -2d .. today

# Text search
summary: {login page}

# Combine
project: DEMO assignee: me #Unresolved Priority: Critical
```

Full reference: https://www.jetbrains.com/help/youtrack/server/Search-and-Command-Attributes.html
