package com.aceburgundy.criminalintent.database;

public class CrimeDbSchema {
    private CrimeDbSchema() { /* Left empty */ }
    public static final class CrimeTable {
        private CrimeTable() { /* Left empty */ }
        public static final String NAME = "crimes";
        public static final class Cols {
            private Cols() { /* Left empty */ }
            public static final String REQUIRES_INTERVENTION = "requires_intervention";
            public static final String SUSPECT_NUMBER = "suspect_number";
            public static final String SUSPECT_NAME = "suspect_name";
            public static final String SOLVED = "solved";
            public static final String TITLE = "title";
            public static final String DATE = "date";
            public static final String TIME = "time";
            public static final String UUID = "uuid";
        }
    }
}