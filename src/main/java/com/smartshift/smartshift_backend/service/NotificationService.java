package com.smartshift.smartshift_backend.service;

import com.smartshift.smartshift_backend.entity.Shift;

public interface NotificationService {
    void sendShiftPublishedEmail(Shift shift);
}