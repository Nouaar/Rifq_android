# Remaining Missing Features in Android Project (Updated)

## ✅ Recently Completed Features

1. **Profile Completion Flow** ✅
   - Profile completion check utility
   - Alert dialog prompting completion
   - Auto-navigation to edit profile

2. **JoinTeamView Screen** ✅
   - Unified join screen with Pet Sitter and Vet cards
   - Matching iOS design and layout

3. **Settings Sheet** ✅
   - Settings modal with theme toggle
   - Change Password, Change Email, Subscription, Help
   - Logout button with confirmation

4. **Profile Stats** ✅
   - Shows "Pets" and "Appointments" counts

5. **Subscription Expiration Alerts** ✅
   - SubscriptionManager handles expiration alerts
   - Periodic checking (every 30 minutes)
   - Alert cooldown mechanism

---

## ❌ Still Missing Features

### High Priority

#### 1. **Integrated Map View in Discover Tab**
**Status:** ❌ Missing
- **Current:** Android navigates to separate `MapScreen` when Map mode is selected
- **iOS:** Map view is directly embedded in Discover tab with mode selector
- **Missing:**
  - Map view directly in Discover tab (not navigation)
  - Vet/Sitter location pins on map
  - Bottom sheets showing vet/sitter details when clicking pins
  - Availability badges on map pins
  - Map legend (Available vet/sitter/Offline)
  - Pull-to-refresh on map

**iOS Reference:** `DiscoverView.swift` lines 270-344, `MapScreen.swift`

---

#### 2. **Auto-Refresh Timer for AI Content**
**Status:** ❌ Missing
- **Current:** AI content loads once when HomeScreen appears
- **iOS:** Refreshes AI content every 1 hour automatically in background
- **Missing:**
  - Background timer that refreshes every 1 hour
  - Silent refresh mode (doesn't show loading if cached content exists)
  - Timer cleanup when screen disappears

**iOS Reference:** `HomeView.swift` lines 44-46, 365-395

**Implementation needed:**
```kotlin
// In HomeScreen.kt
LaunchedEffect(Unit) {
    // Start auto-refresh timer
    while (true) {
        delay(60 * 60 * 1000L) // 1 hour
        aiViewModel.generateContentForPets(petIds, silent = true)
    }
}
```

---

#### 3. **Progressive AI Loading (Per-Pet)**
**Status:** ❌ Missing
- **Current:** Android waits for all pets' AI content before displaying
- **iOS:** Shows tips/statuses/reminders for each pet as they're generated
- **Missing:**
  - Per-pet progressive loading
  - UI updates immediately when content for one pet is ready
  - Better UX - users see content faster

**iOS Reference:** `HomeView.swift` lines 399-560

**Current Android behavior:**
- `generateContentForPets()` processes all pets at once
- UI only updates when all content is ready

**Needed:**
- Process pets one by one
- Update UI state immediately after each pet's content is generated

---

#### 4. **Refresh AI Status Button**
**Status:** ❌ Missing
- **Current:** No manual refresh button in Pet Health Snapshot section
- **iOS:** Has a "Refresh AI Status" button below pet list
- **Missing:**
  - Button with refresh icon
  - Triggers `loadAIContent()` when clicked
  - Styled with orange accent color

**iOS Reference:** `HomeView.swift` lines 288-308

**Location:** Should be in `PetHealthSnapshotSection` in `HomeScreen.kt`

---

#### 5. **Calendar Event Integration with AI**
**Status:** ❌ Missing
- **Current:** AI content generation doesn't use calendar events
- **iOS:** Loads calendar events for each pet before generating AI content
- **Missing:**
  - Calendar authorization check
  - Load calendar events for each pet
  - Pass calendar events to AI generation
  - Filter events by pet ID

**iOS Reference:** `HomeView.swift` lines 415-435, 443-446

**Implementation needed:**
```kotlin
// Before generating AI content
if (calendarManager.hasPermission()) {
    calendarManager.loadEventsForPet(petId)
    val events = calendarManager.getEventsForPet(petId)
    aiViewModel.generateTips(petId, calendarEvents = events)
}
```

---

### Medium Priority

#### 6. **Tab Bar Hide/Show**
**Status:** ❌ Missing
- **Current:** Tab bar always visible on main tabs
- **iOS:** Can hide tab bar on certain screens using `TabBarHiddenPreferenceKey`
- **Missing:**
  - Preference key system for hiding tab bar
  - Hide tab bar on detail screens (e.g., ChatView, BookingDetail)
  - Show tab bar on main tabs (Home, Discover, etc.)

**iOS Reference:** `MainTabView.swift` lines 34-45

---

#### 7. **Tab Refresh Notifications**
**Status:** ❌ Missing
- **Current:** No automatic refresh when switching tabs
- **iOS:** Posts notifications to refresh Profile/MyPets when switching tabs
- **Missing:**
  - Notification system for tab switches
  - Refresh Profile when switching to Profile tab
  - Refresh MyPets when switching to MyPets tab

**iOS Reference:** `MainTabView.swift` lines 37-45

---

#### 8. **Better Error Handling with Cached Content**
**Status:** ⚠️ Partially Implemented
- **Current:** Has caching but doesn't show cached content if new generation fails
- **iOS:** Shows cached tips/reminders even if new generation fails
- **Missing:**
  - Fallback to cached content on error
  - Show error message but keep displaying cached content
  - Better user experience during network issues

**iOS Reference:** `HomeView.swift` lines 208-224, 322-357

---

### Low Priority (Polish)

#### 9. **Haptic Feedback**
**Status:** ❌ Missing
- **Current:** No haptic feedback on button interactions
- **iOS:** Has `hapticTap()` for button interactions
- **Missing:**
  - Haptic feedback utility
  - Light impact feedback on button taps

**iOS Reference:** `HomeView.swift` lines 640-644

**Implementation:**
```kotlin
fun hapticTap(context: Context) {
    val vibrator = context.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
        vibrator.vibrate(VibrationEffect.createOneShot(10, VibrationEffect.DEFAULT_AMPLITUDE))
    }
}
```

---

#### 10. **Spring Animations**
**Status:** ⚠️ Partially Implemented
- **Current:** Some animations but not spring-based
- **iOS:** Uses spring animations for card presses and interactions
- **Missing:**
  - Spring animation specs for card presses
  - More natural, bouncy animations

**iOS Reference:** Various animation examples in iOS codebase

---

#### 11. **Better Empty States**
**Status:** ⚠️ Basic Implementation
- **Current:** Basic empty state messages
- **iOS:** More helpful empty state messages (e.g., "No tips or recommendations - Add a pet to get personalized tips")
- **Missing:**
  - More descriptive empty state messages
  - Actionable suggestions in empty states

**iOS Reference:** `HomeView.swift` lines 234-246, 338-357

---

#### 12. **CommunityView Wrapper**
**Status:** ❌ Missing
- **Current:** ConversationsListView is accessed directly
- **iOS:** Has CommunityView wrapper with close button
- **Missing:**
  - Wrapper view for conversations list
  - Close button in toolbar

**iOS Reference:** `CommunityView.swift`

**Note:** This is a minor UI difference, not critical functionality.

---

## Summary

### Completed ✅ (5 features)
1. Profile completion flow
2. JoinTeamView screen
3. Settings sheet
4. Profile stats
5. Subscription expiration alerts

### High Priority Missing ❌ (5 features)
1. Integrated map view in Discover tab
2. Auto-refresh timer for AI content (1 hour)
3. Progressive AI loading (per-pet)
4. Refresh AI Status button
5. Calendar event integration with AI

### Medium Priority Missing ❌ (3 features)
1. Tab bar hide/show
2. Tab refresh notifications
3. Better error handling with cached content

### Low Priority Missing ❌ (4 features)
1. Haptic feedback
2. Spring animations
3. Better empty states
4. CommunityView wrapper

---

## Implementation Priority

**Next Steps:**
1. **Integrated Map View** - Major UX difference, users expect map in Discover tab
2. **Auto-Refresh Timer** - Important for keeping AI content fresh
3. **Progressive AI Loading** - Better UX, users see content faster
4. **Refresh AI Status Button** - Quick win, easy to implement
5. **Calendar Event Integration** - Enhances AI context and accuracy

---

## Notes

- Android uses FCM for real-time updates, while iOS uses polling. This is a design difference, not a missing feature.
- Some features may be implemented differently in Android (e.g., navigation structure).
- This comparison is based on iOS codebase analysis as of the latest review.

