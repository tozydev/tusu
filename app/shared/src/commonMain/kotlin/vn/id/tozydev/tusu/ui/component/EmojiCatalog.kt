package vn.id.tozydev.tusu.ui.component

import org.jetbrains.compose.resources.StringResource
import vn.id.tozydev.tusu.generated.resources.Res
import vn.id.tozydev.tusu.generated.resources.emoji_group_activities
import vn.id.tozydev.tusu.generated.resources.emoji_group_feelings
import vn.id.tozydev.tusu.generated.resources.emoji_group_nature
import vn.id.tozydev.tusu.generated.resources.emoji_group_symbols
import vn.id.tozydev.tusu.text.foldDiacritics

/** One emoji with the words it can be found by, in Vietnamese and English. */
internal data class EmojiInfo(val emoji: String, val keywords: String)

/** A tab of [EmojiPickerSheet]; the "Recent" tab is built from settings and is not listed here. */
internal data class EmojiGroup(val title: StringResource, val emojis: List<EmojiInfo>)

private fun group(title: StringResource, vararg emojis: Pair<String, String>) =
    EmojiGroup(title, emojis.map { (emoji, keywords) -> EmojiInfo(emoji, keywords) })

internal val EmojiGroups: List<EmojiGroup> =
    listOf(
        group(
            Res.string.emoji_group_feelings,
            "😀" to "cười vui grin smile happy",
            "😃" to "cười vui smile happy",
            "😄" to "cười tươi vui smile happy",
            "😁" to "cười toe grin happy",
            "😆" to "cười lớn laugh",
            "😅" to "cười ngượng mồ hôi sweat laugh",
            "🤣" to "cười lăn rofl laugh",
            "😂" to "cười ra nước mắt joy laugh tears",
            "🙂" to "mỉm cười slight smile",
            "🙃" to "lộn ngược upside down",
            "😉" to "nháy mắt wink",
            "😊" to "cười hiền mắc cỡ blush smile",
            "😇" to "thiên thần ngây thơ angel innocent",
            "🥰" to "yêu thương love hearts",
            "😍" to "mê tim yêu love heart eyes",
            "🤩" to "phấn khích ngôi sao star struck excited",
            "😘" to "hôn kiss",
            "😋" to "ngon miệng yummy delicious",
            "😜" to "lè lưỡi nháy tongue wink",
            "🤪" to "điên khùng zany crazy",
            "😝" to "lè lưỡi tongue",
            "🤗" to "ôm hug",
            "🤭" to "che miệng giggle",
            "🤫" to "suỵt im lặng shush quiet",
            "🤔" to "suy nghĩ thinking",
            "😐" to "bình thường neutral",
            "😑" to "vô cảm expressionless",
            "😏" to "nhếch mép smirk",
            "😒" to "chán unamused bored",
            "😬" to "nhăn răng ngại grimace awkward",
            "😌" to "nhẹ nhõm thư giãn relieved calm",
            "😔" to "trầm tư buồn pensive sad",
            "😴" to "ngủ buồn ngủ sleep tired",
            "🤕" to "đau bị thương hurt injured",
            "🤢" to "buồn nôn nauseated sick",
            "🥵" to "nóng hot",
            "😵" to "chóng mặt dizzy",
            "🥳" to "tiệc mừng party celebrate",
            "🥺" to "năn nỉ cầu xin pleading",
            "😢" to "khóc buồn cry sad",
            "😭" to "khóc to sob cry",
            "😤" to "bực tức triumph huff",
            "😠" to "giận angry",
            "😡" to "giận dữ tức rage angry",
            "🤬" to "chửi tức cursing angry",
            "😱" to "sợ hãi hét scream fear",
            "😨" to "sợ fearful",
            "😰" to "lo lắng anxious",
            "😓" to "mệt mồ hôi downcast sweat",
        ),
        group(
            Res.string.emoji_group_activities,
            "⚽" to "bóng đá thể thao soccer football sport",
            "🏀" to "bóng rổ basketball sport",
            "🏈" to "bóng bầu dục american football",
            "⚾" to "bóng chày baseball",
            "🎾" to "quần vợt tennis",
            "🏐" to "bóng chuyền volleyball",
            "🏸" to "cầu lông badminton",
            "🎮" to "chơi game trò chơi video game",
            "🎯" to "mục tiêu phi tiêu target goal",
            "🎨" to "vẽ hội hoạ nghệ thuật art paint",
            "🎵" to "âm nhạc nhạc music",
            "🎤" to "hát karaoke sing microphone",
            "🎬" to "phim điện ảnh movie film",
            "📷" to "chụp ảnh máy ảnh camera photo",
            "✈️" to "máy bay du lịch plane travel flight",
            "🚗" to "ô tô xe lái xe car drive",
            "🚲" to "xe đạp bike bicycle",
            "🏃" to "chạy bộ run running",
            "🧘" to "yoga thiền meditation",
            "📚" to "sách đọc học books read study",
            "✏️" to "bút chì viết pencil write",
            "💼" to "công việc làm việc work briefcase",
            "💻" to "máy tính laptop computer",
            "🎁" to "quà tặng gift present",
            "🎉" to "chúc mừng tiệc party celebrate",
            "🎂" to "sinh nhật bánh kem birthday cake",
            "🛒" to "mua sắm shopping cart",
        ),
        group(
            Res.string.emoji_group_nature,
            "🌱" to "mầm cây non sprout seedling plant",
            "🌿" to "lá thảo mộc herb leaf",
            "🍀" to "cỏ bốn lá may mắn clover luck",
            "🌸" to "hoa anh đào cherry blossom flower",
            "🌼" to "hoa cúc blossom flower",
            "🌻" to "hoa hướng dương sunflower",
            "🌹" to "hoa hồng rose flower",
            "🌞" to "mặt trời nắng sun sunny",
            "🌙" to "trăng đêm moon night",
            "⭐" to "ngôi sao star",
            "🌈" to "cầu vồng rainbow",
            "☀️" to "nắng trời đẹp sun sunny",
            "☁️" to "mây âm u cloud cloudy",
            "🌧️" to "mưa rain rainy",
            "❄️" to "tuyết lạnh snow cold",
            "🔥" to "lửa cháy fire hot",
            "💧" to "giọt nước water drop",
            "🍎" to "táo apple fruit",
            "🍊" to "cam orange fruit",
            "🍋" to "chanh lemon",
            "🍇" to "nho grapes",
            "🍓" to "dâu strawberry",
            "🍰" to "bánh ngọt cake dessert",
            "☕" to "cà phê coffee",
            "🍜" to "phở mì bún noodles soup",
            "🍕" to "pizza",
            "🍔" to "bánh mì kẹp burger",
            "🍣" to "sushi",
            "🐶" to "chó cún dog puppy",
            "🐱" to "mèo cat",
            "🐼" to "gấu trúc panda",
            "🦊" to "cáo fox",
            "🐸" to "ếch frog",
            "🦋" to "bướm butterfly",
            "🏡" to "nhà home house",
            "⛰️" to "núi leo núi mountain hiking",
        ),
        group(
            Res.string.emoji_group_symbols,
            "❤️" to "tim đỏ yêu red heart love",
            "🧡" to "tim cam orange heart",
            "💛" to "tim vàng yellow heart",
            "💚" to "tim xanh lá green heart",
            "💙" to "tim xanh dương blue heart",
            "💜" to "tim tím purple heart",
            "🖤" to "tim đen black heart",
            "🤍" to "tim trắng white heart",
            "🤎" to "tim nâu brown heart",
            "💔" to "tim vỡ thất tình broken heart",
            "✨" to "lấp lánh sparkles",
            "💫" to "chóng mặt sao dizzy star",
            "💯" to "một trăm điểm hundred perfect",
            "✅" to "xong hoàn thành done check",
            "❌" to "sai không cross no",
            "⚠️" to "cảnh báo warning",
            "💡" to "ý tưởng bóng đèn idea bulb",
            "📌" to "ghim pin",
            "🔔" to "chuông nhắc bell reminder",
            "🔒" to "khoá lock",
            "🔑" to "chìa khoá key",
            "🎈" to "bóng bay balloon",
            "🏆" to "cúp chiến thắng trophy win",
            "👍" to "thích đồng ý like thumbs up",
            "👏" to "vỗ tay clap",
            "🙏" to "cảm ơn cầu nguyện thanks pray",
            "💪" to "mạnh mẽ cơ bắp strong muscle",
            "🫶" to "trái tim tay yêu thương heart hands love",
            "🌟" to "ngôi sao sáng glowing star",
        ),
    )

private val searchIndex: List<Pair<String, String>> by lazy {
    EmojiGroups.flatMap { group ->
        group.emojis.map { it.emoji to it.keywords.foldDiacritics().lowercase() }
    }
}

/**
 * Emojis whose keywords contain every word of [query], ignoring case and Vietnamese diacritics. A
 * blank query finds nothing.
 */
internal fun searchEmoji(query: String): List<String> {
    val words = query.foldDiacritics().lowercase().split(' ').filter { it.isNotBlank() }
    if (words.isEmpty()) return emptyList()
    return searchIndex
        .filter { (_, keywords) -> words.all { it in keywords } }
        .map { (emoji, _) -> emoji }
}
