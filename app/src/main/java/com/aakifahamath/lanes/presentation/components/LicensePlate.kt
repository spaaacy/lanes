package com.aakifahamath.lanes.presentation.components

import androidx.compose.foundation.layout.*
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import com.aakifahamath.lanes.domain.model.Plate

@Composable
fun LicensePlate(
    modifier: Modifier = Modifier,
    plate: Plate,
    fontSize: TextUnit,
    horizontalPadding: Dp,
    verticalPadding: Dp
) {
    ElevatedCard(
        modifier = modifier,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondary)
    ) {
        Row(
            Modifier.padding(horizontalPadding, verticalPadding),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = plate.prefix,
                fontWeight = FontWeight.Bold,
                fontSize = fontSize,
                color = MaterialTheme.colorScheme.onSecondary
            )

            Spacer(modifier = Modifier.width(verticalPadding))

            Text(
                text = plate.number,
                fontWeight = FontWeight.Bold,
                fontSize = fontSize,
                color = MaterialTheme.colorScheme.onSecondary
            )
        }
    }
}