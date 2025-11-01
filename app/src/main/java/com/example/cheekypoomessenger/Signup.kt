package com.example.cheekypoomessenger

import android.content.Intent
import android.os.Bundle
import android.provider.ContactsContract.CommonDataKinds.Email
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import java.util.jar.Attributes.Name

class Signup : AppCompatActivity() {

    private lateinit var edtName : EditText;

    private lateinit var edtEmail: EditText;
    private lateinit var edtPassword: EditText;
    private lateinit var btnSignUp: Button;


    private lateinit var mAuth: FirebaseAuth;
    private lateinit var mDbRef: DatabaseReference

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_signup)
        supportActionBar?.hide()



        mAuth = FirebaseAuth.getInstance()

        edtName = findViewById(R.id.edt_Name);
        edtEmail = findViewById(R.id.edt_email);
        edtPassword = findViewById(R.id.edt_password);
        btnSignUp = findViewById(R.id.btn_sign_up);

        btnSignUp.setOnClickListener{
            val name = edtName.text.toString();
            val email = edtEmail.text.toString();
            val password = edtPassword.text.toString();

            signUp(name,email,password);

        }
    }

    private fun signUp(name: String, email: String, password:String){
        mAuth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {

                    addUserTodatabase(name, email, mAuth.currentUser?.uid!!)
                    val intent  = Intent(this,MainActivity::class.java)
                    finish()
                    startActivity(intent)

                } else {
                    Toast.makeText(this,"Some error occured",Toast.LENGTH_SHORT).show()

                }
            }
    }

    private fun addUserTodatabase(name: String, email: String, uid: String){
        mDbRef = FirebaseDatabase.getInstance().getReference()

        mDbRef.child("user").child(uid).setValue(User(name,email,uid))


    }
}