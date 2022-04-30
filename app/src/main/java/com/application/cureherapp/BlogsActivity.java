package com.application.cureherapp;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;

import androidx.appcompat.app.AppCompatActivity;

import com.application.cureherapp.Utilities.Constants;
import com.application.cureherapp.Utilities.PreferenceManager;
import com.application.cureherapp.databinding.ActivityBlogsBinding;
import com.blogspot.atifsoftwares.animatoolib.Animatoo;

public class BlogsActivity extends AppCompatActivity {

    private ActivityBlogsBinding binding;
    private PreferenceManager preferenceManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityBlogsBinding.inflate(getLayoutInflater());
        View view = binding.getRoot();
        setContentView(view);

        getWindow().addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
        getWindow().setStatusBarColor(getColor(R.color.colorBackground));
        getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);

        preferenceManager = new PreferenceManager(getApplicationContext());

        if (!preferenceManager.getBoolean(Constants.KEY_IS_SIGNED_IN_AS_PATIENT)) {
            startActivity(new Intent(getApplicationContext(), OnBoardingActivity.class));
            Animatoo.animateSlideRight(BlogsActivity.this);
            finish();
        }

        binding.doctorsBtn.setOnClickListener(view1 -> {
            preferenceManager.putString(Constants.KEY_CHOSEN_CATEGORY, "");
            startActivity(new Intent(BlogsActivity.this, DoctorsListActivity.class));
            Animatoo.animateSlideUp(BlogsActivity.this);
        });

        binding.bottomNavigation.setSelectedItemId(R.id.menu_blogs);
        binding.bottomNavigation.setItemIconTintList(null);
        binding.bottomNavigation.setOnNavigationItemSelectedListener(item -> {
            switch (item.getItemId()) {
                case R.id.menu_home:
                    startActivity(new Intent(BlogsActivity.this, HomeActivity.class));
                    overridePendingTransition(0, 0);
                    finish();
                    break;
                case R.id.menu_blogs:
                    break;
                case R.id.menu_community:
                    startActivity(new Intent(BlogsActivity.this, CommunityActivity.class));
                    overridePendingTransition(0, 0);
                    finish();
                    break;
                case R.id.menu_profile:
                    startActivity(new Intent(BlogsActivity.this, ProfileActivity.class));
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