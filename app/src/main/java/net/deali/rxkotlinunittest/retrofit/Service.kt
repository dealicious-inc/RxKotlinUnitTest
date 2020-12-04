package net.deali.rxkotlinunittest.retrofit

import io.reactivex.Single
import net.deali.rxkotlinunittest.models.Post

interface Service {
    fun getPosts(): Single<List<Post>>
}