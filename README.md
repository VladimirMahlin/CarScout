Map installation instructions:
1. Google Play Services
Open Android Studio and go to SDK Manager:

  File > Settings > Appearance & Behavior > System Settings > Android SDK (on Windows/Linux).
  
  Android Studio > Preferences > Appearance & Behavior > System Settings > Android SDK (on macOS).

3. Google Repository
On the same SDK Tools tab, make sure Google Repository is installed, as it is required to include Google libraries, including Maps.

4. Google Maps SDK for Android
For Google Maps support, make sure Google APIs is installed, which includes the Google Maps SDK.
On the SDK Platforms tab, select the Android version you are using and make sure Google APIs for the selected version is installed.

5. API Key
Don't forget to create an API key in Google Cloud Console to work with Google Maps and add it to your AndroidManifest.xml file:
<meta-data
android:name="com.google.android.geo.API_KEY"
android:value="YOUR_API_KEY_HERE"/>

6. Enter the search query google.developer.console in google

7. Manage resources, create a new project

8. Select a project

9. APIs & Services, select library

10. Maps SDK for Android, click Enable

11. Return to APIs & Services and select Credentials, and create an API_KEY and paste it into AndroidManifest.xml: in the line android:value="YOUR_API_KEY_HERE"/>
