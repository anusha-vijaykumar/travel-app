package com.springcloud.booking_service.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.sql.Timestamp;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "booking")
public class Booking {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private Long userId;
    private Long tourId;
    private Integer seatsBooked;
    @Enumerated(EnumType.STRING)
    private BookingStatus bookingStatus;
    private Timestamp createdAt;
    @Column(unique = true)
    private String idempotencyKey;
}
