package io.github.neilyich.glassmorphism.samples

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import glassmorphismcompose.samples.generated.resources.Res
import glassmorphismcompose.samples.generated.resources.tree
import io.github.neilyich.glassmorphism.blurredBackground
import io.github.neilyich.glassmorphism.blurredContent
import io.github.neilyich.glassmorphism.rememberBlurHolder
import io.github.neilyich.glassmorphism.samples.ui.BackIcon
import kotlinx.serialization.Serializable
import org.jetbrains.compose.resources.painterResource

@Serializable
data object ListItemsSample : Sample {
    override val name = "List Items"

    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    override fun Content(navController: NavHostController, isBlurEnabled: Boolean) {
        Scaffold(
            modifier = Modifier.fillMaxSize(),
            topBar = {
                TopAppBar(
                    title = { Text("$name Sample") },
                    navigationIcon = { BackIcon(navController) },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = Color.Transparent,
                    ),
                )
            },
        ) { contentPadding ->
            val blurHolder = rememberBlurHolder(isBlurEnabled)
            Box(Modifier.fillMaxSize()) {
                Image(
                    modifier = Modifier
                        .blurredContent(blurHolder)
                        .fillMaxSize(),
                    painter = painterResource(Res.drawable.tree),
                    contentDescription = null,
                    contentScale = ContentScale.Crop,
                )
                LazyColumn(
                    modifier = Modifier
                        .testTag("lazy_column"),
                    verticalArrangement = Arrangement.spacedBy(24.dp),
                    contentPadding = contentPadding,
                ) {
                    items(50) { index ->
                        ListItem(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp)
                                .blurredBackground(
                                    blurHolder = blurHolder,
                                    blurRadius = 50.dp,
                                    color = Color.White.copy(alpha = 0.25f),
                                    shape = MaterialTheme.shapes.large,
                                ),
                            headlineContent = { Text("Item $index") },
                            colors = ListItemDefaults.colors(
                                containerColor = Color.Transparent,
                            )
                        )
                    }
                }
            }
        }
    }
}