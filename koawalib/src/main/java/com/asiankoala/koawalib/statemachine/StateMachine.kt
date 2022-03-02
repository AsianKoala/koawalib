package com.asiankoala.koawalib.statemachine

import com.asiankoala.koawalib.statemachine.transition.TimedTransition

class StateMachine<StateEnum>(private val stateList: List<State<StateEnum>>, private val universals: List<Action>) {
    var running = false
        private set

    private var currentState = stateList.first()

    fun start() {
        running = true

        if (currentState.transitionCondition is TimedTransition)
            (currentState.transitionCondition as TimedTransition).startTimer()

        currentState.enterActions.forEach(Action::run)
    }

    fun stop() {
        running = false
    }

    fun reset() {
        currentState = stateList.first()
    }

    val state: StateEnum get() = currentState.state

    fun update() {
        if (currentState.transitionCondition?.shouldTransition() == true)
            transition()

        if (!running) return

        currentState.loopActions.forEach(Action::run)
        universals.forEach(Action::run)
    }

    private fun transition() {
        currentState.exitActions.forEach(Action::run)

        if (stateList.last() == currentState) {
            running = false
        } else {
            currentState = stateList[stateList.indexOf(currentState) + 1]
        }

        currentState.enterActions.forEach(Action::run)

        if (currentState.transitionCondition is TimedTransition)
            (currentState.transitionCondition as TimedTransition).startTimer()
    }

    fun smartRun(shouldStart: Boolean) {
        if (shouldStart) {
            reset()
            start()
        }

        if (running) {
            update()
        }
    }
}
