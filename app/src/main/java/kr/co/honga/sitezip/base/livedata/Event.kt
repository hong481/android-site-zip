package kr.co.honga.sitezip.base.livedata

import androidx.lifecycle.Observer
import java.util.concurrent.atomic.AtomicBoolean

open class Event<out T> constructor(

    /**
     * 데이터.
     */
    val data: T? = null

) {

    /**
     * 이벤트를 사용했으면 true.
     */
    val isUsed: AtomicBoolean by lazy {
        AtomicBoolean(false)
    }

}

/**
 * 전달 값이 없는 이벤트.
 */
class EmptyEvent : Event<Unit>(Unit)

/**
 * 이벤트 전용 옵저버.
 */
open class EventObserver<T>(

    private val onEvent: (T) -> Unit

) : Observer<Event<T>> {

    override fun onChanged(event: Event<T>?) {
        if (event?.isUsed?.get() == false) {
            event.data?.let { onEvent(it) }
            event.isUsed.compareAndSet(false, true)
        }
    }
}