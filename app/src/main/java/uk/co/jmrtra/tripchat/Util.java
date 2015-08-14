package uk.co.jmrtra.tripchat;

import android.content.pm.ApplicationInfo;
import android.text.TextUtils;
import android.util.Log;

public class Util {

    private static final String LOG_TAG = "TripDebug";
    public static final String URL_LOG_IN = "http://jmrtra.co.uk/tripchat/log_in.php";
    public static final String URL_SIGN_IN = "http://jmrtra.co.uk/tripchat/sign_in.php";
    public static final String URL_SIGN_IN_SOCIAL
            = "http://jmrtra.co.uk/tripchat/sign_in_social.php";
    public static final String URL_GET_SUGGESTIONS
            = "http://jmrtra.co.uk/tripchat/get_suggestions.php";
    public static final String URL_ADD_TRIP = "http://jmrtra.co.uk/tripchat/add_trip.php";
    public static final String URL_GET_TRIPS = "http://jmrtra.co.uk/tripchat/get_trips.php";
    public static final String URL_GET_THREADS = "http://jmrtra.co.uk/tripchat/get_threads.php";
    public static final String URL_GET_MESSAGES = "http://jmrtra.co.uk/tripchat/get_messages.php";
    public static final String URL_SEND_MESSAGE = "http://jmrtra.co.uk/tripchat/send_message.php";
    public static final String URL_DELETE_TRIP = "http://jmrtra.co.uk/tripchat/delete_trip.php";

    // Methods
    public static void Log(Object text) {
        Log.d(LOG_TAG, text + "");
    }

    public static Boolean isDebugging() {
        return (0 != (BaseApplication.getAppContext().getApplicationInfo().flags
                &= ApplicationInfo.FLAG_DEBUGGABLE));
    }

    public static class Station {

        private String name;
        private String code;
        private String lat;
        private String lng;
        private String img;
        private String distance;

        public Station(String name, String code, String lat, String lng, String img,
                       String distance) {
            this.name = name;
            this.code = code;
            this.lat = lat;
            this.img = img;
            this.distance = distance;
        }

        public String getName() {
            return name;
        }

        public String getCode() {
            return code;
        }

        public String getLat() {
            return name;
        }

        public String getLng() {
            return name;
        }

        public String getImage() {
            return img;
        }

        public String getDistance() {
            return distance;
        }
    }

    public static String formatTime(String timestamp) {
        if (TextUtils.isEmpty(timestamp)) {
            return "";
        } else {
            int differenceMin = (int) (System.currentTimeMillis() - Long.parseLong(timestamp))
                    / 1000 / 60;
            if (differenceMin == 0) {
                return "Just now";
            } else if (differenceMin < 60) {
                return differenceMin + " min ago";
            } else if (differenceMin < 60 * 24) {
                return differenceMin / 60 + " hour(s) ago";
            } else if (differenceMin < 60 * 24 * 30) {
                return differenceMin / 60 / 24 + " day(s) ago";
            } else if (differenceMin < 60 * 60 * 30 * 12) {
                return differenceMin / 60 / 24 * 30 + " month(s) ago";
            } else {
                return differenceMin / 60 / 24 / 30 / 12 + " year(s) ago";
            }
        }
    }
}
