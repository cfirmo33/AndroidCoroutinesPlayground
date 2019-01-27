package andreabresolin.androidcoroutinesplayground.base

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LifecycleRegistry
import androidx.lifecycle.LiveData
import org.assertj.core.api.Assertions.assertThat
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.rules.TestRule
import org.mockito.Mockito.mock

abstract class BaseViewModelTest : BaseMockitoTest() {

    @get:Rule
    internal var rule: TestRule = InstantTaskExecutorRule()

    private lateinit var testLifecycle: Lifecycle
    private lateinit var liveDataChanges: MutableMap<LiveData<*>, MutableList<*>>

    @Before
    fun beforeViewModelTest() {
        val lifecycleRegistry = LifecycleRegistry(mock(LifecycleOwner::class.java))
        lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_RESUME)
        testLifecycle = lifecycleRegistry

        liveDataChanges = mutableMapOf()
    }

    @After
    fun afterViewModelTest() {
        liveDataChanges.forEach { liveData, _ ->
            liveData.removeObservers { testLifecycle }
        }
    }

    @Suppress("UNCHECKED_CAST")
    protected fun <T> trackLiveDataChanges(liveData: LiveData<T>) {
        liveData.observe({ testLifecycle }, { newState ->
            var stateChanges: MutableList<T>? = liveDataChanges[liveData] as MutableList<T>?

            if (stateChanges == null) {
                stateChanges = mutableListOf()
                liveDataChanges[liveData] = stateChanges
            }

            stateChanges.add(newState)
        })
    }

    @Suppress("UNCHECKED_CAST")
    protected fun <T> assertThatLiveDataStateChangesAre(liveData: LiveData<T>, stateChanges: List<T>) {
        assertThat(liveDataChanges[liveData] as List<T>?).isEqualTo(stateChanges)
    }
}