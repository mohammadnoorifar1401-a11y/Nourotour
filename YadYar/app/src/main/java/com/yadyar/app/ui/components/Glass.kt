package com.yadyar.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.yadyar.app.data.model.MemberType
import com.yadyar.app.ui.theme.BgDeep
import com.yadyar.app.ui.theme.BgMid
import com.yadyar.app.ui.theme.BgSoft
import com.yadyar.app.ui.theme.GlassBorder
import com.yadyar.app.ui.theme.GlassBorderSoft
import com.yadyar.app.ui.theme.GoldAccent
import com.yadyar.app.ui.theme.TextPrimary
import com.yadyar.app.ui.theme.TextSecondary

/** پس‌زمینه سراسری گرادیانی + هاله‌های رنگی محو (گلس‌مورفیسم) */
@Composable
fun GradientBackground(content: @Composable BoxScope.() -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Brush.verticalGradient(listOf(BgDeep, BgMid, BgSoft)))
    ) {
        Box(
            modifier = Modifier
                .align(Alignment.TopStart)
                .offset(x = (-70).dp, y = (-50).dp)
                .size(280.dp)
                .blur(90.dp)
                .background(
                    Brush.radialGradient(
                        listOf(Color(0xFF3B7DDD).copy(alpha = 0.50f), Color.Transparent)
                    ),
                    CircleShape
                )
        )
        Box(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .offset(x = 70.dp, y = 90.dp)
                .size(330.dp)
                .blur(100.dp)
                .background(
                    Brush.radialGradient(
                        listOf(Color(0xFFD8B552).copy(alpha = 0.26f), Color.Transparent)
                    ),
                    CircleShape
                )
        )
        Box(
            modifier = Modifier
                .align(Alignment.Center)
                .offset(x = 110.dp, y = (-140).dp)
                .size(230.dp)
                .blur(95.dp)
                .background(
                    Brush.radialGradient(
                        listOf(Color(0xFF9B51E0).copy(alpha = 0.28f), Color.Transparent)
                    ),
                    CircleShape
                )
        )
        content()
    }
}

/** کارت نیمه‌شفاف لبه‌دار — المان اصلی گلس‌مورفیسم */
@Composable
fun GlassCard(
    modifier: Modifier = Modifier,
    cornerRadius: Dp = 22.dp,
    contentPadding: PaddingValues = PaddingValues(16.dp),
    fillSoft: Boolean = false,
    onClick: (() -> Unit)? = null,
    content: @Composable ColumnScope.() -> Unit
) {
    val shape = RoundedCornerShape(cornerRadius)
    val base = Modifier
        .shadow(elevation = 12.dp, shape = shape, spotColor = Color(0x55000000))
        .clip(shape)
        .background(
            Brush.linearGradient(
                if (fillSoft) {
                    listOf(Color.White.copy(alpha = 0.11f), Color.White.copy(alpha = 0.05f))
                } else {
                    listOf(Color.White.copy(alpha = 0.17f), Color.White.copy(alpha = 0.07f))
                }
            )
        )
        .border(
            width = 1.dp,
            brush = Brush.linearGradient(
                listOf(
                    if (fillSoft) GlassBorderSoft else GlassBorder,
                    Color.White.copy(alpha = 0.10f)
                )
            ),
            shape = shape
        )
    val interactive = if (onClick != null) base.clickable(onClick = onClick) else base
    Column(
        modifier = modifier.then(interactive).padding(contentPadding),
        content = content
    )
}

/** عنوان بخش با خط تزئینی */
@Composable
fun SectionTitle(text: String, modifier: Modifier = Modifier) {
    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.titleMedium,
            color = TextPrimary,
            fontWeight = FontWeight.Bold
        )
        Spacer(Modifier.width(12.dp))
        Box(
            modifier = Modifier
                .weight(1f)
                .height(1.dp)
                .background(
                    Brush.horizontalGradient(
                        listOf(
                            GoldAccent.copy(alpha = 0.75f),
                            Color.Transparent
                        )
                    )
                )
        )
    }
}

/** نشان نوع عضو: مشتری / پرسنل */
@Composable
fun TypeBadge(type: MemberType) {
    val color = when (type) {
        MemberType.CUSTOMER -> Color(0xFF5B8DEF)
        MemberType.STAFF -> GoldAccent
    }
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(50))
            .background(color.copy(alpha = 0.22f))
            .border(1.dp, color.copy(alpha = 0.55f), RoundedCornerShape(50))
            .padding(horizontal = 10.dp, vertical = 3.dp)
    ) {
        Text(
            text = type.fa,
            style = MaterialTheme.typography.labelLarge,
            color = color.copy(alpha = 0.95f),
            fontWeight = FontWeight.Bold
        )
    }
}

/** وضعیت خالی */
@Composable
fun EmptyGlass(message: String) {
    GlassCard(modifier = Modifier.fillMaxWidth(), fillSoft = true) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 12.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(message, color = TextSecondary, style = MaterialTheme.typography.bodyMedium)
        }
    }
}
