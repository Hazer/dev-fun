package com.nextfaze.devfun.demo.test;

import android.app.Activity;
import androidx.appcompat.app.AlertDialog;
import com.nextfaze.devfun.demo.inject.ActivityScope;
import com.nextfaze.devfun.function.DeveloperFunction;

import javax.inject.Inject;

@ActivityScope
@TestCat
class InjectablePackagePrivateActivityViaAnnotation {

    @Inject
    InjectablePackagePrivateActivityViaAnnotation() {
    }

    @DeveloperFunction
    public void validateSelf(Activity activity, InjectablePackagePrivateActivityViaAnnotation self) {
        new AlertDialog.Builder(activity)
                .setMessage("this=" + this + "(" + activity + ")\nself=" + self + "\nthis===self: " + (this == self))
                .show();
    }
}
