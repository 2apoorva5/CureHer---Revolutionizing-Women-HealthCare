package com.application.cureherapp;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.speech.RecognizerIntent;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.ScaleAnimation;
import android.view.inputmethod.EditorInfo;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.application.cureherapp.Helper.LoadingDialog;
import com.application.cureherapp.Model.Doctor;
import com.application.cureherapp.Utilities.Common;
import com.application.cureherapp.Utilities.Constants;
import com.application.cureherapp.Utilities.PreferenceManager;
import com.application.cureherapp.databinding.ActivityDoctorsListBinding;
import com.blogspot.atifsoftwares.animatoolib.Animatoo;
import com.bumptech.glide.Glide;
import com.firebase.ui.firestore.FirestoreRecyclerAdapter;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.Query;
import com.makeramen.roundedimageview.RoundedImageView;
import com.tapadoo.alerter.Alerter;

import net.yslibrary.android.keyboardvisibilityevent.KeyboardVisibilityEvent;
import net.yslibrary.android.keyboardvisibilityevent.util.UIUtil;

import java.util.ArrayList;
import java.util.Locale;

import per.wsj.library.AndRatingBar;

public class DoctorsListActivity extends AppCompatActivity {

    private ActivityDoctorsListBinding binding;

    private Query parentQuery;
    private FirestoreRecyclerAdapter<Doctor, DoctorViewHolder> doctorAdapter;

    private PreferenceManager preferenceManager;
    private LoadingDialog loadingDialog;

    private static int LAST_POSITION = -1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityDoctorsListBinding.inflate(getLayoutInflater());
        View view = binding.getRoot();
        setContentView(view);

        getWindow().addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
        getWindow().setStatusBarColor(getColor(R.color.colorBackground));
        getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);

        preferenceManager = new PreferenceManager(getApplicationContext());
        loadingDialog = new LoadingDialog(DoctorsListActivity.this);

        if (!preferenceManager.getBoolean(Constants.KEY_IS_SIGNED_IN_AS_PATIENT)) {
            startActivity(new Intent(getApplicationContext(), OnBoardingActivity.class));
            Animatoo.animateSlideRight(DoctorsListActivity.this);
            finish();
        }

        checkNetworkConnection();
    }

    private void checkNetworkConnection() {
        // Check internet connection
        if (!Common.isConnectedToInternet(DoctorsListActivity.this)) {
            binding.swipeRefreshLayout.setRefreshing(false);
            Common.openNoInternetBottomSheet(DoctorsListActivity.this);
        } else {
            if (preferenceManager.getString(Constants.KEY_CHOSEN_CATEGORY).trim().equals("") ||
                    preferenceManager.getString(Constants.KEY_CHOSEN_CATEGORY).trim().length() == 0 ||
                    preferenceManager.getString(Constants.KEY_CHOSEN_CATEGORY).trim().isEmpty() ||
                    preferenceManager.getString(Constants.KEY_CHOSEN_CATEGORY).trim() == null) {
                parentQuery = FirebaseFirestore.getInstance().collection(Constants.KEY_COLLECTION_DOCTORS);
            } else {
                parentQuery = FirebaseFirestore.getInstance().collection(Constants.KEY_COLLECTION_DOCTORS)
                        .whereEqualTo(Constants.KEY_DOCTOR_CATEGORY, preferenceManager.getString(Constants.KEY_CHOSEN_CATEGORY).trim());
            }
            setActionOnViews();
        }
    }

    @SuppressLint("ResourceType")
    private void setActionOnViews() {
        binding.layoutEmpty.setVisibility(View.GONE);

        binding.backBtn.setOnClickListener(view1 -> {
            onBackPressed();
            finish();
        });

        if (preferenceManager.getString(Constants.KEY_CHOSEN_CATEGORY).trim().equals("") ||
                preferenceManager.getString(Constants.KEY_CHOSEN_CATEGORY).trim().length() == 0 ||
                preferenceManager.getString(Constants.KEY_CHOSEN_CATEGORY).trim().isEmpty() ||
                preferenceManager.getString(Constants.KEY_CHOSEN_CATEGORY).trim() == null) {
            binding.title.setText("All Doctors");
            binding.searchInput.setHint("Search doctor");
        } else {
            binding.title.setText(String.format("Doctors - %s", preferenceManager.getString(Constants.KEY_CHOSEN_CATEGORY)));
            binding.searchInput.setHint(String.format("Search doctor for %s", preferenceManager.getString(Constants.KEY_CHOSEN_CATEGORY)));
        }

        KeyboardVisibilityEvent.setEventListener(DoctorsListActivity.this, isOpen -> {
            if (!isOpen) {
                binding.searchInput.clearFocus();
            }
        });

        binding.swipeRefreshLayout.setRefreshing(false);
        binding.swipeRefreshLayout.setOnRefreshListener(this::checkNetworkConnection);

        loadDoctors();

        binding.micBtn.setOnClickListener(view -> {
            Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
            intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                    RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
            intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault());
            intent.putExtra(RecognizerIntent.EXTRA_PROMPT, String.format("Name a doctor.."));

            try {
                activityResultLauncher.launch(intent);
            } catch (ActivityNotFoundException e) {
                Alerter.create(DoctorsListActivity.this)
                        .setText("Uh oh! Something broke. Try again!")
                        .setTextAppearance(R.style.AlertText)
                        .setBackgroundColorRes(R.color.errorColor)
                        .setIcon(R.drawable.ic_alert_error)
                        .setDuration(2500)
                        .disableOutsideTouch()
                        .show();
            }
        });

        binding.searchInput.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence s, int i, int i1, int i2) {
                Query searchQuery;
                if (s.toString().isEmpty()) {
                    searchQuery = parentQuery;
                } else {
                    searchQuery = parentQuery.orderBy(Constants.KEY_DOCTOR_SEARCH_KEYWORD, Query.Direction.ASCENDING)
                            .startAt(s.toString().toLowerCase().trim()).endAt(s.toString().toLowerCase().trim() + "\uf8ff");
                }

                FirestoreRecyclerOptions<Doctor> searchOptions = new FirestoreRecyclerOptions.Builder<Doctor>()
                        .setLifecycleOwner(DoctorsListActivity.this)
                        .setQuery(searchQuery, Doctor.class)
                        .build();

                doctorAdapter.updateOptions(searchOptions);
            }

            @Override
            public void afterTextChanged(Editable s) {
                binding.searchInput.setOnEditorActionListener((v, actionId, event) -> {
                    if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                        UIUtil.hideKeyboard(DoctorsListActivity.this);
                        Query searchQuery;
                        if (s.toString().isEmpty()) {
                            searchQuery = parentQuery;
                        } else {
                            searchQuery = parentQuery.orderBy(Constants.KEY_DOCTOR_SEARCH_KEYWORD, Query.Direction.ASCENDING)
                                    .startAt(s.toString().toLowerCase().trim()).endAt(s.toString().toLowerCase().trim() + "\uf8ff");
                        }

                        FirestoreRecyclerOptions<Doctor> searchOptions = new FirestoreRecyclerOptions.Builder<Doctor>()
                                .setLifecycleOwner(DoctorsListActivity.this)
                                .setQuery(searchQuery, Doctor.class)
                                .build();

                        doctorAdapter.updateOptions(searchOptions);
                        return true;
                    }
                    return false;
                });
            }
        });

        binding.sortBtn.setOnClickListener(view -> {
            final Dialog dialog = new Dialog(DoctorsListActivity.this);
            dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
            dialog.setContentView(R.layout.bottomsheet_sort_doctor);
            dialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
            dialog.getWindow().getAttributes().windowAnimations = R.style.BottomSheetAnimation;
            dialog.getWindow().setGravity(Gravity.BOTTOM);
            dialog.setCancelable(false);

            ImageView closeBtn = dialog.findViewById(R.id.close_btn);
            ConstraintLayout relevance = dialog.findViewById(R.id.relevance);
            ConstraintLayout newestFirst = dialog.findViewById(R.id.newest_first);
            ConstraintLayout nameAZ = dialog.findViewById(R.id.name_a_z);
            ConstraintLayout nameZA = dialog.findViewById(R.id.name_z_a);
            ConstraintLayout chargesLowHigh = dialog.findViewById(R.id.charges_low_high);
            ConstraintLayout chargesHighLow = dialog.findViewById(R.id.charges_high_low);
            ConstraintLayout rating = dialog.findViewById(R.id.rating);
            ConstraintLayout experience = dialog.findViewById(R.id.experience);

            dialog.show();

            closeBtn.setOnClickListener(view12 -> dialog.dismiss());

            relevance.setOnClickListener(view13 -> {
                Query sortQuery = parentQuery;
                FirestoreRecyclerOptions<Doctor> sortOptions = new FirestoreRecyclerOptions.Builder<Doctor>()
                        .setLifecycleOwner(DoctorsListActivity.this)
                        .setQuery(sortQuery, Doctor.class)
                        .build();

                doctorAdapter.updateOptions(sortOptions);

                dialog.dismiss();
            });

            newestFirst.setOnClickListener(view13 -> {
                Query sortQuery = parentQuery.orderBy(Constants.KEY_DOCTOR_TIMESTAMP, Query.Direction.DESCENDING);
                FirestoreRecyclerOptions<Doctor> sortOptions = new FirestoreRecyclerOptions.Builder<Doctor>()
                        .setLifecycleOwner(DoctorsListActivity.this)
                        .setQuery(sortQuery, Doctor.class)
                        .build();

                doctorAdapter.updateOptions(sortOptions);

                dialog.dismiss();
            });

            nameAZ.setOnClickListener(view13 -> {
                Query sortQuery = parentQuery.orderBy(Constants.KEY_DOCTOR_NAME, Query.Direction.ASCENDING);
                FirestoreRecyclerOptions<Doctor> sortOptions = new FirestoreRecyclerOptions.Builder<Doctor>()
                        .setLifecycleOwner(DoctorsListActivity.this)
                        .setQuery(sortQuery, Doctor.class)
                        .build();

                doctorAdapter.updateOptions(sortOptions);

                dialog.dismiss();
            });

            nameZA.setOnClickListener(view13 -> {
                Query sortQuery = parentQuery.orderBy(Constants.KEY_DOCTOR_NAME, Query.Direction.DESCENDING);
                FirestoreRecyclerOptions<Doctor> sortOptions = new FirestoreRecyclerOptions.Builder<Doctor>()
                        .setLifecycleOwner(DoctorsListActivity.this)
                        .setQuery(sortQuery, Doctor.class)
                        .build();

                doctorAdapter.updateOptions(sortOptions);

                dialog.dismiss();
            });

            chargesLowHigh.setOnClickListener(view13 -> {
                Query sortQuery = parentQuery.orderBy(Constants.KEY_DOCTOR_CHARGES, Query.Direction.ASCENDING);
                FirestoreRecyclerOptions<Doctor> sortOptions = new FirestoreRecyclerOptions.Builder<Doctor>()
                        .setLifecycleOwner(DoctorsListActivity.this)
                        .setQuery(sortQuery, Doctor.class)
                        .build();

                doctorAdapter.updateOptions(sortOptions);

                dialog.dismiss();
            });

            chargesHighLow.setOnClickListener(view13 -> {
                Query sortQuery = parentQuery.orderBy(Constants.KEY_DOCTOR_CHARGES, Query.Direction.DESCENDING);
                FirestoreRecyclerOptions<Doctor> sortOptions = new FirestoreRecyclerOptions.Builder<Doctor>()
                        .setLifecycleOwner(DoctorsListActivity.this)
                        .setQuery(sortQuery, Doctor.class)
                        .build();

                doctorAdapter.updateOptions(sortOptions);

                dialog.dismiss();
            });

            rating.setOnClickListener(view13 -> {
                Query sortQuery = parentQuery.orderBy(Constants.KEY_DOCTOR_RATINGS, Query.Direction.DESCENDING);
                FirestoreRecyclerOptions<Doctor> sortOptions = new FirestoreRecyclerOptions.Builder<Doctor>()
                        .setLifecycleOwner(DoctorsListActivity.this)
                        .setQuery(sortQuery, Doctor.class)
                        .build();

                doctorAdapter.updateOptions(sortOptions);

                dialog.dismiss();
            });

            experience.setOnClickListener(view13 -> {
                Query sortQuery = parentQuery.orderBy(Constants.KEY_DOCTOR_EXPERIENCE_IN_YEARS, Query.Direction.DESCENDING);
                FirestoreRecyclerOptions<Doctor> sortOptions = new FirestoreRecyclerOptions.Builder<Doctor>()
                        .setLifecycleOwner(DoctorsListActivity.this)
                        .setQuery(sortQuery, Doctor.class)
                        .build();

                doctorAdapter.updateOptions(sortOptions);

                dialog.dismiss();
            });
        });
    }

    @SuppressLint("NotifyDataSetChanged")
    private void loadDoctors() {
        binding.shimmerLayout.setVisibility(View.VISIBLE);
        binding.shimmerLayout.startShimmer();

        Query query = parentQuery;
        FirestoreRecyclerOptions<Doctor> options = new FirestoreRecyclerOptions.Builder<Doctor>()
                .setLifecycleOwner(this)
                .setQuery(query, Doctor.class)
                .build();

        doctorAdapter = new FirestoreRecyclerAdapter<Doctor, DoctorViewHolder>(options) {
            @NonNull
            @Override
            public DoctorViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.layout_doctor_item, parent, false);
                return new DoctorViewHolder(view);
            }

            @Override
            protected void onBindViewHolder(@NonNull DoctorViewHolder holder, int position, @NonNull Doctor model) {
                Glide.with(holder.doctorImage.getContext()).load(model.getDoctorImage()).into(holder.doctorImage);

                holder.doctorName.setText(model.getDoctorName());
                holder.doctorSpeciality.setText(model.getDoctorSpeciality());

                float rating = (float) model.getDoctorRatings();
                holder.doctorRatingBar.setRating(rating);
                holder.doctorRating.setText(String.valueOf(rating));

                holder.clickListener.setOnClickListener(v -> {
                    preferenceManager.putString(Constants.KEY_CHOSEN_DOCTOR, model.getDoctorId());
                    startActivity(new Intent(DoctorsListActivity.this, DoctorInfoActivity.class));
                    Animatoo.animateSlideLeft(DoctorsListActivity.this);
                });

                setAnimation(holder.itemView, position);
            }

            public void setAnimation(View viewToAnimate, int position) {
                if (position > LAST_POSITION) {
                    ScaleAnimation scaleAnimation = new ScaleAnimation(0.0f, 1.0f, 0.0f, 1.0f,
                            Animation.RELATIVE_TO_SELF, 0.5f,
                            Animation.RELATIVE_TO_SELF, 0.5f);
                    scaleAnimation.setDuration(1000);

                    viewToAnimate.setAnimation(scaleAnimation);
                    LAST_POSITION = position;
                }
            }

            @Override
            public void onDataChanged() {
                super.onDataChanged();

                doctorAdapter.notifyDataSetChanged();

                binding.swipeRefreshLayout.setRefreshing(false);
                binding.shimmerLayout.stopShimmer();
                binding.shimmerLayout.setVisibility(View.GONE);

                if (getItemCount() == 0) {
                    binding.layoutEmpty.setVisibility(View.VISIBLE);
                } else {
                    binding.layoutEmpty.setVisibility(View.GONE);
                }
            }

            @Override
            public void onError(@NonNull FirebaseFirestoreException e) {
                super.onError(e);
                binding.swipeRefreshLayout.setRefreshing(false);
                binding.shimmerLayout.stopShimmer();
                binding.shimmerLayout.setVisibility(View.GONE);
                Alerter.create(DoctorsListActivity.this)
                        .setText("Uh oh! Something broke. Try again!")
                        .setTextAppearance(R.style.AlertText)
                        .setBackgroundColorRes(R.color.errorColor)
                        .setIcon(R.drawable.ic_alert_error)
                        .setDuration(2500)
                        .disableOutsideTouch()
                        .show();
            }
        };

        doctorAdapter.notifyDataSetChanged();
        binding.recyclerDoctors.setHasFixedSize(true);
        binding.recyclerDoctors.setLayoutManager(new LinearLayoutManager(DoctorsListActivity.this));
        binding.recyclerDoctors.setAdapter(doctorAdapter);
    }

    public static class DoctorViewHolder extends RecyclerView.ViewHolder {
        ConstraintLayout clickListener;
        RoundedImageView doctorImage;
        TextView doctorName, doctorSpeciality, doctorRating;
        AndRatingBar doctorRatingBar;

        public DoctorViewHolder(@NonNull View itemView) {
            super(itemView);

            clickListener = itemView.findViewById(R.id.click_listener);
            doctorImage = itemView.findViewById(R.id.doctor_image);
            doctorName = itemView.findViewById(R.id.doctor_name);
            doctorSpeciality = itemView.findViewById(R.id.doctor_speciality);
            doctorRating = itemView.findViewById(R.id.doctor_rating);
            doctorRatingBar = itemView.findViewById(R.id.doctor_rating_bar);
        }
    }

    ActivityResultLauncher<Intent> activityResultLauncher =
            registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), new ActivityResultCallback<ActivityResult>() {
                @Override
                public void onActivityResult(ActivityResult result) {
                    if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                        ArrayList<String> input = result.getData().getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
                        binding.searchInput.setText(input.get(0));
                        binding.searchInput.clearFocus();
                    }
                }
            });

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        KeyboardVisibilityEvent.setEventListener(DoctorsListActivity.this, isOpen -> {
            if (isOpen) {
                UIUtil.hideKeyboard(DoctorsListActivity.this);
            }
        });
        Animatoo.animateSlideDown(DoctorsListActivity.this);
    }
}