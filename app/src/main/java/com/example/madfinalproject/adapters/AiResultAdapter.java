package com.example.madfinalproject.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import com.example.madfinalproject.R;
import com.example.madfinalproject.models.AiUniversityResult;
import com.example.madfinalproject.views.ScoreRingView;

import java.util.Objects;

public class AiResultAdapter extends ListAdapter<AiUniversityResult, AiResultAdapter.ResultVH> {

    public interface OnItemClickListener {
        void onClick(AiUniversityResult result);
    }

    private final OnItemClickListener listener;

    private static final DiffUtil.ItemCallback<AiUniversityResult> DIFF =
            new DiffUtil.ItemCallback<AiUniversityResult>() {
                @Override
                public boolean areItemsTheSame(
                        @NonNull AiUniversityResult a, @NonNull AiUniversityResult b) {
                    return Objects.equals(a.getUniversityName(), b.getUniversityName());
                }

                @Override
                public boolean areContentsTheSame(
                        @NonNull AiUniversityResult a, @NonNull AiUniversityResult b) {
                    return a.getEligibilityPercentage() == b.getEligibilityPercentage();
                }
            };

    public AiResultAdapter(OnItemClickListener listener) {
        super(DIFF);
        this.listener = listener;
    }

    @NonNull
    @Override
    public ResultVH onCreateViewHolder(@NonNull ViewGroup parent, int vt) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_ai_result_card, parent, false);
        return new ResultVH(v);
    }

    @Override
    public void onBindViewHolder(@NonNull ResultVH h, int pos) {
        h.bind(getItem(pos));
    }

    class ResultVH extends RecyclerView.ViewHolder {

        private final View cardRoot;
        private final ScoreRingView scoreRing;
        private final TextView tvUniName, tvCourse, tvCountry;
        private final View layoutVisa, layoutAccept, layoutGpa;
        private final TextView btnApply;

        ResultVH(@NonNull View v) {
            super(v);
            cardRoot = v;
            scoreRing = v.findViewById(R.id.score_ring);
            tvUniName = v.findViewById(R.id.tv_uni_name);
            tvCourse = v.findViewById(R.id.tv_course);
            tvCountry = v.findViewById(R.id.tv_country);
            layoutVisa = v.findViewById(R.id.layout_visa);
            layoutAccept = v.findViewById(R.id.layout_accept);
            layoutGpa = v.findViewById(R.id.layout_gpa);
            btnApply = v.findViewById(R.id.btn_apply);
        }

        void bind(AiUniversityResult r) {
            tvUniName.setText(r.getUniversityName());

            String courseName = r.getCourseName() != null ? r.getCourseName() : "Program";
            tvCourse.setText(courseName);

            String countryName = r.getCountry() != null ? r.getCountry() : "Country";
            tvCountry.setText(countryName);

            scoreRing.setScore(r.getEligibilityPercentage());

            setMetric(layoutVisa, "Visa Rate", (int) r.getVisaRate());
            setMetric(layoutAccept, "Acceptance", (int) r.getAcceptanceRate());
            setMetric(layoutGpa, "GPA Req", (int) (r.getGpaRequirement() * 20));

            cardRoot.setOnClickListener(v -> {
                if (listener != null) listener.onClick(r);
            });

            btnApply.setOnClickListener(v -> {
                if (listener != null) listener.onClick(r);
            });
        }

        private void setMetric(View layout, String label, int progress) {
            if (layout == null) return;

            TextView tvLabel = layout.findViewById(R.id.tv_metric_label);
            ProgressBar pb = layout.findViewById(R.id.pb_metric);
            TextView tvVal = layout.findViewById(R.id.tv_metric_value);

            if (tvLabel != null) tvLabel.setText(label);
            if (pb != null) pb.setProgress(progress);
            if (tvVal != null) tvVal.setText(progress + "%");
        }
    }
}