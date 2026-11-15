package vn.id.tozydev.tusu.data.sample

import com.mohamedrejeb.richeditor.annotation.ExperimentalRichTextApi
import com.mohamedrejeb.richeditor.json.toJson
import com.mohamedrejeb.richeditor.model.RichTextState
import kotlin.random.Random
import kotlin.time.Clock
import kotlin.time.Duration.Companion.minutes
import kotlin.time.Instant
import kotlin.uuid.Uuid
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.LocalTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.minus
import kotlinx.datetime.toInstant
import kotlinx.datetime.toLocalDateTime
import vn.id.tozydev.tusu.domain.model.EntryDraft
import vn.id.tozydev.tusu.domain.model.EntryLocation

/** A photo to attach; its id is fixed here so a re-run on a fresh database matches. */
internal data class PlannedPhoto(
    val id: Uuid,
    val image: SampleImage,
    val position: Int,
    val takenAt: Instant,
)

/** One sample entry; [labelSlots] index into whatever label pool the seeder resolves them in. */
internal data class PlannedEntry(
    val draft: EntryDraft,
    val labelSlots: List<Int>,
    val photos: List<PlannedPhoto>,
)

private const val SEED = 20_261_115L
internal const val SAMPLE_DAYS = 365
private const val MEDIA_ONLY_ENTRIES = 2

private const val EMOJI_PERCENT = 40
private const val LABELED_PERCENT = 50
private const val LOCATED_PERCENT = 25
private const val MAPPED_LOCATION_PERCENT = 55
private const val PHOTO_PERCENT = 30
private const val HASHTAG_PERCENT = 12

/**
 * Plans the sample journal: the newest entry is [Clock.now]'s day, then back over [SAMPLE_DAYS]
 * days. A fixed [Random] seed makes the plan identical on every run for the same clock and zone.
 */
context(clock: Clock, timeZone: TimeZone)
internal fun planSampleEntries(labelCount: Int, images: List<SampleImage>): List<PlannedEntry> {
    val random = Random(SEED)
    val now = clock.now()
    val today = now.toLocalDateTime(timeZone).date

    val moments = buildList {
        for (daysAgo in SAMPLE_DAYS - 1 downTo 0) {
            val date = today.minus(daysAgo, DateTimeUnit.DAY)
            var count = random.pickEntriesPerDay()
            // The first screens of the timeline should never be empty.
            if (daysAgo == 0) count = maxOf(count, 2)
            if (daysAgo == 1) count = maxOf(count, 1)
            val times =
                List(count) {
                        val (hour, minute) = random.pickTimeOfDay()
                        val candidate = LocalDateTime(date, LocalTime(hour, minute))
                        candidate.toInstant(timeZone)
                    }
                    .sorted()
            // Today's entries cannot be in the future when the app starts in the morning.
            addAll(times.map { if (it > now) now - random.nextInt(1, 31).minutes else it })
        }
    }

    val mediaOnly = mutableSetOf<Int>()
    while (mediaOnly.size < MEDIA_ONLY_ENTRIES.coerceAtMost(moments.size)) {
        mediaOnly += random.nextInt(moments.size)
    }

    return moments.mapIndexed { index, occurredAt ->
        val isMediaOnly = index in mediaOnly && images.isNotEmpty()
        val id = random.nextUuid()
        val body = if (isMediaOnly) emptyBody() else random.composeBody()
        val emoji = SampleContent.emoji.random(random).takeIf { random.chance(EMOJI_PERCENT) }
        val location = if (random.chance(LOCATED_PERCENT)) random.pickLocation() else null
        val labelSlots =
            if (labelCount > 0 && random.chance(LABELED_PERCENT)) {
                random.pickLabelSlots(labelCount)
            } else {
                emptyList()
            }
        val photoCount =
            when {
                images.isEmpty() -> 0
                isMediaOnly -> random.nextInt(1, 4)
                random.chance(PHOTO_PERCENT) -> random.pickPhotoCount()
                else -> 0
            }
        val firstImage = if (images.isEmpty()) 0 else random.nextInt(images.size)
        val photos =
            List(photoCount) { position ->
                PlannedPhoto(
                    id = random.nextUuid(),
                    // Consecutive images, so one entry never repeats a photo.
                    image = images[(firstImage + position) % images.size],
                    position = position,
                    takenAt = occurredAt - random.nextInt(1, 21).minutes,
                )
            }
        PlannedEntry(
            draft =
                EntryDraft(
                    id = id,
                    body = body,
                    emoji = emoji,
                    occurredAt = occurredAt,
                    timeZone = timeZone,
                    location = location,
                ),
            labelSlots = labelSlots,
            photos = photos,
        )
    }
}

private fun Random.nextUuid(): Uuid = Uuid.fromLongs(nextLong(), nextLong())

private fun Random.chance(percent: Int): Boolean = nextInt(100) < percent

/** Picks the key whose cumulative weight covers a roll in 0..99. */
private fun <T> Random.weighted(vararg options: Pair<Int, T>): T {
    var roll = nextInt(options.sumOf { it.first })
    for ((weight, value) in options) {
        if (roll < weight) return value
        roll -= weight
    }
    return options.last().second
}

// Mean of about 0.9 over a year gives roughly 330 entries, with many empty days.
private fun Random.pickEntriesPerDay(): Int =
    weighted(56 to 0, 22 to 1, 10 to 2, 6 to 3, 3 to 4, 2 to 5, 1 to 6)

private fun Random.pickPhotoCount(): Int =
    weighted(45 to 1, 25 to 2, 15 to 3, 7 to 4, 5 to 5, 3 to 6)

private fun Random.pickTimeOfDay(): Pair<Int, Int> {
    val hours = weighted(20 to 6..9, 20 to 11..13, 20 to 14..17, 40 to 18..22)
    return nextInt(hours.first, hours.last + 1) to nextInt(60)
}

private fun Random.pickLocation(): EntryLocation =
    if (chance(MAPPED_LOCATION_PERCENT)) {
        SampleContent.mappedPlaces.random(this)
    } else {
        EntryLocation(name = SampleContent.freeTypedPlaces.random(this))
    }

private fun Random.pickLabelSlots(labelCount: Int): List<Int> {
    val wanted = weighted(50 to 1, 35 to 2, 15 to 3).coerceAtMost(labelCount)
    return (0 until labelCount).shuffled(this).take(wanted).sorted()
}

@OptIn(ExperimentalRichTextApi::class) private fun emptyBody(): String = RichTextState().toJson()

@OptIn(ExperimentalRichTextApi::class)
private fun Random.composeBody(): String = RichTextState().setMarkdown(composeMarkdown()).toJson()

/** One line to several paragraphs; some with a heading, a list, bold or italic text, hashtags. */
private fun Random.composeMarkdown(): String {
    val paragraphs = mutableListOf<String>()
    when (weighted(35 to Length.LINE, 30 to Length.SHORT, 20 to Length.MEDIUM, 15 to Length.LONG)) {
        Length.LINE -> paragraphs += SampleContent.oneLiners.random(this)
        Length.SHORT -> {
            paragraphs +=
                "${SampleContent.openers.random(this)} ${SampleContent.middles.random(this)}"
            if (nextBoolean()) paragraphs += SampleContent.closers.random(this)
        }
        Length.MEDIUM -> {
            paragraphs +=
                "${SampleContent.openers.random(this)} ${SampleContent.middles.random(this)}"
            paragraphs +=
                "${SampleContent.middles.random(this)} ${SampleContent.closers.random(this)}"
        }
        Length.LONG -> {
            if (chance(40)) paragraphs += SampleContent.headings.random(this)
            paragraphs +=
                "${SampleContent.openers.random(this)} ${SampleContent.middles.random(this)}"
            if (chance(60)) {
                paragraphs += "Nhìn lại một chút:"
                paragraphs += SampleContent.lists.random(this).joinToString("\n") { "- $it" }
            }
            paragraphs += SampleContent.middles.random(this)
            paragraphs +=
                "${SampleContent.middles.random(this)} ${SampleContent.closers.random(this)}"
        }
    }
    if (chance(HASHTAG_PERCENT)) {
        val tags = SampleContent.hashtags.shuffled(this).take(nextInt(1, 3)).joinToString(" ")
        // A tag line on its own would read as a heading in Markdown, so it joins a paragraph.
        val last = paragraphs.lastIndex
        if (paragraphs[last].startsWith("- ") || paragraphs[last].startsWith("#")) {
            paragraphs += "${SampleContent.closers.random(this)} $tags"
        } else {
            paragraphs[last] = "${paragraphs[last]} $tags"
        }
    }
    return paragraphs.joinToString("\n\n")
}

private enum class Length {
    LINE,
    SHORT,
    MEDIUM,
    LONG,
}
