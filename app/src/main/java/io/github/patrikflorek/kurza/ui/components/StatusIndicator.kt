package io.github.patrikflorek.kurza.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import io.github.patrikflorek.kurza.ui.theme.KurzaTheme
import io.github.patrikflorek.kurza.ui.theme.StatusConnected
import io.github.patrikflorek.kurza.ui.theme.StatusDisconnected

@Composable
fun StatusIndicator(
    statusText: String,
    statusColor: Color
) {
    Box(
        modifier = Modifier.fillMaxWidth(),
        contentAlignment = Alignment.Center
    ) {
        Surface(
            shape = RoundedCornerShape(20.dp),
            color = statusColor.copy(alpha = 0.15f),
            border = BorderStroke(1.dp, statusColor.copy(alpha = 0.3f))
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(10.dp)
                        .clip(CircleShape)
                        .background(statusColor)
                )
                Text(
                    text = statusText,
                    style = MaterialTheme.typography.bodyMedium,
                    color = statusColor,
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF0D1117)
@Composable
private fun StatusIndicatorPreview() {
    KurzaTheme {
        Column(modifier = Modifier.padding(16.dp)) {
            StatusIndicator(
                statusText = "Desktop-PC",
                statusColor = StatusConnected
            )
            Spacer(modifier = Modifier.height(16.dp))
            StatusIndicator(
                statusText = "Not connected",
                statusColor = StatusDisconnected
            )
        }
    }
}
