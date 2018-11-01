package io.tage.android.app.modules.main.profile.payments

import com.stripe.android.model.Card
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import io.reactivex.subjects.PublishSubject
import io.tage.android.app.common.BankCard
import io.tage.android.app.common.ProgressStateHelper
import io.tage.android.app.common.ProgressStateHelper.*
import io.tage.android.app.common.log_d
import io.tage.android.app.common.log_i
import io.tage.android.app.services.ServerLocalError
import io.tage.android.app.services.server
import java.net.HttpRetryException


class ProfilePaymentsViewModel {

    var disposable: Disposable? = null
    var gettingUserBankCard = PublishSubject.create<PaymentsHelper>()

    fun getUserBankCard() {
        gettingUserBankCard.onNext(PaymentsHelper(START))
        disposable = server.getUserBankCards()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                        {
                            gettingUserBankCard.onNext(PaymentsHelper(it, SUCCESS))
                      },
                        {
                            gettingUserBankCard.onNext(PaymentsHelper(it.localizedMessage))
                            handleError(it)
                        }
                )
    }

    private fun handleError(error: Throwable) = when (error) {
        is HttpRetryException -> log_d(error.toString())
        is ServerLocalError -> log_d(error.toString())
        else -> log_d(error.localizedMessage)
    }

    class PaymentsHelper(val msg: String?, val card: Array<BankCard>?, val state: ProgressStateHelper) {
        constructor(state: ProgressStateHelper) : this(null, null, state)
        constructor(card: Array<BankCard>, state: ProgressStateHelper) : this(null, card, state)
        constructor(msg: String) : this(msg, null, ERROR)
    }
}