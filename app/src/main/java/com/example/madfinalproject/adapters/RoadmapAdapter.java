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
import androidx.recyclerview.widget.RecyclerView;

import com.example.madfinalproject.R;
import com.example.madfinalproject.models.Phase;
import com.example.madfinalproject.models.RoadmapStep;

import java.util.ArrayList;
import java.util.List;

public class RoadmapAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    public static final int TYPE_PHASE = 0;
    public static final int TYPE_STEP = 1;

    public interface RoadmapItem {
        int getType();
    }

    public static class PhaseItem implements RoadmapItem {
        public Phase phase;
        public PhaseItem(Phase p) { this.phase = p; }
        @Override public int getType() { return TYPE_PHASE; }
    }

    public static class StepItem implements RoadmapItem {
        public RoadmapStep step;
        public StepItem(RoadmapStep s) { this.step = s; }
        @Override public int getType() { return TYPE_STEP; }
    }

    public interface OnSheetRequestedListener {
        void onSheetRequested(String sheetType);
    }

    private List<RoadmapItem> items = new ArrayList<>();
    private final OnSheetRequestedListener listener;
    private final Context context;

    public RoadmapAdapter(Context context, OnSheetRequestedListener listener) {
        this.context = context;
        this.listener = listener;
    }

    public void submitList(List<RoadmapItem> newItems) {
        this.items = newItems;
        notifyDataSetChanged();
    }

    @Override
    public int getItemViewType(int position) {
        return items.get(position).getType();
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(context);
        if (viewType == TYPE_PHASE) {
            View v = inflater.inflate(R.layout.item_phase_header, parent, false);
            return new PhaseVH(v);
        } else {
            View v = inflater.inflate(R.layout.item_step_card, parent, false);
            return new StepVH(v);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        if (getItemViewType(position) == TYPE_PHASE) {
            ((PhaseVH) holder).bind(((PhaseItem) items.get(position)).phase);
        } else {
            ((StepVH) holder).bind(((StepItem) items.get(position)).step);
        }
    }

    @Override
    public int getItemCount() { return items.size(); }

    // ─── Phase ViewHolder ───────────────────────────────────────────────────

    class PhaseVH extends RecyclerView.ViewHolder {
        TextView tvPhaseBadge, tvPhaseTitle, tvPhaseDuration;

        PhaseVH(@NonNull View v) {
            super(v);
            tvPhaseBadge   = v.findViewById(R.id.tv_phase_badge);
            tvPhaseTitle   = v.findViewById(R.id.tv_phase_title);
            tvPhaseDuration = v.findViewById(R.id.tv_phase_duration);
        }

        void bind(Phase phase) {
            tvPhaseBadge.setText("PHASE " + phase.order);
            tvPhaseTitle.setText(phase.title);
            tvPhaseDuration.setText(phase.duration);

            int bgRes;
            switch (phase.color == null ? "" : phase.color) {
                case "blue":   bgRes = R.drawable.bg_phase_blue;   break;
                case "green":  bgRes = R.drawable.bg_phase_green;  break;
                case "purple": bgRes = R.drawable.bg_phase_purple; break;
                default:       bgRes = R.drawable.bg_phase_amber;  break;
            }
            tvPhaseBadge.setBackgroundResource(bgRes);
        }
    }

    // ─── Step ViewHolder ────────────────────────────────────────────────────

    class StepVH extends RecyclerView.ViewHolder {
        View accentBar, stepHeader;
        TextView tvStepNum, tvStepTitle, tvStepSubtitle, tvBadge, tvChevron, tvHowtoTitle, tvTip;
        LinearLayout panelExpanded, howtoStepsContainer, tipBox, btnContainer;
        Button btnAction1, btnAction2;
        boolean isExpanded = false;

        StepVH(@NonNull View v) {
            super(v);
            stepHeader          = v.findViewById(R.id.step_header);
            accentBar           = v.findViewById(R.id.accent_bar);
            tvStepNum           = v.findViewById(R.id.tv_step_num);
            tvStepTitle         = v.findViewById(R.id.tv_step_title);
            tvStepSubtitle      = v.findViewById(R.id.tv_step_subtitle);
            tvBadge             = v.findViewById(R.id.tv_badge);
            tvChevron           = v.findViewById(R.id.tv_chevron);
            panelExpanded       = v.findViewById(R.id.panel_expanded);
            howtoStepsContainer = v.findViewById(R.id.howto_steps_container);
            tvHowtoTitle        = v.findViewById(R.id.tv_howto_title);
            tipBox              = v.findViewById(R.id.tip_box);
            tvTip               = v.findViewById(R.id.tv_tip);
            btnContainer        = v.findViewById(R.id.btn_container);
            btnAction1          = v.findViewById(R.id.btn_action1);
            btnAction2          = v.findViewById(R.id.btn_action2);
        }

        void bind(RoadmapStep step) {
            // Reset expand state on recycle
            isExpanded = false;
            panelExpanded.setVisibility(View.GONE);
            panelExpanded.getLayoutParams().height = 0;
            tvChevron.setText("▾");

            // Step number / emoji
            tvStepNum.setText(step.emoji != null && !step.emoji.isEmpty()
                    ? step.emoji : String.valueOf(step.order));

            tvStepTitle.setText(step.title);
            tvStepSubtitle.setText(step.subtitle);

            // Status badge
            applyStatusStyle(step.status);

            // Node drawable
            int nodeRes;
            switch (step.status == null ? "" : step.status) {
                case "done":   nodeRes = R.drawable.bg_step_node_done;    break;
                case "active": nodeRes = R.drawable.bg_step_node_active;  break;
                default:       nodeRes = R.drawable.bg_step_node_pending; break;
            }
            tvStepNum.setBackgroundResource(nodeRes);

            // Howto section
            if (step.howto_title != null) tvHowtoTitle.setText(step.howto_title);
            howtoStepsContainer.removeAllViews();
            if (step.howto_steps != null) {
                for (int i = 0; i < step.howto_steps.size(); i++) {
                    View row = LayoutInflater.from(context)
                            .inflate(R.layout.item_howto_step, howtoStepsContainer, false);
                    ((TextView) row.findViewById(R.id.tv_dot)).setText(String.valueOf(i + 1));
                    ((TextView) row.findViewById(R.id.tv_text)).setText(step.howto_steps.get(i));
                    howtoStepsContainer.addView(row);
                }
            }

            // Tip box
            if (step.tip != null && !step.tip.isEmpty()) {
                tipBox.setVisibility(View.VISIBLE);
                tvTip.setText(step.tip);
            } else {
                tipBox.setVisibility(View.GONE);
            }

            // Action buttons
            if (step.btn1_text != null && !step.btn1_text.isEmpty()) {
                btnContainer.setVisibility(View.VISIBLE);
                btnAction1.setText(step.btn1_text);
                btnAction1.setOnClickListener(v -> {
                    if (listener != null && step.sheet_type != null
                            && !step.sheet_type.equals("none")) {
                        listener.onSheetRequested(step.sheet_type);
                    }
                });
                btnAction2.setText(step.btn2_text != null ? step.btn2_text : "Mark Done");
            } else {
                btnContainer.setVisibility(View.GONE);
            }

            // Expand / collapse on header tap
            stepHeader.setOnClickListener(v -> {
                isExpanded = !isExpanded;
                tvChevron.setText(isExpanded ? "▴" : "▾");
                animatePanel(isExpanded);
            });
        }

        private void applyStatusStyle(String status) {
            if (status == null) status = "";
            switch (status) {
                case "done":
                    tvBadge.setText("Done ✓");
                    tvBadge.setTextColor(0xFF22C55E);
                    tvBadge.setBackgroundResource(R.drawable.bg_badge_done);
                    break;
                case "active":
                    tvBadge.setText("Active ●");
                    tvBadge.setTextColor(0xFF7B93FF);
                    tvBadge.setBackgroundResource(R.drawable.bg_badge_active);
                    break;
                default:
                    tvBadge.setText("Pending");
                    tvBadge.setTextColor(0xFF6B7280);
                    tvBadge.setBackgroundResource(R.drawable.bg_badge_pending);
                    break;
            }
        }

        private void animatePanel(boolean expand) {
            if (expand) {
                panelExpanded.setVisibility(View.VISIBLE);
                panelExpanded.measure(
                        View.MeasureSpec.makeMeasureSpec(
                                ((View) panelExpanded.getParent()).getWidth(),
                                View.MeasureSpec.AT_MOST),
                        View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED));
                int target = panelExpanded.getMeasuredHeight();
                panelExpanded.getLayoutParams().height = 0;
                ValueAnimator anim = ValueAnimator.ofInt(0, target);
                anim.setDuration(280);
                anim.addUpdateListener(a -> {
                    panelExpanded.getLayoutParams().height = (int) a.getAnimatedValue();
                    panelExpanded.requestLayout();
                });
                anim.start();
            } else {
                int start = panelExpanded.getHeight();
                ValueAnimator anim = ValueAnimator.ofInt(start, 0);
                anim.setDuration(220);
                anim.addUpdateListener(a -> {
                    int val = (int) a.getAnimatedValue();
                    panelExpanded.getLayoutParams().height = val;
                    panelExpanded.requestLayout();
                    if (val == 0) panelExpanded.setVisibility(View.GONE);
                });
                anim.start();
            }
        }
    }
}