# 🎯 QUICK FIX: Create Cloudinary Upload Preset in 5 Steps

## Error You're Seeing:
```
Upload failed with code 400: {"error":{"message":"Upload preset not found"}}
```

## 5-Step Solution (Takes 2 Minutes!)

---

### **STEP 1: Open Cloudinary**
Go to: **https://cloudinary.com/console**

---

### **STEP 2: Go to Upload Settings**
Click: **Settings (⚙️)** → **Upload** tab

---

### **STEP 3: Find Upload Presets**
Scroll down to **"Upload presets"** section

---

### **STEP 4: Create New Preset**
Click **"Add upload preset"** button

Fill in:
```
Preset name:     rifq_pets_unsigned
Signing Mode:    Unsigned ⚠️ (VERY IMPORTANT!)
Folder:          pets (optional)
Max file size:   5 MB
```

---

### **STEP 5: Save**
Click **Save** button at the bottom

---

## ✅ Done! Now Test

1. Restart your Android app
2. Try adding a pet with photo
3. Should work now! 🎉

---

## 📸 Visual Guide

```
Cloudinary Dashboard
    ↓
[Settings Icon ⚙️]
    ↓
[Upload Tab]
    ↓
[Scroll to "Upload presets"]
    ↓
[Click "Add upload preset"]
    ↓
Configure:
┌─────────────────────────────────┐
│ Preset name: rifq_pets_unsigned │
│ Signing Mode: ◉ Unsigned        │ ← Important!
│ Folder: pets                    │
│ Max file size: 5 MB             │
└─────────────────────────────────┘
    ↓
[Click Save]
    ↓
✅ Done!
```

---

## ⚠️ Critical Setting

**MUST be "Unsigned":**
```
Signing Mode: 
  ○ Signed
  ◉ Unsigned  ← Select this!
```

If you select "Signed" by mistake, the upload will fail with "Invalid signature" error.

---

## 🔍 Verify It's Working

After creating the preset, you should see:
```
rifq_pets_unsigned (Unsigned)
```
in your presets list.

---

## 🎉 That's All!

Just create that one preset and photo uploads will work perfectly!

**Your Cloudinary account already has the cloud name configured** (`dpc7d0adc`), you just need to add the upload preset.

After this 2-minute setup, you'll be able to:
- ✅ Upload pet photos from Android app
- ✅ Store photos on Cloudinary CDN
- ✅ Display photos in your app
- ✅ Update pet photos

**Easy! 🚀**

