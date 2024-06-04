# Voltaki

**_Voltaki_** is an application for Android™ on which with only one button you can mark places and go back to them later using *Google Maps™* app. The app saves a history of the places you have marked and you can add a bookmark in the places you want to save for future reference. You can also share your current or saved locations.

*Android and Google Maps are trademarks of Google, Inc.*

### Project Info

Status: **in Production**
- Start Date: May 24, 2016
- Alpha Release Date: Jun 7, 2016
- Beta Release Date: Jul 15, 2016
- Production Release Date: Aug 20, 2016

### API Key

This application uses a *Google Maps™* API Key, that for obvious reasons it was removed from this repository. An API Key can be obtained following the instructions here:
https://developers.google.com/maps/documentation/android/signup

After get the key, the file **google_maps_api.xml** must be created at **_./app/src/debug/res/values/_** with the following content:

```xml
<resources>

    <!--Google Maps API Debug Key-->
    <string name="google_maps_key" templateMergeStrategy="preserve" translatable="false">
        ### API Key (it starts with "AIza") ###
    </string>

</resources>
```


####
