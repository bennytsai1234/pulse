package com.pulse.music.domain.usecase

import com.pulse.music.domain.model.ABLoopState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ABLoopRepository @Inject constructor() {
    private val _state = MutableStateFlow(ABLoopState())
    val state: StateFlow<ABLoopState> = _state.asStateFlow()

    fun setA(position: Long) {
        _state.value = _state.value.copy(enabled = true, aPoint = position)
    }

    fun setB(position: Long) {
        _state.value = _state.value.copy(bPoint = position)
    }

    fun clear() {
        _state.value = ABLoopState()
    }
}

class SetALoopPointUseCase @Inject constructor(private val repository: ABLoopRepository) {
    operator fun invoke(position: Long) = repository.setA(position)
}

class SetBLoopPointUseCase @Inject constructor(private val repository: ABLoopRepository) {
    operator fun invoke(position: Long) = repository.setB(position)
}

class ClearABLoopUseCase @Inject constructor(private val repository: ABLoopRepository) {
    operator fun invoke() = repository.clear()
}
