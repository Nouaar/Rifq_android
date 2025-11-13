# 🔧 CLOUDINARY UNSIGNED PRESET SETUP - REQUIRED!

## ❌ Current Error

```
Upload failed with code 400: {"error":{"message":"Upload preset not found"}}
```

**Cause:** The upload preset `rifq_pets_unsigned` doesn't exist in your Cloudinary account yet.

---

## ✅ SOLUTION: Create Unsigned Upload Preset

You need to create an **unsigned upload preset** in your Cloudinary dashboard. This takes 2 minutes!

---

## 📋 Step-by-Step Instructions

### **Step 1: Go to Cloudinary Dashboard**

Open: https://cloudinary.com/console

Log in with your account (cloud name: `dpc7d0adc`)

---

### **Step 2: Navigate to Upload Settings**

1. Click **Settings** (gear icon) in the top right
2. Click **Upload** tab in the left sidebar
3. Scroll down to **Upload presets** section

---

### **Step 3: Create New Upload Preset**

Click **"Add upload preset"** button

---

### **Step 4: Configure the Preset**

**Required Settings:**

| Field | Value |
|-------|-------|
| **Preset name** | `rifq_pets_unsigned` |
| **Signing mode** | **Unsigned** ⚠️ IMPORTANT! |
| **Folder** | `pets` (optional, for organization) |

**Recommended Settings:**

| Field | Value |
|-------|-------|
| **Allowed formats** | `jpg, png, jpeg, webp` |
| **Max file size** | `5 MB` |
| **Max image width** | `1920` (optional) |
| **Max image height** | `1920` (optional) |

**⚠️ CRITICAL:** Make sure **Signing mode** is set to **"Unsigned"**!

---

### **Step 5: Save**

Click **Save** at the bottom of the page.

---

### **Step 6: Verify**

You should see your new preset in the list:
```
rifq_pets_unsigned (Unsigned)
```

---

## 🎉 That's It! Now Test the App

1. Run your Android app
2. Go to **Add Pet** screen
3. Tap the avatar to select a photo
4. Select an image
5. Watch the logs:

**Expected Success Logs:**
```
D/CloudinaryUploader: Starting upload for URI: content://...
D/CloudinaryUploader: Uploading to Cloudinary: https://api.cloudinary.com/...
D/CloudinaryUploader: Using preset: rifq_pets_unsigned
D/CloudinaryUploader: Response code: 200 ✅
D/CloudinaryUploader: Upload successful! URL: https://res.cloudinary.com/dpc7d0adc/...
```

6. You should see Toast: **"Photo uploaded successfully!"** ✅
7. Submit the form
8. Pet should be saved with photo URL! ✅

---

## 🔍 Troubleshooting

### **Still getting "Upload preset not found"?**

**Verify:**
1. Preset name is exactly: `rifq_pets_unsigned` (case-sensitive!)
2. Signing mode is set to: **Unsigned**
3. You clicked **Save**
4. You restarted the Android app after creating preset

### **Getting "Invalid signature"?**

**Problem:** Preset is set to "Signed" instead of "Unsigned"

**Fix:** 
1. Go back to preset settings
2. Change **Signing mode** to **Unsigned**
3. Save again

### **Getting "Unauthorized"?**

**Problem:** Cloudinary account security settings

**Fix:**
1. Go to Settings → Security
2. Make sure **"Unsigned uploads"** is **enabled**
3. Save

---

## 📸 Alternative: Use Different Preset Name

If you prefer a different name, you can:

1. Create preset with any name (e.g., `my_app_upload`)
2. Update the code:

**File:** `/util/CloudinaryUploader.kt`
```kotlin
private const val UPLOAD_PRESET = "my_app_upload" // Your chosen name
```

Just make sure it's **unsigned**!

---

## 🎯 Why Unsigned Upload?

**Unsigned uploads** allow the mobile app to upload directly to Cloudinary without:
- Server-side signature generation
- Backend involvement
- Exposing API secrets

**Safe because:**
- You set file size limits in the preset
- You set allowed formats in the preset
- You can monitor usage in Cloudinary dashboard
- You can revoke/change the preset anytime

---

## 📊 After Setup

Once you create the preset, your photo upload will work like this:

```
1. User selects photo
   ↓
2. Android uploads to Cloudinary (using unsigned preset)
   ↓
3. Cloudinary returns URL
   ↓
4. Android saves pet with photo URL to your backend
   ↓
5. Photo displays from Cloudinary CDN
   ↓
✅ Done!
```

---

## 🎉 Summary

**What you need to do:**
1. ✅ Go to Cloudinary dashboard
2. ✅ Settings → Upload → Upload presets
3. ✅ Create preset named: `rifq_pets_unsigned`
4. ✅ Set signing mode to: **Unsigned**
5. ✅ Save
6. ✅ Test the app

**Time required:** 2 minutes!

**After this:** Photo upload will work perfectly! 📸✨

---

## 📝 Quick Checklist

- [ ] Logged into Cloudinary (https://cloudinary.com/console)
- [ ] Opened Settings → Upload
- [ ] Created new upload preset
- [ ] Named it: `rifq_pets_unsigned`
- [ ] Set signing mode to: **Unsigned** ⚠️
- [ ] Set folder to: `pets` (optional)
- [ ] Set max file size (e.g., 5 MB)
- [ ] Clicked Save
- [ ] Restarted Android app
- [ ] Tested photo upload

Once all checked ✅ - photos will upload! 🎊

