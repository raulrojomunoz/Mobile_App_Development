# City Explorer Challenge

City Explorer Challenge is an Android application that generates personalized city exploration challenges based on the user's location, interests, nearby places, and completed challenge history.

The app is designed as an intelligent exploration assistant. Instead of showing a static list of places, it dynamically creates challenges using real nearby locations from OpenStreetMap through the Overpass API. The recommendation logic considers distance, category, user preferences, previous completions, category balance, and time of day.

---

## Main Features

- First-time user preferences setup
- Personalized challenge generation
- Real nearby places from OpenStreetMap / Overpass API
- User location support with fallback demo location
- Interactive OpenStreetMap screen using osmdroid
- User marker, target marker, and route line
- Demo Mode and Real Mode for challenge completion
- Local challenge history using Room Database
- Statistics screen with category balance and diversity analysis
- Challenge details screen explaining why each challenge was generated
- Modern Jetpack Compose UI with bottom navigation

---

## App Flow

When the app is opened for the first time, the user is asked to select their preferred categories and maximum exploration distance.

After the preferences are saved, the app opens the Home screen and generates the first personalized challenge.

The user can then:

1. View the active challenge on the Home screen.
2. Open the Details screen to understand why the challenge was generated.
3. Open the Map screen to see the user location, target place, and route line.
4. Complete the challenge using Demo Mode or Real Mode.
5. View completed challenges in History.
6. Review exploration statistics in the Statistics screen.

On future app launches, the user goes directly to the Home screen because the preferences are stored locally.

---

## Screens

### Preferences Setup

The first screen shown to a new user. The user selects preferred categories and maximum exploration distance. These preferences are saved locally and used by the challenge generator.

### Home

The Home screen displays location status, nearby places loading status, the current active challenge, progress summary, and a button to generate a new challenge when no active challenge is pending.

The app prevents the user from generating unlimited new challenges while one challenge is still active.

### Challenge Details

The Details screen explains the active challenge, target place, distance, category, challenge status, reasons why the challenge was generated, input data used by the generator, and general recommendation logic.

This screen helps demonstrate that the challenge is not random or static.

### Map

The Map screen shows the OpenStreetMap map, user marker, target place marker, route line between user and destination, current challenge information, Demo Mode / Real Mode switch, and challenge completion button.

### History

The History screen stores and displays completed challenges using Room Database.

Each completed challenge includes challenge title, target place, category, distance, and completion date.

The history persists after closing and reopening the app.

### Statistics

The Statistics screen summarizes the user's exploration activity.

It shows total completed challenges, completed challenges today, total explored distance, most explored category, category distribution, diversity score, balance status, and preferences vs completed exploration.

This helps verify whether the recommendation system is keeping the experience varied.

---

## Challenge Generation Logic

The challenge generator uses several rules to decide which challenge should be shown.

The main factors are:

1. User preferences  
   Categories selected during the first-time setup influence the recommendation.

2. Distance  
   Places closer to the user and within the selected maximum distance receive higher priority.

3. Category balance  
   The app avoids letting one category dominate the user's history.

4. Completed challenge history  
   Previously completed categories and places affect future recommendations.

5. Recent repetition control  
   The generator penalizes recently completed categories to avoid streaks.

6. Previous active challenge  
   The app avoids immediately showing the same type of challenge again.

7. Time of day  
   Some categories receive small priority changes depending on the current time.

8. OpenStreetMap candidate balancing  
   Since some categories, especially food, can have many more results than others, the app creates a balanced candidate pool before generating a challenge.

The goal is to create recommendations that are personalized, varied, and context-aware.

---

## OpenStreetMap and Overpass API

The app uses the Overpass API to request real nearby places from OpenStreetMap.

The app searches for places such as cafes, restaurants, museums, galleries, attractions, historic places, parks, gardens, nature areas, sports centres, stadiums, and sports pitches.

The API can return thousands of raw places. To prevent one category from dominating the recommendation system, the app groups the places by category and creates a balanced candidate pool before passing the data to the challenge generator.

This makes the recommendations more fair and diverse.

---

## Location Handling

The app requests location permission from the user.

If location permission is granted, the app uses the user's current location.

If the location is unavailable or permission is denied, the app uses a fallback demo location. This allows the app to continue working during testing and presentation.

---

## Demo Mode and Real Mode

The Map screen includes two completion modes.

### Demo Mode

Demo Mode allows the challenge to be completed without physically visiting the target place.

This mode is useful for presentations and screencasts.

### Real Mode

Real Mode validates the distance between the user and the target place.

The challenge can only be completed when the user is close enough to the destination.

This demonstrates that the app has real location-based validation logic.

---

## Local Persistence

The app uses Room Database to store completed challenges locally.

Stored information includes challenge ID, title, description, category, target place, coordinates, distance, status, generation reasons, creation date, and completion date.

This allows the History and Statistics screens to persist after closing and reopening the app.

---

## Technologies Used

- Kotlin
- Jetpack Compose
- Material 3
- Navigation Compose
- Room Database
- Retrofit
- OkHttp
- Gson
- Overpass API
- OpenStreetMap
- osmdroid
- Google Play Services Location
- Coroutines
- SharedPreferences

---

## Project Structure

The project follows a simple layered structure:

```text
data
 ├── local
 ├── remote
 └── repository

domain
 ├── engine
 ├── model
 └── repository

location

presentation
 ├── details
 ├── history
 ├── home
 ├── map
 ├── navigation
 └── statistics

ui.theme
```

### Domain Layer

Contains the main models and the challenge generation engine.

Important classes:

- `Challenge`
- `Place`
- `PlaceCategory`
- `ChallengeStatus`
- `UserPreferences`
- `ChallengeGenerator`

### Data Layer

Handles local and remote data.

Important classes:

- `AppDatabase`
- `ChallengeDao`
- `ChallengeEntity`
- `ChallengeMapper`
- `PlaceRemoteDataSource`
- `OverpassApiService`

### Presentation Layer

Contains the Jetpack Compose screens and navigation.

Important screens:

- `PreferencesSetupScreen`
- `HomeScreen`
- `ChallengeDetailsScreen`
- `MapScreen`
- `HistoryScreen`
- `StatisticsScreen`

### Location Layer

Handles user location and fallback location.

Important class:

- `AppLocationManager`

---

## How to Run the Project

1. Open the project in Android Studio.
2. Sync Gradle.
3. Connect an Android device or start an emulator.
4. Run the app.
5. Allow location permission when requested.
6. Select preferences on the first screen.
7. Start exploring challenges.

Recommended SDK configuration:

```text
compileSdk = 34
targetSdk = 34
minSdk = 34
```

---

## Presentation Notes

During the screencast, the following flow can be demonstrated:

1. Open the app for the first time.
2. Select user preferences.
3. Generate the first challenge.
4. Open the Details screen and explain the generation logic.
5. Open the Map screen and show the user marker, destination marker, and route line.
6. Show Demo Mode and Real Mode.
7. Complete the challenge.
8. Open History and show the completed challenge.
9. Open Statistics and show category balance and diversity analysis.
10. Briefly explain the main code files.

---

## Demo Explanation

For presentation purposes, the app includes Demo Mode.

Demo Mode allows the user to complete a challenge without physically walking to the destination. The real validation logic still exists in Real Mode, where the app checks whether the user is close enough to the target place.

This makes the app easier to demonstrate while still preserving the real location-based behavior.

---

## Conclusion

City Explorer Challenge is not a static list application. It uses real location data, external API integration, user preferences, local persistence, category balancing, and adaptive challenge generation.

The app provides a personalized and interactive way to explore a city while demonstrating important Android development concepts such as Jetpack Compose, navigation, API integration, location services, Room Database, and recommendation logic.
