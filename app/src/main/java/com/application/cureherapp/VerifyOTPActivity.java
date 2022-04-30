package com.application.cureherapp;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.text.InputType;
import android.text.TextUtils;
import android.view.View;
import android.view.WindowManager;
import android.widget.EditText;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.application.cureherapp.Helper.LoadingDialog;
import com.application.cureherapp.Utilities.Common;
import com.application.cureherapp.Utilities.Constants;
import com.application.cureherapp.Utilities.PreferenceManager;
import com.application.cureherapp.databinding.ActivityVerifyOtpBinding;
import com.blogspot.atifsoftwares.animatoolib.Animatoo;
import com.daimajia.androidanimations.library.Techniques;
import com.daimajia.androidanimations.library.YoYo;
import com.google.firebase.FirebaseException;
import com.google.firebase.FirebaseTooManyRequestsException;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthOptions;
import com.google.firebase.auth.PhoneAuthProvider;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.tapadoo.alerter.Alerter;

import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.TimeUnit;

public class VerifyOTPActivity extends AppCompatActivity {

    private ActivityVerifyOtpBinding binding;

    private FirebaseAuth firebaseAuth;
    private CollectionReference patientsRef;
    private PhoneAuthProvider.ForceResendingToken resendingToken;
    private PhoneAuthProvider.OnVerificationStateChangedCallbacks mCallbacks;

    private String mobile;
    private String codeBySystem;
    private Timer timer;
    private int count = 60;

    private PreferenceManager preferenceManager;
    private LoadingDialog loadingDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityVerifyOtpBinding.inflate(getLayoutInflater());
        View view = binding.getRoot();
        setContentView(view);

        getWindow().addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
        getWindow().setStatusBarColor(getColor(R.color.colorBackground));
        getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);

        preferenceManager = new PreferenceManager(getApplicationContext());
        loadingDialog = new LoadingDialog(VerifyOTPActivity.this);

        if (preferenceManager.getBoolean(Constants.KEY_IS_SIGNED_IN_AS_PATIENT)) {
            startActivity(new Intent(getApplicationContext(), HomeActivity.class));
            Animatoo.animateSlideLeft(VerifyOTPActivity.this);
            finish();
        } else if (preferenceManager.getBoolean(Constants.KEY_IS_SIGNED_IN_AS_DOCTOR)) {
            startActivity(new Intent(getApplicationContext(), DoctorProfileActivity.class));
            Animatoo.animateSlideLeft(VerifyOTPActivity.this);
            finish();
        }

        firebaseAuth = FirebaseAuth.getInstance();
        patientsRef = FirebaseFirestore.getInstance().collection(Constants.KEY_COLLECTION_PATIENTS);

        Intent intent = getIntent();
        mobile = intent.getStringExtra("patient_mobile");

        binding.closeBtn.setOnClickListener(view1 -> onBackPressed());

        binding.subtitle.setText(String.format("sent to your mobile %s", mobile));

        binding.otpInput.setInputType(InputType.TYPE_NULL);

        mCallbacks = new PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
            @Override
            public void onCodeSent(@NonNull String s, @NonNull PhoneAuthProvider.ForceResendingToken forceResendingToken) {
                super.onCodeSent(s, forceResendingToken);
                resendingToken = forceResendingToken;
                codeBySystem = s;
            }

            @Override
            public void onVerificationCompleted(@NonNull PhoneAuthCredential phoneAuthCredential) {
                String code = phoneAuthCredential.getSmsCode();
                if (code != null) {
                    binding.otpInput.setText(code);
                    verifyCode(code);
                }
            }

            @Override
            public void onVerificationFailed(@NonNull FirebaseException e) {
                if (e instanceof FirebaseAuthInvalidCredentialsException) {
                    loadingDialog.dismissDialog();

                    Alerter.create(VerifyOTPActivity.this)
                            .setText("Whoa! It seems you've got an invalid code!")
                            .setTextAppearance(R.style.AlertText)
                            .setBackgroundColorRes(R.color.errorColor)
                            .setIcon(R.drawable.ic_alert_invalid)
                            .setDuration(2500)
                            .disableOutsideTouch()
                            .show();
                } else if (e instanceof FirebaseTooManyRequestsException) {
                    loadingDialog.dismissDialog();

                    Alerter.create(VerifyOTPActivity.this)
                            .setText("Too many OTP requests at the moment. Try again after 5 hours!")
                            .setTextAppearance(R.style.AlertText)
                            .setBackgroundColorRes(R.color.infoColor)
                            .setIcon(R.drawable.ic_alert_info)
                            .setDuration(2500)
                            .disableOutsideTouch()
                            .show();
                }
            }
        };

        sendVerificationCodeToUser(mobile);

        binding.input1.setOnClickListener(view13 -> inputDigit(binding.otpInput, "1"));
        binding.input2.setOnClickListener(view13 -> inputDigit(binding.otpInput, "2"));
        binding.input3.setOnClickListener(view13 -> inputDigit(binding.otpInput, "3"));
        binding.input4.setOnClickListener(view13 -> inputDigit(binding.otpInput, "4"));
        binding.input5.setOnClickListener(view13 -> inputDigit(binding.otpInput, "5"));
        binding.input6.setOnClickListener(view13 -> inputDigit(binding.otpInput, "6"));
        binding.input7.setOnClickListener(view13 -> inputDigit(binding.otpInput, "7"));
        binding.input8.setOnClickListener(view13 -> inputDigit(binding.otpInput, "8"));
        binding.input9.setOnClickListener(view13 -> inputDigit(binding.otpInput, "9"));
        binding.input0.setOnClickListener(view13 -> inputDigit(binding.otpInput, "0"));

        binding.deleteInput.setOnClickListener(view14 -> {
            String text = binding.otpInput.getText().toString().trim();
            if (!TextUtils.isEmpty(text)) {
                binding.otpInput.setText(text.substring(0, text.length() - 1));
            }
        });

        timer = new Timer();
        timer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                VerifyOTPActivity.this.runOnUiThread(() -> {
                    if (count == 0) {
                        binding.resendOtpBtn.setText("Resend OTP");
                        binding.resendOtpBtn.setTextColor(getColor(R.color.colorPrimary));
                        binding.resendOtpBtn.setEnabled(true);
                        binding.resendOtpBtn.setOnClickListener(v -> {
                            if (!Common.isConnectedToInternet(VerifyOTPActivity.this)) {
                                Common.openNoInternetBottomSheet(VerifyOTPActivity.this);
                            } else {
                                resendOTP();
                                binding.resendOtpBtn.setTextColor(getColor(R.color.colorInactive));
                                binding.resendOtpBtn.setEnabled(false);
                                count = 60;
                            }
                        });
                    } else {
                        binding.resendOtpBtn.setText(String.format("Resend OTP in %d", count));
                        binding.resendOtpBtn.setTextColor(getColor(R.color.colorInactive));
                        binding.resendOtpBtn.setEnabled(false);
                        count--;
                    }
                });
            }
        }, 0, 1000);

        binding.verifyBtn.setOnClickListener(view12 -> {
            if (!Common.isConnectedToInternet(VerifyOTPActivity.this)) {
                Common.openNoInternetBottomSheet(VerifyOTPActivity.this);
            } else {
                if (String.valueOf(binding.otpInput.getText()).isEmpty() || String.valueOf(binding.otpInput.getText()).length() != 6) {
                    loadingDialog.dismissDialog();

                    YoYo.with(Techniques.Shake).duration(700).repeat(0).playOn(binding.otpInput);
                    Alerter.create(VerifyOTPActivity.this)
                            .setText("Enter the valid code received on " + mobile + "!")
                            .setTextAppearance(R.style.AlertText)
                            .setBackgroundColorRes(R.color.errorColor)
                            .setIcon(R.drawable.ic_alert_error)
                            .setDuration(2500)
                            .disableOutsideTouch()
                            .show();
                } else {
                    verifyCode(String.valueOf(binding.otpInput.getText()));
                }
            }
        });
    }

    private void sendVerificationCodeToUser(String mobile) {
        PhoneAuthOptions options =
                PhoneAuthOptions.newBuilder(firebaseAuth)
                        .setPhoneNumber(mobile)                            // Phone number to verify
                        .setTimeout(60L, TimeUnit.SECONDS)        // Timeout and unit
                        .setActivity(this)                                 // Activity (for callback binding)
                        .setCallbacks(mCallbacks)                          // OnVerificationStateChangedCallbacks
                        .build();
        PhoneAuthProvider.verifyPhoneNumber(options);
    }

    private void resendOTP() {
        PhoneAuthOptions options =
                PhoneAuthOptions.newBuilder(firebaseAuth)
                        .setPhoneNumber(mobile)                            // Phone number to verify
                        .setTimeout(60L, TimeUnit.SECONDS)        // Timeout and unit
                        .setActivity(this)                                 // Activity (for callback binding)
                        .setCallbacks(mCallbacks)                          // OnVerificationStateChangedCallbacks
                        .setForceResendingToken(resendingToken)
                        .build();
        PhoneAuthProvider.verifyPhoneNumber(options);
    }

    private void verifyCode(String code) {
        loadingDialog.startDialog();

        PhoneAuthCredential credential = PhoneAuthProvider.getCredential(codeBySystem, code);
        signInWithPhoneAuthCredential(credential);
    }

    @SuppressLint("DefaultLocale")
    private void signInWithPhoneAuthCredential(PhoneAuthCredential credential) {
        firebaseAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        patientsRef.whereEqualTo(Constants.KEY_PATIENT_MOBILE, mobile)
                                .get().addOnCompleteListener(task1 -> {
                            if (task1.isSuccessful()) {
                                if (task1.getResult().getDocuments().isEmpty() || task1.getResult().getDocuments().size() == 0) {
                                    loadingDialog.dismissDialog();

                                    Random random = new Random();
                                    int number1 = random.nextInt(90000) + 10000;
                                    int number2 = random.nextInt(90000) + 10000;

                                    Intent intent = new Intent(VerifyOTPActivity.this, RegisterPatientActivity.class);

                                    intent.putExtra("uid", task.getResult().getUser().getUid());
                                    intent.putExtra("patient_id", String.format("PAT%d%d", number1, number2));
                                    intent.putExtra("patient_mobile", mobile);

                                    startActivity(intent);
                                    Animatoo.animateSlideLeft(VerifyOTPActivity.this);
                                    finish();
                                } else {
                                    loadingDialog.dismissDialog();

                                    preferenceManager.putBoolean(Constants.KEY_IS_SIGNED_IN_AS_PATIENT, true);
                                    preferenceManager.putBoolean(Constants.KEY_IS_SIGNED_IN_AS_DOCTOR, false);
                                    preferenceManager.putString(Constants.KEY_PATIENT_ID, task1.getResult().getDocuments().get(0).getString(Constants.KEY_PATIENT_ID));
                                    preferenceManager.putString(Constants.KEY_PATIENT_UID, task1.getResult().getDocuments().get(0).getString(Constants.KEY_PATIENT_UID));
                                    preferenceManager.putString(Constants.KEY_PATIENT_NAME, task1.getResult().getDocuments().get(0).getString(Constants.KEY_PATIENT_NAME));
                                    preferenceManager.putString(Constants.KEY_PATIENT_EMAIL, task1.getResult().getDocuments().get(0).getString(Constants.KEY_PATIENT_EMAIL));
                                    preferenceManager.putString(Constants.KEY_PATIENT_MOBILE, task1.getResult().getDocuments().get(0).getString(Constants.KEY_PATIENT_MOBILE));
                                    preferenceManager.putString(Constants.KEY_PATIENT_IMAGE, task1.getResult().getDocuments().get(0).getString(Constants.KEY_PATIENT_IMAGE));

                                    startActivity(new Intent(getApplicationContext(), HomeActivity.class));
                                    Animatoo.animateSlideLeft(VerifyOTPActivity.this);
                                    finish();
                                }
                            } else {
                                loadingDialog.dismissDialog();

                                Alerter.create(VerifyOTPActivity.this)
                                        .setText("Uh oh! Something broke. Try again!")
                                        .setTextAppearance(R.style.AlertText)
                                        .setBackgroundColorRes(R.color.errorColor)
                                        .setIcon(R.drawable.ic_alert_error)
                                        .setDuration(2500)
                                        .disableOutsideTouch()
                                        .show();
                            }
                        });
                    } else {
                        if (task.getException() instanceof FirebaseAuthInvalidCredentialsException) {
                            loadingDialog.dismissDialog();

                            Alerter.create(VerifyOTPActivity.this)
                                    .setText("Whoa! It seems you've got an invalid code!")
                                    .setTextAppearance(R.style.AlertText)
                                    .setBackgroundColorRes(R.color.errorColor)
                                    .setIcon(R.drawable.ic_alert_invalid)
                                    .setDuration(2500)
                                    .disableOutsideTouch()
                                    .show();
                        } else {
                            loadingDialog.dismissDialog();

                            Alerter.create(VerifyOTPActivity.this)
                                    .setText("Uh oh! Something broke. Try again!")
                                    .setTextAppearance(R.style.AlertText)
                                    .setBackgroundColorRes(R.color.errorColor)
                                    .setIcon(R.drawable.ic_alert_error)
                                    .setDuration(2500)
                                    .disableOutsideTouch()
                                    .show();
                        }
                    }
                });
    }

    public void inputDigit(EditText inputOTP, String digit) {
        String cache = binding.otpInput.getText().toString().trim();
        inputOTP.setText(String.format("%s%s", cache, digit));
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finishAffinity();
    }
}