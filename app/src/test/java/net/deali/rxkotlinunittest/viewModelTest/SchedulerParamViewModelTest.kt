package net.deali.rxkotlinunittest.viewModelTest

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import io.reactivex.Single
import io.reactivex.schedulers.TestScheduler
import net.deali.rxkotlinunittest.models.Post
import net.deali.rxkotlinunittest.providers.TestSchedulerProvider
import net.deali.rxkotlinunittest.retrofit.Service
import net.deali.rxkotlinunittest.viewModels.SchedulerParamViewModel
import org.junit.*
import org.mockito.Mock
import org.mockito.Mockito
import org.mockito.MockitoAnnotations

class SchedulerParamViewModelTest {
    @get:Rule
    var instantExecutorRule = InstantTaskExecutorRule()

    lateinit var viewModel: SchedulerParamViewModel
    lateinit var testScheduler: TestScheduler

    @Mock
    lateinit var service: Service

    @Before
    fun setUp() {
        MockitoAnnotations.initMocks(this)
        testScheduler = TestScheduler()
        val schedulerProvider =
            TestSchedulerProvider(
                testScheduler
            )
        viewModel = SchedulerParamViewModel(
            service,
            schedulerProvider
        )
    }

    @Test
    fun test() {
        val post = Post("첫번째 포스트", false)
        val post2 = Post("두번째 포스트", false)
        val posts = listOf(post, post2)
        Mockito.`when`(service.getPosts()).thenReturn(Single.create {
            it.onSuccess(posts)
        })

        viewModel.getPosts()
        testScheduler.triggerActions()
        Assert.assertTrue(viewModel.items.value!!.isNotEmpty())
        Assert.assertTrue(viewModel.items.value == posts)
    }

    @After
    fun end() {
        viewModel.dispose()
    }
}