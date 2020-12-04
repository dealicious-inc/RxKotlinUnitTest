package net.deali.rxkotlinunittest

import io.reactivex.Single
import io.reactivex.SingleObserver
import io.reactivex.android.plugins.RxAndroidPlugins
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.disposables.Disposable
import io.reactivex.plugins.RxJavaPlugins
import io.reactivex.schedulers.Schedulers
import net.deali.rxkotlinunittest.models.Post
import net.deali.rxkotlinunittest.retrofit.Service
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.mockito.Mock
import org.mockito.Mockito
import org.mockito.MockitoAnnotations

class RxTrampolineTest {

    val compositeDisposable = CompositeDisposable()

    @Mock
    lateinit var service: Service

    @Before
    fun setUp() {
        MockitoAnnotations.initMocks(this)
        //Schedulers.trampoline() 을 사용하면 현재 실행되고 있는 Thread 에서 실행 하게 해준다. immediate execution !!
        RxJavaPlugins.setIoSchedulerHandler { Schedulers.trampoline() }
        RxJavaPlugins.setComputationSchedulerHandler { Schedulers.trampoline() }
        RxJavaPlugins.setNewThreadSchedulerHandler { Schedulers.trampoline() }
        RxAndroidPlugins.setInitMainThreadSchedulerHandler { Schedulers.trampoline() }
    }

    @Test
    fun whenGetPosts_thenReturnSuccess() {
        val post = Post("첫번째 포스트", false)
        val post2 = Post("두번째 포스트", true)
        val posts = listOf(post, post2)
        Mockito.`when`(service.getPosts()).thenReturn(Single.create {
            it.onSuccess(posts)
        })

        service.getPosts()
            .subscribeOn(Schedulers.newThread())
            .observeOn(Schedulers.newThread())
            .subscribe(
                object : SingleObserver<List<Post>> {
                    override fun onSuccess(t: List<Post>) {
                        assert(t == posts)
                    }

                    override fun onSubscribe(d: Disposable) {
                        compositeDisposable.add(d)
                    }

                    override fun onError(e: Throwable) {

                    }
                }
            )
    }

    @After
    fun end() {
        compositeDisposable.clear()
    }

}