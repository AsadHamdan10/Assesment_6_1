package com.example;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.PriorityQueue;
import java.util.Set;

public class AmbulanceDispatchSystem {
    public enum EmergencyPriority {
        CRITICAL(4),
        HIGH(3),
        MODERATE(2),
        NORMAL(1);
        private final int level;
        EmergencyPriority(int level) {
            this.level = level;
        }
        public int getLevel() {
            return level;
        }
    }

    /**
     * Types of ambulances.
     */
    public enum AmbulanceType {
        BASIC(1),
        ADVANCED_LIFE_SUPPORT(2),
        ICU(3);
        private final int capabilityLevel;
        AmbulanceType(int capabilityLevel) {
            this.capabilityLevel = capabilityLevel;
        }
        public int getCapabilityLevel() {
            return capabilityLevel;
        }
    }
    /**
     * Ambulance state lifecycle.
     *
     * AVAILABLE
     *      ↓
     * DISPATCHED
     *      ↓
     * EN_ROUTE
     *      ↓
     * PATIENT_PICKED_UP
     *      ↓
     * HOSPITAL_ARRIVED
     *      ↓
     * AVAILABLE
     */
    public enum AmbulanceState {
        AVAILABLE,
        DISPATCHED,
        EN_ROUTE,
        PATIENT_PICKED_UP,
        HOSPITAL_ARRIVED
    }

    /**
     * Emergency status.
     */
    public enum EmergencyStatus {
        WAITING,
        DISPATCHED,
        EN_ROUTE,
        PATIENT_PICKED_UP,
        HOSPITAL_ARRIVED,
        COMPLETED
    }

    // ============================================================
    // CUSTOM EXCEPTIONS
    // ============================================================

    /**
     * Thrown when emergency information is invalid.
     */
    public static class InvalidEmergencyRequestException
            extends RuntimeException {

        public InvalidEmergencyRequestException(String message) {
            super(message);
        }
    }

    /**
     * Thrown when ambulance information is invalid.
     */
    public static class InvalidAmbulanceException
            extends RuntimeException {

        public InvalidAmbulanceException(String message) {
            super(message);
        }
    }

    /**
     * Thrown when an ambulance cannot be found.
     */
    public static class AmbulanceNotFoundException
            extends RuntimeException {

        public AmbulanceNotFoundException(String message) {
            super(message);
        }
    }

    /**
     * Thrown when no suitable ambulance is available.
     */
    public static class NoAmbulanceAvailableException
            extends RuntimeException {

        public NoAmbulanceAvailableException(String message) {
            super(message);
        }
    }

    /**
     * Thrown when an invalid ambulance state transition occurs.
     */
    public static class InvalidStateTransitionException
            extends RuntimeException {

        public InvalidStateTransitionException(String message) {
            super(message);
        }
    }

    /**
     * Thrown when duplicate ambulance information is supplied.
     */
    public static class DuplicateAmbulanceException
            extends RuntimeException {

        public DuplicateAmbulanceException(String message) {
            super(message);
        }
    }

    // ============================================================
    // EMERGENCY REQUEST
    // ============================================================

    public static class EmergencyRequest {

        private final String patientId;
        private final String emergencyType;
        private final String pickupLocation;
        private final String destinationHospital;

        /**
         * Estimated distance from pickup location
         * to destination hospital.
         */
        private final double estimatedDistanceKm;

        private final EmergencyPriority priority;

        private EmergencyStatus status;

        public EmergencyRequest(
                String patientId,
                String emergencyType,
                String pickupLocation,
                String destinationHospital,
                double estimatedDistanceKm) {

            this.patientId = patientId;
            this.emergencyType = emergencyType;
            this.pickupLocation = pickupLocation;
            this.destinationHospital = destinationHospital;
            this.estimatedDistanceKm = estimatedDistanceKm;

            this.priority = classifyEmergency(emergencyType);
            this.status = EmergencyStatus.WAITING;
        }

        /**
         * Classifies emergency according to type.
         */
        public static EmergencyPriority classifyEmergency(
                String emergencyType) {

            if (emergencyType == null) {
                throw new InvalidEmergencyRequestException(
                        "Emergency type cannot be null.");
            }

            String type = emergencyType.trim().toLowerCase();

            if (type.isEmpty()) {
                throw new InvalidEmergencyRequestException(
                        "Emergency type is required.");
            }

            if (type.contains("critical")
                    || type.contains("cardiac")
                    || type.contains("heart attack")
                    || type.contains("stroke")
                    || type.contains("unconscious")) {

                return EmergencyPriority.CRITICAL;
            }

            if (type.contains("high")
                    || type.contains("severe")
                    || type.contains("accident")
                    || type.contains("major injury")
                    || type.contains("heavy bleeding")) {

                return EmergencyPriority.HIGH;
            }

            if (type.contains("moderate")
                    || type.contains("fracture")
                    || type.contains("burn")) {

                return EmergencyPriority.MODERATE;
            }

            return EmergencyPriority.NORMAL;
        }

        public String getPatientId() {
            return patientId;
        }

        public String getEmergencyType() {
            return emergencyType;
        }

        public String getPickupLocation() {
            return pickupLocation;
        }

        public String getDestinationHospital() {
            return destinationHospital;
        }

        public double getEstimatedDistanceKm() {
            return estimatedDistanceKm;
        }

        public EmergencyPriority getPriority() {
            return priority;
        }

        public EmergencyStatus getStatus() {
            return status;
        }

        private void setStatus(EmergencyStatus status) {
            this.status = status;
        }
    }

    // ============================================================
    // AMBULANCE
    // ============================================================

    public static class Ambulance {

        private final String ambulanceId;
        private final AmbulanceType type;
        private final String driverName;
        private final String driverPhone;

        /**
         * Distance between ambulance and pickup location.
         */
        private double distanceToPickupKm;

        private AmbulanceState state;

        public Ambulance(
                String ambulanceId,
                AmbulanceType type,
                String driverName,
                String driverPhone) {

            this(
                    ambulanceId,
                    type,
                    driverName,
                    driverPhone,
                    0.0
            );
        }

        public Ambulance(
                String ambulanceId,
                AmbulanceType type,
                String driverName,
                String driverPhone,
                double distanceToPickupKm) {

            this.ambulanceId = ambulanceId;
            this.type = type;
            this.driverName = driverName;
            this.driverPhone = driverPhone;
            this.distanceToPickupKm = distanceToPickupKm;
            this.state = AmbulanceState.AVAILABLE;
        }

        public String getAmbulanceId() {
            return ambulanceId;
        }

        public AmbulanceType getType() {
            return type;
        }

        public String getDriverName() {
            return driverName;
        }

        public String getDriverPhone() {
            return driverPhone;
        }

        public double getDistanceToPickupKm() {
            return distanceToPickupKm;
        }

        public AmbulanceState getState() {
            return state;
        }

        public void setDistanceToPickupKm(double distance) {

            if (Double.isNaN(distance)
                    || Double.isInfinite(distance)
                    || distance < 0) {

                throw new InvalidAmbulanceException(
                        "Distance to pickup cannot be negative.");
            }

            this.distanceToPickupKm = distance;
        }

        private void setState(AmbulanceState state) {
            this.state = state;
        }
    }

    // ============================================================
    // DISPATCH RECORD
    // ============================================================

    public static class DispatchRecord {

        private final EmergencyRequest request;
        private final Ambulance ambulance;
        private final double estimatedArrivalTimeMinutes;

        public DispatchRecord(
                EmergencyRequest request,
                Ambulance ambulance,
                double estimatedArrivalTimeMinutes) {

            this.request = request;
            this.ambulance = ambulance;
            this.estimatedArrivalTimeMinutes =
                    estimatedArrivalTimeMinutes;
        }

        public EmergencyRequest getRequest() {
            return request;
        }

        public Ambulance getAmbulance() {
            return ambulance;
        }

        public double getEstimatedArrivalTimeMinutes() {
            return estimatedArrivalTimeMinutes;
        }
    }

    // ============================================================
    // SYSTEM DATA
    // ============================================================

    private final List<Ambulance> ambulances =
            new ArrayList<>();

    private final List<DispatchRecord> history =
            new ArrayList<>();

    /**
     * Highest-priority emergency is processed first.
     *
     * If priority is the same, earlier insertion order is
     * maintained using sequence number.
     */
    private static class QueueEntry {

        private final EmergencyRequest request;
        private final long sequence;

        private QueueEntry(
                EmergencyRequest request,
                long sequence) {

            this.request = request;
            this.sequence = sequence;
        }
    }

    private final PriorityQueue<QueueEntry> waitingQueue =
            new PriorityQueue<>(
                    Comparator
                            .comparingInt(
                                    (QueueEntry e) ->
                                            e.request
                                                    .getPriority()
                                                    .getLevel())
                            .reversed()
                            .thenComparingLong(
                                    e -> e.sequence)
            );

    private long sequenceCounter = 0;

    /**
     * Average ambulance speed used for ETA.
     */
    private static final double AVERAGE_SPEED_KMH = 40.0;

    // ============================================================
    // AMBULANCE MANAGEMENT
    // ============================================================

    /**
     * Adds an ambulance to the system.
     */
    public void addAmbulance(Ambulance ambulance) {

        validateAmbulance(ambulance);

        if (containsAmbulanceId(
                ambulance.getAmbulanceId())) {

            throw new DuplicateAmbulanceException(
                    "Ambulance ID already exists: "
                            + ambulance.getAmbulanceId());
        }

        ambulances.add(ambulance);

        // If an ambulance is added while requests are waiting,
        // immediately attempt allocation.
        allocateWaitingRequests();
    }

    private void validateAmbulance(
            Ambulance ambulance) {

        if (ambulance == null) {
            throw new InvalidAmbulanceException(
                    "Ambulance cannot be null.");
        }

        if (ambulance.getAmbulanceId() == null
                || ambulance.getAmbulanceId()
                .trim()
                .isEmpty()) {

            throw new InvalidAmbulanceException(
                    "Ambulance ID is required.");
        }

        if (ambulance.getType() == null) {
            throw new InvalidAmbulanceException(
                    "Ambulance type is required.");
        }

        if (ambulance.getDriverName() == null
                || ambulance.getDriverName()
                .trim()
                .isEmpty()) {

            throw new InvalidAmbulanceException(
                    "Driver name is required.");
        }

        if (ambulance.getDriverPhone() == null
                || ambulance.getDriverPhone()
                .trim()
                .isEmpty()) {

            throw new InvalidAmbulanceException(
                    "Driver phone is required.");
        }

        if (ambulance.getDistanceToPickupKm() < 0
                || Double.isNaN(
                ambulance.getDistanceToPickupKm())
                || Double.isInfinite(
                ambulance.getDistanceToPickupKm())) {

            throw new InvalidAmbulanceException(
                    "Distance to pickup cannot be negative.");
        }
    }

    private boolean containsAmbulanceId(String id) {

        for (Ambulance ambulance : ambulances) {

            if (ambulance.getAmbulanceId()
                    .equalsIgnoreCase(id)) {

                return true;
            }
        }

        return false;
    }

    /**
     * Returns an ambulance by ID.
     */
    public Ambulance getAmbulance(String ambulanceId) {

        if (ambulanceId == null
                || ambulanceId.trim().isEmpty()) {

            throw new AmbulanceNotFoundException(
                    "Ambulance ID is required.");
        }

        for (Ambulance ambulance : ambulances) {

            if (ambulance.getAmbulanceId()
                    .equalsIgnoreCase(ambulanceId)) {

                return ambulance;
            }
        }

        throw new AmbulanceNotFoundException(
                "Ambulance not found: " + ambulanceId);
    }

    // ============================================================
    // EMERGENCY PROCESSING
    // ============================================================

    /**
     * Processes an emergency.
     *
     * If a suitable ambulance is available, it is dispatched.
     * Otherwise, the request enters the waiting queue.
     */
    public Ambulance processEmergency(
            EmergencyRequest request) {

        validateEmergencyRequest(request);

        Ambulance ambulance =
                findBestAmbulance(request);

        if (ambulance == null) {

            request.setStatus(
                    EmergencyStatus.WAITING);

            waitingQueue.offer(
                    new QueueEntry(
                            request,
                            sequenceCounter++));

            return null;
        }

        dispatch(request, ambulance);

        return ambulance;
    }

    /**
     * Validates emergency data.
     */
    private void validateEmergencyRequest(
            EmergencyRequest request) {

        if (request == null) {

            throw new InvalidEmergencyRequestException(
                    "Emergency request cannot be null.");
        }

        if (request.getPatientId() == null
                || request.getPatientId()
                .trim()
                .isEmpty()) {

            throw new InvalidEmergencyRequestException(
                    "Patient ID is required.");
        }

        if (request.getEmergencyType() == null
                || request.getEmergencyType()
                .trim()
                .isEmpty()) {

            throw new InvalidEmergencyRequestException(
                    "Emergency type is required.");
        }

        if (request.getPickupLocation() == null
                || request.getPickupLocation()
                .trim()
                .isEmpty()) {

            throw new InvalidEmergencyRequestException(
                    "Pickup location is required.");
        }

        if (request.getDestinationHospital() == null
                || request.getDestinationHospital()
                .trim()
                .isEmpty()) {

            throw new InvalidEmergencyRequestException(
                    "Destination hospital is required.");
        }

        double distance =
                request.getEstimatedDistanceKm();

        if (Double.isNaN(distance)
                || Double.isInfinite(distance)
                || distance <= 0) {

            throw new InvalidEmergencyRequestException(
                    "Estimated distance must be greater than zero.");
        }
    }

    // ============================================================
    // AMBULANCE SELECTION
    // ============================================================

    /**
     * Finds the most appropriate available ambulance.
     *
     * Selection considers:
     * 1. Emergency priority
     * 2. Ambulance capability
     * 3. Distance to pickup location
     */
    public Ambulance findBestAmbulance(
            EmergencyRequest request) {

        validateEmergencyRequest(request);

        List<Ambulance> suitable =
                new ArrayList<>();

        for (Ambulance ambulance : ambulances) {

            // Prevent assignment of an active ambulance.
            if (ambulance.getState()
                    != AmbulanceState.AVAILABLE) {

                continue;
            }

            if (isSuitable(
                    ambulance.getType(),
                    request.getPriority())) {

                suitable.add(ambulance);
            }
        }

        if (suitable.isEmpty()) {
            return null;
        }

        suitable.sort(
                Comparator
                        // First: best capability
                        .comparingInt(
                                (Ambulance a) ->
                                        capabilityScore(
                                                a.getType(),
                                                request.getPriority()))
                        .reversed()

                        // Second: nearest ambulance
                        .thenComparingDouble(
                                Ambulance::getDistanceToPickupKm)

                        // Third: ambulance ID for deterministic result
                        .thenComparing(
                                Ambulance::getAmbulanceId)
        );

        return suitable.get(0);
    }

    /**
     * Determines whether an ambulance can handle
     * an emergency.
     *
     * Critical:
     * ICU preferred, ALS accepted as fallback.
     *
     * High:
     * ICU or ALS.
     *
     * Moderate:
     * ALS or Basic.
     *
     * Normal:
     * Basic, ALS or ICU.
     */
    private boolean isSuitable(
            AmbulanceType ambulanceType,
            EmergencyPriority priority) {

        switch (priority) {

            case CRITICAL:
                return ambulanceType == AmbulanceType.ICU
                        || ambulanceType
                        == AmbulanceType.ADVANCED_LIFE_SUPPORT;

            case HIGH:
                return ambulanceType == AmbulanceType.ICU
                        || ambulanceType
                        == AmbulanceType.ADVANCED_LIFE_SUPPORT;

            case MODERATE:
                return ambulanceType
                        == AmbulanceType.ADVANCED_LIFE_SUPPORT
                        || ambulanceType
                        == AmbulanceType.BASIC;

            case NORMAL:
                return true;

            default:
                return false;
        }
    }

    /**
     * Calculates how appropriate the ambulance type is.
     */
    private int capabilityScore(
            AmbulanceType ambulanceType,
            EmergencyPriority priority) {

        if (priority == EmergencyPriority.CRITICAL) {

            if (ambulanceType == AmbulanceType.ICU) {
                return 3;
            }

            if (ambulanceType
                    == AmbulanceType.ADVANCED_LIFE_SUPPORT) {
                return 2;
            }
        }

        if (priority == EmergencyPriority.HIGH) {

            if (ambulanceType == AmbulanceType.ICU) {
                return 3;
            }

            if (ambulanceType
                    == AmbulanceType.ADVANCED_LIFE_SUPPORT) {
                return 2;
            }
        }

        if (priority == EmergencyPriority.MODERATE) {

            if (ambulanceType
                    == AmbulanceType.ADVANCED_LIFE_SUPPORT) {
                return 2;
            }

            if (ambulanceType == AmbulanceType.BASIC) {
                return 1;
            }
        }

        if (priority == EmergencyPriority.NORMAL) {

            if (ambulanceType == AmbulanceType.BASIC) {
                return 3;
            }

            if (ambulanceType
                    == AmbulanceType.ADVANCED_LIFE_SUPPORT) {
                return 2;
            }

            if (ambulanceType == AmbulanceType.ICU) {
                return 1;
            }
        }

        return 0;
    }

    // ============================================================
    // DISPATCH
    // ============================================================

    private void dispatch(
            EmergencyRequest request,
            Ambulance ambulance) {

        if (ambulance.getState()
                != AmbulanceState.AVAILABLE) {

            throw new NoAmbulanceAvailableException(
                    "Ambulance "
                            + ambulance.getAmbulanceId()
                            + " is not available.");
        }

        // Prevent double assignment.
        ambulance.setState(
                AmbulanceState.DISPATCHED);

        request.setStatus(
                EmergencyStatus.DISPATCHED);

        double eta =
                calculateEstimatedArrivalTime(
                        ambulance.getDistanceToPickupKm());

        DispatchRecord record =
                new DispatchRecord(
                        request,
                        ambulance,
                        eta);

        history.add(record);
    }

    // ============================================================
    // ETA
    // ============================================================

    /**
     * Calculates ETA using:
     *
     * time = distance / speed
     *
     * converted to minutes.
     */
    public double calculateEstimatedArrivalTime(
            double distanceKm) {

        if (Double.isNaN(distanceKm)
                || Double.isInfinite(distanceKm)
                || distanceKm < 0) {

            throw new InvalidEmergencyRequestException(
                    "Distance cannot be negative.");
        }

        return (distanceKm / AVERAGE_SPEED_KMH) * 60.0;
    }

    // ============================================================
    // STATE MANAGEMENT
    // ============================================================

    /**
     * Updates ambulance state.
     */
    public void updateAmbulanceState(
            String ambulanceId,
            AmbulanceState newState) {

        Ambulance ambulance =
                getAmbulance(ambulanceId);

        if (newState == null) {

            throw new InvalidStateTransitionException(
                    "New ambulance state cannot be null.");
        }

        AmbulanceState current =
                ambulance.getState();

        if (!isValidTransition(
                current,
                newState)) {

            throw new InvalidStateTransitionException(
                    "Invalid state transition from "
                            + current
                            + " to "
                            + newState);
        }

        ambulance.setState(newState);

        updateEmergencyStatus(
                ambulance,
                newState);

        // When ambulance becomes available,
        // automatically process waiting requests.
        if (newState == AmbulanceState.AVAILABLE) {

            allocateWaitingRequests();
        }
    }

    /**
     * Valid ambulance lifecycle:
     *
     * AVAILABLE
     * -> DISPATCHED
     * -> EN_ROUTE
     * -> PATIENT_PICKED_UP
     * -> HOSPITAL_ARRIVED
     * -> AVAILABLE
     */
    private boolean isValidTransition(
            AmbulanceState current,
            AmbulanceState next) {

        if (current == AmbulanceState.AVAILABLE
                && next == AmbulanceState.DISPATCHED) {

            return true;
        }

        if (current == AmbulanceState.DISPATCHED
                && next == AmbulanceState.EN_ROUTE) {

            return true;
        }

        if (current == AmbulanceState.EN_ROUTE
                && next
                == AmbulanceState.PATIENT_PICKED_UP) {

            return true;
        }

        if (current
                == AmbulanceState.PATIENT_PICKED_UP
                && next
                == AmbulanceState.HOSPITAL_ARRIVED) {

            return true;
        }

        if (current
                == AmbulanceState.HOSPITAL_ARRIVED
                && next == AmbulanceState.AVAILABLE) {

            return true;
        }

        return false;
    }

    /**
     * Updates the emergency status associated
     * with an ambulance.
     */
    private void updateEmergencyStatus(
            Ambulance ambulance,
            AmbulanceState state) {

        DispatchRecord latestRecord =
                findLatestRecordForAmbulance(
                        ambulance);

        if (latestRecord == null) {
            return;
        }

        EmergencyRequest request =
                latestRecord.getRequest();

        switch (state) {

            case DISPATCHED:
                request.setStatus(
                        EmergencyStatus.DISPATCHED);
                break;

            case EN_ROUTE:
                request.setStatus(
                        EmergencyStatus.EN_ROUTE);
                break;

            case PATIENT_PICKED_UP:
                request.setStatus(
                        EmergencyStatus.PATIENT_PICKED_UP);
                break;

            case HOSPITAL_ARRIVED:
                request.setStatus(
                        EmergencyStatus.HOSPITAL_ARRIVED);
                break;

            case AVAILABLE:
                request.setStatus(
                        EmergencyStatus.COMPLETED);
                break;

            default:
                break;
        }
    }

    /**
     * Finds most recent dispatch record for ambulance.
     */
    private DispatchRecord findLatestRecordForAmbulance(
            Ambulance ambulance) {

        for (int i = history.size() - 1;
             i >= 0;
             i--) {

            DispatchRecord record =
                    history.get(i);

            if (record.getAmbulance()
                    .getAmbulanceId()
                    .equals(ambulance.getAmbulanceId())) {

                return record;
            }
        }

        return null;
    }

    // ============================================================
    // WAITING QUEUE
    // ============================================================

    /**
     * Automatically allocates waiting requests.
     */
    public void allocateWaitingRequests() {

        while (!waitingQueue.isEmpty()) {

            QueueEntry entry =
                    waitingQueue.peek();

            EmergencyRequest request =
                    entry.request;

            Ambulance ambulance =
                    findBestAmbulance(request);

            if (ambulance == null) {
                break;
            }

            waitingQueue.poll();

            dispatch(
                    request,
                    ambulance);
        }
    }

    public int getWaitingQueueSize() {
        return waitingQueue.size();
    }

    // ============================================================
    // HISTORY
    // ============================================================

    public List<DispatchRecord> getHistory() {

        return Collections.unmodifiableList(
                history);
    }

    public int getHistorySize() {
        return history.size();
    }

    public int getAmbulanceCount() {
        return ambulances.size();
    }

    /**
     * Returns the dispatch record for a request.
     */
    public DispatchRecord getDispatchRecord(
            EmergencyRequest request) {

        for (DispatchRecord record : history) {

            if (record.getRequest() == request) {
                return record;
            }
        }

        return null;
    }

    // ============================================================
    // STATUS / DETAILS
    // ============================================================

    public String getDispatchDetails(
            EmergencyRequest request) {

        DispatchRecord record =
                getDispatchRecord(request);

        if (record == null) {

            return "Emergency request is waiting for "
                    + "an ambulance.";
        }

        Ambulance ambulance =
                record.getAmbulance();

        return
                "Patient ID: "
                        + request.getPatientId()
                        + "\nEmergency Type: "
                        + request.getEmergencyType()
                        + "\nEmergency Priority: "
                        + request.getPriority()
                        + "\nEmergency Status: "
                        + request.getStatus()
                        + "\nPickup Location: "
                        + request.getPickupLocation()
                        + "\nDestination Hospital: "
                        + request.getDestinationHospital()
                        + "\nEstimated Trip Distance: "
                        + request.getEstimatedDistanceKm()
                        + " km"
                        + "\nAmbulance ID: "
                        + ambulance.getAmbulanceId()
                        + "\nAmbulance Type: "
                        + ambulance.getType()
                        + "\nDriver: "
                        + ambulance.getDriverName()
                        + "\nDriver Phone: "
                        + ambulance.getDriverPhone()
                        + "\nDistance to Pickup: "
                        + ambulance.getDistanceToPickupKm()
                        + " km"
                        + "\nAmbulance Status: "
                        + ambulance.getState()
                        + "\nEstimated Arrival Time: "
                        + record
                        .getEstimatedArrivalTimeMinutes()
                        + " minutes";
    }

    // ============================================================
    // MAIN DEMONSTRATION
    // ============================================================

    public static void main(String[] args) {

        AmbulanceDispatchSystem system =
                new AmbulanceDispatchSystem();

        Ambulance basic =
                new Ambulance(
                        "AMB101",
                        AmbulanceType.BASIC,
                        "Rahul",
                        "9876543210",
                        5.0);

        Ambulance als =
                new Ambulance(
                        "AMB102",
                        AmbulanceType.ADVANCED_LIFE_SUPPORT,
                        "Arun",
                        "9876543211",
                        3.0);

        Ambulance icu =
                new Ambulance(
                        "AMB103",
                        AmbulanceType.ICU,
                        "Kumar",
                        "9876543212",
                        7.0);

        system.addAmbulance(basic);
        system.addAmbulance(als);
        system.addAmbulance(icu);

        EmergencyRequest request =
                new EmergencyRequest(
                        "P001",
                        "Cardiac Critical",
                        "Vellore Bus Stand",
                        "CMC Hospital",
                        8.0);

        Ambulance selected =
                system.processEmergency(request);

        System.out.println(
                "Selected Ambulance: "
                        + selected.getAmbulanceId());

        System.out.println();

        System.out.println(
                system.getDispatchDetails(request));

        System.out.println();

        // Demonstrate state flow
        system.updateAmbulanceState(
                selected.getAmbulanceId(),
                AmbulanceState.EN_ROUTE);

        system.updateAmbulanceState(
                selected.getAmbulanceId(),
                AmbulanceState.PATIENT_PICKED_UP);

        system.updateAmbulanceState(
                selected.getAmbulanceId(),
                AmbulanceState.HOSPITAL_ARRIVED);

        system.updateAmbulanceState(
                selected.getAmbulanceId(),
                AmbulanceState.AVAILABLE);

        System.out.println(
                "\nFinal Ambulance State: "
                        + selected.getState());

        System.out.println(
                "Final Emergency Status: "
                        + request.getStatus());
    }
}