package io.tage.android.app.modules.main.things.my_thing

import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import io.reactivex.subjects.PublishSubject
import io.tage.android.app.common.ProgressStateHelper
import io.tage.android.app.common.ProgressStateHelper.*
import io.tage.android.app.common.Thing
import io.tage.android.app.common.log_d
import io.tage.android.app.services.ServerLocalError
import io.tage.android.app.services.server
import retrofit2.HttpException

class MyThingsViewModel {

    var disposable: Disposable? = null
    val listMyThings = PublishSubject.create<MyThingsHelper>()

    fun getListMyThings(size: Int, pageNumber: Int?) {
        listMyThings.onNext(MyThingsHelper(START))
        disposable = server.getMyThings(size, pageNumber)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                        {
                            listMyThings.onNext(MyThingsHelper(it))
                        },
                        {
                            handleError(it)
                            listMyThings.onNext(MyThingsHelper(it.localizedMessage))
                        }
                )
    }

    private fun handleError(error: Throwable) = when (error) {
        is HttpException -> log_d(error.toString())
        is ServerLocalError -> log_d(error.toString())
        else -> log_d(error.localizedMessage)
    }

    class MyThingsHelper(val msg: String?, val things: Array<Thing>?, val state: ProgressStateHelper) {
        constructor(state: ProgressStateHelper) : this(null, null, state)
        constructor(things: Array<Thing>) : this(null, things, SUCCESS)
        constructor(msg: String) : this(msg, null, ERROR)
    }
}