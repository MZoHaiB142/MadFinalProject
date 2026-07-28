package com.example.madfinalproject.adapters;

import android.animation.ValueAnimator;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import com.example.madfinalproject.R;
import com.example.madfinalproject.models.UniversityMatch;
import com.example.madfinalproject.views.ScoreRingView;

import java.util.List;
import java.util.Objects;

public class MatchCardAdapter
        extends ListAdapter<UniversityMatch, MatchCardAdapter.MatchViewHolder> {

    // ── Interfaces ──
    public interface OnDetailClickListener { void onClick(UniversityMatch uni); }
    public interface OnApplyClickListener  { void onClick(UniversityMatch uni); }

    private final OnDetailClickListener onDetailClick;
    private final OnApplyClickListener  onApplyClick;

    // ── DiffUtil ──
    private static final DiffUtil.ItemCallback<UniversityMatch> DIFF =
            new DiffUtil.ItemCallback<UniversityMatch>() {
                @Override
                public boolean areItemsTheSame(
                        @NonNull UniversityMatch a, @NonNull UniversityMatch b) {
                    return Objects.equals(a.getId(), b.getId());
                }
                @Override
                public boolean areContentsTheSame(
                        @NonNull UniversityMatch a, @NonNull UniversityMatch b) {
                    return a.getMatchScore()       == b.getMatchScore()
                            && a.getVisaRatePakistan() == b.getVisaRatePakistan()
                            && a.isTopPick()           == b.isTopPick();
                }
            };

    // ── Constructor ──
    public MatchCardAdapter(
            OnDetailClickListener onDetailClick,
            OnApplyClickListener  onApplyClick
    ) {
        super(DIFF);
        this.onDetailClick = onDetailClick;
        this.onApplyClick  = onApplyClick;
    }

    // ─────────────────────────────────────────
    @NonNull
    @Override
    public MatchViewHolder onCreateViewHolder(
            @NonNull ViewGroup parent, int viewType) {

        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_match_card, parent, false);
        return new MatchViewHolder(v);
    }

    @Override
    public void onBindViewHolder(
            @NonNull MatchViewHolder holder, int position) {
        holder.bind(getItem(position));
    }

    // ─────────────────────────────────────────
    // VIEW HOLDER
    // ─────────────────────────────────────────
    class MatchViewHolder extends RecyclerView.ViewHolder {

        private final View          cardRoot;
        private final TextView      tvFlag;
        private final TextView      tvCountry;
        private final TextView      tvPickBadge;
        private final TextView      tvUniName;
        private final TextView      tvProgram;
        private final ScoreRingView scoreRing;
        private final TextView      tvVisaRate;
        private final TextView      tvAcceptRate;
        private final TextView      tvGpaReq;
        private final View          barVisa;
        private final View          barAccept;
        private final View          barGpa;
        private final LinearLayout  tagsRow;
        private final Button        btnDetail;
        private final Button        btnApply;

        MatchViewHolder(@NonNull View itemView) {
            super(itemView);
            cardRoot     = itemView.findViewById(R.id.card_root);
            tvFlag       = itemView.findViewById(R.id.tv_flag);
            tvCountry    = itemView.findViewById(R.id.tv_country);
            tvPickBadge  = itemView.findViewById(R.id.tv_pick_badge);
            tvUniName    = itemView.findViewById(R.id.tv_uni_name);
            tvProgram    = itemView.findViewById(R.id.tv_program);
            scoreRing    = itemView.findViewById(R.id.score_ring);
            tvVisaRate   = itemView.findViewById(R.id.tv_visa_rate);
            tvAcceptRate = itemView.findViewById(R.id.tv_accept_rate);
            tvGpaReq     = itemView.findViewById(R.id.tv_gpa_req);
            barVisa      = itemView.findViewById(R.id.bar_visa);
            barAccept    = itemView.findViewById(R.id.bar_accept);
            barGpa       = itemView.findViewById(R.id.bar_gpa);
            tagsRow      = itemView.findViewById(R.id.tags_row);
            btnDetail    = itemView.findViewById(R.id.btn_detail);
            btnApply     = itemView.findViewById(R.id.btn_apply);
        }

        void bind(UniversityMatch uni) {
            Context ctx = itemView.getContext();

            // ── Basic info ──
            tvFlag.setText(uni.getFlag());
            tvCountry.setText(uni.getCountryName());
            tvUniName.setText(uni.getName());
            tvProgram.setText(uni.getProgram());

            // ── Card background ──
            cardRoot.setBackgroundResource(
                    uni.isTopPick()
                            ? R.drawable.bg_match_card_top
                            : R.drawable.bg_match_card
            );

            // ── Pick badge ──
            setupPickBadge(ctx, uni);

            // ── Score Ring ──
            scoreRing.setScore(uni.getMatchScore());
            scoreRing.setScoreColor(getScoreColor(ctx, uni.getMatchScore()));

            // ── Visa bar ──
            tvVisaRate.setText(uni.getVisaRatePakistan() + "%");
            tvVisaRate.setTextColor(ctx.getColor(
                    uni.getVisaRatePakistan() >= 70 ? R.color.green
                            : uni.getVisaRatePakistan() >= 55 ? R.color.blue
                            : R.color.amber
            ));
            barVisa.setBackgroundResource(
                    uni.getVisaRatePakistan() >= 70 ? R.drawable.bg_bar_green
                            : uni.getVisaRatePakistan() >= 55 ? R.drawable.bg_bar_blue
                            : R.drawable.bg_bar_amber
            );
            animateBar(barVisa, uni.getVisaRatePakistan());

            // ── Accept bar ──
            tvAcceptRate.setText(uni.getAcceptanceRate() + "%");
            tvAcceptRate.setTextColor(ctx.getColor(
                    uni.getAcceptanceRate() >= 40 ? R.color.green
                            : uni.getAcceptanceRate() >= 25 ? R.color.amber
                            : R.color.red
            ));
            barAccept.setBackgroundResource(
                    uni.getAcceptanceRate() >= 40 ? R.drawable.bg_bar_green
                            : uni.getAcceptanceRate() >= 25 ? R.drawable.bg_bar_amber
                            : R.drawable.bg_bar_red
            );
            animateBar(barAccept, uni.getAcceptanceRate());

            // ── GPA bar ──
            tvGpaReq.setText(String.valueOf(uni.getGpaRequired()));
            int gpaPercent = (int)((uni.getGpaRequired() / 4.0) * 100);
            barGpa.setBackgroundResource(R.drawable.bg_bar_blue);
            animateBar(barGpa, gpaPercent);

            // ── Tags ──
            buildTags(ctx, uni);

            // ── Buttons ──
            btnDetail.setOnClickListener(v -> {
                if (onDetailClick != null) onDetailClick.onClick(uni);
            });
            btnApply.setOnClickListener(v -> {
                if (onApplyClick != null) onApplyClick.onClick(uni);
            });
            cardRoot.setOnClickListener(v -> {
                if (onDetailClick != null) onDetailClick.onClick(uni);
            });
        }

        // ── Pick badge setup ──
        private void setupPickBadge(Context ctx, UniversityMatch uni) {
            if (uni.isTopPick()) {
                tvPickBadge.setVisibility(View.VISIBLE);
                tvPickBadge.setText("⭐ Top Pick");
                tvPickBadge.setBackgroundResource(R.drawable.bg_badge_green);
                tvPickBadge.setTextColor(ctx.getColor(R.color.green));
            } else if (uni.isAiPick()) {
                tvPickBadge.setVisibility(View.VISIBLE);
                tvPickBadge.setText("🤖 AI Pick");
                tvPickBadge.setBackgroundResource(R.drawable.bg_badge_blue);
                tvPickBadge.setTextColor(ctx.getColor(R.color.blue_light));
            } else if (uni.getMatchScore() < 60) {
                tvPickBadge.setVisibility(View.VISIBLE);
                tvPickBadge.setText("⚡ Reach");
                tvPickBadge.setBackgroundResource(R.drawable.bg_badge_red);
                tvPickBadge.setTextColor(ctx.getColor(R.color.red));
            } else {
                tvPickBadge.setVisibility(View.GONE);
            }
        }

        // ── Tags row ──
        private void buildTags(Context ctx, UniversityMatch uni) {
            tagsRow.removeAllViews();

            addTagIfNeeded(ctx, uni.isTopPick(),
                    "⭐ Top Pick",     R.drawable.bg_badge_green, R.color.green);
            addTagIfNeeded(ctx, uni.isAiPick(),
                    "🤖 AI Pick",     R.drawable.bg_badge_blue,  R.color.blue_light);
            addTagIfNeeded(ctx, uni.isScholarshipAvailable(),
                    "💰 Scholarship", R.drawable.bg_badge_amber, R.color.amber);
            addTagIfNeeded(ctx, uni.getMatchScore() < 60,
                    "⚡ Reach",       R.drawable.bg_badge_red,   R.color.red);

            if (!uni.getFees().isEmpty()) {
                addTag(ctx, uni.getFees(),
                        R.drawable.bg_badge_gray, R.color.text_secondary);
            }
            if (!uni.getDuration().isEmpty()) {
                addTag(ctx, uni.getDuration(),
                        R.drawable.bg_badge_gray, R.color.text_secondary);
            }
        }

        private void addTagIfNeeded(Context ctx, boolean condition,
                                    String label, int bgRes, int colorRes) {
            if (condition) addTag(ctx, label, bgRes, colorRes);
        }

        private void addTag(Context ctx, String label, int bgRes, int colorRes) {
            TextView chip = new TextView(ctx);
            chip.setText(label);
            chip.setTextSize(10f);
            chip.setTextColor(ctx.getColor(colorRes));
            chip.setBackgroundResource(bgRes);

            int ph = dpToPx(ctx, 8);
            int pv = dpToPx(ctx, 3);
            chip.setPadding(ph, pv, ph, pv);

            LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
            );
            lp.setMarginEnd(dpToPx(ctx, 6));
            chip.setLayoutParams(lp);

            tagsRow.addView(chip);
        }

        // ── Bar animation ──
        private void animateBar(View bar, int percent) {
            bar.post(() -> {
                View parent = (View) bar.getParent();
                if (parent == null) return;

                int parentWidth = parent.getWidth();
                int targetWidth = (int)(parentWidth * (percent / 100f));

                // Reset to 0
                ViewGroup.LayoutParams lp = bar.getLayoutParams();
                lp.width = 0;
                bar.setLayoutParams(lp);

                ValueAnimator anim = ValueAnimator.ofInt(0, targetWidth);
                anim.setDuration(900);
                anim.setStartDelay(300);
                anim.addUpdateListener(a -> {
                    ViewGroup.LayoutParams p = bar.getLayoutParams();
                    p.width = (int) a.getAnimatedValue();
                    bar.setLayoutParams(p);
                });
                anim.start();
            });
        }

        // ── Score color ──
        private int getScoreColor(Context ctx, int score) {
            if (score >= 80) return ctx.getColor(R.color.green);
            if (score >= 65) return ctx.getColor(R.color.amber);
            return ctx.getColor(R.color.red);
        }

        // ── dp to px ──
        private int dpToPx(Context ctx, int dp) {
            return Math.round(dp * ctx.getResources()
                    .getDisplayMetrics().density);
        }
    }
}