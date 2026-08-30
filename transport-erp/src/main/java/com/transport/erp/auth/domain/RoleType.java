package com.transport.erp.auth.domain;

import java.util.Set;

/**
 * RoleType defines each role and its embedded permission set.
 * No DB table needed — permissions are derived in-code at auth time.
 */
public enum RoleType {

    SUPER_ADMIN {
        @Override
        public Set<String> permissions() {
            return Set.of(
                    "VEHICLE_VIEW", "VEHICLE_EDIT",
                    "DRIVER_VIEW", "DRIVER_EDIT", "DRIVER_MEDICAL_VIEW",
                    "TRIP_VIEW", "TRIP_CREATE", "TRIP_ASSIGN",
                    "EXPENSE_VIEW", "EXPENSE_APPROVE",
                    "GPS_VIEW", "GPS_HISTORY_VIEW",
                    "BRANCH_VIEW", "BRANCH_EDIT",
                    "USER_VIEW", "USER_EDIT",
                    "DOCUMENT_VIEW", "DOCUMENT_EDIT",
                    "ASSIGNMENT_VIEW", "ASSIGNMENT_EDIT",
                    "REPORT_VIEW");
        }
    },

    OWNER {
        @Override
        public Set<String> permissions() {
            return Set.of(
                    "VEHICLE_VIEW", "VEHICLE_EDIT",
                    "DRIVER_VIEW", "DRIVER_EDIT", "DRIVER_MEDICAL_VIEW",
                    "TRIP_VIEW", "TRIP_CREATE", "TRIP_ASSIGN",
                    "EXPENSE_VIEW", "EXPENSE_APPROVE",
                    "GPS_VIEW", "GPS_HISTORY_VIEW",
                    "BRANCH_VIEW", "BRANCH_EDIT",
                    "USER_VIEW", "USER_EDIT",
                    "DOCUMENT_VIEW", "DOCUMENT_EDIT",
                    "ASSIGNMENT_VIEW", "ASSIGNMENT_EDIT",
                    "REPORT_VIEW");
        }
    },

    FLEET_MANAGER {
        @Override
        public Set<String> permissions() {
            return Set.of(
                    "VEHICLE_VIEW", "VEHICLE_EDIT",
                    "DRIVER_VIEW", "DRIVER_EDIT",
                    "TRIP_VIEW", "TRIP_CREATE", "TRIP_ASSIGN",
                    "GPS_VIEW", "GPS_HISTORY_VIEW",
                    "BRANCH_VIEW",
                    "DOCUMENT_VIEW", "DOCUMENT_EDIT",
                    "ASSIGNMENT_VIEW", "ASSIGNMENT_EDIT",
                    "REPORT_VIEW");
        }
    },

    DISPATCHER {
        @Override
        public Set<String> permissions() {
            return Set.of(
                    "VEHICLE_VIEW",
                    "DRIVER_VIEW",
                    "TRIP_VIEW", "TRIP_CREATE", "TRIP_ASSIGN",
                    "GPS_VIEW",
                    "ASSIGNMENT_VIEW", "ASSIGNMENT_EDIT",
                    "DOCUMENT_VIEW");
        }
    },

    ACCOUNTANT {
        @Override
        public Set<String> permissions() {
            return Set.of(
                    "VEHICLE_VIEW",
                    "DRIVER_VIEW",
                    "TRIP_VIEW",
                    "EXPENSE_VIEW", "EXPENSE_APPROVE",
                    "DOCUMENT_VIEW",
                    "REPORT_VIEW");
        }
    },

    HR {
        @Override
        public Set<String> permissions() {
            return Set.of(
                    "DRIVER_VIEW", "DRIVER_EDIT", "DRIVER_MEDICAL_VIEW",
                    "USER_VIEW", "USER_EDIT",
                    "DOCUMENT_VIEW", "DOCUMENT_EDIT",
                    "REPORT_VIEW");
        }
    },

    MAINTENANCE_MANAGER {
        @Override
        public Set<String> permissions() {
            return Set.of(
                    "VEHICLE_VIEW", "VEHICLE_EDIT",
                    "DRIVER_VIEW",
                    "DOCUMENT_VIEW", "DOCUMENT_EDIT",
                    "REPORT_VIEW");
        }
    },

    DRIVER {
        @Override
        public Set<String> permissions() {
            return Set.of(
                    "TRIP_VIEW",
                    "GPS_VIEW",
                    "DOCUMENT_VIEW");
        }
    },

    VIEWER {
        @Override
        public Set<String> permissions() {
            return Set.of(
                    "VEHICLE_VIEW",
                    "DRIVER_VIEW",
                    "TRIP_VIEW",
                    "BRANCH_VIEW",
                    "DOCUMENT_VIEW",
                    "ASSIGNMENT_VIEW",
                    "REPORT_VIEW");
        }
    };

    /** Returns the set of permission strings granted to this role. */
    public abstract Set<String> permissions();
}
