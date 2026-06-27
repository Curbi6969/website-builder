package com.rise.app.ui.plant

import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.withFrameNanos
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.unit.dp
import com.rise.app.ui.theme.BerryA
import com.rise.app.ui.theme.BerryB
import com.rise.app.ui.theme.GreenDark
import com.rise.app.ui.theme.PotA
import com.rise.app.ui.theme.PotB
import com.rise.app.ui.theme.PotRim
import com.rise.app.ui.theme.Stem
import com.rise.app.ui.theme.StemTip
import com.rise.app.ui.theme.YellowB
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin
import kotlin.random.Random

/**
 * Animated L-system plant, a faithful port of design-reference/lsys-plant.js.
 * Grows generation by generation (lerping the freshly-sprouted growth in), then
 * idles with a gentle wind sway.
 */
private class PlantState(seed: Int, val maxGeneration: Int) {
    private val seed0: Int = seed
    private var s: Int = seed
    var word: String = "X"
    private var generation: Int = 0
    var growth: Float = 0f
    private val growthRate = 0.18f
    var finished = false

    val ang: Float = (24.0 * PI / 180.0).toFloat()

    /** Bounding box of the fully-grown plant (unit segments), used to fit it in the canvas. */
    val bounds: PlantBounds by lazy { computePlantBounds(seed0, maxGeneration, ang) }

    /** xorshift32, deterministic per seed. */
    private fun rand(): Float {
        var x = s
        x = x xor (x shl 13)
        x = x xor (x ushr 17)
        x = x xor (x shl 5)
        s = x
        return (s.toLong() and 0xFFFFFFFFL).toFloat() / 4294967296f
    }

    fun step() {
        if (finished) return
        if (growth < 1f) {
            val mod = generation + growth
            growth += growthRate / (if (mod == 0f) 1f else mod)
            if (growth > 1f) growth = 1f
        } else {
            nextGeneration()
        }
    }

    private fun nextGeneration() {
        if (generation >= maxGeneration) { finished = true; return }
        word = generate(word)
        generation++
        growth = 0f
    }

    private fun generate(w: String): String {
        val sb = StringBuilder(w.length * 2)
        for (c in w) {
            when (c) {
                'X' -> sb.append(chooseX())
                'F' -> sb.append(chooseF())
                '(', ')' -> {} // parens strip on the next generation
                else -> sb.append(c)
            }
        }
        return sb.toString()
    }

    private fun chooseX(): String {
        val n = rand()
        var t = 0f
        t += 0.5f; if (t > n) return "(F[+X][-X]FX)"
        t += 0.05f; if (t > n) return "(F[-X]FX)"
        t += 0.05f; if (t > n) return "(F[+X]FX)"
        t += 0.1f; if (t > n) return "(F[++X][-X]FX)"
        t += 0.1f; if (t > n) return "(F[+X][--X]FX)"
        t += 0.1f; if (t > n) return "(F[+X][-X]FXA)"
        return "(F[+X][-X]FXB)"
    }

    private fun chooseF(): String {
        val n = rand()
        var t = 0f
        t += 0.85f; if (t > n) return "F(F)"
        t += 0.05f; if (t > n) return "F(FF)"
        return "F"
    }

    fun draw(ds: DrawScope) {
        val b = bounds
        val pad = 0.86f
        val bw = (b.maxX - b.minX).coerceAtLeast(1f)
        val bh = b.height.coerceAtLeast(1f)
        // Fit the whole plant inside the canvas (segment length is small; stroke &
        // berry sizes are derived from the canvas so twigs stay visible at any fit).
        val seg = minOf(ds.size.width * pad / bw, ds.size.height * pad / bh)
        val centerX = (b.minX + b.maxX) / 2f
        val baseX = ds.size.width / 2f - centerX * seg
        val baseY = ds.size.height * 0.99f
        val h = ds.size.height
        val t = growth.coerceIn(0f, 1f)

        var x = baseX
        var y = baseY
        var a = 0f
        val stack = ArrayDeque<Triple<Float, Float, Float>>()
        var lerpOn = 0
        val total = word.length.coerceAtLeast(1)

        for (i in word.indices) {
            when (word[i]) {
                '(' -> lerpOn++
                ')' -> lerpOn = maxOf(0, lerpOn - 1)
                'F' -> {
                    val lt = if (lerpOn > 0) t else 1f
                    val len = seg * lt
                    val age = 1f - i.toFloat() / total
                    val wgt = (0.006f + age * 0.013f) * h
                    val nx = x + (len * sin(a.toDouble())).toFloat()
                    val ny = y - (len * cos(a.toDouble())).toFloat()
                    ds.drawLine(
                        color = if (lerpOn > 0) StemTip else Stem,
                        start = Offset(x, y),
                        end = Offset(nx, ny),
                        strokeWidth = wgt,
                        cap = StrokeCap.Round,
                    )
                    x = nx; y = ny
                }
                '+' -> { val lt = if (lerpOn > 0) t else 1f; a -= ang * lt }
                '-' -> { val lt = if (lerpOn > 0) t else 1f; a += ang * lt }
                '[' -> stack.addLast(Triple(x, y, a))
                ']' -> stack.removeLastOrNull()?.let { x = it.first; y = it.second; a = it.third }
                'A' -> { val lt = if (lerpOn > 0) t else 1f; ds.drawCircle(BerryA, h * 0.018f * lt, Offset(x, y)) }
                'B' -> { val lt = if (lerpOn > 0) t else 1f; ds.drawCircle(BerryB, h * 0.018f * lt, Offset(x, y)) }
            }
        }
    }
}

/** Bounding box (unit-segment coordinates) of a fully-grown plant. */
private class PlantBounds(val minX: Float, val maxX: Float, val height: Float)

/** Re-run the L-system deterministically to size the final plant before drawing. */
private fun computePlantBounds(seed: Int, gens: Int, ang: Float): PlantBounds {
    var s = seed
    fun rand(): Float {
        var x = s
        x = x xor (x shl 13); x = x xor (x ushr 17); x = x xor (x shl 5); s = x
        return (s.toLong() and 0xFFFFFFFFL).toFloat() / 4294967296f
    }
    fun chooseX(): String {
        val n = rand(); var t = 0f
        t += 0.5f; if (t > n) return "(F[+X][-X]FX)"
        t += 0.05f; if (t > n) return "(F[-X]FX)"
        t += 0.05f; if (t > n) return "(F[+X]FX)"
        t += 0.1f; if (t > n) return "(F[++X][-X]FX)"
        t += 0.1f; if (t > n) return "(F[+X][--X]FX)"
        t += 0.1f; if (t > n) return "(F[+X][-X]FXA)"
        return "(F[+X][-X]FXB)"
    }
    fun chooseF(): String {
        val n = rand(); var t = 0f
        t += 0.85f; if (t > n) return "F(F)"
        t += 0.05f; if (t > n) return "F(FF)"
        return "F"
    }
    var w = "X"
    repeat(gens) {
        val sb = StringBuilder(w.length * 2)
        for (c in w) when (c) {
            'X' -> sb.append(chooseX())
            'F' -> sb.append(chooseF())
            '(', ')' -> {}
            else -> sb.append(c)
        }
        w = sb.toString()
    }
    var x = 0f; var y = 0f; var a = 0f
    var minX = 0f; var maxX = 0f; var minY = 0f; var maxY = 0f
    val stack = ArrayDeque<Triple<Float, Float, Float>>()
    for (c in w) when (c) {
        'F' -> {
            x += sin(a.toDouble()).toFloat(); y -= cos(a.toDouble()).toFloat()
            if (x < minX) minX = x; if (x > maxX) maxX = x
            if (y < minY) minY = y; if (y > maxY) maxY = y
        }
        '+' -> a -= ang
        '-' -> a += ang
        '[' -> stack.addLast(Triple(x, y, a))
        ']' -> stack.removeLastOrNull()?.let { x = it.first; y = it.second; a = it.third }
    }
    return PlantBounds(minX, maxX, maxY - minY)
}

@Composable
fun PlantCanvas(generations: Int, modifier: Modifier = Modifier, seed: Int = remember { Random.nextInt() }) {
    val plant = remember(generations, seed) { PlantState(seed, generations) }
    // `frame` only ticks while the plant is still growing; once finished the Canvas
    // stops redrawing entirely (the expensive per-frame turtle walk no longer runs).
    var frame by remember(plant) { mutableStateOf(0) }

    LaunchedEffect(plant) {
        while (!plant.finished) {
            withFrameNanos { plant.step() }
            frame++
        }
    }

    // Idle sway is a cheap GPU layer rotation (pivot at the pot), not a geometry redraw.
    val swayTransition = rememberInfiniteTransition(label = "sway")
    val sway by swayTransition.animateFloat(
        initialValue = -1f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(2600), RepeatMode.Reverse),
        label = "swayAngle",
    )

    Canvas(
        modifier.graphicsLayer {
            transformOrigin = TransformOrigin(0.5f, 1f)
            rotationZ = if (plant.finished) sway * 1.6f else 0f
        },
    ) {
        frame // read so the Canvas redraws each growth step
        plant.draw(this)
    }
}

/** Plant + terracotta pot, matching the home hero. */
@Composable
fun PlantWithPot(generations: Int, modifier: Modifier = Modifier) {
    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = modifier) {
        PlantCanvas(generations, Modifier.width(220.dp).height(168.dp).clipToBounds(), seed = 7)
        Spacer(
            Modifier
                .offset(y = (-6).dp)
                .width(96.dp)
                .height(14.dp)
                .background(PotRim, RoundedCornerShape(8.dp)),
        )
        Spacer(
            Modifier
                .width(78.dp)
                .height(42.dp)
                .background(
                    Brush.verticalGradient(listOf(PotA, PotB)),
                    RoundedCornerShape(bottomStart = 18.dp, bottomEnd = 18.dp),
                ),
        )
    }
}
