package app.adhantv.ui.home

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.tv.material3.ListItem
import androidx.tv.material3.Switch
import androidx.tv.material3.Text
import app.adhantv.ui.theme.Fonts

private val titleStyle = TextStyle(fontFamily = Fonts.plex, fontSize = 20.sp)
private val noteStyle = TextStyle(fontFamily = Fonts.plex, fontSize = 14.sp)

/** On/off setting: OK toggles it. */
@Composable
fun ToggleRow(title: String, checked: Boolean, note: String? = null, onToggle: (Boolean) -> Unit) {
    ListItem(
        selected = false,
        onClick = { onToggle(!checked) },
        headlineContent = { Text(title, style = titleStyle) },
        supportingContent = note?.let { { Text(it, style = noteStyle) } },
        trailingContent = { Switch(checked = checked, onCheckedChange = null) },
    )
}

/** A value from a list: OK moves to the next one (left/right stay free for moving between the panes). */
@Composable
fun <T> ChoiceRow(
    title: String,
    value: T,
    options: List<T>,
    label: (T) -> String,
    note: String? = null,
    swatch: ((T) -> Color)? = null,
    onChange: (T) -> Unit,
) {
    val i = options.indexOf(value).coerceAtLeast(0)
    ListItem(
        selected = false,
        onClick = { onChange(options[(i + 1) % options.size]) },
        headlineContent = { Text(title, style = titleStyle) },
        supportingContent = note?.let { { Text(it, style = noteStyle) } },
        trailingContent = {
            Row {
                swatch?.let { c -> Text("●  ", style = TextStyle(fontSize = 20.sp, color = c(value))) }
                Text("‹ ${label(value)} ›", style = titleStyle, modifier = Modifier.width(220.dp))
            }
        },
    )
}

/** A plain action. */
@Composable
fun ActionRow(title: String, note: String? = null, onClick: () -> Unit) {
    ListItem(
        selected = false,
        onClick = onClick,
        headlineContent = { Text(title, style = titleStyle) },
        supportingContent = note?.let { { Text(it, style = noteStyle) } },
    )
}
