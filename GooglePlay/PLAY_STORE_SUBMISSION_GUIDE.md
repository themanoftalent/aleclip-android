# Google Play Store Submission Guide — AleClip Android

This guide walks you through publishing **AleClip Android (v2.0.1)** to the Google Play Store using the Google Play Console.

---

## 1. Prerequisites Checklist

1. **Google Play Developer Account** ($25 one-time registration fee at [play.google.com/console](https://play.google.com/console)).
2. An **Android App Bundle (`.aab`)** built by GitHub Actions or local Gradle.
3. The 512x512 icon file located at [`GooglePlay/ic_launcher_playstore_512.png`](./ic_launcher_playstore_512.png).

---

## 2. Create the Application in Google Play Console

1. Log in to [Google Play Console](https://play.google.com/console).
2. Click **Create app** (top right):
   - **App name**: `AleClip - Clipboard Manager`
   - **Default language**: English (United States)
   - **App or game**: App
   - **Free or paid**: Free
   - Accept the Developer Program Policies and US export laws.
3. Click **Create app**.

---

## 3. Set Up App Content & Policies (Dashboard Checklist)

In the left sidebar, scroll down to **Policy and programs > App content**:

1. **Privacy Policy**:
   - URL: `https://themanoftalent.github.io/aleclip-android/`
2. **Data Safety**:
   - Does your app collect or share any user data? Select **No**.
   - Note: `android:allowBackup="false"` is explicitly enforced in `AndroidManifest.xml` so clipboard history is never uploaded to cloud backups without user action.
   - Review and submit.
3. **Ads**:
   - Does your app contain ads? Select **No, my app does not contain ads**.
4. **App Access**:
   - Select **All functionality is available without special access**.
5. **Content Ratings**:
   - Complete questionnaire (Utility category: 0 violence, 0 profanity, 0 user location sharing).
   - Rating will be **Everyone / PEGI 3**.
6. **Target Audience**:
   - Select target age groups: **18 and above** (simplifies COPPA requirements).

---

## 4. Main Store Listing

Go to **Grow > Store presence > Main store listing**:
1. **Short Description**:
   ```text
   Fast, colorful & 100% private clipboard manager with undo and trash recovery.
   ```
2. **Full Description**: Copy from [`GooglePlay/METADATA.md`](./METADATA.md).
3. **Graphics**:
   - **App Icon**: Upload `GooglePlay/ic_launcher_playstore_512.png` (512x512 PNG).
   - **Feature Graphic**: Upload a 1024x500 banner (included in repo or generated from logo).
   - **Phone Screenshots**: Take 2–4 screenshots of the app in light/dark mode and upload.

---

## 5. Build & Upload the Release Bundle (.aab)

### Automated GitHub Actions Build
AleClip includes a production CI/CD workflow at [`.github/workflows/android-build.yml`](../.github/workflows/android-build.yml):
1. Whenever code is pushed to `main`, GitHub Actions automatically compiles and packages:
   - `AleClip-v2.0.1.aab` (Android App Bundle for Play Store)
   - `AleClip-v2.0.1.apk` (Direct installable APK)
2. Go to **Actions** in your GitHub repository and download the **AleClip-Release-Bundle** artifact.

### Uploading to Play Console
1. In Play Console, go to **Release > Production** (or **Closed testing**).
2. Click **Create new release**.
3. Under **App bundles**, drag and drop your downloaded `.aab` file.
4. **Release name**: `2.0.1 (2026)`
5. **Release notes**:
   ```text
   - Initial Android release with full Jetpack Compose & Material 3 UI.
   - Accidental delete protection with instant Undo and dedicated Trash recovery.
   - Radiant multi-color spectrum palette (zero pink).
   - Quick Settings Tile and Share menu integration.
   - 100% on-device private storage with zero telemetry.
   ```
6. Click **Next** > **Save** > **Review release** > **Start rollout to Production**!

Google Play review typically takes **1 to 3 days**. Once approved, AleClip will be live worldwide on the Google Play Store!
