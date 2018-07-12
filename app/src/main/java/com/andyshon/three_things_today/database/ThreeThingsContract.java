package com.andyshon.three_things_today.database;


public class ThreeThingsContract {
    private ThreeThingsContract() {}

    public static final String FILEPROVIDER = "com.andyshon.three_things_today.fileprovider";

    public static class ThreeThingsEntry {
        public static final String TABLE_NAME = "ThreeThings";
        public static final String COLUMN_NAME_YEAR = "year";
        public static final String COLUMN_NAME_MONTH = "month";
        public static final String COLUMN_NAME_DAY_OF_MONTH = "dayOfMonth";
        public static final String COLUMN_NAME_FIRST_THING = "firstThing";
        public static final String COLUMN_NAME_SECOND_THING = "secondThing";
        public static final String COLUMN_NAME_THIRD_THING = "thirdThing";

        static final String[] COLUMNS = {
                COLUMN_NAME_YEAR,
                COLUMN_NAME_MONTH,
                COLUMN_NAME_DAY_OF_MONTH,
                COLUMN_NAME_FIRST_THING,
                COLUMN_NAME_SECOND_THING,
                COLUMN_NAME_THIRD_THING
        };
    }
}
