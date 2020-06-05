package intersistemi.it.afp.db;

import android.provider.BaseColumns;

/**
 * Created by ofabbri on 06/12/2016.
 */

public final class ConfigurationsBaseColumns
{

    private ConfigurationsBaseColumns() {}

    public static class ConfigurationsEntry implements BaseColumns {
        public static final String TABLE_NAME = "CONFIGURATIONS";
        public static final String COLUMN_NAME_UPLOAD_URL_REGISTRATION = "UPLOAD_URL_REGISTRATION";
        public static final String COLUMN_NAME_REC_INTERVAL_SPLIT = "REC_INTERVAL_SPLIT";
        public static final String COLUMN_NAME_REC_INTERVAL_REGISTRATION = "REC_INTERVAL_REGISTRATION";
    }

}
