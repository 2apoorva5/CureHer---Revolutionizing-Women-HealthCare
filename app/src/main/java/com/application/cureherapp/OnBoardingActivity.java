package com.application.cureherapp;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;

import androidx.appcompat.app.AppCompatActivity;

import com.application.cureherapp.Utilities.Constants;
import com.application.cureherapp.Utilities.PreferenceManager;
import com.application.cureherapp.databinding.ActivityOnboardingBinding;
import com.blogspot.atifsoftwares.animatoolib.Animatoo;

public class OnBoardingActivity extends AppCompatActivity {

    private ActivityOnboardingBinding binding;
    private PreferenceManager preferenceManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityOnboardingBinding.inflate(getLayoutInflater());
        View view = binding.getRoot();
        setContentView(view);

        getWindow().addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
        getWindow().setStatusBarColor(getColor(R.color.colorBackground));
        getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);

        preferenceManager = new PreferenceManager(getApplicationContext());

        if (preferenceManager.getBoolean(Constants.KEY_IS_SIGNED_IN_AS_PATIENT)) {
            startActivity(new Intent(getApplicationContext(), HomeActivity.class));
            Animatoo.animateSlideLeft(OnBoardingActivity.this);
            finish();
        } else if (preferenceManager.getBoolean(Constants.KEY_IS_SIGNED_IN_AS_DOCTOR)) {
            startActivity(new Intent(getApplicationContext(), DoctorProfileActivity.class));
            Animatoo.animateSlideLeft(OnBoardingActivity.this);
            finish();
        }

        binding.patientLoginBtn.setOnClickListener(view1 -> {
            startActivity(new Intent(OnBoardingActivity.this, InputMobileActivity.class));
            Animatoo.animateSlideUp(OnBoardingActivity.this);
        });

        binding.doctorLoginBtn.setOnClickListener(view12 -> {
            startActivity(new Intent(OnBoardingActivity.this, DoctorLoginActivity.class));
            Animatoo.animateSlideUp(OnBoardingActivity.this);
        });
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finishAffinity();
    }
}