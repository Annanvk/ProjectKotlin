package io.tage.android.app.modules.main.profile.name


import android.os.Bundle
import io.reactivex.internal.disposables.DisposableContainer
import io.tage.android.app.R
import io.tage.android.app.common.*
import io.tage.android.app.modules.main.MainActivity
import io.tage.android.app.modules.main.profile.MyProfileFragment
import kotlinx.android.synthetic.main.fragment_profile_name.*


class ProfileNameFragment : BaseFragment() {


    var viewModel = NameViewModel()
    lateinit var editName: String

    override val layout: Int
        get() = R.layout.fragment_profile_name
    override val title: Int
        get() = R.string.profile

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        editName = arguments!!.getString(MyProfileFragment.EDIT_NAME)
    }

    override fun setUi() {
        activity<MainActivity> { it ->
            it.enableBackBtn(true)
        }
        edit_text_name.setText(editName)
    }

    private fun checkName(): Boolean {
        if (edit_text_name.text.toString().isEmpty()) {
            toast(getString(R.string.enter_name))
            edit_text_name.requestFocus()
        } else return true
        return false
    }

    override fun setObservers(disposableBag: DisposableContainer) {
        button_update_name.setOnClickListener {
            hideKeyboard(edit_text_name)
            if (checkName() && !edit_text_name.text.toString().equals(editName)) {
                viewModel.updateName(edit_text_name.text.toString(), null, null)
                log_i(edit_text_name.text.toString())
            } else if (checkName() && edit_text_name.text.toString().equals(editName)) {
                activity<MainActivity> { it.onBackPressed() }
            }
        }
        disposableBag.add(
                viewModel.updatingName.subscribe {
                    when (it.state) {
                        ProgressStateHelper.START -> {
                            activity<MainActivity> { it.globalProgress(true) }
//                            button_update_name.isEnabled = false
//                            edit_text_name.isEnabled = false
                        }
                        ProgressStateHelper.SUCCESS -> {
                            activity<MainActivity> { it.globalProgress(false) }
                            log_i(it.toString())
                            toast(getString(R.string.name_updated))
                            activity<MainActivity> {
                                it.onBackPressed()
                            }
                        }
                        ProgressStateHelper.ERROR -> {
                            activity<MainActivity> { it.globalProgress(false) }
                            showErrorDialog(getString(R.string.error), { viewModel.updateName(edit_text_name.text.toString(), null, null) }, {})
                        }
                    }
                }
        )
    }

    override fun onPause() {
        super.onPause()
        hideKeyboard(edit_text_name)
    }
}
