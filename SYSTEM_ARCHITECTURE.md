# AbroadIQ System Architecture

## Overview

AbroadIQ is a study-abroad recommendation system that uses **rule-based logic** for eligibility decisions and **AI (optional)** for ranking and explanations.

## Core Principles

1. **Rule-Based First**: All eligibility decisions are made by explicit rules
2. **AI-Assisted**: AI is only used for ranking and generating explanations
3. **Safe & Explainable**: Every recommendation has a clear reason
4. **Scalable**: Modular design allows easy addition of new countries/fields

## System Components

### 1. Data Layer

#### Models
- `University.java`: University data model
- `Scholarship.java`: Scholarship data model
- `UserProfile.java`: User profile model

#### Repository
- `RecommendationRepository.java`: Fetches data from Firebase

### 2. Business Logic Layer

#### Recommendation Engine
- `RecommendationEngine.java`: Rule-based recommendation logic
  - University classification (Safe/Target/Ambitious)
  - Scholarship eligibility checking
  - Explanation generation (rule-based)

#### AI Service (Optional)
- `AIRankingService.java`: AI-powered ranking and explanations
  - Uses Gemini API for ranking
  - Generates personalized explanations
  - Falls back to rule-based if AI unavailable

### 3. Presentation Layer

#### Activities
- `RecommendationsActivity.java`: Main recommendations screen
- `EditProfileActivity.java`: User profile editing (updated with budget/IELTS)

#### Adapters
- `UniversityAdapter.java`: RecyclerView adapter for universities
- `ScholarshipAdapter.java`: RecyclerView adapter for scholarships

### 4. Data Pipeline

#### Python Scraper
- `data_scraper/scraper.py`: Modular web scraper
  - Supports multiple countries
  - Raw data storage
  - Data cleaning & normalization
  - JSON output for Firebase upload

## Recommendation Flow

```
1. User Profile (Firebase)
   ↓
2. Fetch Universities & Scholarships (Firebase)
   ↓
3. Rule-Based Filtering (RecommendationEngine)
   - Country match
   - Field match
   - CGPA/IELTS requirements
   - Budget check (optional)
   ↓
4. Classification (Universities)
   - Safe: User exceeds requirements
   - Target: User matches requirements
   - Ambitious: User slightly below
   ↓
5. AI Ranking (Optional)
   - Rank by best fit
   - Generate explanations
   ↓
6. Display (RecyclerView)
   - Categorized universities
   - Eligible scholarships
```

## Rule-Based Logic

### University Classification

```java
SAFE:     userCgpa >= uniMinCgpa + 0.3  AND  userIelts >= uniMinIelts + 0.5
TARGET:   userCgpa >= uniMinCgpa + 0.1  AND  userIelts >= uniMinIelts + 0.5
AMBITIOUS: userCgpa >= uniMinCgpa - 0.2  AND  userIelts >= uniMinIelts - 0.5
```

### Scholarship Eligibility

```java
ELIGIBLE IF:
  - userCgpa >= scholarshipMinCgpa
  - userField matches scholarshipField
  - userCountry matches scholarshipCountry
  - userDegreeLevel matches scholarshipDegreeLevel
```

## AI Integration (Optional)

### When AI is Used

1. **Ranking**: After rule-based filtering, AI can reorder results by best fit
2. **Explanations**: Generate personalized explanations for recommendations

### When AI is NOT Used

1. **Eligibility**: Never used for eligibility decisions
2. **Filtering**: Never used to filter out recommendations

### Fallback

If AI service fails:
- Falls back to rule-based ranking (Safe > Target > Ambitious, then by ranking)
- Falls back to rule-based explanations

## Database Schema

See `FIREBASE_SCHEMA.md` for detailed schema.

### Key Paths:
- `Users/{userId}`: User profile data
- `Universities/{universityId}`: University data
- `Scholarships/{scholarshipId}`: Scholarship data

## Security

1. **Firebase Rules**: Users can only access their own profile
2. **API Keys**: Gemini API key should be stored securely (Firebase Remote Config or backend)
3. **Data Validation**: All inputs validated before processing

## Scalability

### Adding New Countries

1. Add country to `SUPPORTED_COUNTRIES` in scraper
2. Implement `_scrape_{country}()` method
3. Data automatically flows through pipeline

### Adding New Fields

1. Add field to `supportedFields` in university data
2. Update field matching logic if needed
3. No code changes required for basic fields

## Testing

### Unit Tests
- `RecommendationEngine`: Test classification logic
- `RecommendationRepository`: Test data parsing

### Integration Tests
- End-to-end recommendation flow
- Firebase data fetching
- AI service fallback

## Future Enhancements

1. **Backend API**: Move AI calls to backend for security
2. **Caching**: Cache recommendations for better performance
3. **Analytics**: Track recommendation effectiveness
4. **A/B Testing**: Test different ranking algorithms
5. **Multi-language**: Support multiple languages

## Deployment

### Android App
1. Build APK/AAB
2. Upload to Google Play Store
3. Configure Firebase project

### Data Pipeline
1. Run scraper periodically (cron job)
2. Upload cleaned data to Firebase
3. Monitor data quality

## Support

For issues or questions, refer to:
- `FIREBASE_SCHEMA.md`: Database structure
- `data_scraper/README.md`: Scraper usage
- Code comments: Inline documentation
