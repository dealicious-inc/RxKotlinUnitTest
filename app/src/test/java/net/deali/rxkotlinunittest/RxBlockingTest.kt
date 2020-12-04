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

class RxBlockingTest {

    @Mock
    lateinit var service: Service

    @Before
    fun setUp() {
        MockitoAnnotations.initMocks(this)
    }

    @Test
    fun whenGetPosts_thenReturnSuccess_assertByBlock() {
        val post = Post("포스트", false)
        val posts = listOf(post)
        Mockito.`when`(service.getPosts()).thenReturn(Single.create {
            it.onSuccess(posts)
        })
        //blockingGet은 완료 될 때 까지 대기. blockingGet은 내부에서 CountDownLatch 를 사용 한다.
        val resultPosts = service.getPosts().blockingGet()
        assert(resultPosts.size == 1)
        assert(resultPosts == posts)
    }

    @Test
    fun whenGetPosts_thenReturnError_assertByBlock() {
        val throwable = Throwable()
        val posts = listOf<Post>()

        Mockito.`when`(service.getPosts()).thenReturn(
            Single.create {
                it.onError(throwable)
            }
        )

        val resultPosts = service.getPosts().onErrorReturn {
            assert(it == throwable)
            return@onErrorReturn posts
        }.blockingGet()
        assert(resultPosts == posts)
    }

}