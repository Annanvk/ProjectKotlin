package io.tage.android.app.modules.main.profile.payments.card_detail

import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import io.reactivex.subjects.PublishSubject
import io.tage.android.app.common.ProgressStateHelper
import io.tage.android.app.common.ProgressStateHelper.*
import io.tage.android.app.common.log_d
import io.tage.android.app.services.ServerLocalError
import io.tage.android.app.services.server
import java.net.HttpRetryException

class StatusCardViewModel {

    var disposable: Disposable? = null
    val settingDefaultCard = PublishSubject.create<CardHelper>()
    val deletingCard = PublishSubject.create<CardHelper>()

    fun setDefaultCard(cardId: String) {
        settingDefaultCard.onNext(CardHelper(START))
        disposable = server.setDefaultCard(cardId)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                        {
                            settingDefaultCard.onNext(CardHelper(it, SUCCESS))
                        },
                        {
                            handleError(it)
                            settingDefaultCard.onNext(CardHelper(it.localizedMessage))
                        }
                )
    }


    fun deleteBankCard(cardId: String) {
        deletingCard.onNext(CardHelper(START))
        disposable = server.deleteCard(cardId)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                        {
                            deletingCard.onNext(CardHelper(it, SUCCESS))
                        },
                        {
                            handleError(it)
                            deletingCard.onNext(CardHelper(it.localizedMessage))
                        })
    }

    private fun handleError(error: Throwable) = when (error) {
        is HttpRetryException -> log_d(error.toString())
        is ServerLocalError -> log_d(error.toString())
        else -> log_d(error.localizedMessage)
    }

    class CardHelper(val msg: String?, val card: Boolean?, val state: ProgressStateHelper) {
        constructor(state: ProgressStateHelper) : this(null, null, state)
        constructor(card: Boolean, state: ProgressStateHelper) : this(null, card, state)
        constructor(msg: String) : this(msg, null, ERROR)
    }
}