package com.cameroonquest.app.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.windowInsetsBottomHeight
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.CompositingStrategy
import androidx.compose.ui.graphics.Outline
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import com.cameroonquest.app.ui.theme.Quest
import com.cameroonquest.app.ui.theme.darken
import com.cameroonquest.app.ui.theme.lighten
import kotlin.math.PI
import kotlin.math.absoluteValue
import kotlin.math.max
import kotlin.math.min
import kotlin.math.sin

/** Geometric band in kente / ndop colours, as seen at the bottom of every mockup screen. */
@Composable
fun KentePattern(modifier: Modifier = Modifier) {
    Canvas(modifier) {
        val h = size.height
        val unit = h * 1.25f
        val top = h * 0.2f
        val bottom = h * 0.8f
        drawRect(Quest.KenteBlack)
        drawRect(Quest.KenteGold, size = Size(size.width, h * 0.13f))
        drawRect(Quest.KenteGold, topLeft = Offset(0f, h * 0.87f), size = Size(size.width, h * 0.13f))
        drawRect(Quest.KenteRed, topLeft = Offset(0f, top), size = Size(size.width, bottom - top))
        val triangle = Path()
        val diamond = Path()
        var x = -unit / 2
        var index = 0
        while (x < size.width + unit) {
            triangle.reset()
            triangle.moveTo(x, bottom)
            triangle.lineTo(x + unit / 2, top)
            triangle.lineTo(x + unit, bottom)
            triangle.close()
            drawPath(triangle, if (index % 2 == 0) Quest.KenteGreen else Quest.KenteBlack)
            val cx = x + unit / 2
            val cy = bottom - (bottom - top) * 0.32f
            val d = h * 0.1f
            diamond.reset()
            diamond.moveTo(cx, cy - d)
            diamond.lineTo(cx + d * 0.8f, cy)
            diamond.lineTo(cx, cy + d)
            diamond.lineTo(cx - d * 0.8f, cy)
            diamond.close()
            drawPath(diamond, if (index % 2 == 0) Quest.KenteGold else Quest.KenteOrange)
            drawCircle(Quest.KenteGold, radius = h * 0.045f, center = Offset(x + unit, top + h * 0.1f))
            x += unit
            index++
        }
    }
}

/** Kente band that also fills the gesture/navigation bar area. */
@Composable
fun BottomKente(modifier: Modifier = Modifier, height: Dp = 20.dp) {
    Column(modifier.fillMaxWidth().background(Quest.KenteBlack)) {
        KentePattern(Modifier.fillMaxWidth().height(height))
        Spacer(Modifier.fillMaxWidth().windowInsetsBottomHeight(WindowInsets.navigationBars))
    }
}

@Composable
fun JungleBackground(modifier: Modifier = Modifier) {
    Canvas(modifier) {
        val w = size.width
        val h = size.height
        drawRect(Brush.verticalGradient(listOf(Color(0xFF13734A), Color(0xFF0C5A39), Color(0xFF083D27))))
        leaf(Offset(-w * 0.05f, h * 0.16f), w * 0.5f, -25f, Color(0x333DBE6A))
        leaf(Offset(w * 1.05f, h * 0.3f), w * 0.55f, 200f, Color(0x2E2FA85A))
        leaf(Offset(-w * 0.08f, h * 0.6f), w * 0.6f, 15f, Color(0x2A3DBE6A))
        leaf(Offset(w * 1.04f, h * 0.78f), w * 0.5f, 165f, Color(0x332FA85A))
        leaf(Offset(w * 0.45f, h * 1.03f), w * 0.42f, -75f, Color(0x263DBE6A))
        drawRect(Brush.radialGradient(listOf(Color.Transparent, Color(0x66000000)), radius = max(w, h) * 0.8f))
    }
}

private fun DrawScope.leaf(base: Offset, length: Float, angle: Float, color: Color) {
    rotate(angle, pivot = base) {
        val tip = Offset(base.x + length, base.y)
        val shape = Path().apply {
            moveTo(base.x, base.y)
            cubicTo(base.x + length * 0.25f, base.y - length * 0.32f, base.x + length * 0.75f, base.y - length * 0.28f, tip.x, tip.y)
            cubicTo(base.x + length * 0.75f, base.y + length * 0.28f, base.x + length * 0.25f, base.y + length * 0.32f, base.x, base.y)
            close()
        }
        drawPath(shape, color)
        val vein = color.copy(alpha = min(1f, color.alpha * 2.2f))
        drawLine(vein, base, tip, strokeWidth = length * 0.012f)
        for (k in 1..5) {
            val t = k / 6f
            val start = Offset(base.x + length * t, base.y)
            val spread = length * 0.2f * sin(PI.toFloat() * t)
            drawLine(vein, start, Offset(start.x + length * 0.09f, start.y - spread), strokeWidth = length * 0.007f)
            drawLine(vein, start, Offset(start.x + length * 0.09f, start.y + spread), strokeWidth = length * 0.007f)
        }
    }
}

@Composable
fun SkyClouds(modifier: Modifier = Modifier) {
    Canvas(modifier) {
        cloud(Offset(size.width * 0.18f, size.height * 0.82f), size.width * 0.06f)
        cloud(Offset(size.width * 0.66f, size.height * 0.3f), size.width * 0.045f)
        cloud(Offset(size.width * 0.93f, size.height * 0.86f), size.width * 0.055f)
    }
}

private fun DrawScope.cloud(center: Offset, r: Float) {
    val white = Color(0xFFF4FAFF)
    drawCircle(white, r, center)
    drawCircle(white, r * 0.75f, center + Offset(r * 0.95f, r * 0.2f))
    drawCircle(white, r * 0.7f, center + Offset(-r * 0.95f, r * 0.25f))
    drawRoundRect(white, Offset(center.x - r * 1.6f, center.y), Size(r * 3.2f, r * 0.85f), CornerRadius(r * 0.42f))
}

/** Basket-weave mat behind the Songo board. */
@Composable
fun WovenMat(modifier: Modifier = Modifier) {
    Canvas(modifier) {
        drawRect(Color(0xFFA9692F))
        val cell = 22.dp.toPx()
        val strip = cell / 3f
        val light = Color(0xFFE6AC60)
        val mid = Color(0xFFCF8E45)
        var row = 0
        var y = 0f
        while (y < size.height) {
            var col = 0
            var x = 0f
            while (x < size.width) {
                val horizontal = (row + col) % 2 == 0
                for (k in 0 until 3) {
                    val color = if (k == 1) light else mid
                    if (horizontal) {
                        drawRect(color, Offset(x + 1f, y + k * strip + 1f), Size(cell - 2f, strip - 2f))
                    } else {
                        drawRect(color, Offset(x + k * strip + 1f, y + 1f), Size(strip - 2f, cell - 2f))
                    }
                }
                x += cell
                col++
            }
            y += cell
            row++
        }
        drawRect(Brush.radialGradient(listOf(Color.Transparent, Color(0x77000000)), radius = max(size.width, size.height) * 0.8f))
    }
}

/** Layered waves in a character colour, echoing the waves under Njoh in the mockup. */
@Composable
fun WaveBand(color: Color, modifier: Modifier = Modifier) {
    Canvas(modifier) {
        val w = size.width
        val h = size.height
        for (layer in 0 until 3) {
            val baseY = h * (0.22f + layer * 0.24f)
            val amplitude = h * 0.07f
            val body = Path()
            val crest = Path()
            body.moveTo(0f, h)
            var x = 0f
            while (x <= w + 6f) {
                val y = baseY + amplitude * sin(x / w * 3f * PI.toFloat() + layer * 1.3f)
                if (x == 0f) {
                    body.lineTo(0f, y)
                    crest.moveTo(0f, y)
                } else {
                    body.lineTo(x, y)
                    crest.lineTo(x, y)
                }
                x += 6f
            }
            body.lineTo(w + 6f, h)
            body.close()
            drawPath(
                body,
                Brush.verticalGradient(
                    listOf(color.lighten(0.3f - layer * 0.1f), color.darken(0.2f + layer * 0.12f)),
                    startY = baseY - amplitude,
                    endY = h,
                ),
            )
            drawPath(crest, Color.White.copy(alpha = 0.55f), style = Stroke(width = 2.5.dp.toPx()))
        }
    }
}

/** Parchment card outline with slightly rough edges, like the character detail card. */
class TornPaperShape(private val corner: Dp = 22.dp, private val roughness: Dp = 3.dp) : Shape {
    override fun createOutline(size: Size, layoutDirection: LayoutDirection, density: Density): Outline {
        val c = with(density) { corner.toPx() }.coerceAtMost(min(size.width, size.height) / 2)
        val a = with(density) { roughness.toPx() }
        val w = size.width
        val h = size.height
        fun bump(t: Float, seed: Float) = a * (0.6f * sin(t * 0.09f + seed) + 0.4f * sin(t * 0.23f + seed * 1.7f)).absoluteValue
        val path = Path()
        path.moveTo(c, bump(c, 1f))
        var x = c
        while (x < w - c) {
            path.lineTo(x, bump(x, 1f))
            x += 5f
        }
        path.cubicTo(w - c * 0.45f, 0f, w, c * 0.45f, w, c)
        var y = c
        while (y < h - c) {
            path.lineTo(w - bump(y, 2f), y)
            y += 5f
        }
        path.cubicTo(w, h - c * 0.45f, w - c * 0.45f, h, w - c, h)
        x = w - c
        while (x > c) {
            path.lineTo(x, h - bump(x, 3f))
            x -= 5f
        }
        path.cubicTo(c * 0.45f, h, 0f, h - c * 0.45f, 0f, h - c)
        y = h - c
        while (y > c) {
            path.lineTo(bump(y, 4f), y)
            y -= 5f
        }
        path.cubicTo(0f, c * 0.45f, c * 0.45f, 0f, c, 0f)
        path.close()
        return Outline.Generic(path)
    }
}

/** Fades the content out towards the end and/or bottom edge so artwork blends into its background. */
fun Modifier.fadeEdges(toEnd: Boolean = false, toBottom: Boolean = false, from: Float = 0.72f): Modifier = this
    .graphicsLayer { compositingStrategy = CompositingStrategy.Offscreen }
    .drawWithContent {
        drawContent()
        if (toEnd) drawRect(Brush.horizontalGradient(from to Color.Black, 1f to Color.Transparent), blendMode = BlendMode.DstIn)
        if (toBottom) drawRect(Brush.verticalGradient(from to Color.Black, 1f to Color.Transparent), blendMode = BlendMode.DstIn)
    }
