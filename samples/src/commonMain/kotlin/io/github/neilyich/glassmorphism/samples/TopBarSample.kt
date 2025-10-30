package io.github.neilyich.glassmorphism.samples

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.TileMode
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import io.github.neilyich.glassmorphism.blurredBackground
import io.github.neilyich.glassmorphism.blurredContent
import io.github.neilyich.glassmorphism.rememberBlurHolder
import io.github.neilyich.glassmorphism.resources.Res
import io.github.neilyich.glassmorphism.resources.space
import io.github.neilyich.glassmorphism.resources.tree
import io.github.neilyich.glassmorphism.samples.ui.BackIcon
import kotlinx.serialization.Serializable
import org.jetbrains.compose.resources.painterResource

@Serializable
data object TopBarSample : Sample() {
    override val name = "Top Bar"

    @Composable
    override fun rememberDefaultBlurSettings(isBlurEnabled: Boolean): BlurSettings {
        val colorScheme = MaterialTheme.colorScheme
        return remember(isBlurEnabled) {
            BlurSettings(
                isBlurEnabled = isBlurEnabled,
                blurRadius = 16.dp,
                tintColor = colorScheme.background.copy(alpha = 0.35f),
                backgroundColor = colorScheme.background,
                tileMode = TileMode.Decal,
            )
        }
    }

    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    override fun Content(
        navController: NavHostController,
        blurSettings: BlurSettings,
        isSettingsIconVisible: Boolean
    ) {
        val blurHolder = rememberBlurHolder(blurSettings.isBlurEnabled)
        Scaffold(
            modifier = Modifier.fillMaxSize(),
            topBar = {
                TopAppBar(
                    modifier = Modifier
                        .blurredBackground(
                            blurHolder = blurHolder,
                            blurRadius = blurSettings.blurRadius,
                            tintColor = blurSettings.tintColor,
                            backgroundColor = blurSettings.backgroundColor,
                            tileMode = blurSettings.tileMode,
                        ),
                    title = { Text("$name Sample") },
                    navigationIcon = { BackIcon(navController) },
                    actions = { BlurSettingsIcon(isSettingsIconVisible) },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = Color.Transparent,
                    ),
                )
            },
        ) { contentPadding ->
            val listItems = remember {
                listOf(
                    Res.drawable.tree to "In botany, a tree is a perennial plant with an elongated stem, or trunk, usually supporting branches and leaves.",
                    Res.drawable.space to "The Milky Way or Milky Way Galaxy is the galaxy that includes the Solar System",
                )
            }
            LazyColumn(
                modifier = Modifier
                    .testTag("lazy_column")
                    .blurredContent(blurHolder)
                    .fillMaxSize(),
                contentPadding = contentPadding,
            ) {
                items(20) {
                    val (image, text) = listItems[it % listItems.size]
                    ListItem(
                        //modifier = Modifier.blurredContent(blurHolder),
                        leadingContent = {
                            Image(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(24.dp))
                                    .fillMaxWidth(0.3f)
                                    .aspectRatio(1f),
                                painter = painterResource(image),
                                contentDescription = null,
                                contentScale = ContentScale.Crop,
                            )
                        },
                        headlineContent = {
                            Text(text)
                        }
                    )
                }
            }
        }
    }
}