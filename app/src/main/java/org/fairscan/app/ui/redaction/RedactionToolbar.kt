package org.fairscan.app.ui.redaction

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Undo
import androidx.compose.material.icons.filled.Brush
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

/**
 * Available colors for redaction.
 */
val redactionColors = listOf(
    Color.Black,
    Color.White,
    Color.Red,
    Color.Blue,
    Color(0xFF00AA00), // Green
    Color.Yellow
)

/**
 * Toolbar for redaction controls including color picker, brush size, eraser, and line mode.
 */
@Composable
fun RedactionToolbar(
    state: RedactionState,
    onDone: () -> Unit,
    onCancel: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        color = MaterialTheme.colorScheme.surfaceContainerHigh,
        shape = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp),
        shadowElevation = 8.dp
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            // Top row: Cancel, Title, Done
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onCancel) {
                    Icon(
                        Icons.Default.Close,
                        contentDescription = "Cancel",
                        tint = MaterialTheme.colorScheme.onSurface
                    )
                }
                Text(
                    "Redact",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )
                IconButton(
                    onClick = onDone,
                    enabled = state.hasStrokes()
                ) {
                    Icon(
                        Icons.Default.Check,
                        contentDescription = "Done",
                        tint = if (state.hasStrokes()) 
                            MaterialTheme.colorScheme.primary 
                        else 
                            MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f)
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            
            // Color picker row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                redactionColors.forEach { color ->
                    ColorButton(
                        color = color,
                        isSelected = state.currentColor == color && !state.isEraserMode,
                        onClick = {
                            state.currentColor = color
                            state.isEraserMode = false
                            // Keep line mode active if it was active
                        }
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            
            // Tool buttons row: Brush, Line, and Eraser mode
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Brush mode button (freehand drawing)
                ToolButton(
                    label = "Brush",
                    isSelected = !state.isLineMode && !state.isEraserMode,
                    onClick = {
                        state.isLineMode = false
                        state.isEraserMode = false
                    }
                )
                
                Spacer(modifier = Modifier.width(12.dp))
                
                // Line mode button
                ToolButton(
                    label = "Line",
                    isSelected = state.isLineMode,
                    onClick = {
                        state.isLineMode = true
                        state.isEraserMode = false
                    }
                )
                
                Spacer(modifier = Modifier.width(12.dp))
                
                // Eraser button
                ToolButton(
                    label = "Eraser",
                    isSelected = state.isEraserMode,
                    onClick = {
                        state.isEraserMode = true
                        state.isLineMode = false
                    }
                )
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Brush size slider
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    Icons.Default.Brush,
                    contentDescription = "Brush size",
                    modifier = Modifier.size(24.dp),
                    tint = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.width(8.dp))
                
                // Small brush indicator
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .background(
                            color = MaterialTheme.colorScheme.onSurface,
                            shape = CircleShape
                        )
                )
                
                Slider(
                    value = state.currentStrokeWidth,
                    onValueChange = { state.currentStrokeWidth = it },
                    valueRange = 5f..80f,
                    modifier = Modifier
                        .weight(1f)
                        .padding(horizontal = 8.dp)
                )
                
                // Large brush indicator
                Box(
                    modifier = Modifier
                        .size(24.dp)
                        .background(
                            color = MaterialTheme.colorScheme.onSurface,
                            shape = CircleShape
                        )
                )
            }
            
            // Preview of current brush size and mode
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                val modeText = when {
                    state.isLineMode -> "Line Mode"
                    state.isEraserMode -> "Eraser Mode"
                    else -> "Brush Mode"
                }
                Text(
                    "Size: ${state.currentStrokeWidth.toInt()} • $modeText",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            // Undo button
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center
            ) {
                IconButton(
                    onClick = { state.undo() },
                    enabled = state.hasStrokes(),
                    colors = IconButtonDefaults.iconButtonColors(
                        contentColor = MaterialTheme.colorScheme.primary,
                        disabledContentColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f)
                    )
                ) {
                    Icon(
                        Icons.AutoMirrored.Filled.Undo,
                        contentDescription = "Undo"
                    )
                }
            }
        }
    }
}

@Composable
private fun ColorButton(
    color: Color,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val borderColor = if (isSelected) {
        MaterialTheme.colorScheme.primary
    } else {
        MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)
    }
    
    val borderWidth = if (isSelected) 3.dp else 1.dp
    
    Box(
        modifier = modifier
            .size(36.dp)
            .clip(CircleShape)
            .background(color = color, shape = CircleShape)
            .border(width = borderWidth, color = borderColor, shape = CircleShape)
            .clickable { onClick() }
    ) {
        if (isSelected) {
            Icon(
                Icons.Default.Check,
                contentDescription = "Selected",
                modifier = Modifier
                    .align(Alignment.Center)
                    .size(20.dp),
                tint = if (color == Color.White || color == Color.Yellow) Color.Black else Color.White
            )
        }
    }
}

@Composable
private fun ToolButton(
    label: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val backgroundColor = if (isSelected) {
        MaterialTheme.colorScheme.primaryContainer
    } else {
        MaterialTheme.colorScheme.surfaceVariant
    }
    
    val borderColor = if (isSelected) {
        MaterialTheme.colorScheme.primary
    } else {
        MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)
    }
    
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(8.dp))
            .background(color = backgroundColor, shape = RoundedCornerShape(8.dp))
            .border(width = if (isSelected) 2.dp else 1.dp, color = borderColor, shape = RoundedCornerShape(8.dp))
            .clickable { onClick() }
            .padding(horizontal = 16.dp, vertical = 8.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}
