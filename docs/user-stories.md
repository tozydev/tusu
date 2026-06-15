# Tusu: User Stories

## Feature Summary

- **Writing Experience:** Optimize fast note-taking with a title-less mechanism, Markdown support,
  Auto-save, and emotional expression via Emojis.
- **Management & Navigation:** An intuitive journal management system via a chronological Feed,
  supporting content Previews, Tag-based classification, and multimedia attachments (Images).
- **Data Security:** Ensure privacy and sustainability with a Local-first architecture and backup
  mechanisms.

## Epic 1: Core Writing Experience

**Goal:** Focus on helping users take notes quickly and without barriers.

| ID     | Feature            | Description                                                                                                                              | Acceptance Criteria                                                                                                                                                                                                                  | Status  |
|:-------|:-------------------|:-----------------------------------------------------------------------------------------------------------------------------------------|:-------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|:--------|
| US-1.1 | Quick Entry        | As a user, I want to be able to start writing immediately without needing to set a title, so that my flow of emotion is not interrupted. | - No title field.<br>- The new entry screen defaults the cursor to the content section.                                                                                                                                              | 🆕 Todo |
| US-1.2 | Markdown Support   | As a user, I want to use Markdown syntax, so that I can format text quickly.                                                             | - Support basic syntax: Headings, Bold, Italic, Lists.<br>- Display visual formatting (Rich Text) after writing and in view mode.                                                                                                    | 🆕 Todo |
| US-1.3 | Auto-save          | As a user, I want the content to be saved automatically, so that I don't lose data if the application closes unexpectedly.               | - Automatically save content after typing stops for approximately 500ms or when exiting the screen.<br>- Other inputs (tags, date, time) are auto-saved upon completion of the action.<br>- No save button or save status displayed. | 🆕 Todo |
| US-1.4 | Emotional Reaction | As a user, I want to select an Emoji representing my emotion, so that I can look back at my mood.                                        | - Provide a list of Emojis to choose from.<br>- Each entry can only be assigned a maximum of one Emoji.                                                                                                                              | 🆕 Todo |

## Epic 2: Content Organization & Navigation

**Goal:** Help users manage and retrieve memories easily.

| ID     | Feature            | Description                                                                                                | Acceptance Criteria                                                                                                                                                                                                                                                                                        | Status  |
|:-------|:-------------------|:-----------------------------------------------------------------------------------------------------------|:-----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|:--------|
| US-2.1 | Chronological Feed | As a user, I want to view a list of entries in chronological order, so that I can easily track my journal. | - Main screen displays a list of entries.<br>- Use a custom time field set by the user, defaulting to the creation time of the entry.<br>- Sort order: Newest $\rightarrow$ Oldest.                                                                                                                        | 🆕 Todo |
| US-2.2 | Content Preview    | As a user, I want to see a short summary on the Feed, so that I can quickly identify the content.          | - Display a maximum of X initial characters on the Feed.<br>- Display images/photo albums if the entry has attached images.<br>- Display reaction if the entry has an emoji.                                                                                                                               | 🆕 Todo |
| US-2.3 | Tagging System     | As a user, I want to add tags to entries, so that I can categorize and search for topics.                  | - Allow adding/removing multiple tags to an entry.<br>- Ability to filter the Feed list by selected tags.                                                                                                                                                                                                  | 🆕 Todo |
| US-2.4 | Media Attachment   | As a user, I want to insert images into an entry, so that I can preserve memories through visuals.         | - Allow selecting photos from device storage.<br>- Images are copied to storage managed by the app.<br>- Allow selecting and attaching multiple images from device storage.<br>- Images are displayed as a gallery in the header area.<br>- Images are NOT displayed interspersed within the text content. | 🆕 Todo |

## Epic 3: Data Security & Reliability

**Goal:** Ensure user data is always safe and recoverable.

| ID     | Feature             | Description                                                                                                   | Acceptance Criteria                                                                                                          | Status  |
|:-------|:--------------------|:--------------------------------------------------------------------------------------------------------------|:-----------------------------------------------------------------------------------------------------------------------------|:--------|
| US-3.1 | Local-first Storage | As a user, I want data to be stored locally, so that I can use the application at any time.                   | - Application operates without Internet.<br>- Data is stored in SQLite and local system files.                               | 🆕 Todo |
| US-3.2 | Manual Backup       | As a user, I want to be able to export all data to a compressed file, so that I can store it in a safe place. | - Option to export all data at once.<br>- Exported file is in a compressed format (ZIP/JSON) containing all text and images. | 🆕 Todo |
