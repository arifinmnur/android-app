package com.kelsos.mbrc.mvp

import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LifecycleRegistry
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancelChildren
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.disposables.Disposable

open class BasePresenter<T : BaseView> : Presenter<T>, LifecycleOwner {
  var view: T? = null
    private set

  private val compositeDisposable = CompositeDisposable()
  private val lifecycleRegistry: LifecycleRegistry = LifecycleRegistry(this)

  private val job = SupervisorJob()
  private val coroutineContext = job + Dispatchers.Main
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
    compositeDisposable.clear()
    coroutineContext.cancelChildren()
  }

  protected fun addDisposable(disposable: Disposable) {
    this.compositeDisposable.add(disposable)
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
