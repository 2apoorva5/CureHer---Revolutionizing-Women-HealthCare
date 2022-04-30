package com.application.cureherapp;

import android.annotation.SuppressLint;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import com.application.cureherapp.Helper.LoadingDialog;
import com.application.cureherapp.Utilities.Common;
import com.application.cureherapp.Utilities.Constants;
import com.application.cureherapp.Utilities.PreferenceManager;
import com.application.cureherapp.databinding.ActivityDoctorProfileBinding;
import com.blogspot.atifsoftwares.animatoolib.Animatoo;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.SetOptions;
import com.google.firebase.messaging.FirebaseMessaging;
import com.makeramen.roundedimageview.RoundedImageView;
import com.squareup.picasso.Picasso;
import com.tapadoo.alerter.Alerter;

import java.util.ArrayList;
import java.util.HashMap;

import dev.shreyaspatil.MaterialDialog.MaterialDialog;

public class DoctorProfileActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {

    private ActivityDoctorProfileBinding binding;
    private RoundedImageView headerDoctorImage;
    private TextView headerDoctorName;

    private CollectionReference doctorsRef;

    private PreferenceManager preferenceManager;
    private LoadingDialog loadingDialog;

    private static final float START_SCALE = 0.75f;

    @SuppressLint("DefaultLocale")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityDoctorProfileBinding.inflate(getLayoutInflater());
        View view = binding.getRoot();
        setContentView(view);

        getWindow().addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
        getWindow().setStatusBarColor(getColor(R.color.colorBackground));
        getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);

        preferenceManager = new PreferenceManager(getApplicationContext());
        loadingDialog = new LoadingDialog(DoctorProfileActivity.this);

        if (!preferenceManager.getBoolean(Constants.KEY_IS_SIGNED_IN_AS_DOCTOR)) {
            startActivity(new Intent(getApplicationContext(), OnBoardingActivity.class));
            Animatoo.animateSlideRight(DoctorProfileActivity.this);
            finish();
        }

        checkNetworkConnection();
    }

    private void checkNetworkConnection() {
        // Check internet connection
        if (!Common.isConnectedToInternet(DoctorProfileActivity.this)) {
            binding.swipeRefreshLayout.setRefreshing(false);
            Common.openNoInternetBottomSheet(DoctorProfileActivity.this);
        } else {
            doctorsRef = FirebaseFirestore.getInstance().collection(Constants.KEY_COLLECTION_DOCTORS);
            sendFCMTokenToDatabase();
            setActionOnViews();
        }
    }

    private void setActionOnViews() {
        binding.notificationBtn.setOnClickListener(view12 -> {

        });

        binding.navigationBtn.setOnClickListener(v -> {
            if (binding.drawerLayout.isDrawerVisible(GravityCompat.START)) {
                binding.drawerLayout.closeDrawer(GravityCompat.START);
            } else {
                binding.drawerLayout.openDrawer(GravityCompat.START);
            }
        });

        binding.swipeRefreshLayout.setRefreshing(false);
        binding.swipeRefreshLayout.setOnRefreshListener(this::checkNetworkConnection);

        binding.mainNavigationMenu.bringToFront();
        headerDoctorImage = binding.mainNavigationMenu.getHeaderView(0).findViewById(R.id.header_doctor_image);
        headerDoctorName = binding.mainNavigationMenu.getHeaderView(0).findViewById(R.id.header_doctor_name);
        binding.mainNavigationMenu.setNavigationItemSelectedListener(this);

        animateNavigationDrawer();

        doctorsRef.document(preferenceManager.getString(Constants.KEY_DOCTOR_ID))
                .addSnapshotListener((snapshot, error) -> {
                    if (error != null) {
                        Alerter.create(DoctorProfileActivity.this)
                                .setText("Uh oh! Something broke. Try again!")
                                .setTextAppearance(R.style.AlertText)
                                .setBackgroundColorRes(R.color.errorColor)
                                .setIcon(R.drawable.ic_alert_error)
                                .setDuration(2500)
                                .disableOutsideTouch()
                                .show();
                    } else {
                        if (snapshot != null && snapshot.exists()) {
                            //Set doctor image
                            String image = snapshot.getString(Constants.KEY_DOCTOR_IMAGE).trim();
                            if (image.equals("") || image.length() == 0 || image.isEmpty() || image == null) {
                                binding.doctorImage.setImageResource(R.drawable.img_doctor);
                                headerDoctorImage.setImageResource(R.drawable.img_doctor);
                            } else {
                                Picasso.get().load(Uri.parse(image))
                                        .placeholder(R.drawable.img_doctor)
                                        .into(binding.doctorImage);
                                Picasso.get().load(Uri.parse(image))
                                        .placeholder(R.drawable.img_doctor)
                                        .into(headerDoctorImage);
                            }

                            //Set doctor name
                            String name = snapshot.getString(Constants.KEY_DOCTOR_NAME);
                            String[] splitName = name.split(" ");
                            int emoji = 0x1F44B;
                            binding.doctorName.setText(name);
                            headerDoctorName.setText(String.format("Hi, %s %s %s", splitName[0], splitName[1], new String(Character.toChars(emoji))));

                            //Set doctor speciality
                            String speciality = snapshot.getString(Constants.KEY_DOCTOR_SPECIALITY);
                            binding.doctorSpeciality.setText(speciality);

                            //Set doctor city
                            String city = snapshot.getString(Constants.KEY_DOCTOR_CITY);
                            binding.doctorCity.setText(city);

                            //Set doctor experience
                            int experience = snapshot.getDouble(Constants.KEY_DOCTOR_EXPERIENCE_IN_YEARS).intValue();
                            binding.doctorExperience.setText(String.format("%d+ yrs", experience));

                            //Set doctor number of patients
                            int patients = snapshot.getDouble(Constants.KEY_DOCTOR_NUMBER_OF_PATIENTS_CONSULTED).intValue();
                            binding.doctorPatientsHandled.setText(String.format("%d+", patients));

                            //Set doctor about
                            String about = snapshot.getString(Constants.KEY_DOCTOR_ABOUT);
                            binding.doctorAbout.setText(about);

                            //Set doctor educational details
                            ArrayList<String> educationList = (ArrayList<String>) snapshot.get(Constants.KEY_DOCTOR_EDUCATIONAL_DETAILS);
                            StringBuilder educationBuilder = new StringBuilder();
                            for (String education : educationList) {
                                educationBuilder.append(education + "\n");
                            }
                            binding.doctorEducationalDetails.setText(educationBuilder.toString());

                            //Set doctor membership details
                            ArrayList<String> membershipList = (ArrayList<String>) snapshot.get(Constants.KEY_DOCTOR_MEMBERSHIP_DETAILS);
                            StringBuilder membershipBuilder = new StringBuilder();
                            for (String membership : membershipList) {
                                membershipBuilder.append(membership + "\n");
                            }
                            binding.doctorMembershipDetails.setText(membershipBuilder.toString());

                            //Set doctor clinics
                            ArrayList<String> clinicList = (ArrayList<String>) snapshot.get(Constants.KEY_DOCTOR_CLINICS);
                            StringBuilder clinicBuilder = new StringBuilder();
                            for (String clinic : clinicList) {
                                clinicBuilder.append(clinic + "\n\n");
                            }
                            binding.doctorClinics.setText(clinicBuilder.toString());

                            //Set doctor email
                            String email = snapshot.getString(Constants.KEY_DOCTOR_EMAIL);
                            binding.doctorEmail.setText(email);

                            //Set doctor mobile
                            String mobile = snapshot.getString(Constants.KEY_DOCTOR_MOBILE);
                            binding.doctorMobile.setText(mobile);

                            //Set doctor languages
                            ArrayList<String> languageList = (ArrayList<String>) snapshot.get(Constants.KEY_DOCTOR_LANGUAGES);
                            StringBuilder languageBuilder = new StringBuilder();
                            for (String language : languageList) {
                                languageBuilder.append(language + "\n");
                            }
                            binding.doctorLanguages.setText(languageBuilder.toString());

                            //Set doctor other details
                            String id = snapshot.getString(Constants.KEY_DOCTOR_ID);
                            String registrationNumber = snapshot.getString(Constants.KEY_DOCTOR_REGISTRATION_NUMBER);
                            String category = snapshot.getString(Constants.KEY_DOCTOR_CATEGORY);
                            double charges = snapshot.getDouble(Constants.KEY_DOCTOR_CHARGES);
                            binding.doctorOtherDetails.setText(
                                    String.format("ID : %s\n\nRegistration Number : %s\n\nSpeciality : %s\n\nConsultation Charges : ₹ %s", id, registrationNumber, category, charges)
                            );

                            //Set doctor ratings
                            float rating = snapshot.getDouble(Constants.KEY_DOCTOR_RATINGS).floatValue();
                            binding.doctorRatingBar.setRating(rating);
                            binding.doctorRating.setText(String.valueOf(rating));
                        } else {
                            Alerter.create(DoctorProfileActivity.this)
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

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_edit_profile:
                break;
            case R.id.menu_reviews:
                break;
            case R.id.menu_appointments:
                break;
            case R.id.menu_notifications:
                break;
            case R.id.menu_write_us:
                Intent email = new Intent(Intent.ACTION_SENDTO);
                email.setData(Uri.parse("mailto:cureherapp@gmail.com"));
                startActivity(email);
                break;
            case R.id.menu_rate_us:
                try {
                    startActivity(new Intent(Intent.ACTION_VIEW,
                            Uri.parse("market://details?id=" + DoctorProfileActivity.this.getPackageName())));
                } catch (ActivityNotFoundException e) {
                    startActivity(new Intent(Intent.ACTION_VIEW,
                            Uri.parse("http://play.google.com/store/apps/details?id=" + DoctorProfileActivity.this.getPackageName())));
                }
                break;
            case R.id.menu_about:
                break;
            case R.id.menu_privacy_policy:
                break;
            case R.id.menu_terms_of_services:
                break;
            case R.id.menu_app_settings:
                Intent appInfoIntent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                appInfoIntent.addCategory(Intent.CATEGORY_DEFAULT);
                appInfoIntent.setData(Uri.parse("package:" + DoctorProfileActivity.this.getPackageName()));
                startActivity(appInfoIntent);
                break;
            case R.id.menu_logout:
                if (!Common.isConnectedToInternet(DoctorProfileActivity.this)) {
                    Common.openNoInternetBottomSheet(DoctorProfileActivity.this);
                } else {
                    logout();
                }
                break;
        }
        return false;
    }

    private void animateNavigationDrawer() {
        binding.drawerLayout.addDrawerListener(new DrawerLayout.SimpleDrawerListener() {
            @Override
            public void onDrawerSlide(View drawerView, float slideOffset) {
                final float diffScaledOffset = slideOffset * (1 - START_SCALE);
                final float offsetScale = 1 - diffScaledOffset;
                binding.contentView.setScaleX(offsetScale);
                binding.contentView.setScaleY(offsetScale);

                final float xOffset = drawerView.getWidth() * slideOffset;
                final float xOffsetDiff = binding.contentView.getWidth() * diffScaledOffset / 2;
                final float xTranslation = xOffset - xOffsetDiff;
                binding.contentView.setTranslationX(xTranslation);
            }
        });
    }

    private void sendFCMTokenToDatabase() {
        FirebaseMessaging.getInstance().getToken()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        String refreshToken = task.getResult();
                        HashMap<String, Object> token = new HashMap<>();
                        token.put(Constants.KEY_DOCTOR_TOKEN, refreshToken);

                        DocumentReference documentReference = doctorsRef.document(preferenceManager.getString(Constants.KEY_DOCTOR_ID));
                        documentReference.set(token, SetOptions.merge())
                                .addOnSuccessListener(aVoid -> {
                                })
                                .addOnFailureListener(e -> Toast.makeText(DoctorProfileActivity.this, "Some ERROR occurred!", Toast.LENGTH_SHORT).show());
                    } else {
                        Toast.makeText(DoctorProfileActivity.this, "Some ERROR occurred!", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void logout() {
        MaterialDialog materialDialog = new MaterialDialog.Builder(DoctorProfileActivity.this)
                .setTitle("Log out of CureHer?")
                .setMessage("Are you sure of logging out of CureHer?")
                .setCancelable(false)
                .setPositiveButton("Yes", R.drawable.ic_dialog_okay, (dialogInterface, which) -> {
                    dialogInterface.dismiss();
                    loadingDialog.startDialog();
                    DocumentReference documentReference = doctorsRef.document(preferenceManager.getString(Constants.KEY_DOCTOR_ID));

                    HashMap<String, Object> updates = new HashMap<>();
                    updates.put(Constants.KEY_DOCTOR_TOKEN, FieldValue.delete());
                    documentReference.update(updates)
                            .addOnCompleteListener(task -> {
                                if (task.isSuccessful()) {
                                    loadingDialog.dismissDialog();

                                    preferenceManager.clearPreferences();
                                    Toast.makeText(DoctorProfileActivity.this, "Logged Out!", Toast.LENGTH_SHORT).show();
                                    startActivity(new Intent(getApplicationContext(), OnBoardingActivity.class));
                                    Animatoo.animateSlideRight(DoctorProfileActivity.this);
                                    finish();
                                } else {
                                    loadingDialog.dismissDialog();

                                    Alerter.create(DoctorProfileActivity.this)
                                            .setText("Uh oh! Something broke. Try again!")
                                            .setTextAppearance(R.style.AlertText)
                                            .setBackgroundColorRes(R.color.errorColor)
                                            .setIcon(R.drawable.ic_alert_error)
                                            .setDuration(2500)
                                            .disableOutsideTouch()
                                            .show();
                                }
                            });
                })
                .setNegativeButton("Cancel", R.drawable.ic_dialog_cancel, (dialogInterface, which) -> dialogInterface.dismiss()).build();
        materialDialog.show();
    }

    @Override
    public void onBackPressed() {
        if (binding.drawerLayout.isDrawerVisible(GravityCompat.START)) {
            binding.drawerLayout.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
            finishAffinity();
        }
    }
}