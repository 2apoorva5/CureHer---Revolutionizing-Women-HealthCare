package com.application.cureherapp;

import android.app.Dialog;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;

import com.application.cureherapp.Helper.LoadingDialog;
import com.application.cureherapp.Utilities.Common;
import com.application.cureherapp.Utilities.Constants;
import com.application.cureherapp.Utilities.PreferenceManager;
import com.application.cureherapp.databinding.ActivityProfileBinding;
import com.blogspot.atifsoftwares.animatoolib.Animatoo;
import com.bumptech.glide.Glide;
import com.github.dhaval2404.imagepicker.ImagePicker;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.tapadoo.alerter.Alerter;

import java.util.HashMap;

import dev.shreyaspatil.MaterialDialog.MaterialDialog;

public class ProfileActivity extends AppCompatActivity {

    private ActivityProfileBinding binding;

    private FirebaseAuth firebaseAuth;
    private CollectionReference patientsRef;
    private StorageReference storageReference;

    private Uri profilePicUri = null;

    private PreferenceManager preferenceManager;
    private LoadingDialog loadingDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        binding = ActivityProfileBinding.inflate(getLayoutInflater());
        View view = binding.getRoot();
        setContentView(view);

        getWindow().addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
        getWindow().setStatusBarColor(getColor(R.color.colorBackground));
        getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);

        preferenceManager = new PreferenceManager(getApplicationContext());
        loadingDialog = new LoadingDialog(ProfileActivity.this);

        if (!preferenceManager.getBoolean(Constants.KEY_IS_SIGNED_IN_AS_PATIENT)) {
            startActivity(new Intent(getApplicationContext(), OnBoardingActivity.class));
            Animatoo.animateSlideRight(ProfileActivity.this);
            finish();
        }

        firebaseAuth = FirebaseAuth.getInstance();
        patientsRef = FirebaseFirestore.getInstance().collection(Constants.KEY_COLLECTION_PATIENTS);
        storageReference = FirebaseStorage.getInstance().getReference("PatientImages/");

        //Set patient image
        if (preferenceManager.getString(Constants.KEY_PATIENT_IMAGE).trim().equals("") ||
                preferenceManager.getString(Constants.KEY_PATIENT_IMAGE).trim().length() == 0 ||
                preferenceManager.getString(Constants.KEY_PATIENT_IMAGE).trim().isEmpty() ||
                preferenceManager.getString(Constants.KEY_PATIENT_IMAGE).trim() == null) {
            binding.patientImage.setImageResource(R.drawable.illustration_patient_avatar);
        } else {
            Glide.with(ProfileActivity.this)
                    .load(Uri.parse(preferenceManager.getString(Constants.KEY_PATIENT_IMAGE)))
                    .placeholder(R.drawable.illustration_patient_avatar)
                    .into(binding.patientImage);
        }

        binding.choosePhoto.setOnClickListener(view13 -> {
            if (!Common.isConnectedToInternet(ProfileActivity.this)) {
                Common.openNoInternetBottomSheet(ProfileActivity.this);
            } else {
                openImagePickerBottomSheet();
            }
        });

        //Set patient name, email, mobile
        binding.patientName.setText(preferenceManager.getString(Constants.KEY_PATIENT_NAME));
        binding.patientEmail.setText(preferenceManager.getString(Constants.KEY_PATIENT_EMAIL));
        binding.patientMobile.setText(preferenceManager.getString(Constants.KEY_PATIENT_MOBILE));

        binding.myAppointmentsBtn.setOnClickListener(view12 -> {

        });

        binding.myPrescriptionsBtn.setOnClickListener(view12 -> {

        });

        binding.notificationsBtn.setOnClickListener(view12 -> {

        });

        binding.writeUsBtn.setOnClickListener(view12 -> {
            Intent email = new Intent(Intent.ACTION_SENDTO);
            email.setData(Uri.parse("mailto:cureherapp@gmail.com"));
            startActivity(email);
        });

        binding.rateUsBtn.setOnClickListener(view12 -> {
            try {
                startActivity(new Intent(Intent.ACTION_VIEW,
                        Uri.parse("market://details?id=" + ProfileActivity.this.getPackageName())));
            } catch (ActivityNotFoundException e) {
                startActivity(new Intent(Intent.ACTION_VIEW,
                        Uri.parse("http://play.google.com/store/apps/details?id=" + ProfileActivity.this.getPackageName())));
            }
        });

        binding.shareAppBtn.setOnClickListener(view12 -> {

        });

        binding.aboutUsBtn.setOnClickListener(view12 -> {

        });

        binding.privacyPolicyBtn.setOnClickListener(view12 -> {

        });

        binding.termsOfServicesBtn.setOnClickListener(view12 -> {

        });

        binding.appSettingsBtn.setOnClickListener(view12 -> {
            Intent appInfoIntent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
            appInfoIntent.addCategory(Intent.CATEGORY_DEFAULT);
            appInfoIntent.setData(Uri.parse("package:" + ProfileActivity.this.getPackageName()));
            startActivity(appInfoIntent);
        });

        binding.logOutBtn.setOnClickListener(view12 -> {
            if (!Common.isConnectedToInternet(ProfileActivity.this)) {
                Common.openNoInternetBottomSheet(ProfileActivity.this);
            } else {
                logout();
            }
        });

        binding.doctorsBtn.setOnClickListener(view1 -> {
            preferenceManager.putString(Constants.KEY_CHOSEN_CATEGORY, "");
            startActivity(new Intent(ProfileActivity.this, DoctorsListActivity.class));
            Animatoo.animateSlideUp(ProfileActivity.this);
        });

        binding.bottomNavigation.setSelectedItemId(R.id.menu_profile);
        binding.bottomNavigation.setItemIconTintList(null);
        binding.bottomNavigation.setOnNavigationItemSelectedListener(item -> {
            switch (item.getItemId()) {
                case R.id.menu_home:
                    startActivity(new Intent(ProfileActivity.this, HomeActivity.class));
                    overridePendingTransition(0, 0);
                    finish();
                    break;
                case R.id.menu_blogs:
                    startActivity(new Intent(ProfileActivity.this, BlogsActivity.class));
                    overridePendingTransition(0, 0);
                    finish();
                    break;
                case R.id.menu_community:
                    startActivity(new Intent(ProfileActivity.this, CommunityActivity.class));
                    overridePendingTransition(0, 0);
                    finish();
                    break;
                case R.id.menu_profile:
                    break;
            }
            return true;
        });
    }

    private void openImagePickerBottomSheet() {
        final Dialog dialog = new Dialog(ProfileActivity.this);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.bottomsheet_image_picker);
        dialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        dialog.getWindow().getAttributes().windowAnimations = R.style.BottomSheetAnimation;
        dialog.getWindow().setGravity(Gravity.BOTTOM);
        dialog.setCancelable(false);

        ImageView closeBtn = dialog.findViewById(R.id.close_btn);
        ConstraintLayout removeBtn = dialog.findViewById(R.id.remove_btn);
        ConstraintLayout cameraBtn = dialog.findViewById(R.id.camera_btn);
        ConstraintLayout galleryBtn = dialog.findViewById(R.id.gallery_btn);

        dialog.show();

        closeBtn.setOnClickListener(view -> dialog.dismiss());

        removeBtn.setOnClickListener(view -> {
            dialog.dismiss();
            profilePicUri = null;
            binding.patientImage.setImageResource(R.drawable.illustration_patient_avatar);

            loadingDialog.startDialog();

            patientsRef.document(preferenceManager.getString(Constants.KEY_PATIENT_ID))
                    .update(Constants.KEY_PATIENT_IMAGE, "")
                    .addOnSuccessListener(unused -> {
                        final StorageReference fileRef = storageReference.child(preferenceManager.getString(Constants.KEY_PATIENT_ID) + ".img");

                        fileRef.delete()
                                .addOnSuccessListener(unused1 -> {
                                    loadingDialog.dismissDialog();

                                    preferenceManager.putString(Constants.KEY_PATIENT_IMAGE, "");
                                    Alerter.create(ProfileActivity.this)
                                            .setText("Success! Your profile picture is removed.")
                                            .setTextAppearance(R.style.AlertText)
                                            .setBackgroundColorRes(R.color.successColor)
                                            .setIcon(R.drawable.ic_alert_success)
                                            .setDuration(2500)
                                            .disableOutsideTouch()
                                            .show();
                                }).addOnFailureListener(e -> {
                            loadingDialog.dismissDialog();

                            Alerter.create(ProfileActivity.this)
                                    .setText("Uh oh! Something broke. Try again!")
                                    .setTextAppearance(R.style.AlertText)
                                    .setBackgroundColorRes(R.color.errorColor)
                                    .setIcon(R.drawable.ic_alert_error)
                                    .setDuration(2500)
                                    .disableOutsideTouch()
                                    .show();
                        });
                    }).addOnFailureListener(e -> {
                loadingDialog.dismissDialog();

                Alerter.create(ProfileActivity.this)
                        .setText("Uh oh! Something broke. Try again!")
                        .setTextAppearance(R.style.AlertText)
                        .setBackgroundColorRes(R.color.errorColor)
                        .setIcon(R.drawable.ic_alert_error)
                        .setDuration(2500)
                        .disableOutsideTouch()
                        .show();
            });
        });

        cameraBtn.setOnClickListener(view -> {
            dialog.dismiss();
            ImagePicker.with(ProfileActivity.this)
                    .cameraOnly()
                    .cropSquare()
                    .compress(1024)
                    .maxResultSize(1080, 1080)
                    .createIntent(intent -> {
                        imagePickerLauncher.launch(intent);
                        return null;
                    });
        });

        galleryBtn.setOnClickListener(view -> {
            dialog.dismiss();
            ImagePicker.with(ProfileActivity.this)
                    .galleryOnly()
                    .cropSquare()
                    .compress(1024)
                    .maxResultSize(1080, 1080)
                    .createIntent(intent -> {
                        imagePickerLauncher.launch(intent);
                        return null;
                    });
        });
    }

    ActivityResultLauncher<Intent> imagePickerLauncher =
            registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), new ActivityResultCallback<ActivityResult>() {
                @Override
                public void onActivityResult(ActivityResult result) {
                    if (result.getResultCode() == RESULT_OK) {
                        profilePicUri = result.getData().getData();
                        Glide.with(ProfileActivity.this).load(profilePicUri).into(binding.patientImage);

                        loadingDialog.startDialog();

                        if (profilePicUri != null) {
                            final StorageReference fileRef = storageReference.child(preferenceManager.getString(Constants.KEY_PATIENT_ID) + ".img");

                            fileRef.putFile(profilePicUri)
                                    .addOnCompleteListener(task -> {
                                        if (task.isSuccessful()) {
                                            fileRef.getDownloadUrl()
                                                    .addOnSuccessListener(uri -> {
                                                        final String image = uri.toString();

                                                        patientsRef.document(preferenceManager.getString(Constants.KEY_PATIENT_ID))
                                                                .update(Constants.KEY_PATIENT_IMAGE, image)
                                                                .addOnSuccessListener(unused -> {
                                                                    loadingDialog.dismissDialog();

                                                                    preferenceManager.putString(Constants.KEY_PATIENT_IMAGE, image);
                                                                    Alerter.create(ProfileActivity.this)
                                                                            .setText("Success! Your profile picture just got updated.")
                                                                            .setTextAppearance(R.style.AlertText)
                                                                            .setBackgroundColorRes(R.color.successColor)
                                                                            .setIcon(R.drawable.ic_alert_success)
                                                                            .setDuration(2500)
                                                                            .disableOutsideTouch()
                                                                            .show();
                                                                }).addOnFailureListener(e -> {
                                                            loadingDialog.dismissDialog();

                                                            Alerter.create(ProfileActivity.this)
                                                                    .setText("Uh oh! Something broke. Try again!")
                                                                    .setTextAppearance(R.style.AlertText)
                                                                    .setBackgroundColorRes(R.color.errorColor)
                                                                    .setIcon(R.drawable.ic_alert_error)
                                                                    .setDuration(2500)
                                                                    .disableOutsideTouch()
                                                                    .show();
                                                        });
                                                    }).addOnFailureListener(e -> {
                                                loadingDialog.dismissDialog();

                                                Alerter.create(ProfileActivity.this)
                                                        .setText("Uh oh! Something broke. Try again!")
                                                        .setTextAppearance(R.style.AlertText)
                                                        .setBackgroundColorRes(R.color.errorColor)
                                                        .setIcon(R.drawable.ic_alert_error)
                                                        .setDuration(2500)
                                                        .disableOutsideTouch()
                                                        .show();
                                            });
                                        } else {
                                            loadingDialog.dismissDialog();

                                            Alerter.create(ProfileActivity.this)
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
                    } else if (result.getResultCode() == ImagePicker.RESULT_ERROR) {
                        Alerter.create(ProfileActivity.this)
                                .setText("Uh oh! Something broke. Try again!")
                                .setTextAppearance(R.style.AlertText)
                                .setBackgroundColorRes(R.color.errorColor)
                                .setIcon(R.drawable.ic_alert_error)
                                .setDuration(2500)
                                .disableOutsideTouch()
                                .show();
                    } else {
                        return;
                    }
                }
            });

    private void logout() {
        MaterialDialog materialDialog = new MaterialDialog.Builder(ProfileActivity.this)
                .setTitle("Log out of CureHer?")
                .setMessage("Are you sure of logging out of CureHer?")
                .setCancelable(false)
                .setPositiveButton("Yes", R.drawable.ic_dialog_okay, (dialogInterface, which) -> {
                    dialogInterface.dismiss();
                    loadingDialog.startDialog();
                    DocumentReference documentReference = patientsRef.document(preferenceManager.getString(Constants.KEY_PATIENT_ID));

                    HashMap<String, Object> updates = new HashMap<>();
                    updates.put(Constants.KEY_PATIENT_TOKEN, FieldValue.delete());
                    documentReference.update(updates)
                            .addOnCompleteListener(task -> {
                                if (task.isSuccessful()) {
                                    loadingDialog.dismissDialog();

                                    firebaseAuth.signOut();
                                    preferenceManager.clearPreferences();
                                    Toast.makeText(ProfileActivity.this, "Logged Out!", Toast.LENGTH_SHORT).show();
                                    startActivity(new Intent(getApplicationContext(), OnBoardingActivity.class));
                                    Animatoo.animateSlideRight(ProfileActivity.this);
                                    finish();
                                } else {
                                    loadingDialog.dismissDialog();

                                    Alerter.create(ProfileActivity.this)
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
        super.onBackPressed();
        finishAffinity();
    }
}