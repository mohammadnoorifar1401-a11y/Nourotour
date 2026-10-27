package com.yadyar.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import android.graphics.BitmapFactory
import android.net.Uri
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.yadyar.app.ui.theme.BlueAccent

fun colorFromHex(hex: String): Color =
    try {
        Color(android.graphics.Color.parseColor(hex))
    } catch (e: Exception) {
        BlueAccent
    }

@Composable
fun MemberAvatar(
    letter: String,
    colorHex: String,
    size: Dp = 52.dp,
    fontSize: TextUnit = 20.sp,
    imageUri: String? = null
) {
    val context = LocalContext.current
    val bitmap = remember(imageUri) {
        runCatching {
            imageUri?.let { context.contentResolver.openInputStream(Uri.parse(it))?.use(BitmapFactory::decodeStream) }
        }.getOrNull()
    }
    Box(
        modifier = Modifier
            .size(size)
            .clip(CircleShape)
            .background(colorFromHex(colorHex).copy(alpha = 0.88f))
            .border(1.5.dp, Color.White.copy(alpha = 0.35f), CircleShape),
        contentAlignment = Alignment.Center
    ) {
        if (bitmap != null) {
            Image(bitmap = bitmap.asImageBitmap(), contentDescription = "تصویر پروفایل", modifier = Modifier.size(size), contentScale = ContentScale.Crop)
        } else Text(
            text = letter.ifBlank { "؟" },
            color = Color.White,
            fontWeight = FontWeight.ExtraBold,
            fontSize = fontSize
        )
    }
}
