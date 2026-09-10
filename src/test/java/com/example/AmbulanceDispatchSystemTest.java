package com.example;

import org.junit.Test;

import static org.junit.Assert.*;

/**
 * Comprehensive test suite for
 * Real-Time Emergency Ambulance Dispatch
 * and Tracking System.
 */
public class AmbulanceDispatchSystemTest {

    // ============================================================
    // HELPER METHODS
    // ============================================================

    private AmbulanceDispatchSystem.Ambulance createBasic(
            String id,
            double distance) {

        return new AmbulanceDispatchSystem.Ambulance(
                id,
                AmbulanceDispatchSystem.AmbulanceType.BASIC,
                "Rahul",
                "9000000001",
                distance);
    }

    private AmbulanceDispatchSystem.Ambulance createALS(
            String id,
            double distance) {

        return new AmbulanceDispatchSystem.Ambulance(
                id,
                AmbulanceDispatchSystem.AmbulanceType.ADVANCED_LIFE_SUPPORT,
                "Arun",
                "9000000002",
                distance);
    }

    private AmbulanceDispatchSystem.Ambulance createICU(
            String id,
            double distance) {

        return new AmbulanceDispatchSystem.Ambulance(
                id,
                AmbulanceDispatchSystem.AmbulanceType.ICU,
                "Kumar",
                "9000000003",
                distance);
    }

    private AmbulanceDispatchSystem.EmergencyRequest createRequest(
            String patientId,
            String type,
            double distance) {

        return new AmbulanceDispatchSystem.EmergencyRequest(
                patientId,
                type,
                "Vellore Bus Stand",
                "CMC Hospital",
                distance);
    }

    // ============================================================
    // POSITIVE TESTS
    // ============================================================

    @Test
    public void testAddBasicAmbulance() {

        AmbulanceDispatchSystem system =
                new AmbulanceDispatchSystem();

        system.addAmbulance(
                createBasic("AMB101", 5));

        assertEquals(
                1,
                system.getAmbulanceCount());
    }

    @Test
    public void testAddMultipleAmbulances() {

        AmbulanceDispatchSystem system =
                new AmbulanceDispatchSystem();

        system.addAmbulance(
                createBasic("AMB101", 5));

        system.addALS(
                createALS("AMB102", 3));

        system.addAmbulance(
                createICU("AMB103", 7));

        assertEquals(
                3,
                system.getAmbulanceCount());
    }

    @Test
    public void testCriticalEmergencyGetsICU() {

        AmbulanceDispatchSystem system =
                new AmbulanceDispatchSystem();

        system.addAmbulance(
                createBasic("AMB101", 1));

        system.addAmbulance(
                createALS("AMB102", 1));

        system.addAmbulance(
                createICU("AMB103", 10));

        AmbulanceDispatchSystem.EmergencyRequest request =
                createRequest(
                        "P001",
                        "Cardiac Critical",
                        8);

        AmbulanceDispatchSystem.Ambulance selected =
                system.processEmergency(request);

        assertNotNull(selected);

        assertEquals(
                "AMB103",
                selected.getAmbulanceId());
    }

    @Test
    public void testHighEmergencyGetsALSWhenICUUnavailable() {

        AmbulanceDispatchSystem system =
                new AmbulanceDispatchSystem();

        system.addAmbulance(
                createBasic("AMB101", 1));

        system.addAmbulance(
                createALS("AMB102", 5));

        AmbulanceDispatchSystem.EmergencyRequest request =
                createRequest(
                        "P002",
                        "Severe Accident",
                        10);

        AmbulanceDispatchSystem.Ambulance selected =
                system.processEmergency(request);

        assertNotNull(selected);

        assertEquals(
                "AMB102",
                selected.getAmbulanceId());
    }

    @Test
    public void testModerateEmergencyGetsALS() {

        AmbulanceDispatchSystem system =
                new AmbulanceDispatchSystem();

        system.addAmbulance(
                createBasic("AMB101", 2));

        system.addAmbulance(
                createALS("AMB102", 5));

        AmbulanceDispatchSystem.EmergencyRequest request =
                createRequest(
                        "P003",
                        "Fracture",
                        10);

        AmbulanceDispatchSystem.Ambulance selected =
                system.processEmergency(request);

        assertNotNull(selected);

        assertEquals(
                "AMB102",
                selected.getAmbulanceId());
    }

    @Test
    public void testNormalEmergencyGetsBasic() {

        AmbulanceDispatchSystem system =
                new AmbulanceDispatchSystem();

        system.addAmbulance(
                createBasic("AMB101", 3));

        AmbulanceDispatchSystem.EmergencyRequest request =
                createRequest(
                        "P004",
                        "Fever",
                        5);

        AmbulanceDispatchSystem.Ambulance selected =
                system.processEmergency(request);

        assertNotNull(selected);

        assertEquals(
                "AMB101",
                selected.getAmbulanceId());
    }

    @Test
    public void testNearestAmbulanceIsSelectedWhenCapabilityIsSame() {

        AmbulanceDispatchSystem system =
                new AmbulanceDispatchSystem();

        system.addAmbulance(
                createBasic("AMB101", 10));

        system.addAmbulance(
                createBasic("AMB102", 3));

        AmbulanceDispatchSystem.EmergencyRequest request =
                createRequest(
                        "P005",
                        "Fever",
                        5);

        AmbulanceDispatchSystem.Ambulance selected =
                system.processEmergency(request);

        assertEquals(
                "AMB102",
                selected.getAmbulanceId());
    }

    @Test
    public void testCriticalPriorityIsHigherThanNormal() {

        assertTrue(
                AmbulanceDispatchSystem
                        .EmergencyPriority.CRITICAL
                        .getLevel()
                        >
                        AmbulanceDispatchSystem
                                .EmergencyPriority.NORMAL
                                .getLevel());
    }

    @Test
    public void testEmergencyClassification() {

        assertEquals(
                AmbulanceDispatchSystem.EmergencyPriority.CRITICAL,
                AmbulanceDispatchSystem.EmergencyRequest
                        .classifyEmergency("Cardiac"));

        assertEquals(
                AmbulanceDispatchSystem.EmergencyPriority.CRITICAL,
                AmbulanceDispatchSystem.EmergencyRequest
                        .classifyEmergency("Heart Attack"));

        assertEquals(
                AmbulanceDispatchSystem.EmergencyPriority.HIGH,
                AmbulanceDispatchSystem.EmergencyRequest
                        .classifyEmergency("Accident"));

        assertEquals(
                AmbulanceDispatchSystem.EmergencyPriority.MODERATE,
                AmbulanceDispatchSystem.EmergencyRequest
                        .classifyEmergency("Fracture"));

        assertEquals(
                AmbulanceDispatchSystem.EmergencyPriority.NORMAL,
                AmbulanceDispatchSystem.EmergencyRequest
                        .classifyEmergency("Fever"));
    }

    // ============================================================
    // STATE FLOW TESTS
    // ============================================================

    @Test
    public void testCompleteAmbulanceStateFlow() {

        AmbulanceDispatchSystem system =
                new AmbulanceDispatchSystem();

        AmbulanceDispatchSystem.Ambulance ambulance =
                createBasic("AMB101", 5);

        system.addAmbulance(ambulance);

        AmbulanceDispatchSystem.EmergencyRequest request =
                createRequest(
                        "P010",
                        "Fever",
                        10);

        system.processEmergency(request);

        assertEquals(
                AmbulanceDispatchSystem.AmbulanceState.DISPATCHED,
                ambulance.getState());

        assertEquals(
                AmbulanceDispatchSystem.EmergencyStatus.DISPATCHED,
                request.getStatus());

        system.updateAmbulanceState(
                "AMB101",
                AmbulanceDispatchSystem.AmbulanceState.EN_ROUTE);

        assertEquals(
                AmbulanceDispatchSystem.AmbulanceState.EN_ROUTE,
                ambulance.getState());

        assertEquals(
                AmbulanceDispatchSystem.EmergencyStatus.EN_ROUTE,
                request.getStatus());

        system.updateAmbulanceState(
                "AMB101",
                AmbulanceDispatchSystem.AmbulanceState.PATIENT_PICKED_UP);

        assertEquals(
                AmbulanceDispatchSystem.AmbulanceState.PATIENT_PICKED_UP,
                ambulance.getState());

        assertEquals(
                AmbulanceDispatchSystem.EmergencyStatus.PATIENT_PICKED_UP,
                request.getStatus());

        system.updateAmbulanceState(
                "AMB101",
                AmbulanceDispatchSystem.AmbulanceState.HOSPITAL_ARRIVED);

        assertEquals(
                AmbulanceDispatchSystem.AmbulanceState.HOSPITAL_ARRIVED,
                ambulance.getState());

        assertEquals(
                AmbulanceDispatchSystem.EmergencyStatus.HOSPITAL_ARRIVED,
                request.getStatus());

        system.updateAmbulanceState(
                "AMB101",
                AmbulanceDispatchSystem.AmbulanceState.AVAILABLE);

        assertEquals(
                AmbulanceDispatchSystem.AmbulanceState.AVAILABLE,
                ambulance.getState());

        assertEquals(
                AmbulanceDispatchSystem.EmergencyStatus.COMPLETED,
                request.getStatus());
    }

    // ============================================================
    // ETA TESTS
    // ============================================================

    @Test
    public void testEstimatedArrivalTime() {

        AmbulanceDispatchSystem system =
                new AmbulanceDispatchSystem();

        double eta =
                system.calculateEstimatedArrivalTime(20);

        assertEquals(
                30.0,
                eta,
                0.01);
    }

    @Test
    public void testZeroDistanceETA() {

        AmbulanceDispatchSystem system =
                new AmbulanceDispatchSystem();

        assertEquals(
                0.0,
                system.calculateEstimatedArrivalTime(0),
                0.01);
    }

    @Test
    public void testVeryLargeDistanceETA() {

        AmbulanceDispatchSystem system =
                new AmbulanceDispatchSystem();

        double eta =
                system.calculateEstimatedArrivalTime(1000);

        assertEquals(
                1500.0,
                eta,
                0.01);
    }

    // ============================================================
    // WAITING QUEUE TESTS
    // ============================================================

    @Test
    public void testRequestGoesToWaitingQueueWhenNoAmbulanceAvailable() {

        AmbulanceDispatchSystem system =
                new AmbulanceDispatchSystem();

        AmbulanceDispatchSystem.EmergencyRequest request =
                createRequest(
                        "P020",
                        "Cardiac Critical",
                        10);

        AmbulanceDispatchSystem.Ambulance selected =
                system.processEmergency(request);

        assertNull(selected);

        assertEquals(
                1,
                system.getWaitingQueueSize());

        assertEquals(
                AmbulanceDispatchSystem.EmergencyStatus.WAITING,
                request.getStatus());
    }

    @Test
    public void testAutomaticAllocationFromWaitingQueue() {

        AmbulanceDispatchSystem system =
                new AmbulanceDispatchSystem();

        AmbulanceDispatchSystem.EmergencyRequest request =
                createRequest(
                        "P021",
                        "Fever",
                        10);

        system.processEmergency(request);

        assertEquals(
                1,
                system.getWaitingQueueSize());

        AmbulanceDispatchSystem.Ambulance ambulance =
                createBasic("AMB101", 2);

        system.addAmbulance(ambulance);

        assertEquals(
                0,
                system.getWaitingQueueSize());

        assertEquals(
                AmbulanceDispatchSystem.AmbulanceState.DISPATCHED,
                ambulance.getState());

        assertEquals(
                AmbulanceDispatchSystem.EmergencyStatus.DISPATCHED,
                request.getStatus());
    }

    @Test
    public void testMultipleWaitingRequestsRespectPriority() {

        AmbulanceDispatchSystem system =
                new AmbulanceDispatchSystem();

        AmbulanceDispatchSystem.EmergencyRequest normal =
                createRequest(
                        "P030",
                        "Fever",
                        10);

        AmbulanceDispatchSystem.EmergencyRequest critical =
                createRequest(
                        "P031",
                        "Cardiac Critical",
                        10);

        system.processEmergency(normal);
        system.processEmergency(critical);

        assertEquals(
                2,
                system.getWaitingQueueSize());

        system.addAmbulance(
                createICU("AMB101", 5));

        assertEquals(
                1,
                system.getWaitingQueueSize());

        assertEquals(
                AmbulanceDispatchSystem.EmergencyStatus.DISPATCHED,
                critical.getStatus());

        assertEquals(
                AmbulanceDispatchSystem.EmergencyStatus.WAITING,
                normal.getStatus());
    }

    // ============================================================
    // MULTIPLE ACTIVE EMERGENCY TEST
    // ============================================================

    @Test
    public void testAmbulanceCannotHandleTwoActiveEmergencies() {

        AmbulanceDispatchSystem system =
                new AmbulanceDispatchSystem();

        AmbulanceDispatchSystem.Ambulance ambulance =
                createBasic("AMB101", 2);

        system.addAmbulance(ambulance);

        AmbulanceDispatchSystem.EmergencyRequest first =
                createRequest(
                        "P040",
                        "Fever",
                        10);

        AmbulanceDispatchSystem.EmergencyRequest second =
                createRequest(
                        "P041",
                        "Fever",
                        10);

        AmbulanceDispatchSystem.Ambulance selected =
                system.processEmergency(first);

        assertEquals(
                "AMB101",
                selected.getAmbulanceId());

        AmbulanceDispatchSystem.Ambulance secondSelected =
                system.processEmergency(second);

        assertNull(secondSelected);

        assertEquals(
                1,
                system.getWaitingQueueSize());

        assertEquals(
                AmbulanceDispatchSystem.AmbulanceState.DISPATCHED,
                ambulance.getState());
    }

    // ============================================================
    // HISTORY TESTS
    // ============================================================

    @Test
    public void testHistoryIsMaintained() {

        AmbulanceDispatchSystem system =
                new AmbulanceDispatchSystem();

        system.addAmbulance(
                createBasic("AMB101", 5));

        AmbulanceDispatchSystem.EmergencyRequest request =
                createRequest(
                        "P050",
                        "Fever",
                        10);

        system.processEmergency(request);

        assertEquals(
                1,
                system.getHistorySize());

        assertEquals(
                request,
                system.getHistory()
                        .get(0)
                        .getRequest());
    }

    @Test
    public void testDispatchDetailsContainsRequiredInformation() {

        AmbulanceDispatchSystem system =
                new AmbulanceDispatchSystem();

        system.addAmbulance(
                createBasic("AMB101", 5));

        AmbulanceDispatchSystem.EmergencyRequest request =
                createRequest(
                        "P051",
                        "Fever",
                        10);

        system.processEmergency(request);

        String details =
                system.getDispatchDetails(request);

        assertTrue(
                details.contains("P051"));

        assertTrue(
                details.contains("AMB101"));

        assertTrue(
                details.contains("Rahul"));

        assertTrue(
                details.contains("Fever"));

        assertTrue(
                details.contains("Vellore Bus Stand"));

        assertTrue(
                details.contains("CMC Hospital"));
    }

    // ============================================================
    // NEGATIVE TESTS - INVALID REQUEST
    // ============================================================

    @Test(expected =
            AmbulanceDispatchSystem.InvalidEmergencyRequestException.class)
    public void testNullEmergencyRequest() {

        AmbulanceDispatchSystem system =
                new AmbulanceDispatchSystem();

        system.processEmergency(null);
    }

    @Test
    public void testEmptyPatientId() {

        AmbulanceDispatchSystem system =
                new AmbulanceDispatchSystem();

        AmbulanceDispatchSystem.EmergencyRequest request =
                new AmbulanceDispatchSystem.EmergencyRequest(
                        "",
                        "Fever",
                        "Vellore",
                        "CMC Hospital",
                        5);

        try {

            system.processEmergency(request);

            fail("Expected InvalidEmergencyRequestException");

        } catch (
                AmbulanceDispatchSystem.InvalidEmergencyRequestException e) {

            assertEquals(
                    "Patient ID is required.",
                    e.getMessage());
        }
    }

    @Test
    public void testNullPatientId() {

        AmbulanceDispatchSystem system =
                new AmbulanceDispatchSystem();

        AmbulanceDispatchSystem.EmergencyRequest request =
                new AmbulanceDispatchSystem.EmergencyRequest(
                        null,
                        "Fever",
                        "Vellore",
                        "CMC Hospital",
                        5);

        try {

            system.processEmergency(request);

            fail("Expected exception");

        } catch (
                AmbulanceDispatchSystem.InvalidEmergencyRequestException e) {

            assertEquals(
                    "Patient ID is required.",
                    e.getMessage());
        }
    }

    @Test
    public void testEmptyEmergencyType() {

        AmbulanceDispatchSystem system =
                new AmbulanceDispatchSystem();

        try {

            AmbulanceDispatchSystem.EmergencyRequest request =
                    new AmbulanceDispatchSystem.EmergencyRequest(
                            "P100",
                            "",
                            "Vellore",
                            "CMC Hospital",
                            5);

            system.processEmergency(request);

            fail("Expected exception");

        } catch (
                AmbulanceDispatchSystem.InvalidEmergencyRequestException e) {

            assertEquals(
                    "Emergency type is required.",
                    e.getMessage());
        }
    }

    @Test
    public void testNullEmergencyType() {

        try {

            new AmbulanceDispatchSystem.EmergencyRequest(
                    "P101",
                    null,
                    "Vellore",
                    "CMC Hospital",
                    5);

            fail("Expected exception");

        } catch (
                AmbulanceDispatchSystem.InvalidEmergencyRequestException e) {

            assertEquals(
                    "Emergency type cannot be null.",
                    e.getMessage());
        }
    }

    @Test
    public void testEmptyPickupLocation() {

        AmbulanceDispatchSystem system =
                new AmbulanceDispatchSystem();

        AmbulanceDispatchSystem.EmergencyRequest request =
                new AmbulanceDispatchSystem.EmergencyRequest(
                        "P102",
                        "Fever",
                        "",
                        "CMC Hospital",
                        5);

        try {

            system.processEmergency(request);

            fail("Expected exception");

        } catch (
                AmbulanceDispatchSystem.InvalidEmergencyRequestException e) {

            assertEquals(
                    "Pickup location is required.",
                    e.getMessage());
        }
    }

    @Test
    public void testEmptyDestinationHospital() {

        AmbulanceDispatchSystem system =
                new AmbulanceDispatchSystem();

        AmbulanceDispatchSystem.EmergencyRequest request =
                new AmbulanceDispatchSystem.EmergencyRequest(
                        "P103",
                        "Fever",
                        "Vellore",
                        "",
                        5);

        try {

            system.processEmergency(request);

            fail("Expected exception");

        } catch (
                AmbulanceDispatchSystem.InvalidEmergencyRequestException e) {

            assertEquals(
                    "Destination hospital is required.",
                    e.getMessage());
        }
    }

    // ============================================================
    // NEGATIVE TESTS - DISTANCE
    // ============================================================

    @Test
    public void testZeroEmergencyDistance() {

        AmbulanceDispatchSystem system =
                new AmbulanceDispatchSystem();

        AmbulanceDispatchSystem.EmergencyRequest request =
                createRequest(
                        "P104",
                        "Fever",
                        0);

        try {

            system.processEmergency(request);

            fail("Expected exception");

        } catch (
                AmbulanceDispatchSystem.InvalidEmergencyRequestException e) {

            assertEquals(
                    "Estimated distance must be greater than zero.",
                    e.getMessage());
        }
    }

    @Test
    public void testNegativeEmergencyDistance() {

        AmbulanceDispatchSystem system =
                new AmbulanceDispatchSystem();

        AmbulanceDispatchSystem.EmergencyRequest request =
                createRequest(
                        "P105",
                        "Fever",
                        -5);

        try {

            system.processEmergency(request);

            fail("Expected exception");

        } catch (
                AmbulanceDispatchSystem.InvalidEmergencyRequestException e) {

            assertEquals(
                    "Estimated distance must be greater than zero.",
                    e.getMessage());
        }
    }

    @Test
    public void testNegativeETADistance() {

        AmbulanceDispatchSystem system =
                new AmbulanceDispatchSystem();

        try {

            system.calculateEstimatedArrivalTime(-10);

            fail("Expected exception");

        } catch (
                AmbulanceDispatchSystem.InvalidEmergencyRequestException e) {

            assertEquals(
                    "Distance cannot be negative.",
                    e.getMessage());
        }
    }

    // ============================================================
    // NEGATIVE TESTS - AMBULANCE
    // ============================================================

    @Test
    public void testNullAmbulance() {

        AmbulanceDispatchSystem system =
                new AmbulanceDispatchSystem();

        try {

            system.addAmbulance(null);

            fail("Expected exception");

        } catch (
                AmbulanceDispatchSystem.InvalidAmbulanceException e) {

            assertEquals(
                    "Ambulance cannot be null.",
                    e.getMessage());
        }
    }

    @Test
    public void testDuplicateAmbulanceId() {

        AmbulanceDispatchSystem system =
                new AmbulanceDispatchSystem();

        system.addAmbulance(
                createBasic("AMB101", 5));

        try {

            system.addAmbulance(
                    createALS("AMB101", 3));

            fail("Expected duplicate ambulance exception");

        } catch (
                AmbulanceDispatchSystem.DuplicateAmbulanceException e) {

            assertTrue(
                    e.getMessage()
                            .contains("AMB101"));
        }
    }

    @Test
    public void testAmbulanceNotFound() {

        AmbulanceDispatchSystem system =
                new AmbulanceDispatchSystem();

        try {

            system.getAmbulance("UNKNOWN");

            fail("Expected exception");

        } catch (
                AmbulanceDispatchSystem.AmbulanceNotFoundException e) {

            assertTrue(
                    e.getMessage()
                            .contains("UNKNOWN"));
        }
    }

    @Test
    public void testNullAmbulanceId() {

        AmbulanceDispatchSystem system =
                new AmbulanceDispatchSystem();

        try {

            system.getAmbulance(null);

            fail("Expected exception");

        } catch (
                AmbulanceDispatchSystem.AmbulanceNotFoundException e) {

            assertEquals(
                    "Ambulance ID is required.",
                    e.getMessage());
        }
    }

    // ============================================================
    // NEGATIVE TESTS - STATE TRANSITIONS
    // ============================================================

    @Test
    public void testInvalidAvailableToHospitalArrived() {

        AmbulanceDispatchSystem system =
                new AmbulanceDispatchSystem();

        system.addAmbulance(
                createBasic("AMB101", 5));

        try {

            system.updateAmbulanceState(
                    "AMB101",
                    AmbulanceDispatchSystem.AmbulanceState
                            .HOSPITAL_ARRIVED);

            fail("Expected invalid state transition");

        } catch (
                AmbulanceDispatchSystem.InvalidStateTransitionException e) {

            assertTrue(
                    e.getMessage()
                            .contains("Invalid state transition"));
        }
    }

    @Test
    public void testInvalidAvailableToEnRoute() {

        AmbulanceDispatchSystem system =
                new AmbulanceDispatchSystem();

        system.addAmbulance(
                createBasic("AMB101", 5));

        try {

            system.updateAmbulanceState(
                    "AMB101",
                    AmbulanceDispatchSystem.AmbulanceState
                            .EN_ROUTE);

            fail("Expected invalid transition");

        } catch (
                AmbulanceDispatchSystem.InvalidStateTransitionException e) {

            assertTrue(
                    e.getMessage()
                            .contains("Invalid state transition"));
        }
    }

    @Test
    public void testInvalidDispatchedToAvailable() {

        AmbulanceDispatchSystem system =
                new AmbulanceDispatchSystem();

        AmbulanceDispatchSystem.Ambulance ambulance =
                createBasic("AMB101", 5);

        system.addAmbulance(ambulance);

        AmbulanceDispatchSystem.EmergencyRequest request =
                createRequest(
                        "P200",
                        "Fever",
                        5);

        system.processEmergency(request);

        try {

            system.updateAmbulanceState(
                    "AMB101",
                    AmbulanceDispatchSystem.AmbulanceState
                            .AVAILABLE);

            fail("Expected invalid transition");

        } catch (
                AmbulanceDispatchSystem.InvalidStateTransitionException e) {

            assertTrue(
                    e.getMessage()
                            .contains("Invalid state transition"));
        }
    }

    @Test
    public void testInvalidEnRouteToHospitalArrived() {

        AmbulanceDispatchSystem system =
                new AmbulanceDispatchSystem();

        AmbulanceDispatchSystem.Ambulance ambulance =
                createBasic("AMB101", 5);

        system.addAmbulance(ambulance);

        AmbulanceDispatchSystem.EmergencyRequest request =
                createRequest(
                        "P201",
                        "Fever",
                        5);

        system.processEmergency(request);

        system.updateAmbulanceState(
                "AMB101",
                AmbulanceDispatchSystem.AmbulanceState
                        .EN_ROUTE);

        try {

            system.updateAmbulanceState(
                    "AMB101",
                    AmbulanceDispatchSystem.AmbulanceState
                            .HOSPITAL_ARRIVED);

            fail("Expected invalid transition");

        } catch (
                AmbulanceDispatchSystem.InvalidStateTransitionException e) {

            assertTrue(
                    e.getMessage()
                            .contains("Invalid state transition"));
        }
    }

    @Test
    public void testNullNewState() {

        AmbulanceDispatchSystem system =
                new AmbulanceDispatchSystem();

        system.addAmbulance(
                createBasic("AMB101", 5));

        try {

            system.updateAmbulanceState(
                    "AMB101",
                    null);

            fail("Expected exception");

        } catch (
                AmbulanceDispatchSystem.InvalidStateTransitionException e) {

            assertEquals(
                    "New ambulance state cannot be null.",
                    e.getMessage());
        }
    }

    // ============================================================
    // EDGE CASES
    // ============================================================

    @Test
    public void testVeryShortAmbulanceDistance() {

        AmbulanceDispatchSystem system =
                new AmbulanceDispatchSystem();

        AmbulanceDispatchSystem.Ambulance ambulance =
                createBasic(
                        "AMB101",
                        0.1);

        system.addAmbulance(ambulance);

        AmbulanceDispatchSystem.EmergencyRequest request =
                createRequest(
                        "P300",
                        "Fever",
                        1);

        AmbulanceDispatchSystem.Ambulance selected =
                system.processEmergency(request);

        assertEquals(
                "AMB101",
                selected.getAmbulanceId());
    }

    @Test
    public void testMultipleAmbulancesSameDistanceUsesDeterministicId() {

        AmbulanceDispatchSystem system =
                new AmbulanceDispatchSystem();

        system.addAmbulance(
                createBasic("AMB102", 5));

        system.addAmbulance(
                createBasic("AMB101", 5));

        AmbulanceDispatchSystem.EmergencyRequest request =
                createRequest(
                        "P301",
                        "Fever",
                        5);

        AmbulanceDispatchSystem.Ambulance selected =
                system.processEmergency(request);

        assertEquals(
                "AMB101",
                selected.getAmbulanceId());
    }

    @Test
    public void testCriticalRequestFallsBackToALSWhenICUUnavailable() {

        AmbulanceDispatchSystem system =
                new AmbulanceDispatchSystem();

        system.addAmbulance(
                createALS("AMB102", 4));

        AmbulanceDispatchSystem.EmergencyRequest request =
                createRequest(
                        "P302",
                        "Cardiac Emergency",
                        10);

        AmbulanceDispatchSystem.Ambulance selected =
                system.processEmergency(request);

        assertNotNull(selected);

        assertEquals(
                AmbulanceDispatchSystem.AmbulanceType
                        .ADVANCED_LIFE_SUPPORT,
                selected.getType());
    }

    @Test
    public void testCriticalRequestWaitsIfNoCriticalCapableAmbulanceExists() {

        AmbulanceDispatchSystem system =
                new AmbulanceDispatchSystem();

        system.addAmbulance(
                createBasic("AMB101", 2));

        AmbulanceDispatchSystem.EmergencyRequest request =
                createRequest(
                        "P303",
                        "Cardiac Critical",
                        10);

        AmbulanceDispatchSystem.Ambulance selected =
                system.processEmergency(request);

        assertNull(selected);

        assertEquals(
                1,
                system.getWaitingQueueSize());
    }

    @Test
    public void testAmbulanceDistanceUpdate() {

        AmbulanceDispatchSystem.Ambulance ambulance =
                createBasic("AMB101", 10);

        ambulance.setDistanceToPickupKm(3);

        assertEquals(
                3.0,
                ambulance.getDistanceToPickupKm(),
                0.01);
    }

    @Test
    public void testHistoryContainsMultipleEmergencies() {

        AmbulanceDispatchSystem system =
                new AmbulanceDispatchSystem();

        system.addAmbulance(
                createBasic("AMB101", 2));

        system.addAmbulance(
                createBasic("AMB102", 3));

        AmbulanceDispatchSystem.EmergencyRequest first =
                createRequest(
                        "P400",
                        "Fever",
                        5);

        AmbulanceDispatchSystem.EmergencyRequest second =
                createRequest(
                        "P401",
                        "Fever",
                        5);

        system.processEmergency(first);
        system.processEmergency(second);

        assertEquals(
                2,
                system.getHistorySize());
    }

    // ============================================================
    // MULTIPLE FAILURE SCENARIO
    // ============================================================

    @Test
    public void testMultipleInvalidInputs() {

        AmbulanceDispatchSystem system =
                new AmbulanceDispatchSystem();

        // Failure 1 - invalid ambulance
        try {

            system.addAmbulance(null);

            fail("Invalid ambulance should fail");

        } catch (
                AmbulanceDispatchSystem.InvalidAmbulanceException e) {

            assertTrue(true);
        }

        // Failure 2 - invalid request
        try {

            system.processEmergency(null);

            fail("Null request should fail");

        } catch (
                AmbulanceDispatchSystem.InvalidEmergencyRequestException e) {

            assertTrue(true);
        }

        // Failure 3 - invalid ambulance ID
        try {

            system.getAmbulance("UNKNOWN");

            fail("Unknown ambulance should fail");

        } catch (
                AmbulanceDispatchSystem.AmbulanceNotFoundException e) {

            assertTrue(true);
        }

        // Failure 4 - invalid ETA
        try {

            system.calculateEstimatedArrivalTime(-10);

            fail("Negative distance should fail");

        } catch (
                AmbulanceDispatchSystem.InvalidEmergencyRequestException e) {

            assertTrue(true);
        }
    }
}