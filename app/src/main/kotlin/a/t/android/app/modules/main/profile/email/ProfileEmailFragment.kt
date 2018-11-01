package io.tage.android.app.modules.main.profile.email


import android.os.Bundle
import io.reactivex.internal.disposables.DisposableContainer
import io.tage.android.app.R
import io.tage.android.app.common.*
import io.tage.android.app.modules.main.MainActivity
import io.tage.android.app.modules.main.profile.MyProfileFragment
import kotlinx.android.synthetic.main.fragment_profile_email.*
import org.jetbrains.anko.sdk25.coroutines.onClick


class ProfileEmailFragment : BaseFragment() {

    var viewModel = EmailViewModel()
    lateinit var editEmail: String
    override val layout: Int
        get() = R.layout.fragment_profile_email
    override val title: Int
        get() = R.string.profile

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        editEmail = arguments!!.getString(MyProfileFragment.EDIT_EMAIL)
    }
    override fun setUi() {
        activity<MainActivity> {
            it.enableBackBtn(true)
        }
        edit_text_email.setText(editEmail)
    }

    private fun checkEmail(): Boolean {
        if (edit_text_email.text.toString().isEmpty()) {
            toast(getString(R.string.enter_email))
            edit_text_email.requestFocus()
        } else return true
        return false
    }

    override fun onPause() {
        super.onPause()
        hideKeyboard((edit_text_email))
    }

    override fun setObservers(disposableBag: DisposableContainer) {
        button_update_email.onClick {
            hideKeyboard(edit_text_email)
            if (checkEmail() && !edit_text_email.text.toString().equals(editEmail)) {
                viewModel.updateEmail(null, edit_text_email.text.toString(), null)
                log_i(edit_text_email.text.toString())
            }else if(checkEmail() && edit_text_email.text.toString().equals(editEmail)){
                activity<MainActivity> { it.onBackPressed() }
            }
        }

        disposableBag.add(
                viewModel.updatingEmail.subscribe {
                    when (it.state) {
                        ProgressStateHelper.START -> {
                            activity<MainActivity> { it.globalProgress(true) }
//                            edit_text_email.isEnabled = false
//                            button_update_email.isEnabled = false
                        }
                        ProgressStateHelper.SUCCESS -> {
                            activity<MainActivity> { it.globalProgress(false) }
                            log_i(it.toString())
                            toast(getString(R.string.email_updated))
                            activity<MainActivity> {
                                it.onBackPressed()
                            }
                        }
                        ProgressStateHelper.ERROR -> {
                            activity<MainActivity> { it.globalProgress(false) }
                            showErrorDialog(getString(R.string.error), { viewModel.updateEmail(null, edit_text_email.text.toString(), null) }, {})
                        }
                    }
                }
        )
    }
}