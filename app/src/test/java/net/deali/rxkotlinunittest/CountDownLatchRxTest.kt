package net.deali.rxkotlinunittest

import io.reactivex.Single
import io.reactivex.SingleObserver
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import net.deali.rxkotlinunittest.models.Post
import net.deali.rxkotlinunittest.retrofit.Service
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.mockito.Mock
import org.mockito.Mockito
import org.mockito.MockitoAnnotations
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit

class CountDownLatchRxTest {
    val compositeDisposable = CompositeDisposable()

    @Mock
    lateinit var service: Service

    @Before
    fun setUp() {
        MockitoAnnotations.initMocks(this)
    }

    @Test
    fun whenGetPosts_thenReturnSuccess_assertByCountDownLatch() {
        val post = Post("첫번째 포스트", false)
        val post2 = Post("두번째 포스트", false)
        val posts = listOf(post, post2)
        Mockito.`when`(service.getPosts()).thenReturn(Single.create {
            it.onSuccess(posts)
        })
        val latch = CountDownLatch(1)
        service.getPosts()
            .subscribeOn(Schedulers.io())
            .observeOn(Schedulers.newThread()) //newThread 로 생성 했기 때문에 latch.await 을 사용 하지 않으면 onSuccess 타지 않고 바로 종료 됨.
            .subscribe(
                object : SingleObserver<List<Post>> {
                    override fun onSuccess(t: List<Post>) {
                        assert(t == posts)
                        latch.countDown()
                    }

                    override fun onSubscribe(d: Disposable) {
                        compositeDisposable.add(d)
                    }

                    override fun onError(e: Throwable) {
                        latch.countDown()
                    }
                }
            )

        latch.await(3, TimeUnit.SECONDS) //CountDownLatch count 는 1 로 셋팅 되어서 countDown 이 한번 불리면 종료
    }

    @Test
    fun whenGetPosts_thenReturnError_assertByCountDownLatch() {
        val throwable = Throwable()

        Mockito.`when`(service.getPosts()).thenReturn(Single.create {
            it.onError(throwable)
        })
        val latch = CountDownLatch(1)
        service.getPosts()
            .subscribeOn(Schedulers.io())
            .observeOn(Schedulers.newThread())
            .subscribe(
                object : SingleObserver<List<Post>> {
                    override fun onSuccess(t: List<Post>) {
                        latch.countDown()
                    }

                    override fun onSubscribe(d: Disposable) {
                        compositeDisposable.add(d)
                    }

                    override fun onError(e: Throwable) {
                        assert(e == throwable)
                        latch.countDown()
                    }
                }
            )

        latch.await(3, TimeUnit.SECONDS)
    }

    @After
    fun end() {
        compositeDisposable.clear()
    }

}