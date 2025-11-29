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
import com.example.callinspector.history.data.repository.HistoryRepository
import com.example.callinspector.presentation.viewModel.DiagnosticsViewModel
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.just
import io.mockk.mockk
import io.mockk.runs
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

    // 2. NEW MOCK: The History Repository
    private val historyRepository: HistoryRepository = mockk()

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

        // Stub the save function so it does nothing (just runs) when called
        coEvery {
            historyRepository.saveReport(any(), any(), any(), any(), any(), any(), any(), any())
        } just runs

        // Init ViewModel with the new dependency
        viewModel = DiagnosticsViewModel(
            runAudioTestUseCase = audioUseCase,
            runSpeakerTestUseCase = speakerUseCase,
            runNetworkTestUseCase = networkUseCase,
            runDeviceTestUseCase = deviceUseCase,
            historyRepository = historyRepository // <--- INJECTED HERE
        )
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `calculateScore deducts points for failed hardware AND saves history`() = runTest {
        // SCENARIO: Mic Fails, Speaker Passes, Network Fails, Camera Fails.

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
        testDispatcher.scheduler.advanceUntilIdle()

        // Speaker Test -> User says "Yes" (Pass)
        viewModel.onSpeakerHeard(true)
        testDispatcher.scheduler.advanceUntilIdle()

        // Camera Tests -> User/System reports Fail
        viewModel.onBackCameraResult(false)
        viewModel.onFrontCameraResult(false)

        // Device Test (Triggered auto after Front Camera)
        testDispatcher.scheduler.advanceUntilIdle()

        // 3. Generate Report
        viewModel.finishDiagnostics()

        // Wait for the save coroutine to finish
        testDispatcher.scheduler.advanceUntilIdle()

        // 4. ASSERT (The "Proof")
        val state = viewModel.uiState.value

        // Verify Score Logic
        assert(state.finalScore < 60) { "Score should be failing but was ${state.finalScore}" }
        assertEquals("Grade should be D", "D", state.finalGrade)

        // Verify Persistence (NEW CHECK)
        // Ensure that saveReport was actually called exactly once
        coVerify(exactly = 1) {
            historyRepository.saveReport(
                score = state.finalScore,
                grade = state.finalGrade,
                mic = false, // matched our mock
                speaker = true,
                net = true, // Network test completed technically, even if results were bad
                cam = false,
                speed = any(),
                latency = any()
            )
        }
    }
}