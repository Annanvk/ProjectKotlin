package io.tage.android.app.modules.main.history

import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import io.reactivex.subjects.PublishSubject
import io.tage.android.app.common.ProgressStateHelper
import io.tage.android.app.common.Rent
import io.tage.android.app.common.log_d
import io.tage.android.app.services.ServerLocalError
import io.tage.android.app.services.server
import retrofit2.HttpException

class HistoryViewModel {

    var disposable: Disposable? = null
    val listHistory = PublishSubject.create<HistoryHelper>()
    fun getListHistory(size: Int?, page: Int?) {
        listHistory.onNext(HistoryHelper(ProgressStateHelper.START))
        disposable = server.getHistory(size, page)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({
                    listHistory.onNext(HistoryHelper(it))
                },
                        {
                            handleError(it)
                            listHistory.onNext(HistoryHelper(it.localizedMessage))
                        })
    }

    private fun handleError(error: Throwable) = when (error) {
        is HttpException -> log_d(error.toString())
        is ServerLocalError -> log_d(error.toString())
        else -> log_d(error.localizedMessage)
    }

    class HistoryHelper(val msg: String?, val history: Array<Rent>?, val state: ProgressStateHelper) {
        constructor(state: ProgressStateHelper) : this(null, null, state)
        constructor(history: Array<Rent>) : this(null, history, ProgressStateHelper.SUCCESS)
        constructor(msg: String) : this(msg, null, ProgressStateHelper.ERROR)
    }
}