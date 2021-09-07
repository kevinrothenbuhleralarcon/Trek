package ch.kra.trek.helper


class Chrono {
    private var timeStart: Long = 0
    private var timeEnd: Long = 0

    init {
        start()
    }

    fun start() {
        timeEnd = -1
        timeStart = System.currentTimeMillis()
    }

    fun stop() {timeEnd = System.currentTimeMillis()}

    fun deltaTime(): Long {
        return if (!timeEnd.equals(-1)) {
            timeEnd - timeStart
        } else {
            System.currentTimeMillis() - timeStart
        }
    }

    override fun toString(): String {
        return "${deltaTime()} ms"
    }
}