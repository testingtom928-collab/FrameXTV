# FrameXTV — Android TV project

A native Android application shell that opens **https://framextv.cc/** in a fullscreen WebView and is discoverable from Android TV / Google TV launchers.

## Included
- Android TV `LEANBACK_LAUNCHER` entry and TV banner
- Landscape fullscreen / immersive mode
- WebView JavaScript and DOM storage for modern websites
- TV remote OK / D-pad interaction through normal WebView focus handling
- Back key navigates browser history; exits fullscreen video first
- HTML5 fullscreen video custom-view handling
- Loading and main-frame error/retry screens
- HTTPS-only navigation policy for web content; non-web URI schemes are handed to installed apps

## Build in Android Studio
1. Install Android Studio (current stable) with Android SDK Platform 35 and a compatible JDK (17 recommended).
2. Open this folder (`FrameXTV-AndroidTV`) as an existing project.
3. Allow Gradle sync to finish. Android Studio will download the Android Gradle Plugin and dependencies.
4. Choose **Build → Build Bundle(s) / APK(s) → Build APK(s)**.
5. The debug APK is normally at `app/build/outputs/apk/debug/app-debug.apk`.

## Install on a TCL Android TV
- Enable Developer options and USB debugging, or use an APK installer / ADB over the local network.
- Install `app-debug.apk`. The app should appear in the TV app launcher as **FrameXTV**.

## Important limitations
- This is a WebView-based wrapper, not a native streaming player. Playback, remote navigation, sign-in, and layout depend on how `framextv.cc` is implemented.
- Some websites block embedded WebViews, require browser-specific APIs, or use DRM / protected playback that Android System WebView does not support for a given TV. Such cases require a site-supported Android TV app or a dedicated native player integration.
- The website's actual content and playback could not be inspected during project generation. Test on the target TCL TV and adjust if the site requires special handling.
- No APK is included in this source package because this build environment does not have Android SDK / Gradle installed. Build the APK using Android Studio as above.

## Build the APK using only your Android phone (no PC)

This project includes a GitHub Actions cloud build workflow. You need a free GitHub account.

1. In your phone browser, open https://github.com/new and create a **public** repository named `FrameXTV` (or choose private if you prefer).
2. Upload the contents of this project folder to the repository. Make sure `.github/workflows/build-apk.yml` is included. On GitHub's mobile website, use **Add file → Upload files**; if hidden, enable **Desktop site** in your browser.
3. Open the repository's **Actions** tab and select **Build FrameXTV APK**.
4. Tap **Run workflow**. Wait for the workflow to finish successfully.
5. Open the completed workflow run, scroll to **Artifacts**, and download **FrameXTV-APK**.
6. Extract the downloaded ZIP on your phone. The file `app-debug.apk` is your installable app.
7. Transfer the APK to your TCL TV (USB drive or a file-transfer app), allow installation from unknown sources when prompted, and install it.

The workflow compiles the project in GitHub's cloud; no Android Studio or computer is required. The first build may take several minutes.
