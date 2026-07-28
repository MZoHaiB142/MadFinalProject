package com.example.madfinalproject.ui;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import com.bumptech.glide.Glide;
import com.example.madfinalproject.R;
import com.example.madfinalproject.models.University;
import com.example.madfinalproject.models.UniversityProfileMatch;
import com.example.madfinalproject.utils.LogUtils;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.firebase.firestore.FirebaseFirestore;
import com.example.madfinalproject.scholarships.FavoriteScholarshipRepository;
import java.util.HashSet;
import java.util.Set;

public final class UniversityBottomSheets {
    private static final String TAG="UniversityDetails";
    private UniversityBottomSheets(){}

    public static void openScholarships(Context context,University cached){loadLatest(context,cached,university->showScholarships(context,university));}
    public static void openDetails(Context context,University cached){loadLatest(context,cached,university->showDetails(context,university));}
    public static void openProfileMatch(
            Context context,
            University university,
            UniversityProfileMatch match
    ){
        showProfileMatch(context,university,match);
    }

    private interface Loaded { void show(University university); }
    private static void loadLatest(Context context,University cached,Loaded callback){
        if(cached.id==null||cached.id.trim().isEmpty()){callback.show(cached);return;}
        FirebaseFirestore.getInstance().collection("Universities").document(cached.id).get()
                .addOnSuccessListener(document->{if(document.exists()){University latest=document.toObject(University.class);if(latest!=null){latest.id=document.getId();callback.show(latest);return;}}callback.show(cached);})
                .addOnFailureListener(error->{LogUtils.e(TAG,"University refresh failed",error);Toast.makeText(context,"Showing cached university information",Toast.LENGTH_SHORT).show();callback.show(cached);});
    }

    private static void showScholarships(Context context,University university){
        BottomSheetDialog dialog=new BottomSheetDialog(context);View root=LayoutInflater.from(context).inflate(R.layout.bottom_sheet_university_scholarships,null);dialog.setContentView(root);
        ((TextView)root.findViewById(R.id.sheetTitle)).setText(university.name+" Scholarships");
        LinearLayout container=root.findViewById(R.id.scholarshipContainer);
        if(university.getScholarships().isEmpty())root.findViewById(R.id.emptyScholarships).setVisibility(View.VISIBLE);
        Set<String> favouriteIds=new HashSet<>();FavoriteScholarshipRepository repository=new FavoriteScholarshipRepository();repository.load(favouriteIds::addAll);
        for(University.Scholarship scholarship:university.getScholarships()){
            View item=LayoutInflater.from(context).inflate(R.layout.item_embedded_scholarship,container,false);
            set(item,R.id.scholarshipTitle,scholarship.getTitle(),"Scholarship");set(item,R.id.scholarshipAmount,"Benefit: "+value(scholarship.getAmount()));set(item,R.id.scholarshipDeadline,"Deadline: "+value(scholarship.getDeadline()));set(item,R.id.scholarshipEligibility,"Eligibility: "+value(scholarship.getEligibility()));
            TextView apply=item.findViewById(R.id.scholarshipLink);if(scholarship.getLink().isEmpty())apply.setVisibility(View.GONE);else apply.setOnClickListener(v->openLink(context,scholarship.getLink()));container.addView(item);
            String favoriteId=university.id+"_"+String.valueOf((scholarship.getTitle()+scholarship.getLink()).hashCode());TextView favorite=item.findViewById(R.id.embeddedFavorite);favorite.setText(favouriteIds.contains(favoriteId)?"♥":"♡");favorite.setOnClickListener(v->repository.toggle(context,university.id,scholarship,favouriteIds.contains(favoriteId),(saved,message)->{if(saved)favouriteIds.add(favoriteId);else favouriteIds.remove(favoriteId);favorite.setText(saved?"♥":"♡");Toast.makeText(context,message,Toast.LENGTH_LONG).show();}));
        }
        root.findViewById(R.id.btnCloseSheet).setOnClickListener(v->dialog.dismiss());dialog.show();
    }

    private static void showDetails(Context context,University university){
        BottomSheetDialog dialog=new BottomSheetDialog(context);View root=LayoutInflater.from(context).inflate(R.layout.bottom_sheet_university_details,null);dialog.setContentView(root);
        set(root,R.id.detailName,university.name,"University");set(root,R.id.detailLocation,university.location,"Location unavailable");set(root,R.id.detailStats,"Ranking: "+value(university.ranking)+"   •   Acceptance: "+value(university.acceptanceRate)+"\nVisa ratio: "+value(university.visaRatio)+"   •   Fees: "+value(university.fees)+"\nProfile match: "+university.matchScore+"%   •   Scholarships: "+Math.max(university.scholarshipCount,university.getScholarships().size()));
        ImageView image=root.findViewById(R.id.detailImage);Glide.with(context).load(university.imageUrl).placeholder(R.drawable.img).error(R.drawable.img).centerCrop().into(image);
        set(root,R.id.detailTags,university.tags==null||university.tags.isEmpty()?"":android.text.TextUtils.join("  •  ",university.tags));
        LinearLayout container=root.findViewById(R.id.programContainer);if(university.getPrograms().isEmpty())root.findViewById(R.id.emptyPrograms).setVisibility(View.VISIBLE);
        for(University.Program program:university.getPrograms()){
            View item=LayoutInflater.from(context).inflate(R.layout.item_embedded_program,container,false);set(item,R.id.programName,program.getCourseName(),"Program");set(item,R.id.programDegree,"Degree: "+value(program.getDegreeLevel()));set(item,R.id.programDuration,"Duration: "+value(program.getDuration()));set(item,R.id.programFees,"Yearly fees: "+value(program.getYearlyFees()));container.addView(item);
        }
        root.findViewById(R.id.btnCloseDetails).setOnClickListener(v->dialog.dismiss());dialog.show();
    }

    private static void showProfileMatch(
            Context context,
            University university,
            UniversityProfileMatch match
    ){
        BottomSheetDialog dialog=new BottomSheetDialog(context);
        View root=LayoutInflater.from(context).inflate(
                R.layout.bottom_sheet_university_profile_match,
                null
        );
        dialog.setContentView(root);

        set(root,R.id.matchUniversityName,university.name,"University");
        set(root,R.id.matchScore,match.getScore()+"%");
        TextView suitability=root.findViewById(R.id.matchSuitability);
        suitability.setText(match.getSuitabilityLabel());
        suitability.setBackgroundResource(
                match.isProfileComplete()
                        ? suitabilityBackground(match.getCategory())
                        : R.drawable.bg_badge_amber
        );
        suitability.setTextColor(android.graphics.Color.parseColor(
                match.isProfileComplete()
                        ? suitabilityTextColor(match.getCategory())
                        : "#9A6700"
        ));
        set(root,R.id.matchSummary,match.getSummary(),
                "Complete your profile to calculate a consultant match.");
        set(root,R.id.matchMode,
                match.isAiEnhanced()
                        ? "AI-enhanced consultant analysis"
                        : "Instant consultant analysis");
        set(root,R.id.matchAdvice,match.getConsultantAdvice(),
                "Review the latest official program requirements before applying.");
        set(root,R.id.matchSignals,"Assessment signals: "+match.getEvaluatedSignals());

        LinearLayout strengths=root.findViewById(R.id.matchStrengthsContainer);
        LinearLayout weaknesses=root.findViewById(R.id.matchWeaknessesContainer);
        addAnalysisItems(
                context,
                strengths,
                match.getStrengths(),
                true,
                "No strong profile signal is available yet."
        );
        addAnalysisItems(
                context,
                weaknesses,
                match.getWeakPoints(),
                false,
                "No major weakness was detected from the available data."
        );

        root.findViewById(R.id.btnCloseMatch).setOnClickListener(v->dialog.dismiss());
        dialog.show();
    }

    private static void addAnalysisItems(
            Context context,
            LinearLayout container,
            java.util.List<String> items,
            boolean positive,
            String emptyMessage
    ){
        java.util.List<String> safeItems=items==null
                ? java.util.Collections.emptyList()
                : items;
        if(safeItems.isEmpty()){
            TextView empty=new TextView(context);
            empty.setText(emptyMessage);
            empty.setTextColor(android.graphics.Color.parseColor("#6B7280"));
            empty.setTextSize(13);
            container.addView(empty);
            return;
        }
        for(String value:safeItems){
            if(value==null||value.trim().isEmpty())continue;
            TextView item=new TextView(context);
            item.setText((positive?"✓  ":"•  ")+value.trim());
            item.setTextColor(android.graphics.Color.parseColor(
                    positive?"#137A42":"#B42318"
            ));
            item.setTextSize(13);
            item.setLineSpacing(0,1.12f);
            item.setPadding(0,dp(context,4),0,dp(context,4));
            container.addView(item);
        }
    }

    private static int suitabilityBackground(String category){
        if("Safe".equals(category))return R.drawable.bg_badge_green;
        if("Target".equals(category))return R.drawable.bg_badge_blue;
        if("Ambitious".equals(category))return R.drawable.bg_badge_amber;
        return R.drawable.bg_badge_red;
    }

    private static String suitabilityTextColor(String category){
        if("Safe".equals(category))return "#137A42";
        if("Target".equals(category))return "#2457C5";
        if("Ambitious".equals(category))return "#9A6700";
        return "#B42318";
    }

    private static int dp(Context context,int value){
        return Math.round(value*context.getResources().getDisplayMetrics().density);
    }

    private static void set(View root,int id,String text){((TextView)root.findViewById(id)).setText(text);}
    private static void set(View root,int id,String text,String fallback){((TextView)root.findViewById(id)).setText(text==null||text.trim().isEmpty()?fallback:text);}
    private static String value(String value){return value==null||value.trim().isEmpty()?"Not specified":value;}
    private static void openLink(Context context,String link){try{Uri uri=Uri.parse(link.startsWith("http")?link:"https://"+link);context.startActivity(new Intent(Intent.ACTION_VIEW,uri));}catch(Exception error){Toast.makeText(context,"Scholarship link could not be opened",Toast.LENGTH_SHORT).show();}}
}
