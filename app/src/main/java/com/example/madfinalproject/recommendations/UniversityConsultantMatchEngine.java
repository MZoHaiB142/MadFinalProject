package com.example.madfinalproject.recommendations;

import com.example.madfinalproject.models.AiUniversityResult;
import com.example.madfinalproject.models.University;
import com.example.madfinalproject.models.UniversityProfileMatch;
import com.example.madfinalproject.utils.Constants;
import com.google.firebase.firestore.DocumentSnapshot;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Consultant-style, deterministic profile assessment.
 * This is always available; remote AI can refine its narrative and score.
 */
public final class UniversityConsultantMatchEngine {

    private static final Pattern NUMBER = Pattern.compile("(\\d+(?:\\.\\d+)?)");

    private UniversityConsultantMatchEngine() {
    }

    public static Map<String, UniversityProfileMatch> analyze(
            List<University> universities,
            DocumentSnapshot profile
    ) {
        Map<String, UniversityProfileMatch> results = new LinkedHashMap<>();
        if (universities == null) return results;

        for (University university : universities) {
            if (university == null) continue;
            UniversityProfileMatch match = analyzeUniversity(university, profile);
            results.put(key(university), match);
            university.setMatchScore(match.getScore());
        }
        return results;
    }

    public static UniversityProfileMatch refineWithAi(
            UniversityProfileMatch local,
            AiUniversityResult ai
    ) {
        if (local == null || ai == null) return local;
        int score = ai.getEligibilityPercentage() > 0
                ? clamp(ai.getEligibilityPercentage(), 1, 100)
                : local.getScore();

        local.setScore(score);
        local.setCategory(category(score));
        local.setSuitabilityLabel(
                local.isProfileComplete() ? suitability(score) : "Profile Incomplete"
        );
        if (ai.getStrengths() != null && !ai.getStrengths().isEmpty()) {
            local.setStrengths(ai.getStrengths());
        }
        if (ai.getWeakAreas() != null && !ai.getWeakAreas().isEmpty()) {
            if (local.isProfileComplete()) {
                local.setWeakPoints(ai.getWeakAreas());
            } else {
                List<String> combined = new ArrayList<>(local.getWeakPoints());
                combined.addAll(ai.getWeakAreas());
                local.setWeakPoints(limit(combined, 5));
            }
        }
        if (!blank(ai.getConsultantRecommendation())) {
            local.setConsultantAdvice(ai.getConsultantRecommendation());
        }
        local.setSummary(summary(score, local.isProfileComplete()));
        local.setAiEnhanced(true);
        return local;
    }

    public static String key(University university) {
        if (university == null) return "";
        if (!blank(university.id)) return university.id;
        return normalize(university.name + "|" + university.location);
    }

    private static UniversityProfileMatch analyzeUniversity(
            University university,
            DocumentSnapshot profile
    ) {
        String qualification = value(profile, Constants.KEY_QUALIFICATION);
        String targetCountries = value(profile, Constants.KEY_TARGET_COUNTRIES, "country");
        String interestedFields = value(profile, Constants.KEY_INTERESTED_FIELDS, "field");
        String budgetText = value(profile, Constants.KEY_BUDGET, "budget");
        double gpa = number(value(profile, Constants.KEY_CGPA, "gpa"));
        double english = number(value(profile, Constants.KEY_IELTS_SCORE, "ielts", "pte"));
        double budget = number(budgetText);

        List<String> strengths = new ArrayList<>();
        List<String> weakPoints = new ArrayList<>();
        int missingCoreFields = 0;
        if (gpa <= 0) missingCoreFields++;
        if (qualification.isEmpty()) missingCoreFields++;
        if (targetCountries.isEmpty()) missingCoreFields++;
        if (interestedFields.isEmpty()) missingCoreFields++;
        if (budget <= 0) missingCoreFields++;
        double score = 0;

        double acceptanceRate = number(university.acceptanceRate);
        double estimatedGpa = estimatedGpaRequirement(acceptanceRate);
        if (gpa > 0) {
            double academicPoints = ratioPoints(gpa, estimatedGpa, 25, 1.0);
            score += academicPoints;
            if (gpa >= estimatedGpa) {
                strengths.add(String.format(
                        Locale.US,
                        "Your %.2f GPA meets the estimated %.1f academic benchmark.",
                        gpa,
                        estimatedGpa
                ));
            } else {
                weakPoints.add(String.format(
                        Locale.US,
                        "Your GPA is below the estimated %.1f benchmark for this competition level.",
                        estimatedGpa
                ));
            }
        } else {
            score += 11;
            weakPoints.add("Add your GPA to improve academic eligibility accuracy.");
        }

        if (english > 0) {
            score += ratioPoints(english, 6.0, 15, 1.5);
            if (english >= 6.0) {
                strengths.add("Your recorded English score meets the general study benchmark.");
            } else {
                weakPoints.add("Improve your IELTS/PTE score toward the program requirement.");
            }
        } else {
            score += 8;
            weakPoints.add("Add an IELTS or PTE score for a more precise language assessment.");
        }

        String targetDegree = targetDegree(qualification);
        boolean degreeMatch = false;
        boolean fieldMatch = false;
        if (university.getPrograms().isEmpty()) {
            score += 9;
            weakPoints.add("Program-level requirements are not available for a full comparison.");
            if (targetDegree.isEmpty()) {
                weakPoints.add("Add your latest qualification to determine the correct degree level.");
            }
            if (interestedFields.isEmpty()) {
                weakPoints.add("Add your interested field to evaluate program relevance.");
            }
        } else {
            for (University.Program program : university.getPrograms()) {
                String programText = normalize(
                        program.getCourseName() + " " + program.getDegreeLevel()
                );
                if (!targetDegree.isEmpty() && programText.contains(targetDegree)) {
                    degreeMatch = true;
                }
                if (!interestedFields.isEmpty()
                        && containsAny(programText, interestedFields)) {
                    fieldMatch = true;
                }
            }

            if (targetDegree.isEmpty()) {
                score += 5;
                weakPoints.add("Add your latest qualification to determine the correct degree level.");
            } else if (degreeMatch) {
                score += 9;
                strengths.add("Available programs match your expected next degree level.");
            } else {
                score += 2;
                weakPoints.add("The available degree levels do not clearly match your next study step.");
            }

            if (interestedFields.isEmpty()) {
                score += 5;
                weakPoints.add("Add your interested field to evaluate program relevance.");
            } else if (fieldMatch) {
                score += 11;
                strengths.add("The university offers programs related to your interested field.");
            } else {
                score += 3;
                weakPoints.add("No close program match was found for your recorded study field.");
            }
        }

        String searchableCountry = normalize(university.location);
        if (targetCountries.isEmpty()) {
            score += 8;
            weakPoints.add("Add target countries to measure destination preference.");
        } else if (countryMatches(searchableCountry, targetCountries)) {
            score += 15;
            strengths.add("The university is located in one of your target destinations.");
        } else {
            score += 4;
            weakPoints.add("This destination is outside your recorded target countries.");
        }

        double fee = universityFee(university);
        if (budget <= 0 || fee <= 0) {
            score += 8;
            if (budget <= 0) {
                weakPoints.add("Add your yearly budget to assess affordability.");
            } else {
                weakPoints.add("Current fee data is insufficient for an affordability decision.");
            }
        } else if (fee <= budget) {
            score += 15;
            strengths.add("The listed yearly fee fits within your recorded budget.");
        } else if (fee <= budget * 1.20) {
            score += 10;
            weakPoints.add("Fees are slightly above budget; scholarship support may close the gap.");
        } else {
            score += 3;
            weakPoints.add("The listed yearly fee is significantly above your recorded budget.");
        }

        if (acceptanceRate > 0) {
            score += Math.max(0.5, Math.min(3, acceptanceRate / 33.0));
            if (acceptanceRate >= 60) {
                strengths.add("The stored acceptance rate supports a stronger admission outlook.");
            } else if (acceptanceRate < 25) {
                weakPoints.add("The university has a highly competitive stored acceptance rate.");
            }
        } else {
            score += 1.5;
        }

        double visaRatio = number(university.visaRatio);
        if (visaRatio >= 70) {
            score += 2;
            strengths.add("The stored visa ratio is favourable for a well-prepared application.");
        } else if (visaRatio > 0) {
            score += Math.max(0.5, Math.min(2, visaRatio / 50.0));
            if (visaRatio < 50) {
                weakPoints.add("The stored visa ratio suggests stronger financial and intent evidence is needed.");
            }
        } else {
            score += 1;
        }

        boolean scholarshipAvailable = university.scholarshipCount > 0
                || !university.getScholarships().isEmpty();
        if (scholarshipAvailable) {
            score += 5;
            strengths.add("Scholarship opportunities are available to explore.");
        } else {
            score += 1;
            weakPoints.add("No embedded scholarship opportunity is currently listed.");
        }

        int finalScore = clamp((int) Math.round(score), 10, 98);
        boolean profileComplete = profile != null
                && profile.exists()
                && missingCoreFields == 0;

        UniversityProfileMatch result = new UniversityProfileMatch();
        result.setScore(finalScore);
        result.setCategory(category(finalScore));
        result.setSuitabilityLabel(
                missingCoreFields >= 3 ? "Profile Incomplete" : suitability(finalScore)
        );
        result.setProfileComplete(profileComplete);
        result.setAiEnhanced(false);
        result.setStrengths(limit(strengths, 5));
        result.setWeakPoints(limit(weakPoints, 5));
        result.setSummary(summary(finalScore, profileComplete));
        result.setConsultantAdvice(advice(finalScore, weakPoints, profileComplete));
        result.setEvaluatedSignals(
                "Academic profile · English score · program and degree fit · "
                        + "country preference · budget · acceptance · visa ratio · scholarships"
        );
        return result;
    }

    private static String summary(int score, boolean profileComplete) {
        if (!profileComplete) {
            return "This is a provisional consultant match. Complete the highlighted profile fields for higher confidence.";
        }
        if (score >= 80) {
            return "Your profile is strongly aligned with the available university and program data.";
        }
        if (score >= 65) {
            return "Your profile is suitable, with a few areas to strengthen before applying.";
        }
        if (score >= 50) {
            return "This is an ambitious option that needs targeted profile improvement.";
        }
        return "Your current profile has major gaps against the available university indicators.";
    }

    private static String advice(
            int score,
            List<String> weakPoints,
            boolean profileComplete
    ) {
        if (!profileComplete) {
            return "Complete your GPA, qualification, interested field, target country, and budget before making a final decision.";
        }
        String firstWeakness = weakPoints.isEmpty()
                ? ""
                : " First priority: " + weakPoints.get(0);
        if (score >= 80) {
            return "Keep this university in your shortlist and verify the latest official program requirements."
                    + firstWeakness;
        }
        if (score >= 65) {
            return "This is a realistic option. Strengthen the weak areas and apply before the priority deadline."
                    + firstWeakness;
        }
        if (score >= 50) {
            return "Treat this as an ambitious application and keep safer alternatives in the shortlist."
                    + firstWeakness;
        }
        return "Improve the highlighted gaps before applying, or consider universities with more flexible requirements."
                + firstWeakness;
    }

    private static double universityFee(University university) {
        double fee = number(university.fees);
        if (fee > 0) return fee;
        for (University.Program program : university.getPrograms()) {
            fee = number(program.getYearlyFees());
            if (fee > 0) return fee;
        }
        return 0;
    }

    private static double estimatedGpaRequirement(double acceptanceRate) {
        if (acceptanceRate <= 0) return 3.0;
        if (acceptanceRate >= 70) return 2.5;
        if (acceptanceRate >= 50) return 2.8;
        if (acceptanceRate >= 30) return 3.0;
        return 3.3;
    }

    private static double ratioPoints(
            double actual,
            double required,
            double maximum,
            double penaltyRange
    ) {
        if (actual >= required) return maximum;
        double ratio = 1.0 - ((required - Math.max(0, actual)) / penaltyRange);
        return Math.max(maximum * 0.20, maximum * ratio);
    }

    private static String targetDegree(String qualification) {
        String normalized = normalize(qualification);
        if (normalized.contains("intermediate")
                || normalized.contains("hssc")
                || normalized.contains("a level")
                || normalized.contains("high school")) {
            return "bachelor";
        }
        if (normalized.contains("bachelor")
                || normalized.contains("undergraduate")
                || normalized.contains("bs ")) {
            return "master";
        }
        if (normalized.contains("master")
                || normalized.contains("graduate")
                || normalized.contains("ms ")) {
            return "phd";
        }
        return "";
    }

    private static String category(int score) {
        if (score >= 80) return "Safe";
        if (score >= 65) return "Target";
        if (score >= 50) return "Ambitious";
        return "Low Chance";
    }

    private static String suitability(int score) {
        if (score >= 80) return "Highly Suitable";
        if (score >= 65) return "Suitable";
        if (score >= 50) return "Ambitious Match";
        return "Weak Match";
    }

    private static List<String> limit(List<String> source, int maximum) {
        List<String> result = new ArrayList<>();
        for (String item : source) {
            if (!blank(item) && !result.contains(item)) result.add(item.trim());
            if (result.size() >= maximum) break;
        }
        return result;
    }

    private static String value(DocumentSnapshot profile, String... keys) {
        if (profile == null || !profile.exists()) return "";
        for (String key : keys) {
            Object raw = profile.get(key);
            if (raw != null && !String.valueOf(raw).trim().isEmpty()) {
                return String.valueOf(raw).trim();
            }
        }
        return "";
    }

    private static double number(String value) {
        if (blank(value)) return 0;
        Matcher matcher = NUMBER.matcher(value.replace(",", ""));
        if (!matcher.find()) return 0;
        try {
            return Double.parseDouble(matcher.group(1));
        } catch (NumberFormatException ignored) {
            return 0;
        }
    }

    private static boolean containsAny(String haystack, String values) {
        String normalizedHaystack = normalize(haystack);
        for (String item : normalize(values).split("[,;/|]")) {
            String candidate = item.trim();
            if (candidate.length() > 2 && normalizedHaystack.contains(candidate)) {
                return true;
            }
        }
        return false;
    }

    private static boolean countryMatches(String universityLocation, String targets) {
        return containsAny(
                canonicalCountries(universityLocation),
                canonicalCountries(targets)
        );
    }

    private static String canonicalCountries(String value) {
        return normalize(value)
                .replace("united states of america", "usa")
                .replace("united states", "usa")
                .replace("u s a", "usa")
                .replace("united kingdom", "uk")
                .replace("u k", "uk");
    }

    private static String normalize(String value) {
        if (value == null) return "";
        return value.toLowerCase(Locale.US)
                .replace("_", " ")
                .replaceAll("[^a-z0-9,;/| ]", " ")
                .replaceAll("\\s+", " ")
                .trim();
    }

    private static int clamp(int value, int minimum, int maximum) {
        return Math.max(minimum, Math.min(maximum, value));
    }

    private static boolean blank(String value) {
        return value == null || value.trim().isEmpty();
    }
}
