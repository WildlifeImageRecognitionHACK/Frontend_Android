package wildlifeimagerecognition.wlir

import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount



class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onResume() {
        super.onResume()
        setContentView(R.layout.activity_main)

        // Check for existing Google Sign In account, if the user is already signed in
        // the GoogleSignInAccount will be non-null.
        val googleAccount = GoogleSignIn.getLastSignedInAccount(this)
        val alreadySignedIn = googleAccount != null && googleAccount.idToken != null
        if(!alreadySignedIn) {
            startActivity(Intent(this, LoginActivity::class.java))
        }
    }
}
