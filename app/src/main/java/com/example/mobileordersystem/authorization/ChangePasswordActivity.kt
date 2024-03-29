package com.example.mobileordersystem.authorization

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import android.view.View
import android.widget.EditText
import android.widget.ProgressBar
import android.widget.Toast
import com.example.mobileordersystem.HomeActivity
import com.example.mobileordersystem.R
import com.google.firebase.auth.EmailAuthProvider
import com.google.firebase.auth.FirebaseAuth

class ChangePasswordActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.change_password)
    }
    fun savePassword(view: View) {
        findViewById<ProgressBar>(R.id.progressBar).visibility=View.VISIBLE
        val newPassword = findViewById<EditText>(R.id.newPasswordInput).text.toString()
        val oldPassword = findViewById<EditText>(R.id.oldPasswordInput).text.toString()
        val user = FirebaseAuth.getInstance().currentUser
        lateinit var email: String
        user?.let {
            for (profile in it.providerData) {
                email = profile.email.toString()
            }
        }
        val credential = EmailAuthProvider
            .getCredential(email, oldPassword)
        user?.reauthenticate(credential)
            ?.addOnCompleteListener {
                user.updatePassword(newPassword)
                    .addOnCompleteListener { task ->
                        if (task.isSuccessful) {
                            Toast.makeText(this, R.string.passwordChangeSucces, Toast.LENGTH_LONG).show();
//                            TODO:: wyloguj użytkownika i do strony logowania
                            val intent = Intent(this@ChangePasswordActivity, HomeActivity::class.java)
                            startActivity(intent)
                            finish()
                        } else {
                            findViewById<ProgressBar>(R.id.progressBar).visibility=View.GONE
                            Toast.makeText(this, R.string.passwordChangeFail, Toast.LENGTH_LONG).show();
                        }
                    }
            }

    }

}