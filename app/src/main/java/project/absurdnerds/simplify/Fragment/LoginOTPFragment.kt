package project.absurdnerds.simplify.Fragment

import android.content.Intent
import android.graphics.Color
import androidx.lifecycle.ViewModelProvider
import android.os.Bundle
import android.text.SpannableStringBuilder
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.text.bold
import androidx.core.widget.addTextChangedListener
import cn.pedant.SweetAlert.SweetAlertDialog
import com.google.firebase.FirebaseException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.PhoneAuthCredential
import com.google.firebase.auth.PhoneAuthProvider
import kotlinx.android.synthetic.main.login_mobile_fragment.*
import kotlinx.android.synthetic.main.login_otp_fragment.*
import project.absurdnerds.simplify.FragmentChangeInterface
import project.absurdnerds.simplify.MainActivity
import project.absurdnerds.simplify.R
import project.absurdnerds.simplify.home.HomeActivity
import timber.log.Timber
import java.util.concurrent.TimeUnit

class LoginOTPFragment : Fragment() {

    companion object {
        fun newInstance() = LoginOTPFragment()
    }

    private lateinit var viewModel: LoginOTPViewModel
    private lateinit var fragmentChangeInterface: FragmentChangeInterface
    private var storedVerificationId: String? = null
    private var mobileNumber: String? = null
    private lateinit var firebaseAuth: FirebaseAuth

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.login_otp_fragment, container, false)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        init()

        otpButtonBack.setOnClickListener {
            fragmentChangeInterface.changeFragment(LoginMobileFragment())
        }

        buttonVerifyOtp.setOnClickListener {
            verifyOTP(getOtp())
        }

        buttonResendOtp.setOnClickListener {
            firebaseAuth()
        }
    }

    private fun init() {
        viewModel = ViewModelProvider(this).get(LoginOTPViewModel::class.java)

        fragmentChangeInterface = context as FragmentChangeInterface
        firebaseAuth = FirebaseAuth.getInstance()
        textChangeListener()

        var bundle = this.arguments
        if (bundle != null) {
            mobileNumber = bundle.get("mobileNumber").toString()
            storedVerificationId = bundle.get("storedVerificationId").toString()
        }

        val myCustomizedString = SpannableStringBuilder()
            .append("Enter the OTP sent to ")
            .bold{ append(mobileNumber) }

        tvOtpTitle.text = myCustomizedString

    }

    private fun firebaseAuth() {

        var sweetAlertDialog = SweetAlertDialog(context, SweetAlertDialog.PROGRESS_TYPE)
        sweetAlertDialog.progressHelper.barColor = Color.parseColor("#A5DC86");
        sweetAlertDialog.titleText = "Loading";
        sweetAlertDialog.setCancelable(false);
        sweetAlertDialog.show();

        val callbacks = object : PhoneAuthProvider.OnVerificationStateChangedCallbacks() {

            override fun onVerificationCompleted(credential: PhoneAuthCredential) {

                Timber.d("Phone Verified")

            }

            override fun onVerificationFailed(e: FirebaseException) {

                Timber.e(e.message.toString())
                sweetAlertDialog.cancel()

                SweetAlertDialog(context, SweetAlertDialog.ERROR_TYPE)
                    .setTitleText("Error Sending OTP")
                    .show()

            }

            override fun onCodeSent(
                verificationId: String,
                token: PhoneAuthProvider.ForceResendingToken
            ) {

                sweetAlertDialog.cancel()
                Timber.d("onCodeSent:$verificationId")
                storedVerificationId = verificationId


                var bundle = Bundle()
                bundle.putString("mobileNumber", mobileNumber)
                bundle.putString("storedVerificationId", storedVerificationId)

                var fragment = LoginOTPFragment()
                fragment.arguments = bundle
                fragmentChangeInterface.changeFragment(fragment)

            }

        }

        Timber.d(mobileNumber)

        PhoneAuthProvider.getInstance().verifyPhoneNumber(
            mobileNumber!!, // Phone number to verify
            120, // Timeout duration
            TimeUnit.SECONDS, // Unit of timeout
            activity!!, // Activity (for callback binding)
            callbacks
        ) // OnVerificationStateChangedCallbacks

    }

    private fun signInWithPhoneAuthCredential(credential: PhoneAuthCredential) {

        var sweetAlertDialog = SweetAlertDialog(context, SweetAlertDialog.PROGRESS_TYPE)
        sweetAlertDialog.progressHelper.barColor = Color.parseColor("#A5DC86");
        sweetAlertDialog.titleText = "Loading";
        sweetAlertDialog.setCancelable(false);
        sweetAlertDialog.show();

        firebaseAuth.signInWithCredential(credential)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    // Sign in success, update UI with the signed-in user's information
                    Timber.d("signInWithCredential:success")
                    startActivity(Intent(context, HomeActivity::class.java))
                    activity!!.finish()

                } else {

                    Timber.e(task.exception, "signInWithCredential:failure")
                    if (task.exception is FirebaseAuthInvalidCredentialsException) {
                        Timber.e(task.exception.toString())

                        SweetAlertDialog(context, SweetAlertDialog.ERROR_TYPE)
                            .setTitleText(task.exception.toString())
                            .show()

                        clearFields()
                    }
                }
                sweetAlertDialog.cancel()
            }
    }

    private fun verifyOTP(otp: String){

        if(storedVerificationId != null){
            val credential = PhoneAuthProvider.getCredential(storedVerificationId!!, otp)
            signInWithPhoneAuthCredential(credential)
        }
        else {
            SweetAlertDialog(context, SweetAlertDialog.ERROR_TYPE)
                .setTitleText("Something went wrong")
                .show()
        }
    }

    private fun getOtp(): String{

        return "${otp1.text}${otp2.text}${otp3.text}${otp4.text}${otp5.text}${otp6.text}"
    }

    private fun clearFields() {

        otp1.setText("")
        otp2.setText("")
        otp3.setText("")
        otp4.setText("")
        otp5.setText("")
        otp6.setText("")

        otp1.requestFocus()
    }

    private fun textChangeListener() {

        otp1.addTextChangedListener {
            if (otp1.text.toString().length == 1) {
                otp1.clearFocus()
                otp2.requestFocus()
            }
        }
        otp2.addTextChangedListener {
            if (otp2.text.toString().length == 1) {
                otp2.clearFocus()
                otp3.requestFocus()
            }
        }
        otp3.addTextChangedListener {
            if (otp3.text.toString().length == 1) {
                otp3.clearFocus()
                otp4.requestFocus()
            }
        }
        otp4.addTextChangedListener {
            if (otp4.text.toString().length == 1) {
                otp4.clearFocus()
                otp5.requestFocus()
            }
        }
        otp5.addTextChangedListener {
            if (otp5.text.toString().length == 1) {
                otp6.clearFocus()
                otp6.requestFocus()
            }
        }
        otp6.addTextChangedListener {
            if (otp6.text.toString().length+otp5.text.toString().length+otp4.text.toString().length+otp3.text.toString().length
                +otp2.text.toString().length+otp1.text.toString().length== 6) {
            }
        }
    }

}