package org.fairscan.app.ui.redaction

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Path as AndroidPath
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.unit.IntSize
import java.io.ByteArrayOutputStream

/**
 * Merges the redaction drawing strokes onto the original bitmap.
 * 
 * @param originalBitmap The original document bitmap
 * @param state The redaction state containing the drawing strokes
 * @param canvasSize The size of the canvas where the strokes were drawn
 * @return A new bitmap with the redactions applied, as JPEG bytes
 */
fun mergeRedactionWithBitmap(
    originalBitmap: Bitmap,
    state: RedactionState,
    canvasSize: IntSize
): ByteArray {
    // Create a mutable copy of the bitmap
    val resultBitmap = originalBitmap.copy(Bitmap.Config.ARGB_8888, true)
    val canvas = Canvas(resultBitmap)
    
    // Calculate scale factors between canvas coordinates and bitmap coordinates
    val scaleX = resultBitmap.width.toFloat() / canvasSize.width.toFloat()
    val scaleY = resultBitmap.height.toFloat() / canvasSize.height.toFloat()
    
    // Draw each stroke onto the bitmap
    state.strokes.forEach { stroke ->
        val paint = Paint().apply {
            color = stroke.color.toArgb()
            strokeWidth = stroke.strokeWidth * ((scaleX + scaleY) / 2)
            style = Paint.Style.STROKE
            strokeCap = Paint.Cap.ROUND
            strokeJoin = Paint.Join.ROUND
            isAntiAlias = true
        }
        
        val androidPath = convertComposePathToAndroidPath(stroke.path, scaleX, scaleY)
        canvas.drawPath(androidPath, paint)
    }
    
    // Convert the result bitmap to JPEG bytes
    val outputStream = ByteArrayOutputStream()
    resultBitmap.compress(Bitmap.CompressFormat.JPEG, 95, outputStream)
    
    // Clean up
    resultBitmap.recycle()
    
    return outputStream.toByteArray()
}

/**
 * Converts a Compose UI Path to an Android Graphics Path with scaling.
 */
private fun convertComposePathToAndroidPath(
    composePath: Path,
    scaleX: Float,
    scaleY: Float
): AndroidPath {
    val androidPath = AndroidPath()
    
    // Get the path iterator to extract path segments
    val pathIterator = composePath.iterator()
    
    while (pathIterator.hasNext()) {
        val segment = pathIterator.next()
        when (segment.type) {
            androidx.compose.ui.graphics.PathSegment.Type.Move -> {
                val points = segment.points
                androidPath.moveTo(points[0] * scaleX, points[1] * scaleY)
            }
            androidx.compose.ui.graphics.PathSegment.Type.Line -> {
                val points = segment.points
                androidPath.lineTo(points[2] * scaleX, points[3] * scaleY)
            }
            androidx.compose.ui.graphics.PathSegment.Type.Quadratic -> {
                val points = segment.points
                androidPath.quadTo(
                    points[2] * scaleX, points[3] * scaleY,
                    points[4] * scaleX, points[5] * scaleY
                )
            }
            androidx.compose.ui.graphics.PathSegment.Type.Conic -> {
                // Approximate conic as quadratic
                val points = segment.points
                androidPath.quadTo(
                    points[2] * scaleX, points[3] * scaleY,
                    points[4] * scaleX, points[5] * scaleY
                )
            }
            androidx.compose.ui.graphics.PathSegment.Type.Cubic -> {
                val points = segment.points
                androidPath.cubicTo(
                    points[2] * scaleX, points[3] * scaleY,
                    points[4] * scaleX, points[5] * scaleY,
                    points[6] * scaleX, points[7] * scaleY
                )
            }
            androidx.compose.ui.graphics.PathSegment.Type.Close -> {
                androidPath.close()
            }
            androidx.compose.ui.graphics.PathSegment.Type.Done -> {
                // Nothing to do
            }
        }
    }
    
    return androidPath
}
