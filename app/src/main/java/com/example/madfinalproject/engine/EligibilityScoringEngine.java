package com.example.madfinalproject.engine;

import com.example.madfinalproject.models.AiUniversityResult;
import com.example.madfinalproject.models.UniversityMatch;
import com.example.madfinalproject.models.UserProfile;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Fast, deterministic eligibility scoring used as the guaranteed first result.
 * Remote AI may enrich these results, but the UI never depends on the network.
 */
public final class EligibilityScoringEngine {

    private static final Pattern NUMBER = Pattern.compile("(\\d+(?:\\.\\d+)?)");

    private EligibilityScoringEngine() {
    }

    public static List<AiUniversityResult> analyze(
            UserProfile profile,
            List<UniversityMatch> universities
    ) {
        List<AiUniversityResult> results = new ArrayList<>();
        if (profile == null || universities == null) return results;

        for (UniversityMatch university : universities) {
            if (university == null || isBlank(university.getName())) continue;
            results.add(score(profile, university));
        }

        Collections.sort(results,
                (left, right) -> right.getEligibilityPercentage()
                        - left.getEligibilityPercentage());

        for (int index = 0; index < results.size(); index++) {
            results.get(index).setApplyPriority(index + 1);
        }
        return results;
    }

    private static AiUniversityResult score(
            UserProfile profile,
            UniversityMatch university
    ) {
        List<String> strengths = new ArrayList<>();
        List<String> weakAreas = new ArrayList<>();

        double acceptance = normalizedPercent(university.getAcceptanceRate());
        double visa = normalizedPercent(university.getVisaRatePakistan());
        double requiredGpa = university.getGpaRequired() > 0
                ? university.getGpaRequired()
                : estimatedGpaRequirement(acceptance);
        double requiredEnglish = university.getIeltsRequired() > 0
                ? university.getIeltsRequired()
                : 6.0;

        double gpaPoints = matchPoints(profile.getGpa(), requiredGpa, 30.0, 0.9);
        if (profile.getGpa() >= requiredGpa) {
            strengths.add("GPA meets the estimated academic requirement.");
        } else {
            weakAreas.add(String.format(
                    Locale.US,
                    "GPA is below the estimated %.1f requirement.",
                    requiredGpa
            ));
        }

        double englishPoints;
        if (profile.getIelts() <= 0) {
            englishPoints = 18.0;
            weakAreas.add("Add an IELTS or PTE score for a more precise match.");
        } else {
            englishPoints = matchPoints(
                    profile.getIelts(),
                    requiredEnglish,
                    30.0,
                    1.5
            );
            if (profile.getIelts() >= requiredEnglish) {
                strengths.add("English score meets the estimated requirement.");
            } else {
                weakAreas.add(String.format(
                        Locale.US,
                        "Improve the English score toward %.1f or above.",
                        requiredEnglish
                ));
            }
        }

        double budgetPoints = budgetPoints(profile.getBudget(), university.getFees());
        if (budgetPoints >= 12) {
            strengths.add("Tuition appears compatible with the stated budget.");
        } else if (budgetPoints <= 6) {
            weakAreas.add("Review tuition affordability and funding options.");
        }

        double visaPoints = visa > 0 ? visa / 10.0 : 5.0;
        double acceptancePoints = acceptance > 0 ? acceptance / 10.0 : 5.0;
        double scholarshipPoints = university.isScholarshipAvailable() ? 5.0 : 1.0;

        if (visa >= 70) strengths.add("Current stored visa ratio is favourable.");
        if (acceptance >= 60) strengths.add("Acceptance rate improves the admission outlook.");
        if (university.isScholarshipAvailable()) {
            strengths.add("Scholarship opportunities are listed for this university.");
        }

        double preferenceAdjustment = countryPreferenceAdjustment(
                profile.getPreferredCountry(),
                university.getCountryName()
        );
        double programAdjustment = programFitAdjustment(
                profile.getField(),
                university.getProgram()
        );

        int score = clamp((int) Math.round(
                gpaPoints
                        + englishPoints
                        + budgetPoints
                        + visaPoints
                        + acceptancePoints
                        + scholarshipPoints
                        + preferenceAdjustment
                        + programAdjustment
        ), 20, 98);

        String category = category(score);
        String scholarshipChance = scholarshipChance(
                university.isScholarshipAvailable(),
                score
        );

        AiUniversityResult result = new AiUniversityResult();
        result.setUniversityName(university.getName());
        result.setEligibilityPercentage(score);
        result.setCategory(category);
        result.setStrengths(limit(strengths, 3));
        result.setWeakAreas(limit(weakAreas, 2));
        result.setScholarshipChance(scholarshipChance);
        result.setVisaInsight(visaInsight(university.getVisaRatePakistan()));
        result.setConsultantRecommendation(recommendation(category));
        result.setCourseName(university.getProgram());
        result.setCountry(university.getCountryName());
        result.setVisaRate(university.getVisaRatePakistan());
        result.setAcceptanceRate(university.getAcceptanceRate());
        result.setGpaRequirement(requiredGpa);
        return result;
    }

    private static double estimatedGpaRequirement(double acceptanceRate) {
        if (acceptanceRate <= 0) return 3.0;
        if (acceptanceRate >= 70) return 2.5;
        if (acceptanceRate >= 50) return 2.8;
        if (acceptanceRate >= 30) return 3.0;
        return 3.3;
    }

    private static double matchPoints(
            double actual,
            double required,
            double maximum,
            double fullPenaltyRange
    ) {
        if (required <= 0) return maximum * 0.7;
        if (actual >= required) {
            double bonus = Math.min(2.0, (actual - required) * 2.0);
            return Math.min(maximum, maximum - 1.0 + bonus);
        }
        double deficit = required - Math.max(0, actual);
        double ratio = 1.0 - (deficit / fullPenaltyRange);
        return Math.max(maximum * 0.25, maximum * ratio);
    }

    private static double budgetPoints(String budgetText, String feeText) {
        double budget = firstNumber(budgetText);
        double fee = firstNumber(feeText);
        if (budget <= 0 || fee <= 0) return 9.0;
        if (fee <= budget) return 15.0;
        if (fee <= budget * 1.15) return 11.0;
        if (fee <= budget * 1.35) return 7.0;
        return 4.0;
    }

    private static double firstNumber(String value) {
        if (isBlank(value)) return 0;
        String normalized = value.replace(",", "");
        Matcher matcher = NUMBER.matcher(normalized);
        if (!matcher.find()) return 0;
        try {
            return Double.parseDouble(matcher.group(1));
        } catch (NumberFormatException ignored) {
            return 0;
        }
    }

    private static double countryPreferenceAdjustment(
            String preferredCountry,
            String universityCountry
    ) {
        String preference = normalize(preferredCountry);
        if (preference.isEmpty()
                || preference.equals("any")
                || preference.equals("not specified")) {
            return 0;
        }
        String country = normalize(universityCountry);
        if (country.isEmpty()) return 0;
        return country.contains(preference) || preference.contains(country) ? 2.0 : -1.0;
    }

    private static double programFitAdjustment(String field, String program) {
        String normalizedField = normalize(field);
        String normalizedProgram = normalize(program);
        if (normalizedField.isEmpty() || normalizedProgram.isEmpty()) return 0;

        for (String token : normalizedField.split("\\s+")) {
            if (token.length() > 3 && normalizedProgram.contains(token)) return 2.0;
        }
        return 0;
    }

    private static String category(int score) {
        if (score >= 90) return "Safe";
        if (score >= 70) return "Target";
        if (score >= 50) return "Ambitious";
        return "Low Chance";
    }

    private static String scholarshipChance(boolean available, int score) {
        if (!available) return score >= 85 ? "Low" : "Very Low";
        if (score >= 82) return "High";
        if (score >= 62) return "Medium";
        return "Low";
    }

    private static String visaInsight(int visaRate) {
        if (visaRate <= 0) {
            return "Visa ratio is not available; verify the latest official requirements.";
        }
        if (visaRate >= 75) return "The stored visa ratio is strong, subject to profile quality.";
        if (visaRate >= 55) return "The stored visa ratio is moderate; documentation remains important.";
        return "The stored visa ratio is competitive; prepare finances and intent carefully.";
    }

    private static String recommendation(String category) {
        switch (category) {
            case "Safe":
                return "Keep this university in the shortlist and verify the current program deadline.";
            case "Target":
                return "A realistic option. Strengthen documents and apply early.";
            case "Ambitious":
                return "Apply as an ambitious option and improve the highlighted weak areas.";
            default:
                return "Consider stronger profile evidence or add universities with more flexible requirements.";
        }
    }

    private static List<String> limit(List<String> source, int maximum) {
        List<String> result = new ArrayList<>();
        for (String value : source) {
            if (!isBlank(value) && !result.contains(value)) result.add(value);
            if (result.size() >= maximum) break;
        }
        if (result.isEmpty()) result.add("Profile data was compared with available university data.");
        return result;
    }

    private static double normalizedPercent(double value) {
        return Math.max(0, Math.min(100, value));
    }

    private static int clamp(int value, int minimum, int maximum) {
        return Math.max(minimum, Math.min(maximum, value));
    }

    private static String normalize(String value) {
        if (value == null) return "";
        return value.toLowerCase(Locale.US)
                .replace("_", " ")
                .replaceAll("[^a-z0-9 ]", " ")
                .replaceAll("\\s+", " ")
                .trim();
    }

    private static boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }
}
