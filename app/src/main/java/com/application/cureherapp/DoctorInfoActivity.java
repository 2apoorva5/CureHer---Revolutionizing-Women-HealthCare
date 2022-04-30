package com.application.cureherapp;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;

import androidx.appcompat.app.AppCompatActivity;

import com.application.cureherapp.Helper.LoadingDialog;
import com.application.cureherapp.Utilities.Common;
import com.application.cureherapp.Utilities.Constants;
import com.application.cureherapp.Utilities.PreferenceManager;
import com.application.cureherapp.databinding.ActivityDoctorInfoBinding;
import com.blogspot.atifsoftwares.animatoolib.Animatoo;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.squareup.picasso.Picasso;
import com.tapadoo.alerter.Alerter;

import java.util.ArrayList;

public class DoctorInfoActivity extends AppCompatActivity {

    private ActivityDoctorInfoBinding binding;

    private CollectionReference doctorsRef;

    private PreferenceManager preferenceManager;
    private LoadingDialog loadingDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_doctor_info);

        binding = ActivityDoctorInfoBinding.inflate(getLayoutInflater());
        View view = binding.getRoot();
        setContentView(view);

        getWindow().addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
        getWindow().setStatusBarColor(getColor(R.color.colorBackground));
        getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);

        preferenceManager = new PreferenceManager(getApplicationContext());
        loadingDialog = new LoadingDialog(DoctorInfoActivity.this);

        if (!preferenceManager.getBoolean(Constants.KEY_IS_SIGNED_IN_AS_PATIENT)) {
            startActivity(new Intent(getApplicationContext(), OnBoardingActivity.class));
            Animatoo.animateSlideRight(DoctorInfoActivity.this);
            finish();
        }

        checkNetworkConnection();
    }

    private void checkNetworkConnection() {
        // Check internet connection
        if (!Common.isConnectedToInternet(DoctorInfoActivity.this)) {
            binding.swipeRefreshLayout.setRefreshing(false);
            Common.openNoInternetBottomSheet(DoctorInfoActivity.this);
        } else {
            doctorsRef = FirebaseFirestore.getInstance().collection(Constants.KEY_COLLECTION_DOCTORS);
            setActionOnViews();
        }
    }

    private void setActionOnViews() {
        binding.backBtn.setOnClickListener(view1 -> {
            onBackPressed();
            finish();
        });

        binding.swipeRefreshLayout.setRefreshing(false);
        binding.swipeRefreshLayout.setOnRefreshListener(this::checkNetworkConnection);

        doctorsRef.document(preferenceManager.getString(Constants.KEY_CHOSEN_DOCTOR))
                .addSnapshotListener((snapshot, error) -> {
                    if (error != null) {
                        Alerter.create(DoctorInfoActivity.this)
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
                            } else {
                                Picasso.get().load(Uri.parse(image))
                                        .placeholder(R.drawable.img_doctor)
                                        .into(binding.doctorImage);
                            }

                            //Set doctor name
                            String name = snapshot.getString(Constants.KEY_DOCTOR_NAME);
                            binding.doctorName.setText(name);

                            //Set doctor speciality
                            String speciality = snapshot.getString(Constants.KEY_DOCTOR_SPECIALITY);
                            binding.doctorSpeciality.setText(speciality);

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
                            binding.doctorRating1.setText(String.valueOf(rating));
                            binding.doctorRating2.setText(String.valueOf(rating));
                        } else {
                            Alerter.create(DoctorInfoActivity.this)
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

        binding.bookAppointmentBtn.setOnClickListener(view -> {
            startActivity(new Intent(DoctorInfoActivity.this, AvailableSlotsActivity.class));
            Animatoo.animateSlideUp(DoctorInfoActivity.this);
        });
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        Animatoo.animateSlideRight(DoctorInfoActivity.this);
    }
}