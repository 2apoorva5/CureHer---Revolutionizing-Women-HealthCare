package com.application.cureherapp;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;

import androidx.appcompat.app.AppCompatActivity;

import com.application.cureherapp.Utilities.Constants;
import com.application.cureherapp.Utilities.PreferenceManager;
import com.application.cureherapp.databinding.ActivityCommunityBinding;
import com.blogspot.atifsoftwares.animatoolib.Animatoo;

public class CommunityActivity extends AppCompatActivity {

    private ActivityCommunityBinding binding;
    private PreferenceManager preferenceManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityCommunityBinding.inflate(getLayoutInflater());
        View view = binding.getRoot();
        setContentView(view);

        getWindow().addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
        getWindow().setStatusBarColor(getColor(R.color.colorBackground));
        getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);

        preferenceManager = new PreferenceManager(getApplicationContext());

        if (!preferenceManager.getBoolean(Constants.KEY_IS_SIGNED_IN_AS_PATIENT)) {
            startActivity(new Intent(getApplicationContext(), OnBoardingActivity.class));
            Animatoo.animateSlideRight(CommunityActivity.this);
            finish();
        }

        binding.doctorsBtn.setOnClickListener(view1 -> {
            preferenceManager.putString(Constants.KEY_CHOSEN_CATEGORY, "");
            startActivity(new Intent(CommunityActivity.this, DoctorsListActivity.class));
            Animatoo.animateSlideUp(CommunityActivity.this);
        });

        binding.bottomNavigation.setSelectedItemId(R.id.menu_community);
        binding.bottomNavigation.setItemIconTintList(null);
        binding.bottomNavigation.setOnNavigationItemSelectedListener(item -> {
            switch (item.getItemId()) {
                case R.id.menu_home:
                    startActivity(new Intent(CommunityActivity.this, HomeActivity.class));
                    overridePendingTransition(0, 0);
                    finish();
                    break;
                case R.id.menu_blogs:
                    startActivity(new Intent(CommunityActivity.this, BlogsActivity.class));
                    overridePendingTransition(0, 0);
                    finish();
                    break;
                case R.id.menu_community:
                    break;
                case R.id.menu_profile:
                    startActivity(new Intent(CommunityActivity.this, ProfileActivity.class));
                    overridePendingTransition(0, 0);
                    finish();
                    break;
            }
            return false;
        });
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finishAffinity();
    }
}