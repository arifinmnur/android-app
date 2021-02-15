package com.kelsos.mbrc.mvp

import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LifecycleRegistry
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancelChildren
import rx.Subscription
import rx.subscriptions.CompositeSubscription

open class BasePresenter<T : BaseView>(
  dispatcher: CoroutineDispatcher = Dispatchers.Main
) : Presenter<T>, LifecycleOwner {
  var view: T? = null
    private set

  private val compositeSubscription = CompositeSubscription()
  private val lifecycleRegistry: LifecycleRegistry = LifecycleRegistry(this)

  private val job = SupervisorJob()
  private val coroutineContext = job + dispatcher
  protected val scope: CoroutineScope = CoroutineScope(coroutineContext)

  override val isAttached: Boolean
    get() = view != null

  override fun attach(view: T) {
    this.view = view
    lifecycleRegistry.currentState = Lifecycle.State.CREATED
    lifecycleRegistry.currentState = Lifecycle.State.STARTED
  }

  override fun detach() {
    lifecycleRegistry.currentState = Lifecycle.State.DESTROYED
    this.view = null
    compositeSubscription.clear()
    coroutineContext.cancelChildren()
  }

  protected fun addSubscription(subscription: Subscription) {
    this.compositeSubscription.add(subscription)
  }

  fun checkIfAttached() {
    if (!isAttached) {
      throw ViewNotAttachedException()
    }
  }

  protected class ViewNotAttachedException :
    RuntimeException("Please call Presenter.attach(BaseView) before calling a method on the presenter")

  override fun getLifecycle(): Lifecycle {
    return this.lifecycleRegistry
  }
}
