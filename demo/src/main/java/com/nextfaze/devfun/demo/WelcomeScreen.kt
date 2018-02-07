package com.nextfaze.devfun.demo

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.nextfaze.devfun.annotations.DeveloperFunction
import com.nextfaze.devfun.demo.inject.FragmentInjector
import com.nextfaze.devfun.demo.kotlin.enabled
import com.nextfaze.devfun.demo.kotlin.findOrCreate
import com.nextfaze.devfun.demo.kotlin.startActivity
import com.nextfaze.devfun.demo.util.value
import com.uber.autodispose.android.lifecycle.scope
import com.uber.autodispose.kotlin.autoDisposable
import kotlinx.android.synthetic.main.welcome_layout.*
import javax.inject.Inject

class WelcomeActivity : BaseActivity() {
    companion object {
        fun start(context: Context) = context.startActivity<WelcomeActivity>()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.fragment_activity)

        if (savedInstanceState == null) {
            supportFragmentManager.beginTransaction().replace(R.id.activity_fragment_view, findOrCreate { WelcomeFragment() }).commit()
        }
    }
}

class WelcomeFragment : BaseFragment() {
    @Inject lateinit var config: Config

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        retainInstance = true
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View =
        inflater.inflate(R.layout.welcome_layout, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        signInButton.apply {
            enabled = config.signInEnabled
            setOnClickListener { AuthenticateActivity.start(activity!!) }
        }
        createAccountButton.apply {
            enabled = config.registrationEnabled
            setOnClickListener { RegisterActivity.start(activity!!) }
        }
        motdText.text = config.welcomeString
    }

    override fun onStart() {
        super.onStart()
        config.signInEnabled().observable.autoDisposable(scope()).subscribe { signInButton.isEnabled = it }
        config.registrationEnabled().observable.autoDisposable(scope()).subscribe { createAccountButton.isEnabled = it }
        config.welcomeString().observable.autoDisposable(scope()).subscribe { motdText.text = it.value }
    }

    @DeveloperFunction
    private fun enableSignInButton() {
        signInButton.enabled = true
    }

    @DeveloperFunction
    private fun enableCreateAccountButton() {
        createAccountButton.enabled = true
    }

    override fun inject(injector: FragmentInjector) = injector.inject(this)
}
