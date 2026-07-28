package com.example.madfinalproject.recommendations;

import com.example.madfinalproject.models.TrendingDestination;
import com.example.madfinalproject.models.University;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public final class TrendingDestinationEngine {
    private TrendingDestinationEngine() {}

    public static List<TrendingDestination> rank(List<University> universities, int limit) {
        Map<String, CountryStats> grouped = new HashMap<>();

        for (University university : universities) {
            if (university == null) continue;
            String country = countryFrom(university.location);
            double ratio = parseRatio(university.visaRatio);
            if (country.isEmpty() || ratio < 0) continue;

            String key = country.toLowerCase(Locale.ROOT);
            CountryStats stats = grouped.get(key);
            if (stats == null) {
                stats = new CountryStats(country);
                grouped.put(key, stats);
            }
            stats.totalRatio += ratio;
            stats.count++;
            if (ratio > stats.bestRatio && university.imageUrl != null
                    && !university.imageUrl.trim().isEmpty()) {
                stats.bestRatio = ratio;
                stats.imageUrl = university.imageUrl;
            }
        }

        List<TrendingDestination> result = new ArrayList<>();
        for (CountryStats stats : grouped.values()) {
            result.add(new TrendingDestination(
                    stats.country,
                    stats.imageUrl,
                    (int) Math.round(stats.totalRatio / stats.count),
                    stats.count));
        }
        result.sort((a, b) -> {
            int ratioOrder = Integer.compare(b.getVisaRatio(), a.getVisaRatio());
            return ratioOrder != 0
                    ? ratioOrder
                    : Integer.compare(b.getUniversityCount(), a.getUniversityCount());
        });
        return new ArrayList<>(result.subList(0, Math.min(Math.max(limit, 0), result.size())));
    }

    private static String countryFrom(String location) {
        if (location == null) return "";
        String value = location.trim();
        if (value.isEmpty()) return "";
        int comma = value.lastIndexOf(',');
        return comma >= 0 ? value.substring(comma + 1).trim() : value;
    }

    private static double parseRatio(String value) {
        if (value == null) return -1;
        String numeric = value.replaceAll("[^0-9.]", "");
        if (numeric.isEmpty()) return -1;
        try {
            double ratio = Double.parseDouble(numeric);
            if (ratio >= 0 && ratio <= 1) ratio *= 100;
            return ratio >= 0 && ratio <= 100 ? ratio : -1;
        } catch (NumberFormatException ignored) {
            return -1;
        }
    }

    private static final class CountryStats {
        final String country;
        double totalRatio;
        double bestRatio = -1;
        int count;
        String imageUrl = "";

        CountryStats(String country) { this.country = country; }
    }
}
