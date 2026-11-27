package com.example.litutur.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.rotate

/**
 * Ilustrasi Pohon Kisah per tahap (1..4), digambar vektor dengan Canvas sehingga tidak butuh
 * file gambar / jaringan, tajam di semua ukuran layar, dan setiap tahap tampil berbeda:
 *   1 = tunas, 2 = bibit, 3 = pohon muda, 4 = pohon rimbun megah.
 *
 * Semua koordinat adalah pecahan 0..1 dari sisi kotak PERSEGI tempat ilustrasi digambar.
 * Titik gantung buah ([treeFruitAnchors]) memakai sistem koordinat yang sama, jadi stiker yang
 * ditaruh di titik itu selalu tepat berada di tajuk pohon, berapa pun ukuran layarnya.
 */

const val TREE_MAX_STAGE = 4

private val LeafLight = Color(0xFF7BC96F)
private val LeafMid = Color(0xFF4FAE57)
private val LeafDark = Color(0xFF2E8B4A)
private val TrunkColor = Color(0xFF8B5A2B)
private val GroundLight = Color(0xFF8BD17C)
private val GroundDark = Color(0xFF6DBB63)
private val SkyTop = Color(0xFFCFECFB)
private val SkyBottom = Color(0xFFF1F8E9)

private data class Limb(val from: Offset, val to: Offset, val width: Float)
private data class Blob(val center: Offset, val radius: Float, val color: Color)
private data class Leaf(val center: Offset, val width: Float, val height: Float, val degrees: Float)

private data class TreeArt(
    val trunk: List<Limb>,
    val branches: List<Limb> = emptyList(),
    val blobs: List<Blob> = emptyList(),
    val leaves: List<Leaf> = emptyList(),
    val fruitAnchors: List<Offset> = emptyList()
)

private val treeArt: Map<Int, TreeArt> = mapOf(
    1 to TreeArt(
        trunk = listOf(Limb(Offset(.5f, .93f), Offset(.5f, .66f), .022f)),
        leaves = listOf(
            Leaf(Offset(.43f, .645f), .15f, .085f, -28f),
            Leaf(Offset(.57f, .625f), .15f, .085f, 28f)
        )
        // Tahap 1 belum berbuah
    ),
    2 to TreeArt(
        trunk = listOf(Limb(Offset(.5f, .93f), Offset(.5f, .56f), .036f)),
        blobs = listOf(
            Blob(Offset(.5f, .43f), .15f, LeafMid),
            Blob(Offset(.38f, .50f), .115f, LeafLight),
            Blob(Offset(.62f, .50f), .115f, LeafLight)
        ),
        fruitAnchors = listOf(Offset(.43f, .45f), Offset(.58f, .46f))
    ),
    3 to TreeArt(
        trunk = listOf(Limb(Offset(.5f, .93f), Offset(.5f, .50f), .06f)),
        branches = listOf(
            Limb(Offset(.5f, .64f), Offset(.36f, .50f), .03f),
            Limb(Offset(.5f, .62f), Offset(.64f, .48f), .03f)
        ),
        blobs = listOf(
            Blob(Offset(.5f, .29f), .17f, LeafMid),
            Blob(Offset(.34f, .40f), .14f, LeafLight),
            Blob(Offset(.66f, .40f), .14f, LeafLight),
            Blob(Offset(.5f, .44f), .14f, LeafDark)
        ),
        fruitAnchors = listOf(
            Offset(.36f, .37f), Offset(.64f, .37f), Offset(.5f, .22f), Offset(.5f, .48f)
        )
    ),
    4 to TreeArt(
        trunk = listOf(Limb(Offset(.5f, .93f), Offset(.5f, .52f), .085f)),
        branches = listOf(
            Limb(Offset(.5f, .66f), Offset(.30f, .50f), .04f),
            Limb(Offset(.5f, .64f), Offset(.70f, .50f), .04f),
            Limb(Offset(.5f, .58f), Offset(.5f, .42f), .04f)
        ),
        blobs = listOf(
            Blob(Offset(.5f, .22f), .19f, LeafMid),
            Blob(Offset(.30f, .33f), .16f, LeafLight),
            Blob(Offset(.70f, .33f), .16f, LeafLight),
            Blob(Offset(.22f, .47f), .12f, LeafDark),
            Blob(Offset(.78f, .47f), .12f, LeafDark),
            Blob(Offset(.5f, .40f), .17f, LeafDark),
            Blob(Offset(.36f, .20f), .12f, LeafLight),
            Blob(Offset(.64f, .20f), .12f, LeafLight)
        ),
        fruitAnchors = listOf(
            Offset(.28f, .40f), Offset(.72f, .40f), Offset(.38f, .21f),
            Offset(.62f, .21f), Offset(.50f, .37f), Offset(.50f, .10f)
        )
    )
)

/** Diameter buah sebagai pecahan sisi ilustrasi. Jarak antar titik gantung selalu >= nilai ini. */
const val TREE_FRUIT_SIZE_FRACTION = 0.14f

/**
 * Titik gantung buah (pecahan 0..1) untuk sebuah tahap. Jumlahnya = banyaknya buah (slot stiker)
 * yang boleh tumbuh di tahap itu: tahap 1 = 0, tahap 2 = 2, tahap 3 = 4, tahap 4 = 6.
 */
fun treeFruitAnchors(stage: Int): List<Offset> =
    treeArt[stage.coerceIn(1, TREE_MAX_STAGE)]?.fruitAnchors.orEmpty()

/**
 * Gambar pohon untuk [stage]. Beri modifier berukuran PERSEGI (mis. `size(300.dp)` atau
 * `aspectRatio(1f)`). [withSky] = false bila latar langit sudah disediakan oleh pemanggil.
 */
@Composable
fun PohonIllustration(
    stage: Int,
    modifier: Modifier = Modifier,
    withSky: Boolean = true
) {
    val art = treeArt[stage.coerceIn(1, TREE_MAX_STAGE)] ?: return

    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height
        fun px(o: Offset) = Offset(o.x * w, o.y * h)

        if (withSky) {
            drawRect(brush = Brush.verticalGradient(listOf(SkyTop, SkyBottom)))
        }

        // Tanah
        drawOval(GroundLight, topLeft = Offset(-0.15f * w, 0.85f * h), size = Size(1.30f * w, 0.34f * h))
        drawOval(GroundDark, topLeft = Offset(0f, 0.90f * h), size = Size(1.00f * w, 0.22f * h))

        // Batang & cabang
        (art.trunk + art.branches).forEach { limb ->
            drawLine(
                color = TrunkColor,
                start = px(limb.from),
                end = px(limb.to),
                strokeWidth = limb.width * w,
                cap = StrokeCap.Round
            )
        }

        // Tajuk daun
        art.blobs.forEach { blob ->
            drawCircle(color = blob.color, radius = blob.radius * w, center = px(blob.center))
        }

        // Daun tunas (tahap 1)
        art.leaves.forEach { leaf ->
            val c = px(leaf.center)
            rotate(degrees = leaf.degrees, pivot = c) {
                drawOval(
                    color = LeafMid,
                    topLeft = Offset(c.x - leaf.width * w / 2f, c.y - leaf.height * h / 2f),
                    size = Size(leaf.width * w, leaf.height * h)
                )
            }
        }
    }
}
