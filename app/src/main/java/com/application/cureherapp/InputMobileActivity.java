package com.application.cureherapp;

import android.content.Intent;
import android.os.Bundle;
import android.text.InputType;
import android.text.TextUtils;
import android.view.View;
import android.view.WindowManager;
import android.widget.EditText;

import androidx.appcompat.app.AppCompatActivity;

import com.application.cureherapp.Utilities.Common;
import com.application.cureherapp.Utilities.Constants;
import com.application.cureherapp.Utilities.PreferenceManager;
import com.application.cureherapp.databinding.ActivityInputMobileBinding;
import com.blogspot.atifsoftwares.animatoolib.Animatoo;
import com.tapadoo.alerter.Alerter;

public class InputMobileActivity extends AppCompatActivity {

    private ActivityInputMobileBinding binding;
    private PreferenceManager preferenceManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityInputMobileBinding.inflate(getLayoutInflater());
        View view = binding.getRoot();
        setContentView(view);

        getWindow().addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
        getWindow().setStatusBarColor(getColor(R.color.colorBackground));
        getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);

        preferenceManager = new PreferenceManager(getApplicationContext());

        if (preferenceManager.getBoolean(Constants.KEY_IS_SIGNED_IN_AS_PATIENT)) {
            startActivity(new Intent(getApplicationContext(), HomeActivity.class));
            Animatoo.animateSlideLeft(InputMobileActivity.this);
            finish();
        } else if (preferenceManager.getBoolean(Constants.KEY_IS_SIGNED_IN_AS_DOCTOR)) {
            startActivity(new Intent(getApplicationContext(), DoctorProfileActivity.class));
            Animatoo.animateSlideLeft(InputMobileActivity.this);
            finish();
        }

        binding.closeBtn.setOnClickListener(view12 -> onBackPressed());

        binding.inputMobile.setInputType(InputType.TYPE_NULL);

        binding.input1.setOnClickListener(view13 -> inputDigit(binding.inputMobile, "1"));
        binding.input2.setOnClickListener(view13 -> inputDigit(binding.inputMobile, "2"));
        binding.input3.setOnClickListener(view13 -> inputDigit(binding.inputMobile, "3"));
        binding.input4.setOnClickListener(view13 -> inputDigit(binding.inputMobile, "4"));
        binding.input5.setOnClickListener(view13 -> inputDigit(binding.inputMobile, "5"));
        binding.input6.setOnClickListener(view13 -> inputDigit(binding.inputMobile, "6"));
        binding.input7.setOnClickListener(view13 -> inputDigit(binding.inputMobile, "7"));
        binding.input8.setOnClickListener(view13 -> inputDigit(binding.inputMobile, "8"));
        binding.input9.setOnClickListener(view13 -> inputDigit(binding.inputMobile, "9"));
        binding.input0.setOnClickListener(view13 -> inputDigit(binding.inputMobile, "0"));

        binding.deleteInput.setOnClickListener(view14 -> {
            String text = binding.inputMobile.getText().toString().trim();
            if (!TextUtils.isEmpty(text)) {
                binding.inputMobile.setText(text.substring(0, text.length() - 1));
            }
        });

        binding.sendOtpBtn.setOnClickListener(view1 -> {
            if (!validateMobile()) {
                return;
            } else {
                if (!Common.isConnectedToInternet(InputMobileActivity.this)) {
                    Common.openNoInternetBottomSheet(InputMobileActivity.this);
                } else {
                    final String mobile = binding.countryCode.getText().toString().trim() +
                            binding.inputMobile.getText().toString().trim();

                    Intent intent = new Intent(InputMobileActivity.this, VerifyOTPActivity.class);
                    intent.putExtra("patient_mobile", mobile);
                    startActivity(intent);
                    Animatoo.animateSlideLeft(InputMobileActivity.this);
                    finish();
                }
            }
        });
    }

    public void inputDigit(EditText inputMobile, String digit) {
        String cache = binding.inputMobile.getText().toString().trim();
        inputMobile.setText(String.format("%s%s", cache, digit));
    }

    private boolean validateMobile() {
        String mobile = binding.inputMobile.getText().toString().trim();

        if (mobile.isEmpty()) {
            Alerter.create(InputMobileActivity.this)
                    .setText("Enter your mobile!")
                    .setTextAppearance(R.style.AlertText)
                    .setBackgroundColorRes(R.color.errorColor)
                    .setIcon(R.drawable.ic_alert_mobile)
                    .setDuration(2500)
                    .disableOutsideTouch()
                    .show();
            return false;
        } else if (mobile.length() != 10) {
            Alerter.create(InputMobileActivity.this)
                    .setText("Invalid mobile. Try again!")
                    .setTextAppearance(R.style.AlertText)
                    .setBackgroundColorRes(R.color.errorColor)
                    .setIcon(R.drawable.ic_alert_invalid)
                    .setDuration(2500)
                    .disableOutsideTouch()
                    .show();
            return false;
        } else {
            return true;
        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        Animatoo.animateSlideDown(InputMobileActivity.this);
    }
}