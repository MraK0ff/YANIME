package su.afk.yummy.tv.feature.account.mobile.account.utils

import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.LinkAnnotation
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextLinkStyles
import androidx.compose.ui.text.buildAnnotatedString
import su.afk.yummy.tv.domain.account.model.NotificationLink

/**
 * Removes an anime subject repeated in the body when the title already names that subject.
 * Only strips a leading, well-known notification phrase; the actual episode/comment details remain.
 */
internal fun notificationBodyWithoutRepeatedTitle(title: String, body: String): String {
    val subject = Regex("[«“\u0022'](.+?)[»”\u0022']").find(title)
        ?.groupValues
        ?.getOrNull(1)
        ?.trim()
        ?.takeIf(String::isNotEmpty)
        ?: return body

    val subjectPattern = Regex.escape(subject)
    val prefix = Regex(
        pattern = "(?i)^\\s*(?:в\\s+аниме|в\\s+аніме|in\\s+(?:the\\s+)?anime)\\s+$subjectPattern(?:\\s*[,.:!?—–-]\\s*|\\s+)",
    )
    return prefix.replaceFirst(body, "").trim().ifBlank { body }
}

/**
 * Текст уведомления, где названия аниме из [links] — кликабельные ссылки. Название ищется в тексте
 * последовательно, поэтому ссылка, которой в тексте нет (например, только в заголовке), пропускается.
 */
internal fun buildNotificationText(
    text: String,
    links: List<NotificationLink>,
    linkStyle: SpanStyle,
    onLinkClick: (slug: String) -> Unit,
): AnnotatedString = buildAnnotatedString {
    append(text)
    var cursor = 0
    links.forEach { link ->
        val start = text.indexOf(link.label, startIndex = cursor)
        if (start < 0) return@forEach
        val end = start + link.label.length
        addLink(
            LinkAnnotation.Clickable(
                tag = link.slug,
                styles = TextLinkStyles(style = linkStyle),
                linkInteractionListener = { onLinkClick(link.slug) },
            ),
            start = start,
            end = end,
        )
        cursor = end
    }
}
