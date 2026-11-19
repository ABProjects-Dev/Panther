package com.panther.app.core.designsystem.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Switch
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.panther.app.R
import com.panther.app.core.designsystem.theme.LocalPantherColors
import com.panther.app.core.designsystem.theme.PantherSpacing
import com.panther.app.core.format.PantherDateFormat
import com.panther.app.domain.model.ExerciseSet
import com.panther.app.domain.model.TargetType

@Composable
fun PantherBrandHeader(
    modifier: Modifier = Modifier,
    subtitle: String? = null,
    trailing: (@Composable () -> Unit)? = null,
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(MaterialTheme.colorScheme.surface)
                    .border(
                        width = 1.dp,
                        color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f),
                        shape = RoundedCornerShape(12.dp),
                    ),
                contentAlignment = Alignment.Center,
            ) {
                Image(
                    painter = painterResource(R.drawable.ic_panther_logo),
                    contentDescription = stringResource(R.string.app_name),
                    modifier = Modifier.size(28.dp),
                )
            }
            Spacer(Modifier.width(PantherSpacing.compact))
            Column {
                Text(
                    text = stringResource(R.string.app_name),
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground,
                )
                if (subtitle != null) {
                    Text(
                        text = subtitle,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
        }
        trailing?.invoke()
    }
}

@Composable
fun PantherActiveBadge(modifier: Modifier = Modifier) {
    val activeColors = LocalPantherColors.current
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(50),
        color = activeColors.activeGreen.copy(alpha = 0.16f),
        border = BorderStroke(1.dp, activeColors.activeGreen.copy(alpha = 0.6f)),
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            Box(
                modifier = Modifier
                    .size(7.dp)
                    .clip(CircleShape)
                    .background(activeColors.activeGreen),
            )
            Text(
                text = stringResource(R.string.status_active),
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.SemiBold,
                color = activeColors.activeGreen,
            )
        }
    }
}

/** 48dp keeps the row of squares compact while staying above the 44dp hit-target floor. */
val PantherSetSquareSize = 48.dp

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun ExerciseSetsSquares(
    sets: List<ExerciseSet>,
    modifier: Modifier = Modifier,
    highlightGreen: Boolean = false,
) {
    val activeColors = LocalPantherColors.current
    val accentBorder = if (highlightGreen) {
        activeColors.activeGreen.copy(alpha = 0.55f)
    } else {
        MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.55f)
    }
    val boxBackground = if (highlightGreen) {
        activeColors.activeGreen.copy(alpha = 0.10f)
    } else {
        MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.55f)
    }
    val valueColor = if (highlightGreen) {
        activeColors.activeGreen
    } else {
        MaterialTheme.colorScheme.onSurface
    }

    FlowRow(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        sets.sortedBy { it.setNumber }.forEach { set ->
            SetSquareBox(
                set = set,
                backgroundColor = boxBackground,
                borderColor = accentBorder,
                valueColor = valueColor,
            )
        }
    }
}

/**
 * One set, rendered as a small square with the set number in a badge at its top-start corner and an
 * `R` (repetitions) or `S` (duration, from the Persian word for time) marker under the value.
 */
@Composable
fun SetSquareBox(
    set: ExerciseSet,
    backgroundColor: Color,
    borderColor: Color,
    valueColor: Color,
    modifier: Modifier = Modifier,
    showTypeMarker: Boolean = true,
) {
    val shape = RoundedCornerShape(12.dp)
    val mainText = when (set.targetType) {
        TargetType.REPS -> PantherDateFormat.persianNumber(set.targetValue ?: 0)
        TargetType.DURATION -> {
            val min = (set.targetMinutes ?: 0).toString().padStart(2, '0')
            val sec = (set.targetSeconds ?: 0).toString().padStart(2, '0')
            PantherDateFormat.persianDigits("$min:$sec")
        }
    }
    val typeMarker = if (set.targetType == TargetType.REPS) {
        stringResource(R.string.set_type_reps_marker)
    } else {
        stringResource(R.string.set_type_duration_marker)
    }
    val numberLabel = stringResource(R.string.set_label, PantherDateFormat.persianNumber(set.setNumber))
    Box(modifier = modifier.size(PantherSetSquareSize)) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .clip(shape)
                .background(backgroundColor)
                .border(1.dp, borderColor, shape)
                .padding(horizontal = 4.dp, vertical = 4.dp)
                // Keep the badge inset clear: the value is centred in the remaining space.
                .padding(top = 12.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        ) {
            Text(
                text = mainText,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = valueColor,
                textAlign = TextAlign.Center,
                maxLines = 1,
            )
            if (showTypeMarker) {
                Text(
                    text = typeMarker,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                )
            }
        }
        Surface(
            modifier = Modifier
                .align(Alignment.TopStart)
                .padding(2.dp),
            shape = RoundedCornerShape(6.dp),
            color = borderColor,
            contentColor = MaterialTheme.colorScheme.surface,
        ) {
            Text(
                text = PantherDateFormat.persianNumber(set.setNumber),
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Bold,
                modifier = Modifier
                    .padding(horizontal = 4.dp, vertical = 1.dp)
                    .semantics { contentDescription = numberLabel },
                maxLines = 1,
            )
        }
    }
}

@Composable
fun PantherButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
) {
    Button(onClick = onClick, modifier = modifier, enabled = enabled) {
        Text(text, fontWeight = FontWeight.SemiBold)
    }
}

@Composable
fun PantherCard(
    modifier: Modifier = Modifier,
    containerColor: Color = MaterialTheme.colorScheme.surface,
    border: BorderStroke? = null,
    contentPadding: Dp = PantherSpacing.medium,
    content: @Composable ColumnScope.() -> Unit,
) {
    Card(
        modifier = modifier,
        shape = MaterialTheme.shapes.medium,
        colors = CardDefaults.cardColors(containerColor = containerColor),
        border = border,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(PaddingValues(contentPadding)),
            content = content,
        )
    }
}

@Composable
fun PantherTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    modifier: Modifier = Modifier,
    isError: Boolean = false,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        modifier = modifier,
        label = { Text(label) },
        isError = isError,
        singleLine = true,
        keyboardOptions = keyboardOptions,
        shape = MaterialTheme.shapes.small,
    )
}

@Composable
fun PantherSectionHeader(text: String, modifier: Modifier = Modifier) {
    Text(
        text = text,
        modifier = modifier,
        style = MaterialTheme.typography.titleLarge,
        fontWeight = FontWeight.Bold,
        color = MaterialTheme.colorScheme.onSurface,
    )
}

/**
 * Screen-level header for every nested page: the title (and optional subtitle) sits at the start and
 * the back action is an icon-only button at the **top end** corner — in this RTL app, the top right
 * of the screen. `contentDescription` keeps it discoverable for TalkBack ([UI-02]).
 */
@Composable
fun PantherTopBar(
    title: String,
    modifier: Modifier = Modifier,
    subtitle: String? = null,
    onBack: (() -> Unit)? = null,
    backContentDescription: String = stringResource(R.string.back),
    actions: (@Composable () -> Unit)? = null,
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            if (subtitle != null) {
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
        }
        Row(verticalAlignment = Alignment.CenterVertically) {
            actions?.invoke()
            if (onBack != null) {
                IconButton(
                    onClick = onBack,
                    modifier = Modifier.semantics { contentDescription = backContentDescription },
                ) {
                    // Mirrored arrow: in RTL this points to the start of the reading direction.
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                        contentDescription = null,
                    )
                }
            }
        }
    }
}

/**
 * Full-width destructive action for the end of a page. It only *opens* the confirmation dialog:
 * the actual delete stays behind the explicit confirm step, so the larger target cannot cause an
 * accidental removal ([UI-01]).
 */
@Composable
fun PantherDeleteButton(
    text: String,
    onConfirm: () -> Unit,
    modifier: Modifier = Modifier,
    confirmTitle: String = text,
    confirmMessage: String? = null,
    confirmLabel: String = stringResource(R.string.delete),
    enabled: Boolean = true,
) {
    var showConfirmation by rememberSaveable { mutableStateOf(false) }

    OutlinedButton(
        onClick = { showConfirmation = true },
        modifier = modifier.fillMaxWidth(),
        enabled = enabled,
        colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.error),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.error.copy(alpha = 0.6f)),
        contentPadding = PaddingValues(vertical = 14.dp),
    ) {
        Text(text = text, fontWeight = FontWeight.SemiBold)
    }

    if (showConfirmation) {
        AlertDialog(
            onDismissRequest = { showConfirmation = false },
            title = { Text(confirmTitle) },
            text = confirmMessage?.let { { Text(it) } },
            confirmButton = {
                TextButton(
                    onClick = {
                        showConfirmation = false
                        onConfirm()
                    },
                    colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.error),
                ) {
                    Text(confirmLabel)
                }
            },
            dismissButton = {
                TextButton(onClick = { showConfirmation = false }) {
                    Text(stringResource(R.string.cancel))
                }
            },
        )
    }
}

/**
 * Toggle that gates reordering. Dragging is off by default; the user switches "move mode" on to
 * rearrange and off again to lock the order, which keeps accidental drags out of the normal flow.
 */
@Composable
fun PantherReorderToggle(
    active: Boolean,
    onToggle: () -> Unit,
    modifier: Modifier = Modifier,
    label: String = stringResource(R.string.reorder_mode),
    description: String = stringResource(R.string.reorder_mode_desc),
    toggleContentDescription: String = stringResource(R.string.reorder_mode_toggle),
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.small,
        color = if (active) {
            LocalPantherColors.current.activeGreen.copy(alpha = 0.12f)
        } else {
            MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f)
        },
        border = BorderStroke(
            1.dp,
            if (active) {
                LocalPantherColors.current.activeGreen.copy(alpha = 0.6f)
            } else {
                MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
            },
        ),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable(onClick = onToggle)
                .padding(horizontal = PantherSpacing.compact, vertical = PantherSpacing.small),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = label,
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.SemiBold,
                )
                Text(
                    text = description,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            Switch(
                checked = active,
                onCheckedChange = { onToggle() },
                modifier = Modifier.semantics { contentDescription = toggleContentDescription },
            )
        }
    }
}
