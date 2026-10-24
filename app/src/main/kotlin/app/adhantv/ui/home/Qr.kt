package app.adhantv.ui.home

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import com.google.zxing.BarcodeFormat
import com.google.zxing.EncodeHintType
import com.google.zxing.qrcode.QRCodeWriter

/** A QR code for [text], drawn as squares (dark on a light quiet zone so phone cameras read it on a dark TV). */
@Composable
fun Qr(text: String, size: Dp) {
    val m = remember(text) { QRCodeWriter().encode(text, BarcodeFormat.QR_CODE, 0, 0, mapOf(EncodeHintType.MARGIN to 2)) }
    Canvas(Modifier.size(size)) {
        val cell = this.size.width / m.width
        drawRect(Color.White)
        for (y in 0 until m.height) for (x in 0 until m.width) {
            if (m[x, y]) drawRect(Color.Black, Offset(x * cell, y * cell), Size(cell + .5f, cell + .5f))
        }
    }
}
