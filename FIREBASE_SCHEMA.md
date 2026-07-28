# AbroadIQ Firebase Database Schema

## Overview

This document describes the Firebase Realtime Database structure for AbroadIQ recommendation system.

## Database Structure

```
Firebase Realtime Database
├── Users/
│   └── {userId}/
│       ├── fullName: string
│       ├── email: string
│       ├── cgpa: number (0-4.0)
│       ├── bands: number (IELTS score, 0-9)
│       ├── budget: number (USD per year)
│       ├── targetCountries: string (comma-separated)
│       ├── interestedFields: string (comma-separated)
│       ├── qualification: string
│       ├── degreeLevel: "Bachelors" | "Masters"
│       └── ... (other profile fields)
│
├── Universities/
│   └── {universityId}/
│       ├── id: string
│       ├── name: string
│       ├── country: string
│       ├── minCgpa: number
│       ├── minIelts: number
│       ├── averageTuitionFee: number (USD)
│       ├── globalRanking: number
│       └── supportedFields: array<string>
│
└── Scholarships/
    └── {scholarshipId}/
        ├── id: string
        ├── title: string
        ├── country: string
        ├── minCgpa: number
        ├── fieldEligibility: string
        ├── fundingAmount: number (USD)
        └── degreeLevel: "Bachelors" | "Masters"
```

## User Profile Schema

### Path: `Users/{userId}`

| Field | Type | Description | Required |
|-------|------|-------------|----------|
| `fullName` | string | User's full name | Yes |
| `email` | string | User's email | Yes |
| `cgpa` | number | CGPA out of 4.0 | Yes (for recommendations) |
| `bands` | number | IELTS score (0-9) | Yes (for recommendations) |
| `budget` | number | Annual budget in USD | Yes (for recommendations) |
| `targetCountries` | string | Comma-separated list | Yes (for recommendations) |
| `interestedFields` | string | Comma-separated list | Yes (for recommendations) |
| `qualification` | string | Current qualification level | No |
| `degreeLevel` | string | "Bachelors" or "Masters" | Yes (for recommendations) |

### Example:
```json
{
  "fullName": "John Doe",
  "email": "john@example.com",
  "cgpa": 3.7,
  "bands": 7.5,
  "budget": 50000,
  "targetCountries": "USA, UK, Australia",
  "interestedFields": "Computer Science, Data Science",
  "qualification": "Bachelor's (BS)",
  "degreeLevel": "Masters"
}
```

## University Schema

### Path: `Universities/{universityId}`

| Field | Type | Description | Required |
|-------|------|-------------|----------|
| `id` | string | Unique identifier | Yes |
| `name` | string | University name | Yes |
| `country` | string | Country name | Yes |
| `minCgpa` | number | Minimum CGPA requirement | Yes |
| `minIelts` | number | Minimum IELTS requirement | Yes |
| `averageTuitionFee` | number | Average tuition in USD/year | Yes |
| `globalRanking` | number | Global university ranking | Yes |
| `supportedFields` | array | List of supported fields | Yes |

### Example:
```json
{
  "id": "mit_001",
  "name": "Massachusetts Institute of Technology",
  "country": "USA",
  "minCgpa": 3.7,
  "minIelts": 7.0,
  "averageTuitionFee": 53790,
  "globalRanking": 1,
  "supportedFields": ["Computer Science", "Engineering", "Business"]
}
```

## Scholarship Schema

### Path: `Scholarships/{scholarshipId}`

| Field | Type | Description | Required |
|-------|------|-------------|----------|
| `id` | string | Unique identifier | Yes |
| `title` | string | Scholarship name | Yes |
| `country` | string | Country name | Yes |
| `minCgpa` | number | Minimum CGPA requirement | Yes |
| `fieldEligibility` | string | Eligible field of study | Yes |
| `fundingAmount` | number | Funding amount in USD | Yes |
| `degreeLevel` | string | "Bachelors" or "Masters" | Yes |

### Example:
```json
{
  "id": "chevening_001",
  "title": "Chevening Scholarships",
  "country": "UK",
  "minCgpa": 3.3,
  "fieldEligibility": "Computer Science",
  "fundingAmount": 25000,
  "degreeLevel": "Masters"
}
```

## Firebase Security Rules

```json
{
  "rules": {
    "Users": {
      "$userId": {
        ".read": "$userId === auth.uid",
        ".write": "$userId === auth.uid"
      }
    },
    "Universities": {
      ".read": "auth != null",
      ".write": false
    },
    "Scholarships": {
      ".read": "auth != null",
      ".write": false
    }
  }
}
```

## Data Upload Script

Use the Python scraper to generate cleaned JSON, then upload to Firebase:

```python
# Example: Upload universities to Firebase
import firebase_admin
from firebase_admin import credentials, db

cred = credentials.Certificate("path/to/serviceAccountKey.json")
firebase_admin.initialize_app(cred, {
    'databaseURL': 'https://your-project.firebaseio.com'
})

# Load cleaned data
with open('data/cleaned/universities_all_cleaned.json') as f:
    universities = json.load(f)

# Upload to Firebase
ref = db.reference('Universities')
for uni in universities:
    ref.push(uni)
```

## Notes

1. **User Data Privacy**: Users can only read/write their own data
2. **Universities/Scholarships**: Read-only for authenticated users, write-only via admin scripts
3. **Data Updates**: Universities and scholarships should be updated via admin scripts, not directly by users
4. **Indexing**: Consider adding indexes for common queries (e.g., by country, by field)
