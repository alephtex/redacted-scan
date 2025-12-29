package org.fairscan.app.ui.redaction

import android.graphics.Bitmap
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp

/**
 * Screen for redacting/drawing on a scanned document page.
 */
@Composable
fun RedactionScreen(
    bitmap: Bitmap,
    onDone: (RedactionState, IntSize) -> Unit,
    onCancel: () -> Unit,
    modifier: Modifier = Modifier
) {
    val redactionState = rememberRedactionState()
    var canvasSize by remember { mutableStateOf(IntSize.Zero) }
    val aspectRatio = bitmap.width.toFloat() / bitmap.height.toFloat()
    
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.surfaceContainerLow)
    ) {
        // Image with drawing overlay
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .padding(8.dp),
            contentAlignment = Alignment.Center
        ) {
            // Container for image and canvas - use aspectRatio to size it properly
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(aspectRatio)
                    .onSizeChanged { size ->
                        canvasSize = size
                    }
            ) {
                // Background image
                Image(
                    bitmap = bitmap.asImageBitmap(),
                    contentDescription = "Document",
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Fit
                )
                
                // Drawing overlay canvas
                RedactionCanvas(
                    state = redactionState,
                    modifier = Modifier.fillMaxSize()
                )
            }
        }
        
        // Toolbar at the bottom
        RedactionToolbar(
            state = redactionState,
            onDone = { onDone(redactionState, canvasSize) },
            onCancel = onCancel,
            modifier = Modifier.fillMaxWidth()
        )
    }
}
