package io.tage.android.app.modules.main.things.confirm

import com.stripe.android.model.Card
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.internal.disposables.DisposableContainer
import io.reactivex.schedulers.Schedulers
import io.reactivex.subjects.PublishSubject
import io.tage.android.app.common.BankCard
import io.tage.android.app.common.ProgressStateHelper
import io.tage.android.app.common.ProgressStateHelper.*
import io.tage.android.app.common.Thing
import io.tage.android.app.common.log_d
import io.tage.android.app.services.ServerLocalError
import io.tage.android.app.services.server
import retrofit2.HttpException

class ConfirmationFragmentViewModel {
    private lateinit var disposableBag: DisposableContainer
    var confirm = PublishSubject.create<ConfirmRentHelper>()
    var usersCard = PublishSubject.create<PaymentsHelper>()

    lateinit var rentalItem: Thing

    fun confirmRent(thingId: String) {
        confirm.onNext(ConfirmRentHelper(START))
        disposableBag.add(server.confirmRent(thingId)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                        {
                            confirm.onNext(ConfirmRentHelper(it, SUCCESS))
                        },
                        {
                            handleError(it)
                            confirm.onNext(ConfirmRentHelper(it.localizedMessage))
                        })
        )
    }

    fun getUsersCards(){
        usersCard.onNext(PaymentsHelper(START))
        disposableBag.add(server.getUserBankCards()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                        {
                            usersCard.onNext(PaymentsHelper(it, SUCCESS))
                        },
                        {
                            handleError(it)
                            usersCard.onNext(PaymentsHelper(it.localizedMessage))
                        }))
    }

    private fun handleError(error: Throwable) = when (error) {
        is HttpException -> log_d(error.toString())
        is ServerLocalError -> log_d(error.toString())
        else -> log_d(error.localizedMessage)
    }

    fun setDisposableBag(disposableBag: DisposableContainer) {
        this.disposableBag = disposableBag
    }

    class ConfirmRentHelper(val msg: String?, val res: String?, val state: ProgressStateHelper) {
        constructor(state: ProgressStateHelper) : this(null, null, state)
        constructor(res: String, state: ProgressStateHelper) : this(null, res, state)
        constructor(msg: String) : this(msg, null, ERROR)
    }

    class PaymentsHelper(val msg: String?, val card: Array<BankCard>?, val state: ProgressStateHelper) {
        constructor(state: ProgressStateHelper) : this(null, null, state)
        constructor(card: Array<BankCard>, state: ProgressStateHelper) : this(null, card, state)
        constructor(msg: String) : this(msg, null, ERROR)
    }
}
