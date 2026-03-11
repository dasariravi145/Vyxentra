package com.vyxentra.vehicle.service;


import com.vyxentra.vehicle.entity.Booking;

public interface SnapshotService {

    void createSnapshot(Booking booking, String snapshotType, String createdBy);
}
