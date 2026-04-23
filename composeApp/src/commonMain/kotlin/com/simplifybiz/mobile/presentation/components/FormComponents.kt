package com.simplifybiz.mobile.presentation.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

@Composable
fun SectionWithHelp(
    title: String,
    helpTitle: String,
    helpText: String,
    content: @Composable () -> Unit
) {
    var showHelp by remember { mutableStateOf(false) }

    Column {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(
                text = title.uppercase(),
                style = MaterialTheme.typography.labelLarge.copy(
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF616161)
                ),
                modifier = Modifier.weight(1f)
            )

            Row(
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .clickable { showHelp = !showHelp }
                    .background(if (showHelp) Color(0xFFE1BEE7) else Color.Transparent)
                    .padding(horizontal = 8.dp, vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                androidx.compose.material3.Icon(
                    imageVector = if (showHelp) Icons.Default.Close else Icons.Default.Info,
                    contentDescription = "Help",
                    tint = Color(0xFF4A148C)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = "Help Me",
                    style = MaterialTheme.typography.labelSmall,
                    color = Color(0xFF4A148C)
                )
            }
        }

        AnimatedVisibility(visible = showHelp) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFFF3E5F5), RoundedCornerShape(8.dp))
                    .padding(16.dp)
            ) {
                Text(helpTitle, fontWeight = FontWeight.Bold, color = Color(0xFF4A148C))
                Spacer(modifier = Modifier.height(8.dp))
                Text(helpText, style = MaterialTheme.typography.bodySmall, color = Color.Black)
            }
        }

        Spacer(modifier = Modifier.height(12.dp))
        content()
    }
}

@Composable
fun LuxuryTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    minLines: Int = 1,
    modifier: Modifier = Modifier
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label) },
        modifier = modifier.fillMaxWidth(),
        minLines = minLines,
        shape = RoundedCornerShape(12.dp),
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = Color(0xFF4A148C),
            unfocusedBorderColor = Color(0xFFE0E0E0),
            focusedContainerColor = Color.White,
            unfocusedContainerColor = Color.White
        )
    )
}
