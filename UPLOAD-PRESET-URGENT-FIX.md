# 🚨 URGENT FIX: Upload Preset Not Found

## ❌ Current Error
```
Upload failed with code 400: {"error":{"message":"Upload preset not found"}}
```

**This means:** The preset `rifq_pets_unsigned` doesn't exist in your Cloudinary account.

---

## ✅ IMMEDIATE SOLUTIONS (Choose One)

### **SOLUTION 1: Create the Preset (2 Minutes)** ⭐ RECOMMENDED

This is the permanent fix:

1. **Open:** https://cloudinary.com/console/settings/upload
2. **Scroll to:** "Upload presets" section
3. **Click:** "Add upload preset" button
4. **Fill in:**
   ```
   Preset name:     rifq_pets_unsigned
   Signing Mode:    Unsigned ⚠️ MUST BE UNSIGNED!
   Folder:          pets (optional)
   ```
5. **Click:** Save
6. **Restart app** and try again

**That's it!** ✅

---

### **SOLUTION 2: Use an Existing Preset** ⚡ QUICK FIX

If you already have an unsigned preset in your Cloudinary account:

1. **Check your presets:**
   - Go to: https://cloudinary.com/console/settings/upload
   - Scroll to "Upload presets"
   - Look for any preset marked as **(Unsigned)**

2. **Copy the preset name** (e.g., `my_preset`, `unsigned_upload`, etc.)

3. **Update the code:**
   - Open: `/util/CloudinaryUploader.kt`
   - Find line ~20:
     ```kotlin
     private const val UPLOAD_PRESET = "rifq_pets_unsigned"
     ```
   - Change to your preset name:
     ```kotlin
     private const val UPLOAD_PRESET = "your_existing_preset_name"
     ```

4. **Rebuild and test!**

---

### **SOLUTION 3: Check for Default Presets**

Some Cloudinary accounts have default presets:

**Try these preset names:**

1. In `/util/CloudinaryUploader.kt`, change line ~20 to one of these:
   ```kotlin
   private const val UPLOAD_PRESET = "ml_default"
   ```
   OR
   ```kotlin
   private const val UPLOAD_PRESET = "unsigned_preset"
   ```
   OR
   ```kotlin
   private const val UPLOAD_PRESET = "default"
   ```

2. **Rebuild app and test each one**
3. Check logs for which one works

---

## 🔍 How to Find Your Existing Presets

1. **Go to:** https://cloudinary.com/console/settings/upload
2. **Scroll to:** "Upload presets" section
3. **Look for:** Presets marked as **(Unsigned)**
4. **Note the name** and use it in Solution 2 above

**Example:**
```
Your Presets:
- rifq_uploads (Unsigned) ← Use this!
- signed_preset (Signed) ← Don't use
```

---

## ⚠️ CRITICAL: Must Be "Unsigned"

**Only "Unsigned" presets work from mobile apps!**

```
✅ Unsigned preset - Works from Android
❌ Signed preset - Requires server signature
```

When creating a preset, the Signing Mode setting is crucial:
```
Signing Mode:
  ○ Signed    ← NO!
  ◉ Unsigned  ← YES!
```

---

## 📋 Step-by-Step: Create Preset (Detailed)

### **Step 1: Login to Cloudinary**
- URL: https://cloudinary.com/console
- Use your credentials for cloud: `dpc7d0adc`

### **Step 2: Go to Settings**
- Click the **⚙️ Settings** icon (top right)
- OR go directly to: https://cloudinary.com/console/settings/upload

### **Step 3: Find Upload Tab**
- Click **"Upload"** tab in the left sidebar

### **Step 4: Scroll to Upload Presets**
- Scroll down until you see **"Upload presets"** section
- You'll see a list of existing presets (if any)

### **Step 5: Create New Preset**
- Click **"Add upload preset"** button (blue button)

### **Step 6: Configure Preset**

Fill in these fields:

| Field | Value | Notes |
|-------|-------|-------|
| **Preset name** | `rifq_pets_unsigned` | Must match code |
| **Signing mode** | **Unsigned** | ⚠️ CRITICAL! |
| **Folder** | `pets` | Optional, for organization |
| **Allowed formats** | `jpg,png,jpeg,webp` | Recommended |
| **Max file size** | `5` MB | Recommended |
| **Max width** | `1920` | Optional |
| **Max height** | `1920` | Optional |

### **Step 7: Save**
- Scroll to bottom
- Click **"Save"** button
- You should see success message

### **Step 8: Verify**
- Look in the presets list
- You should see: `rifq_pets_unsigned (Unsigned)`

### **Step 9: Test App**
- Restart your Android app
- Try uploading a photo
- Should work now! ✅

---

## 🧪 Test if It Works

After creating/updating the preset:

1. **Restart app**
2. **Go to Add Pet screen**
3. **Tap avatar to select photo**
4. **Watch logs:**

**Success:**
```
D/CloudinaryUploader: Response code: 200 ✅
D/CloudinaryUploader: Upload successful! URL: https://res.cloudinary.com/...
Toast: "Photo uploaded successfully!" ✅
```

**Still failing:**
```
E/CloudinaryUploader: Upload failed with code 400
E/CloudinaryUploader: "Upload preset not found"
```
→ Double-check preset name matches exactly!

---

## 🔧 Troubleshooting

### **"Upload preset not found" - Still Getting This?**

**Check:**
1. ✅ Preset name in Cloudinary dashboard
2. ✅ Preset name in code matches **exactly** (case-sensitive!)
3. ✅ Preset is set to "Unsigned" mode
4. ✅ You saved the preset in Cloudinary
5. ✅ You rebuilt the Android app after code change

**Common Mistakes:**
```
❌ Code: "rifq_pets_unsigned"
   Dashboard: "Rifq_Pets_Unsigned"  ← Case mismatch!

❌ Code: "rifq_pets_unsigned"
   Dashboard: "rifq_pets_unsigned (Signed)" ← Must be Unsigned!

❌ Created preset but didn't rebuild app ← Rebuild required!
```

---

## 🎯 Quick Reference

**Your Cloudinary Account:**
- Cloud Name: `dpc7d0adc` ✅
- Dashboard: https://cloudinary.com/console
- Upload Settings: https://cloudinary.com/console/settings/upload

**Preset Name (must match exactly):**
- Code: `/util/CloudinaryUploader.kt` line ~20
- Dashboard: Settings → Upload → Upload presets

**Required Settings:**
- Signing Mode: **Unsigned** (not Signed!)
- Preset name must match code exactly

---

## 💡 Pro Tips

### **Tip 1: Check Existing Presets First**
Before creating a new preset, check if you already have an unsigned one!

### **Tip 2: Use Descriptive Names**
Good preset names:
- `rifq_pets_unsigned` ✅
- `app_upload_unsigned` ✅
- `mobile_uploads` ✅

Bad preset names:
- `preset1` ❌
- `test` ❌
- `temp` ❌

### **Tip 3: Set Limits**
Always set max file size (e.g., 5 MB) to prevent huge uploads!

### **Tip 4: Use Folders**
Set folder to `pets` to organize uploads in your Cloudinary account.

---

## 🎉 After Setup Works

Once photo upload works, you'll see:

1. ✅ Photo preview in app
2. ✅ "Uploading..." indicator
3. ✅ "Photo uploaded successfully!" toast
4. ✅ Pet saved with photo URL
5. ✅ Photo displays in pet detail screen
6. ✅ Photos stored on Cloudinary CDN

**Example uploaded photo URL:**
```
https://res.cloudinary.com/dpc7d0adc/image/upload/v1763066417/pets/abc123.webp
```

---

## 📝 Summary

**To fix "Upload preset not found" error:**

1. **Easiest:** Create preset named `rifq_pets_unsigned` (Unsigned mode)
2. **Alternative:** Use existing unsigned preset, update code
3. **Test:** Try common preset names like `ml_default`

**Time required:** 2-5 minutes

**Once fixed:** Photo uploads will work perfectly! 📸✨

---

## 🆘 Still Not Working?

If you've tried everything above:

1. **Share screenshot** of your Cloudinary upload presets list
2. **Check exact error** in Android logs
3. **Verify** cloud name is `dpc7d0adc`
4. **Try** creating preset with a different name and updating code

**The issue is definitely the preset not existing or not being unsigned!**

