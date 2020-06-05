package intersistemi.it.afp.db;

import android.provider.BaseColumns;

/**
 * Created by orazio on 21/11/2016.
 */

public final class UserAlarmBaseColumns
{

    private UserAlarmBaseColumns() {}

    public static class UserAlarmEntry implements BaseColumns {
        public static final String TABLE_NAME = "USER_ALARM";
        public static final String COLUMN_NAME_TIME_IN_MILLIS = "TIME_IN_MILLIS";
    }
}
