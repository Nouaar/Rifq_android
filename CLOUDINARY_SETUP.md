# Cloudinary Setup Instructions

## Overview
The Rifq Android app uses Cloudinary for pet image upload and storage. Follow these steps to configure Cloudinary for your project.

## Steps to Configure Cloudinary

### 1. Create a Cloudinary Account
1. Go to [https://cloudinary.com/](https://cloudinary.com/)
2. Sign up for a free account
3. Verify your email address

### 2. Get Your Cloudinary Credentials
1. Log in to your Cloudinary dashboard
2. Note down your **Cloud Name** (found at the top of the dashboard)

### 3. Create an Upload Preset
1. In the Cloudinary dashboard, go to **Settings** → **Upload**
2. Scroll down to **Upload presets**
3. Click **Add upload preset**
4. Configure the preset:
   - **Preset name**: Choose a name (e.g., `rifq_pets`)
   - **Signing Mode**: Select **Unsigned** (this allows client-side uploads)
   - **Folder**: (Optional) Set to `rifq/pets` to organize uploads
   - **Allowed formats**: jpg, png, jpeg, webp
   - **Max file size**: Set appropriate limit (e.g., 5 MB)
5. Click **Save**

### 4. Update Android App Configuration

Open the file: `app/src/main/java/tn/rifq_android/util/CloudinaryUploader.kt`

Replace the following constants with your values:

```kotlin
private const val CLOUD_NAME = "your_cloud_name" // Replace with your Cloud Name
private const val UPLOAD_PRESET = "your_upload_preset" // Replace with your preset name
```

Example:
```kotlin
private const val CLOUD_NAME = "dexample123"
private const val UPLOAD_PRESET = "rifq_pets"
```

### 5. Test Image Upload
1. Run the app
2. Navigate to **Add Pet** screen
3. Tap on the circular avatar to select an image
4. Choose an image from your device
5. Wait for upload to complete (you'll see "Photo uploaded ✓")
6. Submit the form

### 6. Verify Upload in Cloudinary
1. Go to your Cloudinary dashboard
2. Click on **Media Library**
3. You should see the uploaded pet image
4. The image URL will be saved to your backend database

## Troubleshooting

### Upload Fails
- **Check internet connection**: Ensure device has internet access
- **Verify credentials**: Double-check CLOUD_NAME and UPLOAD_PRESET
- **Check preset settings**: Ensure the upload preset is set to "Unsigned"
- **File size**: Make sure image isn't too large (check preset limits)

### Image Not Displaying
- **Check URL**: Verify the Cloudinary URL is correctly saved in the backend
- **Internet access**: Ensure device can access Cloudinary CDN
- **URL format**: Should look like: `https://res.cloudinary.com/{cloud_name}/image/upload/...`

## Features Implemented

### Add Pet Screen
- ✅ Image picker integration
- ✅ Automatic upload to Cloudinary
- ✅ Upload progress indicator
- ✅ Image preview before upload
- ✅ Photo URL sent to backend

### Edit Pet Screen
- ✅ Display existing pet photo
- ✅ Option to change photo
- ✅ Upload new photo to Cloudinary
- ✅ Update photo URL in backend

### Pet Detail Screen
- ✅ Display pet photo from Cloudinary
- ✅ Fallback to emoji if no photo
- ✅ Circular avatar display

### Home Screen
- ✅ Display pet photos in pet cards
- ✅ Fallback to emoji if no photo

## Image Handling

### Upload Flow
1. User taps avatar → Image picker opens
2. User selects image → Image preview shown
3. Image automatically uploads to Cloudinary
4. Upload progress displayed
5. Cloudinary returns secure URL
6. URL saved with pet data

### Display Flow
1. Fetch pet data from backend (includes photo URL)
2. If photo URL exists → Load from Cloudinary using Coil
3. If no photo URL → Show emoji based on species

## Security Considerations

### Unsigned Upload Preset
- ✅ Convenient for client-side uploads
- ⚠️ Anyone with preset name can upload
- 💡 Recommendation: Set upload limits and transformations

### Production Best Practices
1. **Set upload limits** in preset configuration
2. **Enable moderation** if needed
3. **Use folders** to organize uploads
4. **Set auto-tagging** for better organization
5. **Monitor usage** in Cloudinary dashboard

## Additional Resources

- [Cloudinary Documentation](https://cloudinary.com/documentation)
- [Android Upload Guide](https://cloudinary.com/documentation/android_integration)
- [Upload Presets](https://cloudinary.com/documentation/upload_presets)

## Support

If you encounter issues:
1. Check Cloudinary dashboard for error logs
2. Verify app logs in Android Studio
3. Test upload directly in Cloudinary console
4. Contact Cloudinary support if needed

