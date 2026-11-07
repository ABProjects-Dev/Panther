package com.panther.app.core.designsystem.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
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

@Composable
private fun SetSquareBox(
    set: ExerciseSet,
    backgroundColor: Color,
    borderColor: Color,
    valueColor: Color,
) {
    val shape = RoundedCornerShape(10.dp)
    val mainText = when (set.targetType) {
        TargetType.REPS -> PantherDateFormat.persianNumber(set.targetValue ?: 0)
        TargetType.DURATION -> {
            val min = (set.targetMinutes ?: 0).toString().padStart(2, '0')
            val sec = (set.targetSeconds ?: 0).toString().padStart(2, '0')
            PantherDateFormat.persianDigits("$min:$sec")
        }
    }
    Column(
        modifier = Modifier
            .size(64.dp)
            .clip(shape)
            .background(backgroundColor)
            .border(1.dp, borderColor, shape)
            .padding(4.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceBetween,
    ) {
        Text(
            text = stringResource(R.string.set_label, PantherDateFormat.persianNumber(set.setNumber)),
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            maxLines = 1,
        )
        Text(
            text = mainText,
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Bold,
            color = valueColor,
            textAlign = TextAlign.Center,
            maxLines = 1,
        )
        Text(
            text = if (set.targetType == TargetType.REPS) stringResource(R.string.reps_unit_short) else stringResource(R.string.duration),
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            maxLines = 1,
        )
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
