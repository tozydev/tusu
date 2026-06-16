package vn.io.tozyworks.tusu.data.db

import androidx.room.immediateTransaction
import androidx.room.useWriterConnection
import kotlin.time.Instant
import kotlin.uuid.Uuid

internal suspend fun seedDatabase(db: TusuDatabase) {
    db.useWriterConnection {
        it.immediateTransaction {
            if (db.entryDao().getFirstEntryId() != null) {
                return@immediateTransaction
            }
            db.tagDao().insertAll(tagSeeds)
            db.entryDao().insertAll(entrySeeds)
            db.entryTagDao().insertAll(entryTagSeeds)
        }
    }
}

private object SeedIds {
    // Tags
    val projectTagId = Uuid.random()
    val learningTagId = Uuid.random()
    val workTagId = Uuid.random()
    val personalTagId = Uuid.random()
    val reflectionTagId = Uuid.random()
    val ideaTagId = Uuid.random()
    val productivityTagId = Uuid.random()
    val designTagId = Uuid.random()

    // Entries
    val entry1Id = Uuid.random()
    val entry2Id = Uuid.random()
    val entry3Id = Uuid.random()
    val entry4Id = Uuid.random()
    val entry5Id = Uuid.random()
    val entry6Id = Uuid.random()
    val entry7Id = Uuid.random()
    val entry8Id = Uuid.random()
    val entry9Id = Uuid.random()
    val entry10Id = Uuid.random()
}

internal val tagSeeds =
    listOf(
        TagEntity(SeedIds.projectTagId, "project"),
        TagEntity(SeedIds.learningTagId, "learning"),
        TagEntity(SeedIds.workTagId, "work"),
        TagEntity(SeedIds.personalTagId, "personal"),
        TagEntity(SeedIds.reflectionTagId, "reflection"),
        TagEntity(SeedIds.ideaTagId, "idea"),
        TagEntity(SeedIds.productivityTagId, "productivity"),
        TagEntity(SeedIds.designTagId, "design"),
    )

internal val entrySeeds =
    listOf(
        EntryEntity(
            id = SeedIds.entry1Id,
            recordedAt = Instant.parse("2026-04-18T08:15:00Z"),
            emoji = "🌱",
            content =
                """
                # Khởi đầu dự án Journal

                Hôm nay mình bắt đầu xây dựng ứng dụng journal mới.

                Mục tiêu:

                - Offline-first
                - Hỗ trợ Markdown
                - Giao diện tối giản

                > Một ứng dụng ghi chép tốt nên biến việc viết thành thói quen tự nhiên.
                """
                    .trimIndent(),
        ),
        EntryEntity(
            id = SeedIds.entry2Id,
            recordedAt = Instant.parse("2026-06-02T12:30:00Z"),
            emoji = "☕",
            content =
                """
                # Khám phá Markdown trong Compose

                Dành thời gian nghiên cứu cách render Markdown.

                ```kotlin
                Markdown(content)
                ```

                Kết quả ban đầu khá ổn.
                """
                    .trimIndent(),
        ),
        EntryEntity(
            id = SeedIds.entry3Id,
            recordedAt = Instant.parse("2026-06-03T09:20:00Z"),
            emoji = "📚",
            content =
                """
                # Ghi chú học tập

                Hôm nay đọc lại về **Clean Architecture**.

                Điều đáng chú ý:

                - Domain độc lập framework
                - UseCase chứa business rules
                - UI chỉ là chi tiết triển khai
                """
                    .trimIndent(),
        ),
        EntryEntity(
            id = SeedIds.entry4Id,
            recordedAt = Instant.parse("2026-06-03T21:10:00Z"),
            emoji = "🚀",
            content =
                """
                # Phiên bản đầu tiên

                Đã hoàn thành:

                - [x] Tạo entry
                - [x] Danh sách entry
                - [ ] Tìm kiếm
                - [ ] Đồng bộ dữ liệu

                Một cột mốc nhỏ nhưng đáng nhớ.
                """
                    .trimIndent(),
        ),
        EntryEntity(
            id = SeedIds.entry5Id,
            recordedAt = Instant.parse("2026-06-05T21:05:00Z"),
            emoji = "🌙",
            content =
                """
                # Một ngày yên tĩnh

                Tối nay mình chỉ ngồi nghe nhạc và chỉnh sửa giao diện.

                *Không quá năng suất nhưng rất thư giãn.*
                """
                    .trimIndent(),
        ),
        EntryEntity(
            id = SeedIds.entry6Id,
            recordedAt = Instant.parse("2026-06-06T10:15:00Z"),
            emoji = "🛠️",
            content =
                """
                # Refactor Database

                Một số logic được chuyển khỏi UI xuống repository.

                Lợi ích:

                1. Dễ test hơn
                2. Giảm lặp code
                3. Dễ bảo trì hơn
                """
                    .trimIndent(),
        ),
        EntryEntity(
            id = SeedIds.entry7Id,
            recordedAt = Instant.parse("2026-06-06T20:40:00Z"),
            emoji = "🌤️",
            content =
                """
                # Ý tưởng mới

                Muốn thêm tính năng thống kê nhật ký.

                ## Theo dõi

                - Số entry mỗi ngày
                - Chuỗi ngày liên tiếp
                - Emoji được dùng nhiều nhất
                """
                    .trimIndent(),
        ),
        EntryEntity(
            id = SeedIds.entry8Id,
            recordedAt = Instant.parse("2026-06-08T19:15:00Z"),
            emoji = "🎨",
            content =
                """
                # Thiết kế giao diện

                Hướng tới phong cách:

                - Minimal
                - Calm
                - Nature-inspired

                Trải nghiệm viết nên nhẹ nhàng và ít gây xao nhãng.
                """
                    .trimIndent(),
        ),
        EntryEntity(
            id = SeedIds.entry9Id,
            recordedAt = Instant.parse("2026-06-08T22:30:00Z"),
            emoji = "🧠",
            content =
                """
                # Suy nghĩ trong ngày

                Đôi khi mình dành quá nhiều thời gian để tối ưu kiến trúc.

                **Hoàn thành trước, hoàn hảo sau.**
                """
                    .trimIndent(),
        ),
        EntryEntity(
            id = SeedIds.entry10Id,
            recordedAt = Instant.parse("2026-07-01T22:10:00Z"),
            emoji = "✨",
            content =
                """
                # Kết thúc một tuần

                Thành tựu nổi bật:

                - Hoàn thiện Entry
                - Bổ sung Tag
                - Hỗ trợ Media
                - Cải thiện UX nhập liệu

                Dự án đang đi đúng hướng.
                """
                    .trimIndent(),
        ),
    )

private val entryTagSeeds =
    listOf(
        EntryTagCrossRef(SeedIds.entry1Id, SeedIds.projectTagId),
        EntryTagCrossRef(SeedIds.entry1Id, SeedIds.ideaTagId),
        EntryTagCrossRef(SeedIds.entry2Id, SeedIds.learningTagId),
        EntryTagCrossRef(SeedIds.entry3Id, SeedIds.learningTagId),
        EntryTagCrossRef(SeedIds.entry3Id, SeedIds.workTagId),
        EntryTagCrossRef(SeedIds.entry4Id, SeedIds.projectTagId),
        EntryTagCrossRef(SeedIds.entry4Id, SeedIds.workTagId),
        EntryTagCrossRef(SeedIds.entry5Id, SeedIds.personalTagId),
        EntryTagCrossRef(SeedIds.entry5Id, SeedIds.reflectionTagId),
        EntryTagCrossRef(SeedIds.entry6Id, SeedIds.workTagId),
        EntryTagCrossRef(SeedIds.entry6Id, SeedIds.projectTagId),
        EntryTagCrossRef(SeedIds.entry7Id, SeedIds.ideaTagId),
        EntryTagCrossRef(SeedIds.entry7Id, SeedIds.projectTagId),
        EntryTagCrossRef(SeedIds.entry8Id, SeedIds.designTagId),
        EntryTagCrossRef(SeedIds.entry8Id, SeedIds.projectTagId),
        EntryTagCrossRef(SeedIds.entry9Id, SeedIds.reflectionTagId),
        EntryTagCrossRef(SeedIds.entry9Id, SeedIds.productivityTagId),
        EntryTagCrossRef(SeedIds.entry10Id, SeedIds.projectTagId),
        EntryTagCrossRef(SeedIds.entry10Id, SeedIds.reflectionTagId),
        EntryTagCrossRef(SeedIds.entry10Id, SeedIds.productivityTagId),
    )
