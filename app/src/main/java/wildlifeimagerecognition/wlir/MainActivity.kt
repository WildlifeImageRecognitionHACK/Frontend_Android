package wildlifeimagerecognition.wlir

import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.view.Menu
import android.view.MenuItem
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import android.graphics.BitmapFactory
import android.graphics.drawable.Drawable
import android.support.v4.graphics.drawable.RoundedBitmapDrawableFactory
import kotlinx.coroutines.experimental.CommonPool
import kotlinx.coroutines.experimental.android.UI
import kotlinx.coroutines.experimental.async
import java.net.URL


class MainActivity : AppCompatActivity() {
    companion object {
        var googleAccount: GoogleSignInAccount? = null
        var profilePic: Drawable? = null
        const val signInResult = 0
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_main, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onPrepareOptionsMenu(menu: Menu?): Boolean {
        val isSignedIn = signedIn(googleAccount)

        // Show/hide menu elements based on sign in status
        menu?.findItem(R.id.action_sign_in)?.isVisible = !isSignedIn
        menu?.findItem(R.id.action_sign_out)?.isVisible = isSignedIn

        val profileItem = menu?.findItem(R.id.action_profile_pic)
        profileItem?.let {
            profileItem.isVisible = false
            if (isSignedIn) {
                if (profilePic == null) {
                    getProfilePicAsync()
                } else {
                    profileItem.icon = profilePic
                    profileItem.isVisible = true
                }
            }
        }
        return true
    }

    private fun signedIn(account: GoogleSignInAccount?) = account?.idToken != null

    override fun onResume() {
        super.onResume()
        setContentView(R.layout.activity_main)

        // Check for existing Google Sign In account, if the user is already signed in
        // the GoogleSignInAccount and token will be non-null.
        googleAccount = GoogleSignIn.getLastSignedInAccount(this)
        if(googleAccount?.idToken == null) {
            promptLogin()
        }
    }

    private fun promptLogin() {
        startActivityForResult(Intent(this, SignInActivity::class.java), signInResult)
    }

    private fun getProfilePicAsync() = async(UI){
        val connection = URL(googleAccount!!.photoUrl.toString()).openConnection()

        try {
            val job = async(CommonPool) {
                connection.connect()
                val input = connection.inputStream
                val bitmap = BitmapFactory.decodeStream(input)
                val drawable =
                    RoundedBitmapDrawableFactory.create(resources, bitmap)
                drawable.cornerRadius = Math.max(bitmap.width, bitmap.height) / 1.0f
                profilePic = drawable
            }
            job.await()
        } catch (exception: Exception){
            print(exception)
            // TODO: need a placeholder
        }
        invalidateOptionsMenu()
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        when(item?.itemId){
            R.id.action_sign_in -> {
                promptLogin()
            }

            R.id.action_sign_out -> {
                try {
                        GoogleSignIn.getClient(this, GoogleSignInOptions.DEFAULT_SIGN_IN)
                                .signOut().addOnCompleteListener {
                                    googleAccount = GoogleSignIn.getLastSignedInAccount(this)
                                    profilePic = null
                                    promptLogin()
                    }
                } catch (exception: Exception){
                    print("Error signing out")
                }
            }
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if(requestCode == signInResult){
            invalidateOptionsMenu()
        }
    }
}
