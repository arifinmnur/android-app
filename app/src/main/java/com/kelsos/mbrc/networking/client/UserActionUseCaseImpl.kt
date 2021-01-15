package com.kelsos.mbrc.networking.client

import com.kelsos.mbrc.events.UserAction

class UserActionUseCaseImpl(private val messageQueue: MessageQueue) : UserActionUseCase {
  override fun perform(action: UserAction) {
    messageQueue.queue(SocketMessage.create(action.protocol, action.data))
  }
}
