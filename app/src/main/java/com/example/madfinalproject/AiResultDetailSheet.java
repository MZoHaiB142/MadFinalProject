package com.example.madfinalproject;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;

import com.example.madfinalproject.databinding.BottomSheetAiDetailBinding;
import com.example.madfinalproject.models.AiUniversityResult;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;

import java.io.Serializable;

public class AiResultDetailSheet extends BottomSheetDialogFragment {

    private static final String KEY = "ai_result";
    private BottomSheetAiDetailBinding binding;
    private AiUniversityResult result;

    public static AiResultDetailSheet newInstance(AiUniversityResult r) {
        AiResultDetailSheet sheet = new AiResultDetailSheet();
        Bundle args = new Bundle();
        args.putSerializable(KEY, (Serializable) r);
        sheet.setArguments(args);
        return sheet;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inf,
                             @Nullable ViewGroup container,
                             @Nullable Bundle saved) {
        binding = BottomSheetAiDetailBinding.inflate(inf, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view,
                              @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        if (getArguments() != null) {
            result = (AiUniversityResult) getArguments().getSerializable(KEY);
        }
        if (result == null) { dismiss(); return; }

        bindData();

        binding.btnClose.setOnClickListener(v -> dismiss());
        binding.btnApplyNow.setOnClickListener(v -> {
            // Navigate to SOP generator
            dismiss();
        });
    }

    private void bindData() {
        // Header
        binding.tvSheetUniName.setText(result.getUniversityName());
        binding.tvSheetCategory.setText(result.getCategory());
        binding.tvSheetScore.setText(
                result.getEligibilityPercentage() + "% Match");

        // Category color
        setCategoryColor(result.getCategory());

        // Strengths
        binding.strengthsLayout.removeAllViews();
        for (String s : result.getStrengths()) {
            addRow(binding.strengthsLayout, "✓ " + s, R.color.green);
        }

        // Weak areas
        binding.weakLayout.removeAllViews();
        for (String w : result.getWeakAreas()) {
            addRow(binding.weakLayout, "✗ " + w, R.color.red);
        }

        // Scholarship
        binding.tvSheetScholarship.setText(result.getScholarshipChance());

        // Visa insight
        binding.tvSheetVisaInsight.setText(result.getVisaInsight());

        // Recommendation
        binding.tvSheetRecommendation.setText(
                result.getConsultantRecommendation());

        // Priority
        binding.tvSheetPriority.setText(
                "Apply Priority: #" + result.getApplyPriority());
    }

    private void addRow(LinearLayout layout, String text, int colorRes) {
        TextView tv = new TextView(requireContext());
        tv.setText(text);
        tv.setTextSize(12f);
        tv.setTextColor(ContextCompat.getColor(requireContext(), colorRes));
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        );
        lp.setMargins(0, 0, 0, 8);
        tv.setLayoutParams(lp);
        layout.addView(tv);
    }

    private void setCategoryColor(String cat) {
        int bgRes, colorRes;
        switch (cat) {
            case "Safe":
                bgRes = R.drawable.bg_badge_green; colorRes = R.color.green; break;
            case "Target":
                bgRes = R.drawable.bg_badge_blue; colorRes = R.color.blue_light; break;
            case "Ambitious":
                bgRes = R.drawable.bg_badge_amber; colorRes = R.color.amber; break;
            default:
                bgRes = R.drawable.bg_badge_red; colorRes = R.color.red; break;
        }
        binding.tvSheetCategory.setBackgroundResource(bgRes);
        binding.tvSheetCategory.setTextColor(
                ContextCompat.getColor(requireContext(), colorRes));
        binding.tvSheetScore.setTextColor(
                ContextCompat.getColor(requireContext(), colorRes));
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}