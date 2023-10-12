package com.kovcom.mowid.base.ui

interface State

interface Event

interface UserIntent {

    val priority: Priority
        get() = Priority.Low
}

enum class Priority {
    Low,
    High,
}

interface Effect
