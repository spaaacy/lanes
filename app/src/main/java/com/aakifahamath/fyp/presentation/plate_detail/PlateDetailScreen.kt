package com.aakifahamath.fyp.presentation.plate_detail

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.aakifahamath.fyp.R.drawable
import com.aakifahamath.fyp.domain.model.Plate
import com.ramcosta.composedestinations.annotation.Destination

@Composable
@Destination(navArgsDelegate = PlateDetailNavArgs::class)
fun UserHomeScreen() {

    val viewModel: PlateDetailViewModel = hiltViewModel()

    val plate = viewModel.plate.collectAsState(initial = Plate("",0,0.0))

    Column(
        Modifier.padding(32.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally) {


        Text(text = plate.value.reputation.toInt().toString(),
            fontSize = 24.sp,
            color = MaterialTheme.colorScheme.onBackground,
            fontWeight = FontWeight.Bold)

        Spacer(Modifier.height(8.dp))

        Row(Modifier.fillMaxWidth()) {
            Button(modifier = Modifier
                .weight(1f)
                .fillMaxWidth(),
                onClick = { viewModel.onEvent(PlateDetailEvent.ClickedThumbsUp) }) {
                Icon(painterResource(drawable.ic_thumb_up), "Thumbs up")
                Spacer(Modifier.width(12.dp))
                Text(text = "Thumbs Up")
            }
            Spacer(Modifier.width(8.dp))
            Button(modifier = Modifier
                .weight(1f)
                .fillMaxWidth(),
                onClick = { viewModel.onEvent(PlateDetailEvent.ClickedThumbsDown) }) {
                Icon(painterResource(drawable.ic_thumb_down), "Thumb down")
                Spacer(Modifier.width(12.dp))
                Text(text = "Thumbs Down")
            }
        }

    }

}