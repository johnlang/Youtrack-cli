# YouTrack CLI — Full Documentation

A command-line client for [JetBrains YouTrack](https://www.jetbrains.com/youtrack/) written in Kotlin.
The binary is invoked as `yt`.

---

## Table of Contents

1. [Configuration](#configuration)
2. [Projects](#projects)
3. [Browse](#browse)
4. [Issues (CRUD)](#issues-crud)
   - [List issues](#list-issues)
   - [Get issue](#get-issue)
   - [Create issue](#create-issue)
   - [Update issue](#update-issue)
   - [Delete issue](#delete-issue)
   - [Add comment](#add-comment)
   - [Apply command](#apply-command)
5. [Issue Links](#issue-links)
   - [List links](#list-links)
   - [Link issues](#link-issues)
   - [List link types](#list-link-types)
6. [Work Items](#work-items)
   - [List work items](#list-work-items)
   - [Add work item](#add-work-item)
   - [Delete work item](#delete-work-item)
7. [Search](#search)
8. [Agile Boards](#agile-boards)
   - [List boards](#list-boards)
   - [Get board](#get-board)
   - [View sprint](#view-sprint)
9. [Activity](#activity)
   - [User activity](#user-activity)
   - [Issue activity](#issue-activity)
10. [Users](#users)
    - [List users](#list-users)
    - [Get user](#get-user)
    - [Current user](#current-user)
11. [Tags](#tags)
12. [Articles](#articles)
    - [List articles](#list-articles)
    - [Get article](#get-article)
    - [Create article](#create-article)
    - [Update article](#update-article)
    - [Delete article](#delete-article)
13. [Exit Codes](#exit-codes)
14. [YouTrack Query Language](#youtrack-query-language)

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

## Browse

Open an issue directly in the browser. The URL is constructed from the configured base URL.

```
yt browse <ISSUE_ID>
yt b <ISSUE_ID>
```

**Examples:**
```bash
yt b DEMO-1
yt browse BACK-42
```

**Output:**
```
Opening https://acme.youtrack.cloud/issue/DEMO-1
```

The command opens the URL using the system default browser (`xdg-open` on Linux, `open` on macOS, `start` on Windows) and exits immediately.

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

## Issue Links

### List Links

Show all links (relations) on an issue.

```
yt issue links <ISSUE_ID>
```

**Example:**
```bash
yt issue links DEMO-42
```

**Output:**
```
Links for DEMO-42:
  relates to:
    DEMO-10  API returns wrong error code
  subtask of:
    BACK-5   Auth service refactor
  duplicated by:
    DEMO-51  [resolved]  Same 500 on login
```

---

### Link Issues

Link an issue to another using a relation command string.
Run `yt issue linktypes` first to see the exact relation phrases for your instance.

```
yt issue link <ISSUE_ID> <LINK_COMMAND>
```

`LINK_COMMAND` combines the relation phrase and the target issue ID, e.g. `"relates to DEMO-10"`.

**Examples:**
```bash
yt issue link DEMO-42 "relates to DEMO-10"
yt issue link DEMO-42 "subtask of BACK-5"
yt issue link DEMO-42 "duplicates MOB-7"
```

**Output:**
```
✓ Link applied to DEMO-42: relates to DEMO-10
```

---

### List Link Types

Display all issue link type names and their directed phrases.

```bash
yt issue linktypes
```

**Output:**
```
Issue Link Types (4):
  Relates  (relates to / is related to)
  Subtask  (subtask of / parent for)
  Duplicate  (duplicates)
  Depend  (depends on / is required for)
```

Use the direction phrases (e.g. `relates to`, `subtask of`) as the verb in `yt issue link`.

---

## Work Items

Work items record time spent on an issue (time tracking).

### List Work Items

```
yt issue workitem list <ISSUE_ID> [--top <N>]
```

| Option | Default | Description |
|--------|---------|-------------|
| `--top`, `-n` | 50 | Maximum work items to return |

**Example:**
```bash
yt issue workitem list DEMO-42
```

**Output:**
```
Work items for DEMO-42 (2):
  2-1  1h 30m  john.smith  2024-01-16 14:00  — Fixed JWT refresh logic
  2-2  45m     jane.doe    2024-01-17 09:30
```

---

### Add Work Item

Log time spent on an issue.

```
yt issue workitem add <ISSUE_ID> --duration <DURATION> [--description <TEXT>] [--date <YYYY-MM-DD>]
```

| Option | Required | Description |
|--------|----------|-------------|
| `--duration`, `-d` | yes | Duration string, e.g. `1h30m`, `2h`, `45m` |
| `--description`, `--desc` | no | Description of work done |
| `--date` | no | Date of work as `YYYY-MM-DD` (defaults to today) |

**Examples:**
```bash
# Log 1h 30m with a description
yt issue workitem add DEMO-42 --duration 1h30m --description "Implemented JWT refresh"

# Log 45 minutes for a specific date
yt issue workitem add DEMO-42 -d 45m --date 2024-01-15
```

**Output:**
```
✓ Work item logged on DEMO-42 (id: 2-3): 1h 30m
```

---

### Delete Work Item

```
yt issue workitem delete <ISSUE_ID> <WORK_ITEM_ID> [--yes]
```

**Example:**
```bash
yt issue workitem delete DEMO-42 2-3 --yes
```

**Output:**
```
✓ Deleted work item 2-3 from DEMO-42
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

## Users

### List Users

List all users visible with the configured token.

```
yt user list [--top <N>]
```

| Option | Default | Description |
|--------|---------|-------------|
| `--top`, `-n` | 50 | Maximum users to return |

**Example:**
```bash
yt user list
```

**Output:**
```
Users (3):
  john.smith  John Smith  <john.smith@example.com>
  jane.doe    Jane Doe    <jane.doe@example.com>
  bot.ci      CI Bot
```

---

### Get User

Show full details for a single user.

```
yt user get <LOGIN>
```

**Example:**
```bash
yt user get john.smith
```

**Output:**
```
──────────────────────────────────────────────────
Login:     john.smith
Name:      John Smith
Email:     john.smith@example.com
ID:        1-101
Groups:    Developers, All Users
──────────────────────────────────────────────────
```

---

### Current User

Show the profile of the currently authenticated user (token owner).

```bash
yt user me
```

**Output:**
```
──────────────────────────────────────────────────
Login:     john.smith
Name:      John Smith
Email:     john.smith@example.com
ID:        1-101
Groups:    Developers, All Users
──────────────────────────────────────────────────
```

---

## Tags

### List Tags

List all tags accessible with the configured token.

```
yt tag list [--top <N>]
```

| Option | Default | Description |
|--------|---------|-------------|
| `--top`, `-n` | 100 | Maximum tags to return |

**Example:**
```bash
yt tag list
```

**Output:**
```
Tags (5):
  6-0  regression
  6-1  auth
  6-2  performance
  6-3  ui
  6-4  blocked
```

Use the tag name in issue search queries: `tag: regression`.
To add a tag to an issue, use: `yt issue command DEMO-42 "tag regression"`.

---

## Articles

YouTrack's knowledge base stores documentation as articles. Articles belong to projects and support nesting (parent/child).

### List Articles

```
yt article list [--project <PROJECT>] [--top <N>] [--skip <N>]
```

| Option | Default | Description |
|--------|---------|-------------|
| `--project`, `-p` | all projects | Filter by project short name |
| `--top`, `-n` | 25 | Maximum results |
| `--skip` | 0 | Skip N results (pagination) |

**Examples:**
```bash
# All articles across all projects
yt article list

# Articles in project DEMO
yt article list --project DEMO

# Paginate
yt article list --project DEMO --top 10 --skip 10
```

**Output:**
```
Articles in 'DEMO' (3):
  DEMO-A-1 [DEMO]  Getting started guide
  DEMO-A-2 [DEMO]  API authentication
  DEMO-A-3 [DEMO]  Troubleshooting FAQ
```

---

### Get Article

Show full content and metadata of an article.

```
yt article get <ARTICLE_ID>
```

**Example:**
```bash
yt article get DEMO-A-1
```

**Output:**
```
──────────────────────────────────────────────────────────────────────
ID:          DEMO-A-1
Title:       Getting started guide
Project:     Demo Project (DEMO)
Author:      Jane Doe
Tags:        onboarding, public
Created:     2024-01-10 09:00
Updated:     2024-01-20 15:30

Sub-articles (2):
  DEMO-A-4  Installation steps
  DEMO-A-5  First configuration

Content:
Welcome to the project! This guide walks you through...
──────────────────────────────────────────────────────────────────────
```

---

### Create Article

Create a new knowledge base article.

```
yt article create --summary <TITLE> [--project <PROJECT>] [--content <TEXT>]
```

| Option | Required | Description |
|--------|----------|-------------|
| `--summary`, `-s` | yes | Article title |
| `--project`, `-p` | yes (or default) | Target project |
| `--content`, `-c` | no | Article body text |

**Example:**
```bash
yt article create --project DEMO --summary "How to reset your password" \
  --content "Navigate to Profile → Account Security → Change password."
```

**Output:**
```
✓ Created article DEMO-A-6: How to reset your password
```

---

### Update Article

Update the title or content of an existing article.

```
yt article update <ARTICLE_ID> [--summary <TITLE>] [--content <TEXT>]
```

**Examples:**
```bash
# Update title only
yt article update DEMO-A-6 --summary "How to reset your password (updated)"

# Update content
yt article update DEMO-A-6 --content "New instructions..."

# Update both
yt article update DEMO-A-6 -s "New title" -c "New content"
```

**Output:**
```
✓ Updated DEMO-A-6
```

---

### Delete Article

Permanently delete an article. Prompts for confirmation unless `--yes` is passed.

```
yt article delete <ARTICLE_ID> [--yes]
```

**Examples:**
```bash
yt article delete DEMO-A-6
yt article delete DEMO-A-6 --yes
```

**Output:**
```
Delete article DEMO-A-6? This cannot be undone. [y/N] y
✓ Deleted article DEMO-A-6
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
