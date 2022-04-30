package com.application.cureherapp;

import android.content.Intent;
import android.os.Bundle;
import android.util.Patterns;
import android.view.View;
import android.view.WindowManager;

import androidx.appcompat.app.AppCompatActivity;

import com.application.cureherapp.Helper.LoadingDialog;
import com.application.cureherapp.Utilities.Common;
import com.application.cureherapp.Utilities.Constants;
import com.application.cureherapp.Utilities.PreferenceManager;
import com.application.cureherapp.databinding.ActivityRegisterPatientBinding;
import com.blogspot.atifsoftwares.animatoolib.Animatoo;
import com.daimajia.androidanimations.library.Techniques;
import com.daimajia.androidanimations.library.YoYo;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.tapadoo.alerter.Alerter;

import net.yslibrary.android.keyboardvisibilityevent.KeyboardVisibilityEvent;
import net.yslibrary.android.keyboardvisibilityevent.util.UIUtil;

import java.util.HashMap;

public class RegisterPatientActivity extends AppCompatActivity {

    private ActivityRegisterPatientBinding binding;

    private CollectionReference patientsRef;

    private String uid, id, mobile;

    private PreferenceManager preferenceManager;
    private LoadingDialog loadingDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityRegisterPatientBinding.inflate(getLayoutInflater());
        View view = binding.getRoot();
        setContentView(view);

        getWindow().addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
        getWindow().setStatusBarColor(getColor(R.color.colorBackground));
        getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);

        preferenceManager = new PreferenceManager(getApplicationContext());
        loadingDialog = new LoadingDialog(RegisterPatientActivity.this);

        if (preferenceManager.getBoolean(Constants.KEY_IS_SIGNED_IN_AS_PATIENT)) {
            startActivity(new Intent(getApplicationContext(), HomeActivity.class));
            Animatoo.animateSlideLeft(RegisterPatientActivity.this);
            finish();
        } else if (preferenceManager.getBoolean(Constants.KEY_IS_SIGNED_IN_AS_DOCTOR)) {
            startActivity(new Intent(getApplicationContext(), DoctorProfileActivity.class));
            Animatoo.animateSlideLeft(RegisterPatientActivity.this);
            finish();
        }

        patientsRef = FirebaseFirestore.getInstance().collection(Constants.KEY_COLLECTION_PATIENTS);

        Intent intent = getIntent();
        uid = intent.getStringExtra("uid");
        id = intent.getStringExtra("patient_id");
        mobile = intent.getStringExtra("patient_mobile");

        binding.closeBtn.setOnClickListener(view1 -> onBackPressed());

        KeyboardVisibilityEvent.setEventListener(RegisterPatientActivity.this, isOpen -> {
            if (!isOpen) {
                binding.nameInput.clearFocus();
                binding.emailInput.clearFocus();
            }
        });

        binding.privacyPolicy.setOnClickListener(view12 -> {

        });

        binding.registerBtn.setOnClickListener(view13 -> {
            UIUtil.hideKeyboard(RegisterPatientActivity.this);

            if (!validateName() | !validateEmail()) {
                return;
            } else {
                if (!binding.privacyPolicyCheckBox.isChecked()) {
                    YoYo.with(Techniques.Shake).duration(700).repeat(0).playOn(binding.privacyPolicyCheckBox);
                    YoYo.with(Techniques.Shake).duration(700).repeat(0).playOn(binding.privacyPolicy);
                    Alerter.create(RegisterPatientActivity.this)
                            .setText("Review and accept the privacy policy first!")
                            .setTextAppearance(R.style.AlertText)
                            .setBackgroundColorRes(R.color.infoColor)
                            .setIcon(R.drawable.ic_alert_info)
                            .setDuration(2500)
                            .disableOutsideTouch()
                            .show();
                } else {
                    if (!Common.isConnectedToInternet(RegisterPatientActivity.this)) {
                        Common.openNoInternetBottomSheet(RegisterPatientActivity.this);
                        return;
                    } else {
                        loadingDialog.startDialog();

                        HashMap<String, Object> newUser = new HashMap<>();
                        newUser.put(Constants.KEY_PATIENT_UID, uid);
                        newUser.put(Constants.KEY_PATIENT_ID, id);
                        newUser.put(Constants.KEY_PATIENT_NAME, binding.nameInput.getEditText().getText().toString().trim());
                        newUser.put(Constants.KEY_PATIENT_EMAIL, binding.emailInput.getEditText().getText().toString().toLowerCase().trim());
                        newUser.put(Constants.KEY_PATIENT_MOBILE, mobile);
                        newUser.put(Constants.KEY_PATIENT_IMAGE, "");
                        newUser.put(Constants.KEY_PATIENT_SEARCH_KEYWORD, binding.nameInput.getEditText().getText().toString().trim().toLowerCase());
                        newUser.put(Constants.KEY_PATIENT_TIMESTAMP, FieldValue.serverTimestamp());

                        patientsRef.document(id).set(newUser)
                                .addOnSuccessListener(unused -> {
                                    loadingDialog.dismissDialog();

                                    preferenceManager.putBoolean(Constants.KEY_IS_SIGNED_IN_AS_PATIENT, true);
                                    preferenceManager.putBoolean(Constants.KEY_IS_SIGNED_IN_AS_DOCTOR, false);
                                    preferenceManager.putString(Constants.KEY_PATIENT_ID, id);
                                    preferenceManager.putString(Constants.KEY_PATIENT_UID, uid);
                                    preferenceManager.putString(Constants.KEY_PATIENT_NAME, binding.nameInput.getEditText().getText().toString().trim());
                                    preferenceManager.putString(Constants.KEY_PATIENT_EMAIL, binding.emailInput.getEditText().getText().toString().toLowerCase().trim());
                                    preferenceManager.putString(Constants.KEY_PATIENT_MOBILE, mobile);
                                    preferenceManager.putString(Constants.KEY_PATIENT_IMAGE, "");

                                    startActivity(new Intent(getApplicationContext(), HomeActivity.class));
                                    Animatoo.animateSlideLeft(RegisterPatientActivity.this);
                                    finish();
                                }).addOnFailureListener(e -> {
                            loadingDialog.dismissDialog();

                            Alerter.create(RegisterPatientActivity.this)
                                    .setText("Uh oh! Something broke. Try again!")
                                    .setTextAppearance(R.style.AlertText)
                                    .setBackgroundColorRes(R.color.errorColor)
                                    .setIcon(R.drawable.ic_alert_error)
                                    .setDuration(2500)
                                    .disableOutsideTouch()
                                    .show();
                        });
                    }
                }
            }
        });
    }

    private boolean validateName() {
        String name = binding.nameInput.getEditText().getText().toString().trim();

        if (name.isEmpty()) {
            binding.nameInput.setError("Enter your name!");
            binding.nameInput.requestFocus();
            return false;
        } else {
            binding.nameInput.setError(null);
            binding.nameInput.setErrorEnabled(false);
            return true;
        }
    }

    private boolean validateEmail() {
        String email = binding.emailInput.getEditText().getText().toString().toLowerCase().trim();

        if (email.isEmpty()) {
            binding.emailInput.setError("Enter an email to set up account!");
            binding.emailInput.requestFocus();
            return false;
        } else if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            binding.emailInput.setError("Invalid email. Try again!");
            binding.emailInput.requestFocus();
            return false;
        } else {
            binding.emailInput.setError(null);
            binding.emailInput.setErrorEnabled(false);
            return true;
        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        KeyboardVisibilityEvent.setEventListener(RegisterPatientActivity.this, isOpen -> {
            if (isOpen) {
                UIUtil.hideKeyboard(RegisterPatientActivity.this);
            }
        });
        finishAffinity();
    }
}