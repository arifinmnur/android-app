package com.kelsos.mbrc.features.output

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import arrow.core.Try
import com.kelsos.mbrc.content.output.OutputApi
import com.kelsos.mbrc.content.output.OutputResponse
import com.kelsos.mbrc.di.modules.AppCoroutineDispatchers
import com.kelsos.mbrc.ui.BaseViewModel
import kotlinx.coroutines.launch
import java.net.SocketException
import java.net.SocketTimeoutException

class OutputSelectionViewModel(
  private val outputApi: OutputApi,
  dispatchers: AppCoroutineDispatchers
) : BaseViewModel<OutputSelectionResult>(dispatchers) {

  private val _outputs: MutableLiveData<List<String>> = MutableLiveData()
  private val _selection: MutableLiveData<String> = MutableLiveData()

  init {
      _outputs.postValue(emptyList())
      _selection.postValue("")
  }

  val outputs: LiveData<List<String>>
    get() = _outputs

  val selection: LiveData<String>
    get() = _selection

  private fun updateState(response: OutputResponse) {
    _outputs.postValue(response.devices)
    _selection.postValue(response.active)
  }

  private fun code(throwable: Throwable?): OutputSelectionResult {
    return when (throwable?.cause ?: throwable) {
      is SocketException -> OutputSelectionResult.ConnectionError
      is SocketTimeoutException -> OutputSelectionResult.ConnectionError
      else -> OutputSelectionResult.UnknownError
    }
  }

  private fun Try<OutputResponse>.toResult(): OutputSelectionResult {
    return toEither().fold({ code(it) }, { OutputSelectionResult.Success })
  }

  fun reload() {
    scope.launch {
      val result = Try {
        outputApi.getOutputs().also {
          updateState(it)
        }
      }.toResult()
      emit(result)
    }
  }

  fun setOutput(output: String) {
    scope.launch {
      val result = Try {
        outputApi.setOutput(output).also {
          updateState(it)
        }
      }.toResult()
      emit(result)
    }
  }
}