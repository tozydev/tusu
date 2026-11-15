package vn.id.tozydev.tusu.ui.component

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import vn.id.tozydev.tusu.domain.error.fold
import vn.id.tozydev.tusu.text.richTextToAnnotatedString
import vn.id.tozydev.tusu.ui.theme.AppTheme

/**
 * Read-only entry text with its formatting. When the body cannot be read, [fallbackText] (the
 * entry's plain text) is shown instead.
 */
@Composable
fun RichTextContent(
    body: String,
    fallbackText: String,
    modifier: Modifier = Modifier,
    maxLines: Int = Int.MAX_VALUE,
    style: TextStyle = MaterialTheme.typography.bodyLarge,
) {
    val text =
        remember(body, fallbackText) {
            richTextToAnnotatedString(body)
                .fold(
                    onSuccess = { it },
                    onFailure = { AnnotatedString(fallbackText) },
                )
        }
    Text(
        text = text,
        modifier = modifier,
        style = style,
        maxLines = maxLines,
        overflow = TextOverflow.Ellipsis,
    )
}

@Preview
@Composable
private fun RichTextContentPreview() {
    AppTheme { RichTextContent(body = "", fallbackText = "Hôm nay trời đẹp, đi bộ quanh hồ.") }
}

@Preview
@Composable
private fun RichTextContentTruncatedPreview() {
    val longText =
        "Một đoạn ghi chép dài để xem cách hiển thị khi nội dung vượt quá số dòng cho phép. " +
            "Phần còn lại được rút gọn bằng dấu ba chấm ở cuối dòng cuối cùng."
    AppTheme { RichTextContent(body = "", fallbackText = longText, maxLines = 2) }
}
