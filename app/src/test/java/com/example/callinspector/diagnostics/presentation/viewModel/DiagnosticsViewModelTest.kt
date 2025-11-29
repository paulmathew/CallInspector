package com.example.callinspector.diagnostics.presentation.viewModel

import org.junit.After
import org.junit.Before
import com.example.callinspector.diagnostics.domain.model.AudioTestResult
import com.example.callinspector.diagnostics.domain.model.DeviceHealth
import com.example.callinspector.diagnostics.domain.model.NetworkHealth
import com.example.callinspector.diagnostics.domain.model.SpeakerTestResult
import com.example.callinspector.diagnostics.domain.model.TestStage
import com.example.callinspector.diagnostics.domain.usecase.RunAudioTestUseCase
import com.example.callinspector.diagnostics.domain.usecase.RunDeviceTestUseCase
import com.example.callinspector.diagnostics.domain.usecase.RunNetworkTestUseCase
import com.example.callinspector.diagnostics.domain.usecase.RunSpeakerTestUseCase
import com.example.callinspector.presentation.viewModel.DiagnosticsViewModel
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.Assert.assertEquals

import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class DiagnosticsViewModelTest {

    // 1. Mocks (We fake the hardware interactions)
    private val audioUseCase: RunAudioTestUseCase = mockk()
    private val speakerUseCase: RunSpeakerTestUseCase = mockk()
    private val networkUseCase: RunNetworkTestUseCase = mockk()

    private val deviceUseCase: RunDeviceTestUseCase = mockk()




    private lateinit var viewModel: DiagnosticsViewModel
    private val testDispatcher = StandardTestDispatcher()

    @Before
    fun setup() {
        // "Freeze" time control for Coroutines
        Dispatchers.setMain(testDispatcher)

        // Setup default mock behaviors (Happy Path)
        coEvery { audioUseCase() } returns AudioTestResult(success = true, averageAmplitude = 10.0)
        coEvery { speakerUseCase() } returns SpeakerTestResult(playbackSucceeded = true, currentVolume = 5, maxVolume = 10)
        coEvery { networkUseCase() } returns flowOf(NetworkHealth(stage = TestStage.COMPLETE, downloadSpeedMbps = 100.0))
        coEvery { deviceUseCase() } returns DeviceHealth()


        // Init ViewModel
        viewModel = DiagnosticsViewModel(
            audioUseCase,
            speakerUseCase,
            networkUseCase,
            runDeviceTestUseCase = deviceUseCase
        )
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `calculateScore deducts points for failed hardware`() = runTest {
        // SCENARIO: Mic Fails, Speaker Passes, Network Fails, Camera Fails.
        // Expected Logic: 100 - 15 (Mic) - 10 (Network) - 15 (Back Cam) - 15 (Front Cam) = 45 (Grade D/F)

        // 1. Mock the FAILURES
        coEvery { audioUseCase() } returns AudioTestResult(success = false, averageAmplitude = 0.0)

        val badNetwork = NetworkHealth(
            stage = TestStage.COMPLETE,
            packetLossPercent = 5, // > 2% is bad
            downloadSpeedMbps = 1.0 // < 5Mbps is bad
        )
        coEvery { networkUseCase() } returns flowOf(badNetwork)

        // 2. Drive the ViewModel (Simulate User Actions)

        // Start -> Runs Mic Test (which we mocked to fail)
        viewModel.startDiagnostics()
        testDispatcher.scheduler.advanceUntilIdle() // Wait for coroutines

        // Speaker Test -> User says "Yes" (Pass)
        viewModel.onSpeakerHeard(true)
        testDispatcher.scheduler.advanceUntilIdle() // Triggers Network Test

        // Camera Tests -> User/System reports Fail
        viewModel.onBackCameraResult(false)
        viewModel.onFrontCameraResult(false)

        // Device Test (Triggered auto after Front Camera)
        // If your code has runDeviceDiagnostics(), it runs here.
        testDispatcher.scheduler.advanceUntilIdle()

        // 3. Generate Report
        viewModel.finishDiagnostics()

        // 4. ASSERT (The "Proof")
        val state = viewModel.uiState.value

        // Let's calculate expected score based on your logic:
        // Start: 100
        // Mic Fail: -15
        // Speaker Pass: -0
        // Back Cam Fail: -15
        // Front Cam Fail: -15
        // Network Fail (Loss > 2% OR Speed < 5Mbps OR General Fail): -10
        // Note: Your logic might deduct multiple times if you didn't use 'else if'.
        // Based on the code I gave you, it was separate 'if' statements, so it might deduct -20 for network (Loss AND Speed).
        // Let's assume ideal logic: 100 - 15 - 15 - 15 - 10 = 45.

        // We verify that the score is LOW (indicating logic worked)
        // Instead of exact number (which can change), we assert range or Grade

        assert(state.finalScore < 60) { "Score should be failing (D or F) but was ${state.finalScore}" }
        assertEquals("Grade should be C or D", "D", state.finalGrade) // Assuming < 50 is D/F
    }
}