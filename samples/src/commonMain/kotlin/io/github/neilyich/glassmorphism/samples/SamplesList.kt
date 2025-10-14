package io.github.neilyich.glassmorphism.samples

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.NavigateNext
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.navigation.NavHostController
import io.github.neilyich.glassmorphism.Greeting
import kotlinx.serialization.Serializable

@Serializable
data object SamplesListDestination

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SamplesList(navController: NavHostController, samples: List<Sample>) {
    Scaffold(
        modifier = Modifier.fillMaxSize(),
        topBar = {
            val greeting = remember { Greeting().greet() }
            CenterAlignedTopAppBar(
                title = { Text("Samples for $greeting") },
            )
        },
    ) {
        LazyColumn(
            modifier = Modifier
                .testTag("sample_list")
                .fillMaxSize()
                .padding(it),
        ) {
            items(samples.size) { index ->
                val sample = samples[index]
                ListItem(
                    modifier = Modifier
                        .testTag(sample.name)
                        .clickable { navController.navigate(sample) },
                    headlineContent = { Text(sample.name) },
                    trailingContent = { Icon(Icons.AutoMirrored.Filled.NavigateNext, null) }
                )
            }
        }
    }
}