package io.tage.android.app.modules.main.profile

import android.content.Context
import android.net.Uri
import android.provider.MediaStore
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import io.reactivex.subjects.PublishSubject
import io.tage.android.app.common.Profile
import io.tage.android.app.common.ProgressInfo
import io.tage.android.app.common.ProgressStateHelper
import io.tage.android.app.common.log_d
import io.tage.android.app.services.ServerLocalError
import io.tage.android.app.services.server
import retrofit2.HttpException
import io.tage.android.app.common.ProgressStateHelper.*
import java.net.UnknownHostException

class MyProfileViewModel {

    var disposable: Disposable? = null
    val usersData = PublishSubject.create<MyProfileHelper>()
    val uploadingPhotoMyProfile = PublishSubject.create<UploadImageHelper>()
    val updatingPhoto = PublishSubject.create<MyProfileHelper>()

    fun getProfileData(){
        usersData.onNext(MyProfileHelper(START))
        disposable = server.getProfile()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                        {
                            usersData.onNext(MyProfileHelper(it, SUCCESS))},
                        {usersData.onNext(MyProfileHelper(ERROR))
                            handleError(it)})
    }

    fun updatePhoto(photo: String?){
        updatingPhoto.onNext(MyProfileViewModel.MyProfileHelper(START))
        disposable = server.updateProfile(null, null,  photo)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                        {updatingPhoto.onNext(MyProfileViewModel.MyProfileHelper(it, SUCCESS))},
                        {updatingPhoto.onNext(MyProfileViewModel.MyProfileHelper(it.localizedMessage))
                            handleError(it)})
    }

    fun uploadPhotoMyProfile(photoUri: Uri, context: Context){
        uploadingPhotoMyProfile.onNext(UploadImageHelper(START))
        val btmp = MediaStore.Images.Media.getBitmap(context.contentResolver, photoUri)
            disposable = server.uploadProfilePhoto(btmp)
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe({
                        if(it.complited) uploadingPhotoMyProfile.onNext(UploadImageHelper(it, SUCCESS))
                        else uploadingPhotoMyProfile.onNext(UploadImageHelper(it, IN_PROCESS))
                    },
                            {
                                handleError(it)
                                uploadingPhotoMyProfile.onNext(UploadImageHelper(it.localizedMessage))
                            })
    }

    private fun handleError(error: Throwable) = when (error) {
        is HttpException -> log_d(error.toString())
        is ServerLocalError -> log_d(error.toString())
        is UnknownHostException -> log_d(error.toString())
        else -> log_d(error.localizedMessage)
    }

    fun dispose(){
        disposable!!.dispose()
    }

    class UploadImageHelper(val msg: String?, val progressInfo: ProgressInfo<String>?, val state: ProgressStateHelper) {
        constructor(state: ProgressStateHelper) : this(null, null, state)
        constructor(progressInfo: ProgressInfo<String>, state: ProgressStateHelper) : this(null, progressInfo, state)
        constructor(msg: String): this(msg, null, ERROR)
    }

   class MyProfileHelper(val msg: String?, val profile: Profile?, val state: ProgressStateHelper){
       constructor(state: ProgressStateHelper):this( null,null, state)
       constructor(profile: Profile, state: ProgressStateHelper): this(null, profile, state)
       constructor(msg: String): this(msg, null, ERROR)
   }
}