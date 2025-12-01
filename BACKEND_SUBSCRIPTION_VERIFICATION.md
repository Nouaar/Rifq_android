# Backend Subscription Integration Verification

## Base URL
- **Production**: `https://rifq.onrender.com/`

## Required Subscription API Endpoints

### 1. Create Subscription
- **Endpoint**: `POST /subscriptions`
- **Request Body**:
  ```json
  {
    "role": "vet" | "sitter",
    "paymentMethodId": "string (optional)"
  }
  ```
- **Response**:
  ```json
  {
    "subscription": {
      "id": "string",
      "userId": "string",
      "role": "vet" | "sitter",
      "status": "pending",
      "stripeSubscriptionId": "string",
      "stripeCustomerId": "string",
      "currentPeriodStart": "ISO 8601 date",
      "currentPeriodEnd": "ISO 8601 date",
      "cancelAtPeriodEnd": false,
      "createdAt": "ISO 8601 date",
      "updatedAt": "ISO 8601 date"
    },
    "clientSecret": "string (for Stripe PaymentSheet)",
    "message": "string"
  }
  ```
- **Expected Behavior**:
  - Create subscription with status `pending`
  - Send verification email with 6-digit code
  - User role should NOT be upgraded yet (still `owner`)
  - User should NOT appear in discover list/map yet

### 2. Get User Subscription
- **Endpoint**: `GET /subscriptions/me`
- **Response**:
  ```json
  {
    "id": "string",
    "userId": "string",
    "role": "vet" | "sitter" | "owner",
    "status": "active" | "expires_soon" | "canceled" | "expired" | "pending" | "none",
    "stripeSubscriptionId": "string",
    "stripeCustomerId": "string",
    "currentPeriodStart": "ISO 8601 date",
    "currentPeriodEnd": "ISO 8601 date",
    "cancelAtPeriodEnd": boolean,
    "createdAt": "ISO 8601 date",
    "updatedAt": "ISO 8601 date"
  }
  ```
- **Expected Behavior**:
  - Return subscription with current status
  - If no subscription exists, return status `"none"`

### 3. Verify Email (Activate Subscription)
- **Endpoint**: `POST /subscriptions/verify-email`
- **Request Body**:
  ```json
  {
    "code": "6-digit string"
  }
  ```
- **Response**:
  ```json
  {
    "success": true,
    "message": "string",
    "subscription": {
      "id": "string",
      "userId": "string",
      "role": "vet" | "sitter",
      "status": "active",
      ...
    }
  }
  ```
- **Expected Behavior**:
  - Verify the code matches the one sent via email
  - Change subscription status from `pending` → `active`
  - **Upgrade user role** from `owner` → `vet` or `sitter`
  - **Add user to discover list/map** (should appear in `/veterinarians` or `/pet-sitters`)

### 4. Resend Verification Code
- **Endpoint**: `POST /subscriptions/resend-verification`
- **Response**:
  ```json
  {
    "message": "Verification code sent to your email"
  }
  ```
- **Expected Behavior**:
  - Resend 6-digit verification code to user's email
  - Works even if user is already verified (for subscription activation)

### 5. Cancel Subscription
- **Endpoint**: `POST /subscriptions/cancel`
- **Response**:
  ```json
  {
    "subscription": {
      "id": "string",
      "userId": "string",
      "role": "vet" | "sitter",
      "status": "canceled",
      "cancelAtPeriodEnd": true,
      ...
    },
    "message": "string"
  }
  ```
- **Expected Behavior**:
  - Set `cancelAtPeriodEnd: true`
  - Change status to `canceled`
  - Subscription remains active until `currentPeriodEnd`
  - **At period end**: Downgrade role to `owner` and remove from discover list/map

### 6. Reactivate Subscription
- **Endpoint**: `POST /subscriptions/reactivate`
- **Response**:
  ```json
  {
    "id": "string",
    "userId": "string",
    "role": "vet" | "sitter",
    "status": "active",
    "cancelAtPeriodEnd": false,
    ...
  }
  ```
- **Expected Behavior**:
  - Can only reactivate if subscription hasn't ended yet
  - Set `cancelAtPeriodEnd: false`
  - Change status back to `active`
  - User remains in discover list/map

### 7. Renew Subscription
- **Endpoint**: `POST /subscriptions/renew`
- **Response**:
  ```json
  {
    "id": "string",
    "userId": "string",
    "role": "vet" | "sitter",
    "status": "active",
    "currentPeriodStart": "new date",
    "currentPeriodEnd": "new date (extended by 1 month)",
    ...
  }
  ```
- **Expected Behavior**:
  - Extend subscription for another billing period (1 month)
  - Update `currentPeriodStart` and `currentPeriodEnd`
  - Change status to `active` if it was `expires_soon` or `expired`
  - User remains in discover list/map

## Subscription Status Management

### Status Transitions

1. **pending** → **active** (via email verification)
   - User pays $30/month
   - Subscription created with status `pending`
   - User verifies email → status becomes `active`
   - Role upgraded to `vet`/`sitter`
   - Added to discover list/map

2. **active** → **expires_soon** (7 days before expiration)
   - Backend should automatically set status to `expires_soon` when `currentPeriodEnd` is within 7 days
   - User can renew or let expire

3. **active** → **canceled** (user cancels)
   - User cancels subscription
   - Status becomes `canceled`
   - `cancelAtPeriodEnd: true`
   - At period end: Role downgraded to `owner`, removed from discover list/map

4. **active** → **expired** (period ends without renewal)
   - When `currentPeriodEnd` passes, status becomes `expired`
   - User can renew within 3 days
   - After 3 days: Auto-canceled (status → `canceled`), role downgraded to `owner`, removed from discover list/map

5. **expired** → **canceled** (auto-cancellation after 3 days)
   - Backend should automatically cancel subscriptions that have been `expired` for 3+ days
   - This should be a scheduled job/cron task

6. **expires_soon** → **active** (user renews)
   - User renews subscription
   - Status becomes `active`
   - Period extended

7. **expired** → **active** (user renews within 3 days)
   - User renews before auto-cancellation
   - Status becomes `active`
   - Period extended

## Discover List/Map Filtering

### GET /veterinarians
- **Expected Behavior**:
  - **ONLY return users with `role: "vet"` AND `subscription.status: "active"`**
  - Do NOT return users with:
    - `subscription.status: "pending"`
    - `subscription.status: "canceled"`
    - `subscription.status: "expired"`
    - `subscription.status: "expires_soon"`
    - No subscription (role is `owner`)

### GET /pet-sitters
- **Expected Behavior**:
  - **ONLY return users with `role: "sitter"` AND `subscription.status: "active"`**
  - Same filtering rules as `/veterinarians`

## Role Management

### Role Upgrades
- **When**: Subscription status changes from `pending` → `active` (via email verification)
- **Action**: Update user `role` field from `owner` → `vet` or `sitter`
- **Also**: Add user to discover list/map

### Role Downgrades
- **When**: 
  1. Subscription is canceled AND period ends
  2. Subscription expires AND 3 days pass (auto-cancellation)
- **Action**: Update user `role` field from `vet`/`sitter` → `owner`
- **Also**: Remove user from discover list/map

## Auto-Cancellation Job

### Required Scheduled Task
- **Frequency**: Daily (or as needed)
- **Logic**:
  1. Find all subscriptions with `status: "expired"`
  2. Check if `currentPeriodEnd` is more than 3 days ago
  3. For each expired subscription:
     - Set `status: "canceled"`
     - Downgrade user role to `owner`
     - Remove user from discover list/map (they won't appear in `/veterinarians` or `/pet-sitters`)

## Email Verification

### Email Sending
- **When**: After subscription creation (`POST /subscriptions`)
- **Content**: 6-digit verification code
- **Purpose**: Activate subscription and upgrade role

### Code Validation
- **Endpoint**: `POST /subscriptions/verify-email`
- **Validation**: Check if code matches the one sent
- **On Success**: Activate subscription, upgrade role, add to discover list

## Testing Checklist

### ✅ Backend Verification Checklist

- [ ] `POST /subscriptions` creates subscription with status `pending`
- [ ] `POST /subscriptions` sends verification email with 6-digit code
- [ ] `GET /subscriptions/me` returns subscription with correct status
- [ ] `POST /subscriptions/verify-email` verifies code and activates subscription
- [ ] `POST /subscriptions/verify-email` upgrades user role to `vet`/`sitter`
- [ ] `POST /subscriptions/verify-email` adds user to discover list/map
- [ ] `POST /subscriptions/resend-verification` resends verification code
- [ ] `POST /subscriptions/cancel` cancels subscription (at period end)
- [ ] `POST /subscriptions/cancel` downgrades role when period ends
- [ ] `POST /subscriptions/cancel` removes user from discover list when period ends
- [ ] `POST /subscriptions/reactivate` reactivates canceled subscription
- [ ] `POST /subscriptions/renew` extends subscription period
- [ ] `GET /veterinarians` only returns users with active subscriptions
- [ ] `GET /pet-sitters` only returns users with active subscriptions
- [ ] Auto-cancellation job runs daily and cancels expired subscriptions after 3 days
- [ ] Status automatically changes to `expires_soon` 7 days before expiration
- [ ] Status automatically changes to `expired` when period ends
- [ ] Role is properly downgraded when subscription is canceled/expired

## Notes

- The Android app expects all these endpoints to be implemented
- The backend should handle all role upgrades/downgrades automatically
- The backend should filter discover list/map based on subscription status
- The backend should handle auto-cancellation via scheduled jobs
- All date fields should be in ISO 8601 format (UTC)
