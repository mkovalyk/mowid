package com.kovcom.mowid.base.ui

interface IState

interface IEvent

interface UserIntent {

    val priority: Priority
        get() = Priority.Low
}

enum class Priority {
    Low,
    High,
}

interface IEffect
