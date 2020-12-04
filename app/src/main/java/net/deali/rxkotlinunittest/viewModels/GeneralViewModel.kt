package net.deali.rxkotlinunittest.viewModels

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import io.reactivex.SingleObserver
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import net.deali.rxkotlinunittest.models.Post
import net.deali.rxkotlinunittest.retrofit.Service

class GeneralViewModel(private val service: Service) : ViewModel() {
    val compositeDisposable = CompositeDisposable()
    val items: MutableLiveData<List<Post>> = MutableLiveData()

    fun getPosts() {
        service.getPosts()
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
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