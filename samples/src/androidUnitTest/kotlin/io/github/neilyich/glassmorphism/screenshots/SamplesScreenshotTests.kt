package io.github.neilyich.glassmorphism.screenshots

import android.content.ContentProvider
import android.util.Log
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onRoot
import androidx.compose.ui.test.performScrollToIndex
import androidx.navigation.testing.TestNavHostController
import com.github.takahirom.roborazzi.RoborazziOptions
import com.github.takahirom.roborazzi.RoborazziRule
import com.github.takahirom.roborazzi.ThresholdValidator
import io.github.neilyich.glassmorphism.samples.BottomSheetSample
import io.github.neilyich.glassmorphism.samples.DialogSample
import io.github.neilyich.glassmorphism.samples.ListItemsSample
import io.github.neilyich.glassmorphism.samples.Sample
import io.github.neilyich.glassmorphism.samples.TopBarSample
import io.github.neilyich.glassmorphism.samples.ui.SamplesTheme
import kotlinx.coroutines.runBlocking
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.Robolectric
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(
    sdk = [34],
    qualifiers = "+night",
)
class SamplesScreenshotTests {

    @get:Rule
    val composeTestRule = createComposeRule()

    @get:Rule
    val roborazziRule = RoborazziRule(
        composeRule = composeTestRule,
        captureRoot = composeTestRule.onRoot(),
        options = RoborazziRule.Options(
            captureType = RoborazziRule.CaptureType.LastImage(),
            roborazziOptions = RoborazziOptions(
                compareOptions = RoborazziOptions.CompareOptions(
                    resultValidator = ThresholdValidator(0.001f),
                ),
            ),
        ),
    )

    @Before
    fun setup() {
        setupAndroidContextProvider()
    }

    // Configures Compose's AndroidContextProvider to access resources in tests.
    // See https://youtrack.jetbrains.com/issue/CMP-6612
    private fun setupAndroidContextProvider() {
        val type = findAndroidContextProvider() ?: return
        Robolectric.setupContentProvider(type)
    }

    @Test
    fun bottomSheetBlur() = runTest(BottomSheetSample)

    @Test
    fun dialogBlur() = runTest(DialogSample)

    @Test
    fun listItemsBlur() = runTest(ListItemsSample)

    @Test
    fun topBarBlur() = runTest(TopBarSample)

    private fun runTest(sample: Sample) = runBlocking {
        composeTestRule.setContent {
            sample.TestContent()
        }
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithTag("lazy_column").performScrollToIndex(3)
        composeTestRule.waitForIdle()
    }

    @Composable
    private fun Sample.TestContent() {
        SamplesTheme {
            Content(
                navController = TestNavHostController(LocalContext.current),
                isBlurEnabled = true,
            )
        }
    }

    private fun findAndroidContextProvider(): Class<ContentProvider>? {
        val providerClassName = "org.jetbrains.compose.resources.AndroidContextProvider"
        return try {
            @Suppress("UNCHECKED_CAST")
            Class.forName(providerClassName) as Class<ContentProvider>
        } catch (_: ClassNotFoundException) {
            Log.i("Robolectric", "Class not found: $providerClassName")
            null
        }
    }
}