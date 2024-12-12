package com.example.apilogin.Utility;

public class SnowflakeIdGenerator {

    private static final long EPOCH = 1609459200000L; // Start epoch (e.g., 01 Jan 2021)
    private static final long MACHINE_ID_BITS = 10L;
    private static final long DATACENTER_ID_BITS = 5L;
    private static final long SEQUENCE_BITS = 12L;

    private static final long MAX_MACHINE_ID = ~(-1L << MACHINE_ID_BITS);
    private static final long MAX_DATACENTER_ID = ~(-1L << DATACENTER_ID_BITS);
    private static final long SEQUENCE_MASK = ~(-1L << SEQUENCE_BITS);

    private long machineId;
    private long datacenterId;
    private long sequence = 0L;
    private long lastTimestamp = -1L;

    public SnowflakeIdGenerator(long machineId, long datacenterId) {
        if (machineId > MAX_MACHINE_ID || machineId < 0) {
            throw new IllegalArgumentException("Machine ID can't be greater than " + MAX_MACHINE_ID + " or less than 0");
        }
        if (datacenterId > MAX_DATACENTER_ID || datacenterId < 0) {
            throw new IllegalArgumentException("Datacenter ID can't be greater than " + MAX_DATACENTER_ID + " or less than 0");
        }
        this.machineId = machineId;
        this.datacenterId = datacenterId;
    }

    public synchronized long generateId() {
        long timestamp = System.currentTimeMillis() - EPOCH;

        // Handle sequence overflow
        if (timestamp == lastTimestamp) {
            sequence = (sequence + 1) & SEQUENCE_MASK;
            if (sequence == 0) {
                // Wait for the next millisecond if sequence overflows
                while (timestamp <= lastTimestamp) {
                    timestamp = System.currentTimeMillis() - EPOCH;
                }
            }
        } else {
            sequence = 0;
        }

        lastTimestamp = timestamp;

        // Safeguard: Check if timestamp can fit in the available bits
        long timestampShifted = timestamp;

        // Calculate the total number of bits needed for machine ID, datacenter ID, and sequence
        long totalBitsForMachineAndDatacenterAndSequence = MACHINE_ID_BITS + DATACENTER_ID_BITS + SEQUENCE_BITS;

        // Adjust the timestamp bit allocation dynamically if it's too large to fit
        if (timestamp > (Long.MAX_VALUE >> totalBitsForMachineAndDatacenterAndSequence)) {
            // Reduce the size of the timestamp field by shifting the timestamp down
            timestampShifted = timestamp >> 2; // Reduce the timestamp to fit into the available space
            System.out.println("Timestamp too large, reducing timestamp size. New timestamp: " + timestampShifted);
        }

        // Shift the timestamp correctly
        timestampShifted = timestampShifted << totalBitsForMachineAndDatacenterAndSequence;

        // Debug print statements to ensure correct values
        System.out.println("Timestamp: " + timestamp);
        System.out.println("Adjusted Timestamp: " + timestampShifted);

        // Return the final Snowflake ID with no overflow
        return timestampShifted |
                (datacenterId << (MACHINE_ID_BITS + SEQUENCE_BITS)) |
                (machineId << SEQUENCE_BITS) |
                sequence;
    }
}
