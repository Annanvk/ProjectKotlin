package io.tage.android.app.modules.main.profile.name

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

class NameViewModel {

    var disposable : Disposable? = null

    var updatingName = PublishSubject.create<MyProfileViewModel.MyProfileHelper>()

    fun updateName(name: String, email: String?, photo: String?){
        updatingName.onNext(MyProfileViewModel.MyProfileHelper(START))
        disposable = server.updateProfile(name, email, photo)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                        {updatingName.onNext(MyProfileViewModel.MyProfileHelper( it, SUCCESS))},
                        {updatingName.onNext(MyProfileViewModel.MyProfileHelper(ERROR))
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