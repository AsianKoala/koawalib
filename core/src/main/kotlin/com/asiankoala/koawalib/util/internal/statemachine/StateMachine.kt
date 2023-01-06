package com.asiankoala.koawalib.util.internal.statemachine

internal class StateMachine<StateEnum>(
    private val stateList: List<State<StateEnum>>,
    private val universals: List<() -> Unit>
) {
    private var currentState = stateList.first()
    var running = false
        private set
    val state: StateEnum get() = currentState.state

    fun start() {
        running = true
        if (currentState.transitionCondition is TimedTransition) {
            (currentState.transitionCondition as TimedTransition).resetTimer()
        }
        currentState.enterActions.forEach { it.invoke() }
    }

    fun reset() {
        currentState = stateList.first()
    }

    fun update() {
        if (currentState.transitionCondition.invoke()) transition()
        if (!running) return
        currentState.loopActions.forEach { it.invoke() }
        universals.forEach { it.invoke() }
    }

    private fun transition() {
        if (stateList.last() == currentState) running = false
        else currentState = stateList[stateList.indexOf(currentState) + 1]
        currentState.enterActions.forEach { it.invoke() }
        if (currentState.transitionCondition is TimedTransition) {
            (currentState.transitionCondition as TimedTransition).resetTimer()
        }
    }
}
