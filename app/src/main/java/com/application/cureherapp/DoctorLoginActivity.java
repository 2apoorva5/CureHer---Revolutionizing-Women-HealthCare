package com.application.cureherapp;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;

import androidx.appcompat.app.AppCompatActivity;

import com.application.cureherapp.Helper.LoadingDialog;
import com.application.cureherapp.Utilities.Common;
import com.application.cureherapp.Utilities.Constants;
import com.application.cureherapp.Utilities.PreferenceManager;
import com.application.cureherapp.databinding.ActivityDoctorLoginBinding;
import com.blogspot.atifsoftwares.animatoolib.Animatoo;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.tapadoo.alerter.Alerter;

import net.yslibrary.android.keyboardvisibilityevent.KeyboardVisibilityEvent;
import net.yslibrary.android.keyboardvisibilityevent.util.UIUtil;

public class DoctorLoginActivity extends AppCompatActivity {

    private ActivityDoctorLoginBinding binding;

    private CollectionReference doctorsRef;

    private PreferenceManager preferenceManager;
    private LoadingDialog loadingDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityDoctorLoginBinding.inflate(getLayoutInflater());
        View view = binding.getRoot();
        setContentView(view);

        getWindow().addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
        getWindow().setStatusBarColor(getColor(R.color.colorBackground));
        getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);

        preferenceManager = new PreferenceManager(getApplicationContext());
        loadingDialog = new LoadingDialog(DoctorLoginActivity.this);

        if (preferenceManager.getBoolean(Constants.KEY_IS_SIGNED_IN_AS_PATIENT)) {
            startActivity(new Intent(getApplicationContext(), HomeActivity.class));
            Animatoo.animateSlideLeft(DoctorLoginActivity.this);
            finish();
        } else if (preferenceManager.getBoolean(Constants.KEY_IS_SIGNED_IN_AS_DOCTOR)) {
            startActivity(new Intent(getApplicationContext(), DoctorProfileActivity.class));
            Animatoo.animateSlideLeft(DoctorLoginActivity.this);
            finish();
        }

        doctorsRef = FirebaseFirestore.getInstance().collection(Constants.KEY_COLLECTION_DOCTORS);

        binding.closeBtn.setOnClickListener(view1 -> onBackPressed());

        KeyboardVisibilityEvent.setEventListener(DoctorLoginActivity.this, isOpen -> {
            if (!isOpen) {
                binding.idInput.clearFocus();
                binding.passwordInput.clearFocus();
            }
        });

        binding.forgotPasswordBtn.setOnClickListener(view12 -> {

        });

        binding.signInBtn.setOnClickListener(view13 -> {
            UIUtil.hideKeyboard(DoctorLoginActivity.this);

            if (!validateLoginID() | !validatePassword()) {
                return;
            } else {
                if (!Common.isConnectedToInternet(DoctorLoginActivity.this)) {
                    Common.openNoInternetBottomSheet(DoctorLoginActivity.this);
                } else {
                    loadingDialog.startDialog();

                    String id = binding.idInput.getPrefixText().toString().trim() +
                            binding.idInput.getEditText().getText().toString().trim();
                    String password = binding.passwordInput.getEditText().getText().toString().trim();

                    doctorsRef.document(id).get()
                            .addOnCompleteListener(task -> {
                                if (task.isSuccessful()) {
                                    DocumentSnapshot doctorDocument = task.getResult();
                                    if (doctorDocument.exists()) {
                                        if (doctorDocument.getString(Constants.KEY_DOCTOR_PROFILE_PASSWORD).trim().equals(password)) {
                                            loadingDialog.dismissDialog();

                                            preferenceManager.putBoolean(Constants.KEY_IS_SIGNED_IN_AS_PATIENT, false);
                                            preferenceManager.putBoolean(Constants.KEY_IS_SIGNED_IN_AS_DOCTOR, true);
                                            preferenceManager.putString(Constants.KEY_DOCTOR_ID, id);

                                            startActivity(new Intent(getApplicationContext(), DoctorProfileActivity.class));
                                            Animatoo.animateSlideLeft(DoctorLoginActivity.this);
                                            finish();
                                        } else {
                                            loadingDialog.dismissDialog();

                                            Alerter.create(DoctorLoginActivity.this)
                                                    .setText("Whoops! You've got a wrong password!")
                                                    .setTextAppearance(R.style.AlertText)
                                                    .setBackgroundColorRes(R.color.errorColor)
                                                    .setIcon(R.drawable.ic_alert_error)
                                                    .setDuration(2500)
                                                    .disableOutsideTouch()
                                                    .show();
                                        }
                                    } else {
                                        loadingDialog.dismissDialog();

                                        Alerter.create(DoctorLoginActivity.this)
                                                .setText("Shoot! We didn't find any account with that Login ID!")
                                                .setTextAppearance(R.style.AlertText)
                                                .setBackgroundColorRes(R.color.errorColor)
                                                .setIcon(R.drawable.ic_alert_invalid)
                                                .setDuration(2500)
                                                .disableOutsideTouch()
                                                .show();
                                    }
                                } else {
                                    loadingDialog.dismissDialog();

                                    Alerter.create(DoctorLoginActivity.this)
                                            .setText("Uh oh! Something broke. Try again!")
                                            .setTextAppearance(R.style.AlertText)
                                            .setBackgroundColorRes(R.color.errorColor)
                                            .setIcon(R.drawable.ic_alert_error)
                                            .setDuration(2500)
                                            .disableOutsideTouch()
                                            .show();
                                }
                            });
                }
            }
        });
    }

    private boolean validateLoginID() {
        String id = binding.idInput.getEditText().getText().toString().trim();

        if (id.isEmpty()) {
            binding.idInput.setError("Enter your Login ID!");
            binding.idInput.requestFocus();
            return false;
        } else {
            binding.idInput.setError(null);
            binding.idInput.setErrorEnabled(false);
            return true;
        }
    }

    private boolean validatePassword() {
        String password = binding.passwordInput.getEditText().getText().toString().toLowerCase().trim();

        if (password.isEmpty()) {
            binding.passwordInput.setError("Enter your account password!");
            binding.passwordInput.requestFocus();
            return false;
        } else {
            binding.passwordInput.setError(null);
            binding.passwordInput.setErrorEnabled(false);
            return true;
        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        KeyboardVisibilityEvent.setEventListener(DoctorLoginActivity.this, isOpen -> {
            if (isOpen) {
                UIUtil.hideKeyboard(DoctorLoginActivity.this);
            }
        });
        Animatoo.animateSlideDown(DoctorLoginActivity.this);
    }
}