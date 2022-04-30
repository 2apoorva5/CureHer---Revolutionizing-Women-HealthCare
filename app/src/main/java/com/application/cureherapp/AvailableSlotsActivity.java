package com.application.cureherapp;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.application.cureherapp.Helper.LoadingDialog;
import com.application.cureherapp.Model.TimeSlot;
import com.application.cureherapp.Utilities.Common;
import com.application.cureherapp.Utilities.Constants;
import com.application.cureherapp.Utilities.PreferenceManager;
import com.application.cureherapp.databinding.ActivityAvailableSlotsBinding;
import com.blogspot.atifsoftwares.animatoolib.Animatoo;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.tapadoo.alerter.Alerter;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import devs.mulham.horizontalcalendar.HorizontalCalendar;
import devs.mulham.horizontalcalendar.utils.HorizontalCalendarListener;

public class AvailableSlotsActivity extends AppCompatActivity {

    private ActivityAvailableSlotsBinding binding;

    private DocumentReference doctorRef;

    private PreferenceManager preferenceManager;
    private LoadingDialog loadingDialog;

    private Calendar selectedDate;
    private SimpleDateFormat dateFormat;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_available_slots);

        binding = ActivityAvailableSlotsBinding.inflate(getLayoutInflater());
        View view = binding.getRoot();
        setContentView(view);

        getWindow().addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
        getWindow().setStatusBarColor(getColor(R.color.colorViews));
        getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);

        preferenceManager = new PreferenceManager(getApplicationContext());
        loadingDialog = new LoadingDialog(AvailableSlotsActivity.this);

        if (!preferenceManager.getBoolean(Constants.KEY_IS_SIGNED_IN_AS_PATIENT)) {
            startActivity(new Intent(getApplicationContext(), OnBoardingActivity.class));
            Animatoo.animateSlideRight(AvailableSlotsActivity.this);
            finish();
        }

        checkNetworkConnection();
    }

    private void checkNetworkConnection() {
        // Check internet connection
        if (!Common.isConnectedToInternet(AvailableSlotsActivity.this)) {
            Common.openNoInternetBottomSheet(AvailableSlotsActivity.this);
        } else {
            doctorRef = FirebaseFirestore.getInstance().collection(Constants.KEY_COLLECTION_DOCTORS)
                    .document(preferenceManager.getString(Constants.KEY_CHOSEN_DOCTOR));
            setActionOnViews();
        }
    }

    private void setActionOnViews() {
        binding.closeBtn.setOnClickListener(view1 -> {
            onBackPressed();
            finish();
        });

        dateFormat = new SimpleDateFormat("dd-MM-yyyy");
        selectedDate = Calendar.getInstance();
        selectedDate.add(Calendar.DATE, 0);    // Init with current date

        Calendar date = Calendar.getInstance();
        date.add(Calendar.DATE, 0);            // Current date
        loadAvailableTimeSlots(dateFormat.format(date.getTime()));      // Load current date's slots

        // RecyclerView
        binding.recyclerTimeSlots.setHasFixedSize(true);
        binding.recyclerTimeSlots.setLayoutManager(new GridLayoutManager(AvailableSlotsActivity.this, 3));

        // Calendar
        Calendar startDate = Calendar.getInstance();
        startDate.add(Calendar.DATE, 0);
        Calendar endDate = Calendar.getInstance();
        endDate.add(Calendar.DATE, 9);

        HorizontalCalendar horizontalCalendar = new HorizontalCalendar.Builder(AvailableSlotsActivity.this, R.id.calendar)
                .range(startDate, endDate)
                .datesNumberOnScreen(5)
                .mode(HorizontalCalendar.Mode.DAYS)
                .defaultSelectedDate(startDate)
                .build();
        horizontalCalendar.setCalendarListener(new HorizontalCalendarListener() {
            @Override
            public void onDateSelected(Calendar date, int position) {
                if (selectedDate.getTimeInMillis() != date.getTimeInMillis()) {
                    selectedDate = date;
                    loadAvailableTimeSlots(dateFormat.format(date.getTime()));      // Load selected date's slots
                }
            }
        });
    }

    private void loadAvailableTimeSlots(String appointmentDate) {
        loadingDialog.startDialog();

        doctorRef.get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                DocumentSnapshot doctorSnapshot = task.getResult();

                if (doctorSnapshot.exists()) {
                    doctorRef.collection(Constants.KEY_COLLECTION_APPOINTMENTS)
                            .document(appointmentDate)
                            .collection(Constants.KEY_COLLECTION_BOOKED_SLOTS)
                            .get().addOnCompleteListener(task1 -> {
                        if (task1.isSuccessful()) {
                            QuerySnapshot querySnapshot = task1.getResult();

                            loadingDialog.dismissDialog();

                            if (querySnapshot.isEmpty() || querySnapshot.size() == 0) {
                                // No appointment present
                                TimeSlotAdapter timeSlotAdapter = new TimeSlotAdapter(getApplicationContext());
                                binding.recyclerTimeSlots.setAdapter(timeSlotAdapter);
                            } else {
                                // Appointment present
                                List<TimeSlot> timeSlots = new ArrayList<>();

                                for (QueryDocumentSnapshot slotSnapshot : task1.getResult())
                                    timeSlots.add(slotSnapshot.toObject(TimeSlot.class));

                                TimeSlotAdapter timeSlotAdapter = new TimeSlotAdapter(getApplicationContext(), timeSlots);
                                binding.recyclerTimeSlots.setAdapter(timeSlotAdapter);
                            }
                        } else {
                            loadingDialog.dismissDialog();

                            Alerter.create(AvailableSlotsActivity.this)
                                    .setText("Uh oh! Something broke. Try again!")
                                    .setTextAppearance(R.style.AlertText)
                                    .setBackgroundColorRes(R.color.errorColor)
                                    .setIcon(R.drawable.ic_alert_error)
                                    .setDuration(2500)
                                    .disableOutsideTouch()
                                    .show();
                        }
                    });
                } else {
                    loadingDialog.dismissDialog();

                    Alerter.create(AvailableSlotsActivity.this)
                            .setText("Uh oh! Something broke. Try again!")
                            .setTextAppearance(R.style.AlertText)
                            .setBackgroundColorRes(R.color.errorColor)
                            .setIcon(R.drawable.ic_alert_error)
                            .setDuration(2500)
                            .disableOutsideTouch()
                            .show();
                }
            } else {
                loadingDialog.dismissDialog();

                Alerter.create(AvailableSlotsActivity.this)
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

    public class TimeSlotAdapter extends RecyclerView.Adapter<TimeSlotAdapter.TimeSlotViewHolder> {

        Context context;
        List<TimeSlot> timeSlotList;

        public TimeSlotAdapter(Context context) {
            this.context = context;
            this.timeSlotList = new ArrayList<>();
        }

        public TimeSlotAdapter(Context context, List<TimeSlot> timeSlotList) {
            this.context = context;
            this.timeSlotList = timeSlotList;
        }

        @NonNull
        @Override
        public TimeSlotViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(context)
                    .inflate(R.layout.layout_time_slot, parent, false);
            return new TimeSlotViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull TimeSlotViewHolder holder, int position) {
            holder.timeSlot.setText(new StringBuilder(Common.convertTimeSlotToString(position)).toString());

            if (timeSlotList.size() == 0) {
                // If all time slots are available, just show the list
                holder.timeSlot.setTextColor(context.getColor(R.color.colorTextDark));
                holder.backgroundSlot.setBackgroundResource(R.drawable.background_slot_available);
            } else {
                // If slots are booked
                for (TimeSlot slotValue : timeSlotList) {
                    // Loop all time slots from server and set different colors
                    int slot = Integer.parseInt(slotValue.getSlot().toString());

                    if (slot == position) {
                        holder.timeSlot.setTextColor(context.getColor(R.color.colorInactive));
                        holder.backgroundSlot.setBackgroundResource(R.drawable.background_slot_unavailable);
                    }
                }
            }
        }

        @Override
        public int getItemCount() {
            return Constants.TOTAL_TIME_SLOTS;
        }

        public class TimeSlotViewHolder extends RecyclerView.ViewHolder {
            ConstraintLayout backgroundSlot;
            TextView timeSlot;

            public TimeSlotViewHolder(@NonNull View itemView) {
                super(itemView);

                backgroundSlot = itemView.findViewById(R.id.background_slot);
                timeSlot = itemView.findViewById(R.id.time_slot);
            }
        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        Animatoo.animateSlideDown(AvailableSlotsActivity.this);
    }
}