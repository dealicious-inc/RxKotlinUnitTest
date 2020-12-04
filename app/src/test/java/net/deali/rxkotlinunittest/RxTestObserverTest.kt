package net.deali.rxkotlinunittest

import io.reactivex.Single
import io.reactivex.disposables.CompositeDisposable
import net.deali.rxkotlinunittest.models.Post
import net.deali.rxkotlinunittest.retrofit.Service
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.mockito.Mock
import org.mockito.Mockito
import org.mockito.MockitoAnnotations

class RxTestObserverTest {

    @Mock
    lateinit var service: Service

    @Before
    fun setUp() {
        MockitoAnnotations.initMocks(this)
    }

    @Test
    fun whenGetPosts_thenReturnSuccess_assertByTestObserver() {
        val post = Post("포스트", false)
        val posts = listOf(post)
        Mockito.`when`(service.getPosts()).thenReturn(Single.create {
            it.onSuccess(posts)
        })
        service.getPosts().test()
            .assertValueCount(1)
            .assertSubscribed()
            .assertNoErrors()
            .assertValues(posts)
    }

    @Test
    fun whenGetPosts_thenReturnError_assertByTestObserver() {
        val throwable = Throwable()

        Mockito.`when`(service.getPosts()).thenReturn(
            Single.create {
                it.onError(throwable)
            }
        )

        val testObserver = service.getPosts().test()
        testObserver
            .assertSubscribed()
            .assertError(throwable)
    }

}