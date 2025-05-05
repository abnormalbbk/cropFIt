package com.bibek.cropfit.home

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.bibek.cropfit.R
import com.bibek.cropfit.extensions.dashedBorder

val rainbowColors = listOf(
    Color.Red, Color(0xFFFF7F00), // Orange
    Color.Blue, Color.Green, Color.Blue, Color(0xFF4B0082), // Indigo
    Color(0xFF8B00FF)  // Violet
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen() {
    val brush = Brush.linearGradient(colors = rainbowColors)
    val scrollState = rememberScrollState()

    Scaffold(
        modifier = Modifier
            .fillMaxSize()
            .background(color = Color.White), topBar = {
            TopAppBar(
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.primary,
                ), title = {
                    Text(stringResource(R.string.app_name))
                })
        }) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(scrollState)
        ) {
            Text(
                modifier = Modifier
                    .padding(16.dp)
                    .align(alignment = Alignment.CenterHorizontally)
                    .clickable(
                        enabled = true, onClick = {

                        }), text = buildAnnotatedString {
                withStyle(
                    SpanStyle(
                        brush = brush, alpha = .5f
                    )
                ) {
                    append("My Fields")
                }
                withStyle(
                    SpanStyle(
                        brush = brush, alpha = 1f
                    )
                ) {
                    append(" \uD83C\uDF3D")
                }
            })
            repeat(3) { index ->
                Text("Field $index", modifier = Modifier.padding(16.dp))
            }

            OutlinedButton(
                onClick = {}, modifier = Modifier
                    .padding(16.dp)
                    .align(alignment = Alignment.End)
            ) {
                Text("View All")
            }

            Image(
                modifier = Modifier
                    .height(250.dp)
                    .fillMaxWidth()
                    .padding(16.dp),
                painter = painterResource(R.drawable.banner),
                contentDescription = "Banner",
                contentScale = ContentScale.Crop,
            )

            Box(
                modifier = Modifier
                    .padding(16.dp)
                    .align(alignment = Alignment.CenterHorizontally)
                    .dashedBorder(1.dp, MaterialTheme.colorScheme.primary, 8.dp)
                    .clickable(
                        onClick = {

                        }),

                ) {
                Text(
                    "Tap Here",
                    modifier = Modifier
                        .align(alignment = Alignment.Center)
                        .padding(vertical = 16.dp, horizontal = 64.dp)
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun HomeScreenPreview() {
    HomeScreen()
}