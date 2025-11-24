# Missing Features in Android Project (Compared to iOS)

## Overview
This document lists all features present in the iOS project but missing or incomplete in the Android project.

---

## 1. **Home Screen Features**

### ✅ Implemented
- Daily Tips carousel (with AI-generated tips)
- Pet Health Snapshot section
- Upcoming Reminders section
- Find Care button

### ❌ Missing
- **Auto-refresh timer** - iOS refreshes AI content every 1 hour automatically
- **Refresh AI Status button** - Manual refresh button for pet statuses
- **Progressive loading** - iOS shows tips/statuses as they're generated (per-pet), Android waits for all
- **Silent refresh mode** - iOS has silent refresh that doesn't show loading if cached content exists

**iOS Reference:** `HomeView.swift` lines 44-46, 365-395, 399-560

---

## 2. **Discover Screen Features**

### ✅ Implemented
- Find Care view with hero card
- Find a Vet / Find a Pet Sitter destination cards
- Care Highlights section
- Mode selector (Find Care / Map)

### ❌ Missing
- **Integrated Map View** - iOS has a full map view with vet/sitter locations directly in Discover tab
- **Map annotations** - Clickable pins showing vets and sitters on map
- **Map sheets** - Bottom sheets showing vet/sitter details when clicking map pins
- **Availability badges** - Real-time availability status on map pins
- **Map legend** - Legend showing available vet/sitter/offline status

**iOS Reference:** `DiscoverView.swift` lines 270-344, `MapScreen.swift`

**Note:** Android has a separate `MapScreen` but it's not integrated into Discover tab like iOS.

---

## 3. **Profile Completion Flow**

### ❌ Missing
- **Profile completion check** - iOS checks if profile needs completion (photo, phone, location)
- **Profile completion alert** - Alert dialog prompting user to complete profile
- **Auto-navigation to edit profile** - iOS automatically navigates to edit profile when completion is needed
- **Profile completion state management** - `requiresProfileCompletion` and `shouldPresentEditProfile` flags

**iOS Reference:** 
- `MainTabView.swift` lines 60-88
- `SessionManager.swift` lines 23-24, 636-647

---

## 4. **Join Team Screen**

### ❌ Missing
- **JoinTeamView** - A unified screen that lets users choose between joining as Pet Sitter or Vet
- **Join card components** - Beautiful cards with emoji, title, blurb, bullet points, and CTA button
- **Navigation to specific join screens** - From JoinTeamView to JoinPetSitterView or JoinVetView

**iOS Reference:** `JoinTeamView.swift`

**Android Status:** Android has `JoinScreen` but it may not match the iOS design/layout.

---

## 5. **Map Integration**

### ✅ Implemented
- Separate `MapScreen` exists

### ❌ Missing
- **Integrated map in Discover tab** - iOS shows map directly in Discover tab with mode selector
- **Vet/Sitter location loading** - Loading vets and sitters with coordinates from backend
- **Map annotations with availability** - Color-coded pins (orange for vets, blue for sitters, gray for offline)
- **Bottom sheets on pin tap** - Shows vet/sitter details in a bottom sheet
- **Map legend** - Visual legend explaining pin colors
- **Refreshable map** - Pull-to-refresh functionality

**iOS Reference:** `MapScreen.swift`, `DiscoverView.swift` lines 270-344

---

## 6. **Profile Screen Features**

### ✅ Implemented
- User profile display
- Account info section
- Settings section
- Subscription management

### ❌ Missing
- **Profile stats** - iOS shows "Pets" and "Appointments" counts
- **Settings sheet** - iOS has a settings sheet with theme toggle, change password, change email, help, logout
- **Auto-refresh on tab switch** - iOS refreshes profile/pets when switching to Profile or My Pets tabs
- **Profile completion prompt** - Button/alert to complete profile if missing required fields

**iOS Reference:** `ProfileView.swift` lines 147-166, 200-443

---

## 7. **Navigation & Tab Bar**

### ✅ Implemented
- Bottom navigation bar with 5 tabs
- Tab switching animations

### ❌ Missing
- **Tab bar hide/show** - iOS can hide tab bar on certain screens using `TabBarHiddenPreferenceKey`
- **Tab transition animations** - iOS has opacity + scale transitions when switching tabs
- **Tab refresh notifications** - iOS posts notifications to refresh Profile/MyPets when switching tabs

**iOS Reference:** `MainTabView.swift` lines 34-45

---

## 8. **AI Content Generation**

### ✅ Implemented
- AI tips generation
- AI status generation
- AI reminders generation

### ❌ Missing
- **Per-pet progressive loading** - iOS loads and displays content for each pet as it's generated
- **Rate limiting handling** - iOS has centralized rate limiting in GeminiService (max 2 requests per minute)
- **Calendar event integration** - iOS loads calendar events for each pet before generating AI content
- **Silent refresh mode** - iOS can refresh without showing loading if cached content exists
- **Error handling with cached content** - iOS shows cached tips/reminders even if new generation fails

**iOS Reference:** `HomeView.swift` lines 399-560, `PetAIViewModel.swift`

---

## 9. **Calendar Integration**

### ✅ Implemented
- Calendar screen
- Add calendar event screen

### ❌ Missing
- **Calendar event loading for AI** - iOS loads calendar events for each pet before generating AI content
- **Calendar authorization check** - iOS requests calendar access before loading events
- **Event filtering by pet** - iOS filters calendar events by pet ID

**iOS Reference:** `HomeView.swift` lines 415-435

---

## 10. **Chat/Conversations**

### ✅ Implemented
- Conversations list
- Chat view
- Message sending/receiving

### ❌ Missing
- **CommunityView wrapper** - iOS has a CommunityView that wraps ConversationsListView with a close button
- **Chat polling** - iOS has `chatManager.startPolling()` for real-time updates (though Android uses FCM)

**iOS Reference:** `CommunityView.swift`, `HomeView.swift` line 102

---

## 11. **Notifications**

### ✅ Implemented
- Notifications screen
- Notification badges
- Deep linking from notifications

### ❌ Missing
- **Notification polling** - iOS has `notificationManager.startPolling()` (though Android uses FCM)
- **Unread count updates** - iOS updates unread count on HomeView appear

**iOS Reference:** `HomeView.swift` lines 103-110

---

## 12. **Subscription Management**

### ✅ Implemented
- Subscription management screen
- Email verification
- Resend verification code
- Subscribe again button

### ❌ Missing
- **Subscription expiration alerts** - iOS shows alerts when subscription is expiring soon
- **Auto-check on app foreground** - iOS checks subscription when app comes to foreground
- **Auto-check on login** - iOS checks subscription when user logs in

**iOS Reference:** `MainTabView.swift` lines 89-129

---

## 13. **UI/UX Polish**

### ❌ Missing
- **Haptic feedback** - iOS has `hapticTap()` for button interactions
- **Spring animations** - iOS uses spring animations for card presses
- **Staggered animations** - iOS has staggered fade-in for pet cards
- **Loading states** - More granular loading states (e.g., loading tips vs loading statuses separately)
- **Empty states** - Better empty state messages (e.g., "No tips or recommendations - Add a pet to get personalized tips")

**iOS Reference:** `HomeView.swift` lines 640-644, various animation examples

---

## 14. **Settings Sheet**

### ❌ Missing
- **Settings sheet/modal** - iOS has a dedicated settings sheet with:
  - Theme toggle (Light/Dark/System)
  - Change Password
  - Change Email
  - Help
  - Logout confirmation

**iOS Reference:** `ProfileView.swift` lines 200-443

---

## 15. **Error Handling & User Feedback**

### ❌ Missing
- **Better error messages** - iOS shows specific error messages (e.g., "AI unavailable" with retry button)
- **Retry mechanisms** - iOS has retry buttons for failed AI content generation
- **Cached content fallback** - iOS shows cached tips/reminders even if new generation fails
- **Progressive error handling** - iOS handles errors per-pet, not all-or-nothing

**iOS Reference:** `HomeView.swift` lines 208-224, 322-357

---

## Summary

### High Priority Missing Features:
1. **Profile completion flow** - Critical for user onboarding
2. **Integrated map in Discover tab** - Major feature difference
3. **JoinTeamView** - Missing unified join screen
4. **Settings sheet** - Missing settings UI
5. **Auto-refresh timer for AI content** - Missing background refresh
6. **Progressive AI loading** - Better UX for AI content

### Medium Priority:
1. **Tab bar hide/show** - Better navigation UX
2. **Calendar event integration with AI** - Enhanced AI context
3. **Subscription expiration alerts** - Better subscription management
4. **Haptic feedback** - Better user interaction feedback

### Low Priority (Polish):
1. **Spring animations** - Visual polish
2. **Staggered animations** - Visual polish
3. **Better empty states** - UX improvement
4. **CommunityView wrapper** - Minor UI difference

---

## Notes

- Android uses FCM for real-time updates, while iOS uses polling. This is a design difference, not a missing feature.
- Some features may be implemented differently in Android (e.g., navigation structure).
- This comparison is based on iOS codebase analysis as of the latest review.

