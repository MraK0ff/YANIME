package su.afk.yummy.tv.feature.comments.tv.view

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.Flag
import androidx.compose.material.icons.filled.ThumbDown
import androidx.compose.material.icons.filled.ThumbUp
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import su.afk.yummy.tv.core.designsystem.theme.YummyShapeTokens
import su.afk.yummy.tv.core.designsystem.theme.YummySpacing
import coil3.compose.AsyncImage
import su.afk.yummy.tv.core.designsystem.R as CoreR
import su.afk.yummy.tv.core.designsystem.components.YummyLoadingIndicator
import su.afk.yummy.tv.core.designsystem.error.asString
import su.afk.yummy.tv.core.designsystem.theme.YummySemanticColors
import su.afk.yummy.tv.core.utils.formatting.formatCommentDateTime
import su.afk.yummy.tv.domain.comments.model.CommentVote
import su.afk.yummy.tv.feature.comments.CommentsState
import su.afk.yummy.tv.feature.comments.presentation.R

@Composable
internal fun CommentCard(
    item: CommentsState.CommentUi,
    currentUserId: Int,
    isModerator: Boolean,
    isMutating: Boolean,
    depth: Int,
    onDelete: (Int, FocusRequester) -> Unit,
    onReport: (Int, FocusRequester) -> Unit,
    onVote: (Int, CommentVote) -> Unit,
    onToggleChildren: (Int) -> Unit,
    onLoadMoreChildren: (Int) -> Unit,
    modifier: Modifier = Modifier,
) {
    val comment = item.comment
    val canEdit = currentUserId == comment.author.id || isModerator
    val likeFocusRequester = androidx.compose.runtime.remember { FocusRequester() }
    val deleteFocusRequester = androidx.compose.runtime.remember { FocusRequester() }
    val reportFocusRequester = androidx.compose.runtime.remember { FocusRequester() }
    Surface(
        modifier = modifier.fillMaxWidth(),
        color = MaterialTheme.colorScheme.surfaceContainerLow,
        shape = YummyShapeTokens.Card,
        tonalElevation = 0.dp,
    ) {
        Column(
            modifier = Modifier.padding(horizontal = YummySpacing.XLarge, vertical = YummySpacing.Medium + 2.dp),
            verticalArrangement = Arrangement.spacedBy(YummySpacing.Medium - 2.dp),
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                AsyncImage(
                    model = comment.author.avatarSmallUrl,
                    contentDescription = null,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .size(YummySpacing.TouchTarget - YummySpacing.Medium + 2.dp)
                        .background(MaterialTheme.colorScheme.surfaceVariant, CircleShape),
                )
                Spacer(Modifier.width(YummySpacing.Medium - 2.dp))
                Text(
                    text = comment.author.name.ifBlank {
                        stringResource(R.string.comments_unknown_author)
                    },
                    style = MaterialTheme.typography.titleSmallEmphasized,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f),
                )
                Text(
                    text = comment.createdAtEpochSeconds.formatCommentDateTime(),
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }

            if (comment.deletedAtEpochSeconds != null) {
                Text(
                    text = stringResource(R.string.comments_deleted),
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    style = MaterialTheme.typography.bodyLarge,
                )
            } else {
                CommentBodyText(comment.text, nextFocusRequester = likeFocusRequester)
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(YummySpacing.Small),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                if (comment.childrenCount > 0 || item.children.isNotEmpty()) {
                    CommentActionButton(
                        label = if (item.childrenVisible) {
                            stringResource(R.string.comments_hide_replies)
                        } else {
                            stringResource(R.string.comments_show_replies, comment.childrenCount)
                        },
                        icon = if (item.childrenVisible) {
                            Icons.Filled.ExpandLess
                        } else {
                            Icons.Filled.ExpandMore
                        },
                        enabled = !item.childrenLoading,
                        onClick = { onToggleChildren(comment.id) },
                    )
                }
                Spacer(Modifier.weight(1f))
                if (comment.deletedAtEpochSeconds == null) {
                    CommentActionButton(
                        label = comment.likes.toString(),
                        icon = Icons.Filled.ThumbUp,
                        selected = comment.vote == CommentVote.LIKE,
                        accentColor = YummySemanticColors.Like,
                        enabled = !isMutating,
                        modifier = Modifier.focusRequester(likeFocusRequester),
                        onClick = { onVote(comment.id, CommentVote.LIKE) },
                    )
                    CommentActionButton(
                        label = comment.dislikes.toString(),
                        icon = Icons.Filled.ThumbDown,
                        selected = comment.vote == CommentVote.DISLIKE,
                        accentColor = YummySemanticColors.Dislike,
                        enabled = !isMutating,
                        onClick = { onVote(comment.id, CommentVote.DISLIKE) },
                    )
                    if (canEdit) {
                        CommentActionButton(
                            label = stringResource(CoreR.string.common_delete),
                            icon = Icons.Filled.Delete,
                            enabled = !isMutating,
                            modifier = Modifier.focusRequester(deleteFocusRequester),
                            onClick = { onDelete(comment.id, deleteFocusRequester) },
                        )
                    }
                    CommentActionButton(
                        label = stringResource(R.string.comments_report),
                        icon = Icons.Filled.Flag,
                        showLabel = false,
                        enabled = !isMutating,
                        modifier = Modifier.focusRequester(reportFocusRequester),
                        onClick = { onReport(comment.id, reportFocusRequester) },
                    )
                }
            }

            if (item.childrenVisible) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = if (depth == 0) YummySpacing.XXLarge else YummySpacing.Medium),
                    verticalArrangement = Arrangement.spacedBy(YummySpacing.Medium - 2.dp),
                ) {
                    item.children.forEach { child ->
                        CommentCard(
                            item = child,
                            currentUserId = currentUserId,
                            isModerator = isModerator,
                            isMutating = isMutating,
                            depth = (depth + 1).coerceAtMost(2),
                            onDelete = onDelete,
                            onReport = onReport,
                            onVote = onVote,
                            onToggleChildren = onToggleChildren,
                            onLoadMoreChildren = onLoadMoreChildren,
                        )
                    }
                    if (item.childrenLoading) {
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(10.dp),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            YummyLoadingIndicator(modifier = Modifier.size(YummySpacing.Large + YummySpacing.XSmall), strokeWidth = 2.dp)
                            Text(
                                text = stringResource(CoreR.string.common_loading),
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                    }
                    item.childrenError?.let { error ->
                        Text(
                            text = error.asString(),
                            color = MaterialTheme.colorScheme.error,
                            style = MaterialTheme.typography.bodyMedium,
                        )
                    }
                    if (item.childrenHasMore && !item.childrenLoading) {
                        Box {
                            CommentActionButton(
                                label = stringResource(R.string.comments_load_more_replies),
                                icon = Icons.Filled.ExpandMore,
                                onClick = { onLoadMoreChildren(comment.id) },
                            )
                        }
                    }
                }
            }
        }
    }
}
