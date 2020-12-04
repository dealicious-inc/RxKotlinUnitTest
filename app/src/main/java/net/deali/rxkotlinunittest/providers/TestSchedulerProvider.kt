package net.deali.rxkotlinunittest.providers

import io.reactivex.Scheduler
import io.reactivex.schedulers.TestScheduler
import net.deali.rxkotlinunittest.providers.SchedulerProvider

class TestSchedulerProvider constructor(private val testScheduler: TestScheduler) :
    SchedulerProvider {
    override fun ui(): Scheduler = testScheduler
    override fun computation(): Scheduler = testScheduler
    override fun io(): Scheduler = testScheduler
}