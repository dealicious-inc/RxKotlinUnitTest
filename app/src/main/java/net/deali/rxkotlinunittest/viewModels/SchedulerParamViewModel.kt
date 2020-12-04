package net.deali.rxkotlinunittest.viewModels

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import io.reactivex.SingleObserver
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.disposables.Disposable
import net.deali.rxkotlinunittest.models.Post
import net.deali.rxkotlinunittest.providers.SchedulerProvider
import net.deali.rxkotlinunittest.retrofit.Service

class SchedulerParamViewModel(
    private val service: Service,
    private val schedulerProvider: SchedulerProvider
) : ViewModel() {
    val compositeDisposable = CompositeDisposable()
    val items: MutableLiveData<List<Post>> = MutableLiveData()

    fun getPosts() {
        service.getPosts()
            .subscribeOn(schedulerProvider.io())
            .observeOn(schedulerProvider.ui())
            .subscribe(object : SingleObserver<List<Post>> {
                override fun onSuccess(t: List<Post>) {
                    items.value = t
                }

                override fun onSubscribe(d: Disposable) {
                    compositeDisposable.add(d)
                }

                override fun onError(e: Throwable) {

                }
            })
    }

    override fun onCleared() {
        dispose()
        super.onCleared()
    }

    fun dispose() {
        compositeDisposable.clear()
    }
}