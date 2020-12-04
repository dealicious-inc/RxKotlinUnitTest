package net.deali.rxkotlinunittest.viewModelTest

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import io.reactivex.Single
import io.reactivex.android.plugins.RxAndroidPlugins
import io.reactivex.plugins.RxJavaPlugins
import io.reactivex.schedulers.Schedulers
import net.deali.rxkotlinunittest.models.Post
import net.deali.rxkotlinunittest.retrofit.Service
import net.deali.rxkotlinunittest.viewModels.GeneralViewModel
import org.junit.*
import org.mockito.Mock
import org.mockito.Mockito
import org.mockito.MockitoAnnotations


class GeneralViewModelTest {
    @get:Rule
    var instantExecutorRule = InstantTaskExecutorRule()

    lateinit var viewModel: GeneralViewModel

    @Mock
    lateinit var service: Service

    @Before
    fun setUp() {
        MockitoAnnotations.initMocks(this)
        RxJavaPlugins.setIoSchedulerHandler { Schedulers.trampoline() }
        RxJavaPlugins.setComputationSchedulerHandler { Schedulers.trampoline() }
        RxJavaPlugins.setNewThreadSchedulerHandler { Schedulers.trampoline() }
        RxAndroidPlugins.setInitMainThreadSchedulerHandler { Schedulers.trampoline() }

        //Test 코드는 바로 뷰모델을 만들었지만 실제 사용하는 경우 AAC ViewModel 은 파라미터가 있을 경우 ViewModelProvider.Factory 를 Custom 해서 만들면 된다.
        viewModel = GeneralViewModel(service)
    }

    @Test
    fun test() {
        val post = Post("첫번째 포스트", false)
        val post2 = Post("두번째 포스트", false)
        val posts = listOf(post, post2)
        Mockito.`when`(service.getPosts()).thenReturn(Single.create {
            it.onSuccess(posts)
        })

        //getPost 내부에서 scheduler 셋팅을 io , mainThread 로 하지만 @Before 에서 셋팅한 스케줄러 사용됨.
        viewModel.getPosts()

        Assert.assertTrue(viewModel.items.value!!.isNotEmpty())
        Assert.assertTrue(viewModel.items.value == posts)
    }

    @After
    fun end() {
        viewModel.dispose()
    }
}