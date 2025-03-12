package com.example.triviapp.presentation

import ComposeNavigation
import android.annotation.SuppressLint
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.triviapp.presentation.theme.TriviAppTheme

@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@Composable
fun MainComponent() {
    Scaffold(
        modifier = Modifier.fillMaxSize(),
        topBar = { Header() },
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(top = it.calculateTopPadding())
                .background(MaterialTheme.colorScheme.background)
        ) {
            ComposeNavigation()
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun Header() {
    TopAppBar(
        title = {
            Text(
                text = "TriviApp",
                modifier = Modifier
                    .fillMaxWidth(),
                style = MaterialTheme.typography.titleMedium,
                fontSize = 26.sp,
                textAlign = TextAlign.Center
            )
        },
        modifier = Modifier
            .wrapContentHeight()
            .fillMaxWidth()
            .padding(
                top = 10.dp,
                start = 10.dp,
                end = 10.dp
            )
            .shadow(
                elevation = 50.dp,
                spotColor = Color.DarkGray,
                shape = RoundedCornerShape(10.dp)
            )
    )
}

@Preview(showBackground = true)
@Composable
fun MainPreview() {
    TriviAppTheme {
        MainComponent()
    }
}