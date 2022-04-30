package com.application.cureherapp;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.application.cureherapp.Utilities.Common;
import com.application.cureherapp.Utilities.Constants;
import com.application.cureherapp.Utilities.PreferenceManager;
import com.application.cureherapp.databinding.ActivityHomeBinding;
import com.blogspot.atifsoftwares.animatoolib.Animatoo;
import com.bumptech.glide.Glide;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.SetOptions;
import com.google.firebase.messaging.FirebaseMessaging;

import java.util.HashMap;

public class HomeActivity extends AppCompatActivity {

    private ActivityHomeBinding binding;

    private CollectionReference patientsRef;

    private PreferenceManager preferenceManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityHomeBinding.inflate(getLayoutInflater());
        View view = binding.getRoot();
        setContentView(view);

        getWindow().addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
        getWindow().setStatusBarColor(getColor(R.color.colorBackground));
        getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);

        preferenceManager = new PreferenceManager(getApplicationContext());

        if (!preferenceManager.getBoolean(Constants.KEY_IS_SIGNED_IN_AS_PATIENT)) {
            startActivity(new Intent(getApplicationContext(), OnBoardingActivity.class));
            Animatoo.animateSlideRight(HomeActivity.this);
            finish();
        }

        checkNetworkConnection();
    }

    private void checkNetworkConnection() {
        // Check internet connection
        if (!Common.isConnectedToInternet(HomeActivity.this)) {
            binding.swipeRefreshLayout.setRefreshing(false);
            Common.openNoInternetBottomSheet(HomeActivity.this);
        } else {
            patientsRef = FirebaseFirestore.getInstance().collection(Constants.KEY_COLLECTION_PATIENTS);
            sendFCMTokenToDatabase();
            setActionOnViews();
        }
    }

    private void setActionOnViews() {
        binding.notificationBtn.setOnClickListener(view12 -> {

        });

        binding.swipeRefreshLayout.setRefreshing(false);
        binding.swipeRefreshLayout.setOnRefreshListener(this::checkNetworkConnection);

        //Set patient name
        String name = preferenceManager.getString(Constants.KEY_PATIENT_NAME);
        String[] splitName = name.split(" ");
        int emoji = 0x1F44B;
        binding.title.setText(String.format("Hi, %s %s", splitName[0], new String(Character.toChars(emoji))));

        //Set patient image
        if (preferenceManager.getString(Constants.KEY_PATIENT_IMAGE).equals("") ||
                preferenceManager.getString(Constants.KEY_PATIENT_IMAGE).length() == 0 ||
                preferenceManager.getString(Constants.KEY_PATIENT_IMAGE).isEmpty() ||
                preferenceManager.getString(Constants.KEY_PATIENT_IMAGE) == null) {
            binding.patientImage.setImageResource(R.drawable.illustration_patient_avatar);
        } else {
            Glide.with(HomeActivity.this)
                    .load(Uri.parse(preferenceManager.getString(Constants.KEY_PATIENT_IMAGE)))
                    .placeholder(R.drawable.illustration_patient_avatar)
                    .into(binding.patientImage);
        }

        binding.patientImage.setOnClickListener(view13 -> {
            startActivity(new Intent(HomeActivity.this, ProfileActivity.class));
            overridePendingTransition(0, 0);
            finish();
        });

        binding.searchBtn.setOnClickListener(view1 -> {

        });

        binding.category1Btn.setOnClickListener(view14 -> {
            preferenceManager.putString(Constants.KEY_CHOSEN_CATEGORY, "Breast Care");
            startActivity(new Intent(HomeActivity.this, DoctorsListActivity.class));
            Animatoo.animateSlideUp(HomeActivity.this);
        });

        binding.category2Btn.setOnClickListener(view14 -> {
            preferenceManager.putString(Constants.KEY_CHOSEN_CATEGORY, "Sexual Health");
            startActivity(new Intent(HomeActivity.this, DoctorsListActivity.class));
            Animatoo.animateSlideUp(HomeActivity.this);
        });

        binding.category3Btn.setOnClickListener(view14 -> {
            preferenceManager.putString(Constants.KEY_CHOSEN_CATEGORY, "Gynecology");
            startActivity(new Intent(HomeActivity.this, DoctorsListActivity.class));
            Animatoo.animateSlideUp(HomeActivity.this);
        });

        binding.category4Btn.setOnClickListener(view14 -> {
            preferenceManager.putString(Constants.KEY_CHOSEN_CATEGORY, "Pregnancy");
            startActivity(new Intent(HomeActivity.this, DoctorsListActivity.class));
            Animatoo.animateSlideUp(HomeActivity.this);
        });

        binding.category5Btn.setOnClickListener(view14 -> {
            preferenceManager.putString(Constants.KEY_CHOSEN_CATEGORY, "Infertility");
            startActivity(new Intent(HomeActivity.this, DoctorsListActivity.class));
            Animatoo.animateSlideUp(HomeActivity.this);
        });

        binding.category6Btn.setOnClickListener(view14 -> {
            preferenceManager.putString(Constants.KEY_CHOSEN_CATEGORY, "Bladder Care");
            startActivity(new Intent(HomeActivity.this, DoctorsListActivity.class));
            Animatoo.animateSlideUp(HomeActivity.this);
        });

        binding.doctorsBtn.setOnClickListener(view14 -> {
            preferenceManager.putString(Constants.KEY_CHOSEN_CATEGORY, "");
            startActivity(new Intent(HomeActivity.this, DoctorsListActivity.class));
            Animatoo.animateSlideUp(HomeActivity.this);
        });

        binding.bottomNavigation.setSelectedItemId(R.id.menu_home);
        binding.bottomNavigation.setItemIconTintList(null);
        binding.bottomNavigation.setOnNavigationItemSelectedListener(item -> {
            switch (item.getItemId()) {
                case R.id.menu_home:
                    break;
                case R.id.menu_blogs:
                    startActivity(new Intent(HomeActivity.this, BlogsActivity.class));
                    overridePendingTransition(0, 0);
                    finish();
                    break;
                case R.id.menu_community:
                    startActivity(new Intent(HomeActivity.this, CommunityActivity.class));
                    overridePendingTransition(0, 0);
                    finish();
                    break;
                case R.id.menu_profile:
                    startActivity(new Intent(HomeActivity.this, ProfileActivity.class));
                    overridePendingTransition(0, 0);
                    finish();
                    break;
            }
            return false;
        });
    }

    private void sendFCMTokenToDatabase() {
        FirebaseMessaging.getInstance().getToken()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        String refreshToken = task.getResult();
                        HashMap<String, Object> token = new HashMap<>();
                        token.put(Constants.KEY_PATIENT_TOKEN, refreshToken);

                        DocumentReference documentReference = patientsRef.document(preferenceManager.getString(Constants.KEY_PATIENT_ID));
                        documentReference.set(token, SetOptions.merge())
                                .addOnSuccessListener(aVoid -> {
                                })
                                .addOnFailureListener(e -> Toast.makeText(HomeActivity.this, "Some ERROR occurred!", Toast.LENGTH_SHORT).show());
                    } else {
                        Toast.makeText(HomeActivity.this, "Some ERROR occurred!", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finishAffinity();
    }
}