package kr.co.honga.sitezip.base.model

interface Model {

    companion object {
        const val NONE_ID: Long = 0

        const val TRUE: Int = 1
        const val FALSE: Int = 0
    }

    var id: Long?
}

fun Model?.isNullOrEmptyId(): Boolean = (this?.id ?: Model.NONE_ID) <= Model.NONE_ID