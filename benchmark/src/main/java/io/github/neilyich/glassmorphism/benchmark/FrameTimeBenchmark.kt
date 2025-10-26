package io.github.neilyich.glassmorphism.benchmark

import androidx.benchmark.macro.FrameTimingMetric
import androidx.benchmark.macro.StartupMode
import androidx.benchmark.macro.junit4.MacrobenchmarkRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.LargeTest
import androidx.test.uiautomator.By
import androidx.test.uiautomator.Direction
import androidx.test.uiautomator.Until
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import kotlin.time.Duration.Companion.seconds

@RunWith(AndroidJUnit4::class)
@LargeTest
class FrameTimeBenchmark {

    @get:Rule
    val benchmarkRule = MacrobenchmarkRule()

    private val targetPackage = "io.github.neilyich.glassmorphism"
    private val iterations = 4
    private val scrollIterations = 4

    @Test
    fun dialogBlur() = runTest("Dialog", true)

    @Test
    fun dialogNoBlur() = runTest("Dialog", false)

    @Test
    fun topBarBlur() = runTest("Top Bar", true)

    @Test
    fun topBarNoBlur() = runTest("Top Bar", false)

    @Test
    fun bottomSheetBlur() = runTest("Bottom Sheet", true)

    @Test
    fun bottomSheetNoBlur() = runTest("Bottom Sheet", false)

    @Test
    fun listItemsBlur() = runTest("List Items", true)

    @Test
    fun listItemsNoBlur() = runTest("List Items", false)

    private fun runTest(sampleName: String, isBlurEnabled: Boolean) {
        benchmarkRule.measureRepeated(
            packageName = targetPackage,
            metrics = listOf(
                FrameTimingMetric(),
            ),
            startupMode = StartupMode.WARM,
            iterations = iterations,
            setupBlock = {
                startActivityAndWait { intent ->
                    intent.putExtra("isBlurEnabled", isBlurEnabled)
                }
                with(device) {
                    val sampleList = wait(Until.findObject(By.res("sample_list")), 5.seconds.inWholeMilliseconds)
                    sampleList.setGestureMarginPercentage(0.1f)
                    val sample = sampleList.scrollUntil(Direction.DOWN, Until.findObject(By.res(sampleName)))
                    sample.click()
                    waitForIdle()
                    wait(Until.hasObject(By.res("lazy_column")), 5.seconds.inWholeMilliseconds)
                }

            },
        ) {
            val list = device.findObject(By.res("lazy_column"))
            repeat(scrollIterations) {
                list.fling(Direction.DOWN, 20_000)
                list.fling(Direction.UP, 20_000)
            }
        }
    }
}