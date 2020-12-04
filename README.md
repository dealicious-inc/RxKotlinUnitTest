# RxKotlin Unit Test 

5가지 케이스로 나누었다.
다양한 케이스를 통해 어떤식으로 테스트를 진행하는지 알아보자.

1. countDownLatch 
```
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
```
2. blocking
```
val post = Post("포스트", false)
val posts = listOf(post)
Mockito.`when`(service.getPosts()).thenReturn(Single.create {
   it.onSuccess(posts)
})
//blockingGet은 완료 될 때 까지 대기. blockingGet은 내부에서 CountDownLatch 를 사용 한다.
val resultPosts = service.getPosts().blockingGet()
assert(resultPosts.size == 1)
assert(resultPosts == posts)
```
3. TestObserver
```
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
```
4. Trampoline 
```
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
```
5. ViewModel Unit Test
##### scheduler , service 파라미터를 가진 ViewModel
```
@Before
fun setUp() {
  MockitoAnnotations.initMocks(this)
  testScheduler = TestScheduler()
  val schedulerProvider = TestSchedulerProvider(testScheduler)
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
  testScheduler.triggerActions() //ViewModel 생성 시 주입된 scheduler
  Assert.assertTrue(viewModel.items.value!!.isNotEmpty())
  Assert.assertTrue(viewModel.items.value == posts)
}
```
##### service 파라미터를 가진 ViewModel
```
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

   //getPost 내부에서 scheduler 셋팅을 io , mainThread 로 하지만 @Before 에서 셋팅한 스케줄러 값을 따라간다. 그래서 문제 없이 돌아감
  viewModel.getPosts()

  Assert.assertTrue(viewModel.items.value!!.isNotEmpty())
  Assert.assertTrue(viewModel.items.value == posts)
}
```
