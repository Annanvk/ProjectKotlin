package io.tage.android.app.modules.main.profile.email

import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import io.reactivex.subjects.PublishSubject
import io.tage.android.app.common.log_d
import io.tage.android.app.modules.main.profile.MyProfileViewModel
import io.tage.android.app.services.ServerLocalError
import io.tage.android.app.services.server
import retrofit2.HttpException
import io.tage.android.app.common.ProgressStateHelper.*

class EmailViewModel {

    var disposable: Disposable? = null
    val updatingEmail = PublishSubject.create<MyProfileViewModel.MyProfileHelper>()

    fun updateEmail(name: String?, email: String, photo: String?){
        updatingEmail.onNext(MyProfileViewModel.MyProfileHelper(START))
        disposable = server.updateProfile(name, email, photo)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                        {updatingEmail.onNext(MyProfileViewModel.MyProfileHelper(SUCCESS))},
                        {updatingEmail.onNext(MyProfileViewModel.MyProfileHelper(ERROR))
                            handleError(it)})
    }

    private fun handleError(error: Throwable) = when (error) {
        is HttpException -> log_d(error.toString())
        is ServerLocalError -> log_d(error.toString())
        else -> log_d(error.localizedMessage)
    }

    fun dispose(){
        disposable?.dispose()
    }
}