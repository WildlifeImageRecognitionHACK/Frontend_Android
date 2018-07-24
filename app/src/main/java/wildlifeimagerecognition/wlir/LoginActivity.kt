package wildlifeimagerecognition.wlir

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.SignInButton
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.tasks.Task
import android.content.Intent
import com.google.android.gms.common.api.ApiException


class LoginActivity : AppCompatActivity() {
    companion object {
        const val googleSignInCode = 0
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        googleSignInSetup()
    }

    private fun googleSignInSetup() {
        // Configure sign-in to request the user's ID, email address, and basic
        // profile. ID and basic profile are included in DEFAULT_SIGN_IN.
        val googleSignIn = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestEmail()
                .build()
        val googleSignInClient = GoogleSignIn.getClient(this, googleSignIn)

        val signInButton = findViewById<SignInButton>(R.id.google_sign_in_button)
        signInButton.setSize(SignInButton.SIZE_WIDE)

        findViewById<SignInButton>(R.id.google_sign_in_button).setOnClickListener {
            startActivityForResult(googleSignInClient.signInIntent, googleSignInCode)
        }
    }

    public override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent) {
        super.onActivityResult(requestCode, resultCode, data)

        // Result returned from launching the Intent from GoogleSignInClient.getSignInIntent(...);
        if (requestCode == googleSignInCode) {
            // The Task returned from this call is always completed, no need to attach
            // a listener.
            try {
                val googleAccount =
                        GoogleSignIn.getSignedInAccountFromIntent(data).getResult(ApiException::class.java)
                print("Hi")
            } catch (exception: ApiException){
                print("Bye")
            }
        }
    }
}