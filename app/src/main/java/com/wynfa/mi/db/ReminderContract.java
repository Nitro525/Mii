package com.wynfa.mi.db;

import android.provider.BaseColumns;

public class ReminderContract {
    public static final String DB_NAME = "com.wynfa.mi.db";
    public static final int DB_VERSION = 1;

    public class ReminderEntry implements BaseColumns {
        public static final String TABLE = "reminders";

        public static final String COL_REMINDER_TITLE = "title";
        public static final String COL_REMINDER_TIME = "time";
    }
}
