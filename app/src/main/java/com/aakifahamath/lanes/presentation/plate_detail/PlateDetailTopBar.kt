package com.aakifahamath.lanes.presentation.plate_detail

import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import com.aakifahamath.lanes.presentation.theme.md_theme_light_onSecondaryContainer
import com.aakifahamath.lanes.presentation.theme.md_theme_light_secondaryContainer
import com.aakifahamath.lanes.R

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PlateDetailTopBar(navigateUp: () -> Unit) {
    TopAppBar(
        title = { Text(stringResource(R.string.plate_details)) },
        colors = TopAppBarDefaults.smallTopAppBarColors(
            containerColor = md_theme_light_secondaryContainer,
            titleContentColor = md_theme_light_onSecondaryContainer
        ),
        navigationIcon = {
            IconButton(onClick = {
                navigateUp()
            }) {
                Icon(
                    painterResource(id = R.drawable.ic_back_arrow),
                    contentDescription = stringResource(R.string.go_back),
                )
            }
             }
    )
}