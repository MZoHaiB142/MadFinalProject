package com.example.madfinalproject;

import android.content.DialogInterface;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.os.Bundle;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

import com.google.android.material.button.MaterialButton;
import com.google.firebase.firestore.FirebaseFirestore;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;

public class CostCalculatorActivity extends AppCompatActivity {

    // 1. UI Elements
    private TextView tvDestinationCountry, tvUniversity, tvDegreeType;
    private TextView tvTotalEstimate, tvExchangeRate;
    private TextView tvTuitionSub, tvTuitionAmount, tvTestAmount, tvFlightAmount;
    private TextView btnAddExpense;
    private LinearLayout customExpensesContainer;

    // Toggle Buttons
    private MaterialButton btnFullYear, btnFirstSemester;

    // 2. Firebase Database Reference
    private FirebaseFirestore db;

    // 3. Variables for Calculation
    private double exchangeRate = 180.0;
    private String currentCurrencyCode = "AUD";
    private double currentTuitionForeign = 20000.0;
    private double ieltsFeePkr = 65000.0;
    private double flightFeePkr = 300000.0;
    private boolean isFirstSemesterOnly = true;

    // 🔥 Object class and list to track custom expenses for Edit/Delete
    private static class CustomExpense {
        String name;
        double amount;
        CustomExpense(String name, double amount) {
            this.name = name;
            this.amount = amount;
        }
    }
    private ArrayList<CustomExpense> customExpensesList = new ArrayList<>();

    // Emoji Flags
    private final String[] countries = {"🇦🇺 Australia", "🇬🇧 UK", "🇨🇦 Canada", "🇺🇸 USA", "🇩🇪 Germany"};
    private final String[] universities = {"University of Sydney", "Monash University", "University of Melbourne", "UNSW"};
    private final String[] degrees = {"Bachelors (4 Years)", "Masters (2 Years)", "PhD (3 Years)"};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cost_calculator);

        db = FirebaseFirestore.getInstance();

        initViews();
        setupClickListeners();

        updateToggleUI();
        tvExchangeRate.setText("Select a country to see live rates");
        calculateAndDisplayTotals();
    }

    private void initViews() {
        tvDestinationCountry = findViewById(R.id.tvDestinationCountry);
        tvUniversity = findViewById(R.id.tvUniversity);
        tvDegreeType = findViewById(R.id.tvDegreeType);

        btnFullYear = findViewById(R.id.btnFullYear);
        btnFirstSemester = findViewById(R.id.btnFirstSemester);

        tvTotalEstimate = findViewById(R.id.tvTotalEstimate);
        tvExchangeRate = findViewById(R.id.tvExchangeRate);

        tvTuitionSub = findViewById(R.id.tvTuitionSub);
        tvTuitionAmount = findViewById(R.id.tvTuitionAmount);
        tvTestAmount = findViewById(R.id.tvTestAmount);
        tvFlightAmount = findViewById(R.id.tvFlightAmount);

        btnAddExpense = findViewById(R.id.btnAddExpense);
        customExpensesContainer = findViewById(R.id.customExpensesContainer);
    }

    private void setupClickListeners() {
        tvDestinationCountry.setOnClickListener(v -> showSelectionDialog("Select Country", countries, tvDestinationCountry));
        tvUniversity.setOnClickListener(v -> showSelectionDialog("Select University", universities, tvUniversity));
        tvDegreeType.setOnClickListener(v -> showSelectionDialog("Select Program", degrees, tvDegreeType));

        btnFullYear.setOnClickListener(v -> {
            if (isFirstSemesterOnly) {
                isFirstSemesterOnly = false;
                updateToggleUI();
                calculateAndDisplayTotals();
            }
        });

        btnFirstSemester.setOnClickListener(v -> {
            if (!isFirstSemesterOnly) {
                isFirstSemesterOnly = true;
                updateToggleUI();
                calculateAndDisplayTotals();
            }
        });

        // Pass 'null' indicating we are adding a brand new expense
        btnAddExpense.setOnClickListener(v -> showExpenseDialog(null));
    }

    private void updateToggleUI() {
        if (isFirstSemesterOnly) {
            btnFirstSemester.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#2164F3")));
            btnFirstSemester.setTextColor(Color.WHITE);
            btnFullYear.setBackgroundTintList(ColorStateList.valueOf(Color.TRANSPARENT));
            btnFullYear.setTextColor(Color.parseColor("#6B7280"));
        } else {
            btnFullYear.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#2164F3")));
            btnFullYear.setTextColor(Color.WHITE);
            btnFirstSemester.setBackgroundTintList(ColorStateList.valueOf(Color.TRANSPARENT));
            btnFirstSemester.setTextColor(Color.parseColor("#6B7280"));
        }
    }

    private void showSelectionDialog(String title, String[] items, TextView targetTextView) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(title);
        builder.setItems(items, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                String selectedItem = items[which];

                targetTextView.setText(selectedItem);
                targetTextView.setTextColor(Color.parseColor("#111827"));

                if (title.equals("Select Country")) {
                    if (selectedItem.contains("Australia")) currentCurrencyCode = "AUD";
                    else if (selectedItem.contains("UK")) currentCurrencyCode = "GBP";
                    else if (selectedItem.contains("USA")) currentCurrencyCode = "USD";
                    else if (selectedItem.contains("Canada")) currentCurrencyCode = "CAD";
                    else if (selectedItem.contains("Germany")) currentCurrencyCode = "EUR";

                    fetchRealTimeExchangeRate(currentCurrencyCode);
                } else {
                    calculateAndDisplayTotals();
                }
            }
        });
        builder.show();
    }

    // 🔥 Modified to handle both "Add" and "Edit/Delete"
    private void showExpenseDialog(final CustomExpense expenseToEdit) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_add_expense, null);
        builder.setView(dialogView);

        AlertDialog dialog = builder.create();
        dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);

        EditText etName = dialogView.findViewById(R.id.etExpenseName);
        EditText etAmount = dialogView.findViewById(R.id.etExpenseAmount);
        MaterialButton btnAdd = dialogView.findViewById(R.id.btnDialogAdd);
        MaterialButton btnCancel = dialogView.findViewById(R.id.btnDialogCancel);
        ImageView btnDelete = dialogView.findViewById(R.id.btnDialogDelete);

        // If editing an existing expense, pre-fill data and show delete icon
        if (expenseToEdit != null) {
            etName.setText(expenseToEdit.name);
            etAmount.setText(String.format("%.0f", expenseToEdit.amount));
            btnAdd.setText("Update");
            btnDelete.setVisibility(View.VISIBLE);

            // Handle Delete
            btnDelete.setOnClickListener(v -> {
                customExpensesList.remove(expenseToEdit);
                renderCustomExpenses(); // Re-draw UI
                dialog.dismiss();
            });
        }

        btnCancel.setOnClickListener(v -> dialog.dismiss());

        // Handle Add / Update
        btnAdd.setOnClickListener(v -> {
            String name = etName.getText().toString().trim();
            String amountStr = etAmount.getText().toString().trim();

            if (name.isEmpty() || amountStr.isEmpty()) {
                Toast.makeText(this, "Please fill both fields", Toast.LENGTH_SHORT).show();
                return;
            }

            double amount = Double.parseDouble(amountStr);

            if (expenseToEdit == null) {
                // Add new
                customExpensesList.add(new CustomExpense(name, amount));
            } else {
                // Update existing
                expenseToEdit.name = name;
                expenseToEdit.amount = amount;
            }

            renderCustomExpenses(); // Re-draw UI
            dialog.dismiss();
        });

        dialog.show();
    }

    // 🔥 Refreshes the container based on the list
    private void renderCustomExpenses() {
        customExpensesContainer.removeAllViews();

        for (CustomExpense expense : customExpensesList) {
            View card = createCustomExpenseCardView(expense);

            // Make the entire card clickable to edit/delete
            card.setOnClickListener(v -> showExpenseDialog(expense));

            customExpensesContainer.addView(card);
        }

        calculateAndDisplayTotals(); // Recalculate totals after drawing
    }

    // Creates the dynamic UI element for a single expense
    private View createCustomExpenseCardView(CustomExpense expense) {
        com.google.android.material.card.MaterialCardView card = new com.google.android.material.card.MaterialCardView(this);
        LinearLayout.LayoutParams cardParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        cardParams.setMargins(0, 0, 0, dpToPx(12));
        card.setLayoutParams(cardParams);
        card.setCardBackgroundColor(Color.WHITE);
        card.setRadius(dpToPx(12));
        card.setCardElevation(0f);
        card.setStrokeColor(Color.parseColor("#2164F3"));
        card.setStrokeWidth(dpToPx(1));

        // Adding ripple effect for touch feedback
        card.setClickable(true);
        card.setFocusable(true);
        TypedValue outValue = new TypedValue();
        getTheme().resolveAttribute(android.R.attr.selectableItemBackground, outValue, true);
        card.setForeground(getDrawable(outValue.resourceId));

        LinearLayout innerLayout = new LinearLayout(this);
        innerLayout.setLayoutParams(new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT));
        innerLayout.setOrientation(LinearLayout.HORIZONTAL);
        innerLayout.setPadding(dpToPx(16), dpToPx(16), dpToPx(16), dpToPx(16));
        innerLayout.setGravity(android.view.Gravity.CENTER_VERTICAL);

        CardView iconCard = new CardView(this);
        LinearLayout.LayoutParams iconParams = new LinearLayout.LayoutParams(dpToPx(40), dpToPx(40));
        iconParams.setMarginEnd(dpToPx(12));
        iconCard.setLayoutParams(iconParams);
        iconCard.setRadius(dpToPx(20));
        iconCard.setCardBackgroundColor(Color.WHITE);
        iconCard.setCardElevation(0f);

        ImageView icon = new ImageView(this);
        icon.setImageResource(android.R.drawable.ic_menu_edit);
        icon.setColorFilter(Color.parseColor("#2164F3"));
        icon.setPadding(dpToPx(8), dpToPx(8), dpToPx(8), dpToPx(8));
        iconCard.addView(icon);

        LinearLayout textLayout = new LinearLayout(this);
        LinearLayout.LayoutParams textParams = new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f);
        textLayout.setLayoutParams(textParams);
        textLayout.setOrientation(LinearLayout.VERTICAL);

        TextView tvTitle = new TextView(this);
        tvTitle.setText(expense.name);
        tvTitle.setTextColor(Color.parseColor("#111827"));
        tvTitle.setTextSize(15f);
        tvTitle.setTypeface(null, android.graphics.Typeface.BOLD);

        TextView tvSub = new TextView(this);
        tvSub.setText("Tap to edit or delete");
        tvSub.setTextColor(Color.parseColor("#6B7280"));
        tvSub.setTextSize(12f);

        textLayout.addView(tvTitle);
        textLayout.addView(tvSub);

        TextView tvAmount = new TextView(this);
        tvAmount.setText("Rs " + String.format("%,.0f", expense.amount));
        tvAmount.setTextColor(Color.parseColor("#111827"));
        tvAmount.setTextSize(15f);
        tvAmount.setTypeface(null, android.graphics.Typeface.BOLD);

        innerLayout.addView(iconCard);
        innerLayout.addView(textLayout);
        innerLayout.addView(tvAmount);
        card.addView(innerLayout);

        return card;
    }

    private int dpToPx(int dp) {
        return (int) TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_DIP, dp, getResources().getDisplayMetrics());
    }

    private void fetchRealTimeExchangeRate(String baseCurrency) {
        tvExchangeRate.setText("Fetching live rate...");

        new Thread(() -> {
            try {
                URL url = new URL("https://open.er-api.com/v6/latest/" + baseCurrency);
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("GET");

                BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                StringBuilder response = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) {
                    response.append(line);
                }
                reader.close();

                JSONObject jsonObject = new JSONObject(response.toString());
                JSONObject rates = jsonObject.getJSONObject("rates");
                final double livePkrRate = rates.getDouble("PKR");

                runOnUiThread(() -> {
                    exchangeRate = livePkrRate;
                    tvExchangeRate.setText("Live Rate: 1 " + baseCurrency + " = Rs " + String.format("%.2f", exchangeRate));
                    calculateAndDisplayTotals();
                });

            } catch (Exception e) {
                e.printStackTrace();
                runOnUiThread(() -> {
                    if(baseCurrency.equals("AUD")) exchangeRate = 185.0;
                    else if(baseCurrency.equals("GBP")) exchangeRate = 350.0;
                    else if(baseCurrency.equals("USD")) exchangeRate = 278.0;
                    else if(baseCurrency.equals("EUR")) exchangeRate = 300.0;
                    else if(baseCurrency.equals("CAD")) exchangeRate = 205.0;

                    tvExchangeRate.setText("Offline Rate: 1 " + baseCurrency + " = Rs " + exchangeRate);
                    calculateAndDisplayTotals();
                });
            }
        }).start();
    }

    private void calculateAndDisplayTotals() {
        String country = tvDestinationCountry.getText().toString();
        String uni = tvUniversity.getText().toString();
        String program = tvDegreeType.getText().toString();

        // Calculate dynamic custom expenses total
        double totalCustomExpensesPkr = 0.0;
        for (CustomExpense expense : customExpensesList) {
            totalCustomExpensesPkr += expense.amount;
        }

        if (country.equals("Select Country") || uni.equals("Select University") || program.equals("Select Program")) {
            tvTuitionAmount.setText("Rs 0");
            tvTestAmount.setText("Rs 0");
            tvFlightAmount.setText("Rs 0");
            tvTotalEstimate.setText("Rs " + String.format("%,.0f", totalCustomExpensesPkr));
            tvTuitionSub.setText("Select all fields to view details");
            return;
        }

        double totalTuitionPkr = currentTuitionForeign * exchangeRate;
        String currencySymbol = getCurrencySymbol(currentCurrencyCode);

        if (isFirstSemesterOnly) {
            tvTuitionSub.setText("First Semester (" + currencySymbol + String.format("%,.0f", currentTuitionForeign) + ")");
        } else {
            totalTuitionPkr = totalTuitionPkr * 2;
            tvTuitionSub.setText("Full Year (" + currencySymbol + String.format("%,.0f", currentTuitionForeign * 2) + ")");
        }

        double grandTotal = totalTuitionPkr + ieltsFeePkr + flightFeePkr + totalCustomExpensesPkr;

        tvTuitionAmount.setText("Rs " + String.format("%,.0f", totalTuitionPkr));
        tvTestAmount.setText("Rs " + String.format("%,.0f", ieltsFeePkr));
        tvFlightAmount.setText("Rs " + String.format("%,.0f", flightFeePkr));

        tvTotalEstimate.setText("Rs " + String.format("%,.0f", grandTotal));
    }

    private String getCurrencySymbol(String code) {
        switch (code) {
            case "AUD": return "A$";
            case "GBP": return "£";
            case "USD": return "$";
            case "CAD": return "C$";
            case "EUR": return "€";
            default: return "$";
        }
    }
}