package org.fairscan.app.ui.redaction

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput

/**
 * Represents a single drawing stroke with its path, color, and width.
 */
data class DrawingStroke(
    val path: Path,
    val color: Color,
    val strokeWidth: Float,
    val isLine: Boolean = false,
    val startPoint: Offset? = null,
    val endPoint: Offset? = null
)

/**
 * State holder for the redaction canvas.
 */
class RedactionState {
    var strokes by mutableStateOf<List<DrawingStroke>>(emptyList())
    var currentPath by mutableStateOf<Path?>(null)
    var currentColor by mutableStateOf(Color.Black)
    var currentStrokeWidth by mutableStateOf(20f)
    var isEraserMode by mutableStateOf(false)
    var isLineMode by mutableStateOf(false)
    
    // Counter to force recomposition on path updates
    var updateCounter by mutableIntStateOf(0)
    
    private var lineStartPoint: Offset? = null
    private var currentStrokeColor: Color = Color.Black
    
    fun startStroke(position: Offset) {
        currentStrokeColor = currentColor
        if (isLineMode) {
            lineStartPoint = position
            currentPath = Path().apply {
                moveTo(position.x, position.y)
                lineTo(position.x, position.y)
            }
        } else if (!isEraserMode) {
            currentPath = Path().apply {
                moveTo(position.x, position.y)
            }
        }
        updateCounter++
    }
    
    fun updateStroke(position: Offset) {
        if (isEraserMode) {
            // Eraser mode: remove strokes near the touch position
            eraseStrokesNear(position)
            return
        }
        
        currentPath?.let { path ->
            if (isLineMode && lineStartPoint != null) {
                // For line mode, recreate path from start to current position
                currentPath = Path().apply {
                    moveTo(lineStartPoint!!.x, lineStartPoint!!.y)
                    lineTo(position.x, position.y)
                }
            } else {
                // For brush mode, add to existing path
                path.lineTo(position.x, position.y)
            }
            updateCounter++
        }
    }
    
    private fun eraseStrokesNear(position: Offset) {
        // Remove strokes that are near the touch position
        val eraseRadius = currentStrokeWidth * 2
        val newStrokes = strokes.filter { stroke ->
            !isStrokeNearPosition(stroke, position, eraseRadius)
        }
        if (newStrokes.size != strokes.size) {
            strokes = newStrokes
            updateCounter++
        }
    }
    
    private fun isStrokeNearPosition(stroke: DrawingStroke, position: Offset, radius: Float): Boolean {
        // For line strokes, check distance to line segment
        if (stroke.isLine && stroke.startPoint != null && stroke.endPoint != null) {
            return distanceToLineSegment(position, stroke.startPoint, stroke.endPoint) < radius
        }
        // For brush strokes, we approximate by checking the bounding area
        // This is a simplified check - in a real app you'd iterate through path points
        val bounds = stroke.path.getBounds()
        val expandedBounds = bounds.inflate(radius)
        return expandedBounds.contains(position)
    }
    
    private fun distanceToLineSegment(point: Offset, lineStart: Offset, lineEnd: Offset): Float {
        val dx = lineEnd.x - lineStart.x
        val dy = lineEnd.y - lineStart.y
        val lengthSquared = dx * dx + dy * dy
        
        if (lengthSquared == 0f) {
            return kotlin.math.sqrt((point.x - lineStart.x) * (point.x - lineStart.x) + 
                                   (point.y - lineStart.y) * (point.y - lineStart.y))
        }
        
        var t = ((point.x - lineStart.x) * dx + (point.y - lineStart.y) * dy) / lengthSquared
        t = t.coerceIn(0f, 1f)
        
        val nearestX = lineStart.x + t * dx
        val nearestY = lineStart.y + t * dy
        
        return kotlin.math.sqrt((point.x - nearestX) * (point.x - nearestX) + 
                               (point.y - nearestY) * (point.y - nearestY))
    }
    
    fun endStroke() {
        if (!isEraserMode) {
            currentPath?.let { path ->
                strokes = strokes + DrawingStroke(
                    path = path,
                    color = currentStrokeColor,
                    strokeWidth = currentStrokeWidth,
                    isLine = isLineMode,
                    startPoint = if (isLineMode) lineStartPoint else null,
                    endPoint = if (isLineMode) null else null // We don't track end for brush
                )
            }
        }
        currentPath = null
        lineStartPoint = null
        updateCounter++
    }
    
    fun undo() {
        if (strokes.isNotEmpty()) {
            strokes = strokes.dropLast(1)
            updateCounter++
        }
    }
    
    fun clear() {
        strokes = emptyList()
        currentPath = null
        updateCounter++
    }
    
    fun hasStrokes(): Boolean = strokes.isNotEmpty()
}

@Composable
fun rememberRedactionState(): RedactionState {
    return remember { RedactionState() }
}

/**
 * A canvas component for drawing redaction marks on documents.
 */
@Composable
fun RedactionCanvas(
    state: RedactionState,
    modifier: Modifier = Modifier
) {
    // Read state values here to trigger recomposition when they change
    val strokes = state.strokes
    val currentPath = state.currentPath
    val currentColor = state.currentColor
    val currentStrokeWidth = state.currentStrokeWidth
    val isEraserMode = state.isEraserMode
    val updateCounter = state.updateCounter // Force recomposition
    
    Canvas(
        modifier = modifier
            .pointerInput(Unit) {
                detectDragGestures(
                    onDragStart = { offset ->
                        state.startStroke(offset)
                    },
                    onDrag = { change, _ ->
                        change.consume()
                        state.updateStroke(change.position)
                    },
                    onDragEnd = {
                        state.endStroke()
                    },
                    onDragCancel = {
                        state.endStroke()
                    }
                )
            }
            .pointerInput(Unit) {
                detectTapGestures(
                    onTap = { offset ->
                        if (isEraserMode) {
                            state.startStroke(offset)
                            state.updateStroke(offset)
                            state.endStroke()
                        } else {
                            state.startStroke(offset)
                            state.endStroke()
                        }
                    }
                )
            }
    ) {
        // Use updateCounter to ensure this block re-runs (suppress unused warning)
        @Suppress("UNUSED_VARIABLE")
        val forceRecomposition = updateCounter
        
        // Draw all completed strokes
        strokes.forEach { stroke ->
            drawPath(
                path = stroke.path,
                color = stroke.color,
                style = Stroke(
                    width = stroke.strokeWidth,
                    cap = StrokeCap.Round,
                    join = StrokeJoin.Round
                )
            )
        }
        
        // Draw current stroke being drawn (live preview)
        currentPath?.let { path ->
            drawPath(
                path = path,
                color = currentColor,
                style = Stroke(
                    width = currentStrokeWidth,
                    cap = StrokeCap.Round,
                    join = StrokeJoin.Round
                )
            )
        }
    }
}
