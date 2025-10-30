package io.github.neilyich.glassmorphism.samples

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.BottomSheetDefaults
import androidx.compose.material3.BottomSheetScaffold
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.rememberBottomSheetScaffoldState
import androidx.compose.material3.rememberStandardBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
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
data object BottomSheetSample : Sample() {
    override val name = "Bottom Sheet"

    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    override fun rememberDefaultBlurSettings(isBlurEnabled: Boolean): BlurSettings {
        val colorScheme = MaterialTheme.colorScheme
        val shape = BottomSheetDefaults.ExpandedShape
        return remember(isBlurEnabled) {
            BlurSettings(
                isBlurEnabled = isBlurEnabled,
                blurRadius = 30.dp,
                tintColor = colorScheme.onBackground.copy(alpha = 0.15f),
                shape = shape,
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
        val scaffoldState = rememberBottomSheetScaffoldState(rememberStandardBottomSheetState())
        val blurHolder = rememberBlurHolder(blurSettings.isBlurEnabled)
        BottomSheetScaffold(
            modifier = Modifier.fillMaxSize(),
            scaffoldState = scaffoldState,
            topBar = {
                TopAppBar(
                    title = { Text("$name Sample") },
                    navigationIcon = { BackIcon(navController) },
                    actions = { BlurSettingsIcon(isSettingsIconVisible) },
                )
            },
            sheetContent = {
                Column(
                    modifier = Modifier
                        .fillMaxHeight(0.8f)
                        .fillMaxWidth()
                        .blurredBackground(
                            blurHolder = blurHolder,
                            tintColor = blurSettings.tintColor,
                            shape = blurSettings.shape,
                            tileMode = blurSettings.tileMode
                        ) {
                            blurRadius = blurSettings.blurRadius
                        }
                        .padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    Text("Bottom Sheet with blurred background")
                }
            },
            sheetDragHandle = null,
            sheetContainerColor = MaterialTheme.colorScheme.background.copy(alpha = 0.25f),
            sheetShadowElevation = 0.dp,
            sheetPeekHeight = 300.dp,
            containerColor = MaterialTheme.colorScheme.background,
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
                    // Alternative to blurredContent in ListItem
                    //.blurredContent(blurHolder)
                    .fillMaxSize(),
                contentPadding = contentPadding,
                overscrollEffect = null,
            ) {
                items(20) {
                    val (image, text) = listItems[it % listItems.size]
                    ListItem(
                        // Alternative to blurredContent in LazyColumn
                        modifier = Modifier.blurredContent(blurHolder),
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