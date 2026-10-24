package app.adhantv.ui.home

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.tv.material3.ListItem
import androidx.tv.material3.Text
import app.adhantv.data.Cities
import app.adhantv.data.City
import app.adhantv.ui.theme.Fonts
import app.adhantv.ui.theme.Palette
import java.util.Locale

/** Search box plus results; empty search lists the current country's largest cities. */
@Composable
fun CityPicker(cities: Cities, country: String, onPick: (City) -> Unit, onBack: () -> Unit) {
    BackHandler(onBack = onBack)
    var query by remember { mutableStateOf("") }
    val results = remember(query) { cities.search(query, country) }
    Column(Modifier.fillMaxSize().padding(horizontal = 58.dp, vertical = 36.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text("اختر المدينة", style = TextStyle(fontFamily = Fonts.amiri, fontSize = 30.sp, color = Palette.gold))
        BasicTextField(
            value = query,
            onValueChange = { query = it },
            singleLine = true,
            textStyle = TextStyle(fontFamily = Fonts.plex, fontSize = 22.sp, color = Palette.ink),
            cursorBrush = SolidColor(Palette.gold),
            modifier = Modifier.width(560.dp).background(Palette.bgCentre, RoundedCornerShape(10.dp)).padding(14.dp),
            decorationBox = { inner ->
                if (query.isEmpty()) Text("اكتب اسم المدينة…", style = TextStyle(fontFamily = Fonts.plex, fontSize = 22.sp, color = Palette.mute))
                inner()
            },
        )
        LazyColumn(Modifier.fillMaxWidth()) {
            items(results, key = { it.id }) { c ->
                ListItem(
                    selected = false,
                    onClick = { onPick(c) },
                    headlineContent = { Text(c.name, style = TextStyle(fontFamily = Fonts.plex, fontSize = 20.sp)) },
                    supportingContent = {
                        Text("${c.english} · ${Locale("", c.country).getDisplayCountry(Locale("ar"))}", style = TextStyle(fontFamily = Fonts.plex, fontSize = 14.sp))
                    },
                )
            }
        }
    }
}
