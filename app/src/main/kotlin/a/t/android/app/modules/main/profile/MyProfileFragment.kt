package io.tage.android.app.modules.main.profile

import android.net.Uri
import android.os.Bundle
import android.view.ContextMenu
import android.view.MenuItem
import android.view.View
import androidx.navigation.fragment.findNavController
import io.reactivex.internal.disposables.DisposableContainer
import io.tage.android.app.R
import io.tage.android.app.common.*
import io.tage.android.app.common.ProgressStateHelper.*
import io.tage.android.app.modules.main.MainActivity
import io.tage.android.app.services.Services
import kotlinx.android.synthetic.main.fragment_my_profile.*
import org.jetbrains.anko.sdk25.coroutines.onClick


class MyProfileFragment : ImageHelperBaseFragment() {


    companion object {
        val EDIT_NAME = "edit_name"
        val EDIT_EMAIL = "edit_email"
    }

    val viewModel = MyProfileViewModel()
    lateinit var photoURI: Uri
    var path: String? = null

    override val layout: Int
        get() = R.layout.fragment_my_profile
    override val title: Int
        get() = R.string.profile

    override fun setUi() {
        activity<MainActivity> { it.enableBackBtn(false) }
    }

    override fun setObservers(disposableBag: DisposableContainer) {
        layout_profile_log_out.onClick {
            Services.logout()
            activity<MainActivity> { it.onBackPressed() }
        }

        layout_payments.onClick {
            findNavController().navigate(R.id.action_myProfileFragment_to_profilePaymentsFragment)
        }
        layout_profile_name.onClick {
            val bundle = Bundle()
            bundle.putString(EDIT_NAME, text_view_name.text.toString())
            findNavController().navigate(R.id.action_myProfileFragment_to_profileNameFragment, bundle)

        }
        button_enter_phone_number.onClick {
            findNavController().navigate(R.id.action_myProfileFragment_to_profilePhoneFragment)
        }
        layout_profile_email.onClick {
            val bundle = Bundle()
            bundle.putString(EDIT_EMAIL, text_view_email.text.toString())
            findNavController().navigate(R.id.action_myProfileFragment_to_profileEmailFragment, bundle)

        }
        button_edit_photo.setOnClickListener {
            it.setOnCreateContextMenuListener(this)
            it.showContextMenu()
        }
        disposableBag.add(
                viewModel.uploadingPhotoMyProfile.subscribe {
                    when (it.state) {
                        START -> {
                            image_for_profile.gone()
                            progress_photo.visible()
                        }
                        SUCCESS -> {
                            image_for_profile.visible()
                            path = it.progressInfo!!.var1
                            viewModel.updatePhoto(path)
                        }
                        ERROR -> {
                            showErrorDialog(it.msg!!, { viewModel.uploadPhotoMyProfile(photoURI, context!!) }, {
                                progress_photo.invisible()
                                image_for_profile.visible()
                            })
                        }
                    }
                }
        )
        disposableBag.add(
                viewModel.updatingPhoto.subscribe {
                    when (it.state) {
                        START -> {
                            image_for_profile.gone()
                            progress_photo.visible()
                        }
                        SUCCESS -> {
                            image_for_profile.visible()
                            image_for_profile.loadCircle(it.profile?.photo!!, R.drawable.avatar, progress_photo)
                        }
                        ERROR -> {
                            showErrorDialog(it.msg!!, { viewModel.updatePhoto(path) }, {})
                        }
                    }
                }
        )

        disposableBag.add(
                viewModel.usersData.subscribe {
                    when (it.state) {
                        ProgressStateHelper.START -> {
                            activity<MainActivity> { it.globalProgress(true) }
                            image_for_profile.isEnabled = false
                            button_edit_photo.isEnabled = false
                        }
                        ProgressStateHelper.SUCCESS -> {
                            activity<MainActivity> { it.globalProgress(false) }
                            button_edit_photo.isEnabled = true
                            image_for_profile.isEnabled = true
                            layout_my_profile.visible()
                            log_i(it.toString())
                            if (it.profile?.name.isNullOrEmpty()) text_view_name.text = "-"
                            else text_view_name.text = it.profile?.name

                            text_view_phone_number.text = it.profile?.phone
                            if (it.profile?.email.isNullOrEmpty()) text_view_email.text = "-"
                            else text_view_email.text = it.profile?.email

                            if (!it.profile?.photo.isNullOrEmpty()) {
                                val params = image_for_profile.layoutParams
                                image_for_profile.loadCircle(it.profile?.photo!!, R.drawable.avatar, progress_photo)
                            }
                            when (it.profile?.emailVerified) {
                                true -> {
                                    text_view_email_varified.text = getString(R.string.verified)
                                    text_view_email_varified.setTextColor(resources.getColor(R.color.verified))
                                }
                                else -> {
                                    text_view_email_varified.text = getString(R.string.not_verified)
                                }
                            }
                        }
                        ProgressStateHelper.ERROR -> {
                            activity<MainActivity> { it.globalProgress(false) }
                            showErrorDialog(getString(R.string.error), {
                                viewModel.getProfileData()
                                layout_my_profile.gone()
                            })
                        }
                    }
                }
        )
        viewModel.getProfileData()
        layout_my_profile.gone()
    }

    override fun startPreparingImage() {
        image_for_profile.gone()
    }

    override fun onImagePreparedSuccess(photoURI: Uri) {
        this.photoURI = photoURI
        viewModel.uploadPhotoMyProfile(photoURI, context!!)
    }

    override fun onCreateContextMenu(menu: ContextMenu?, v: View?, menuInfo: ContextMenu.ContextMenuInfo?) {
        super.onCreateContextMenu(menu, v, menuInfo)
        menu?.add(0, CAMERA, 0, R.string.camera)
        menu?.add(0, GALLERY, 0, R.string.gallery)
    }

    override fun onContextItemSelected(item: MenuItem?): Boolean {
        when (item?.itemId) {
            CAMERA -> onImageCaptureClicked()
            GALLERY -> onImageGalleryClicked()
        }
        return super.onContextItemSelected(item)
    }
}