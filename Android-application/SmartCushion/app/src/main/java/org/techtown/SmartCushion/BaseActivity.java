package org.techtown.SmartCushion;

import android.content.Intent;
import android.support.v4.app.FragmentActivity;
import android.util.Log;

/**
 * @author leoshin, created at 15. 7. 20..
 */
public abstract class BaseActivity extends FragmentActivity {
    /*
    protected void showWaitingDialog() { WaitingDialog.showWaitingDialog(this); }
    protected void cancelWaitingDialog() { WaitingDialog.cancelWaitingDialog(); }
    */
    protected void redirectLoginActivity() {
        final Intent intent = new Intent(this, LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);
    }

    protected void redirectSignupActivity() {
        Log.d("TEST", "redirectSignup-base");
        final Intent intent = new Intent(this, SignupActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);
    }
}
