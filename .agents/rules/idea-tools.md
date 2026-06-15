---
trigger: always_on
description: >
  This document provides guidelines for using IntelliJ IDEA MCP server tools to interact with the IDE for reading,
  editing, searching, formatting, refactoring, running, and debugging tasks in a project-aware manner.
---

# IDEA Tools

This project uses IntelliJ IDEA MCP server. It provides tools with ability to control and interact
with the IDE, such as
reading files, finding usages, replacing text, replace symbols, and more. These tools can be used to
perform complex
code transformations, refactorings, and other IDE operations programmatically.

## When to use IDEA tools

Prefer IDEA tools for project-aware reading, editing, searching, formatting, refactoring, running,
and debugging.
Use shell commands only when an IDEA tool is not available or not a good fit for the task.

| Tool group        | When to use                                                                     | Common tools                                                                                                        |
|-------------------|---------------------------------------------------------------------------------|---------------------------------------------------------------------------------------------------------------------|
| Read tools        | Inspect source, symbols, or open editors without changing files.                | `read_file`, `get_file_text_by_path`, `get_symbol_info`, `get_all_open_file_paths`                                  |
| File tools        | Discover, open, or create project files and folders.                            | `find_files_by_glob`, `find_files_by_name_keyword`, `list_directory_tree`, `open_file_in_editor`, `create_new_file` |
| Search tools      | Locate files, symbols, or text across the project before editing.               | `search_file`, `search_regex`, `search_symbol`, `search_text`                                                       |
| Text tools        | Make targeted content edits directly in a file.                                 | `replace_text_in_file`, `search_in_files_by_text`, `search_in_files_by_regex`                                       |
| Formatting tools  | Reformat files after edits to keep style consistent.                            | `reformat_file`                                                                                                     |
| Refactoring tools | Rename symbols safely and update references across the project.                 | `rename_refactoring`                                                                                                |
| Analysis tools    | Check for problems, dependencies, or module structure before and after changes. | `get_file_problems`, `build_project`, `get_project_dependencies`, `get_project_modules`                             |
| Execution tools   | Run a configuration or inspect available run targets from the IDE.              | `get_run_configurations`, `execute_run_configuration`                                                               |

Use the narrowest tool that solves the task: read before edit, search before replace, refactor
before manual rename, and
format after edits when needed.

### Rules of thumb

- **Always include `projectPath` parameter** when using IDEA tools to ensure they run in the correct
  project context.
- **Prefer specific search tools** (`search_symbol`, `search_regex`) over generic text search when
  looking for code
  elements, as they are more accurate and efficient.
- **Use file tools to navigate and manage files** instead of shell commands, as they are aware of
  the project structure
  and can handle edge cases (e.g., ignored files, virtual files).
- **Use refactoring tools for renames** to automatically update all references and avoid breaking
  the codebase.
- **Use formatting tools after edits** to maintain a consistent code style across the project.
- Use `include_external=true` to search SDK and library symbols when use `search_symbol`.
