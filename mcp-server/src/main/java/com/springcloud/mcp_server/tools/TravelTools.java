package com.springcloud.mcp_server.tools;

import com.springcloud.mcp_server.dto.BookingRequest;
import com.springcloud.mcp_server.dto.BookingResponse;
import com.springcloud.mcp_server.dto.InventoryRequest;
import com.springcloud.mcp_server.dto.InventoryResponse;
import com.springcloud.mcp_server.dto.PaymentRequest;
import com.springcloud.mcp_server.dto.PaymentResponse;
import com.springcloud.mcp_server.dto.UserRequest;
import com.springcloud.mcp_server.dto.UserResponse;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.util.List;

@Service
public class TravelTools {

    private final RestClient travelApi;

    public TravelTools(RestClient travelApiRestClient) {
        this.travelApi = travelApiRestClient;
    }

    @Tool(description = "Create a user account for the travel booking platform.")
    public UserResponse createUser(@ToolParam(description = "User profile fields.") UserRequest request) {
        return travelApi.post()
                .uri("/api/users")
                .body(request)
                .retrieve()
                .body(UserResponse.class);
    }

    @Tool(description = "Get a travel platform user by numeric user id.")
    public UserResponse getUserById(@ToolParam(description = "Numeric user id.") Long userId) {
        return travelApi.get()
                .uri("/api/users/{id}", userId)
                .retrieve()
                .body(UserResponse.class);
    }

    @Tool(description = "Get a travel platform user by email address.")
    public UserResponse getUserByEmail(@ToolParam(description = "User email address.") String email) {
        return travelApi.get()
                .uri("/api/users/email/{email}", email)
                .retrieve()
                .body(UserResponse.class);
    }

    @Tool(description = "Create a tour inventory record.")
    public InventoryResponse createInventory(@ToolParam(description = "Tour inventory fields.") InventoryRequest request) {
        return travelApi.post()
                .uri("/api/inventories")
                .body(request)
                .retrieve()
                .body(InventoryResponse.class);
    }

    @Tool(description = "Get tour inventory by inventory id.")
    public InventoryResponse getInventoryById(@ToolParam(description = "Inventory id.") Long inventoryId) {
        return travelApi.get()
                .uri("/api/inventories/{id}", inventoryId)
                .retrieve()
                .body(InventoryResponse.class);
    }

    @Tool(description = "Get tour inventory by tour package id.")
    public InventoryResponse getInventoryByTourPackageId(@ToolParam(description = "Tour package id.") Long tourPackageId) {
        return travelApi.get()
                .uri("/api/inventories/by-tour/{tourPackageId}", tourPackageId)
                .retrieve()
                .body(InventoryResponse.class);
    }

    @Tool(description = "Reserve seats for a tour package.")
    public String reserveSeats(
            @ToolParam(description = "Tour package id.") Long tourPackageId,
            @ToolParam(description = "Number of seats to reserve.") Integer seats) {
        travelApi.post()
                .uri(uriBuilder -> uriBuilder
                        .path("/api/inventories/reserve")
                        .queryParam("tourPackageId", tourPackageId)
                        .queryParam("seats", seats)
                        .build())
                .retrieve()
                .toBodilessEntity();
        return "Reserved " + seats + " seat(s) for tour package " + tourPackageId + ".";
    }

    @Tool(description = "Release seats for a tour package.")
    public String releaseSeats(
            @ToolParam(description = "Tour package id.") Long tourPackageId,
            @ToolParam(description = "Number of seats to release.") Integer seats) {
        travelApi.post()
                .uri(uriBuilder -> uriBuilder
                        .path("/api/inventories/release")
                        .queryParam("tourPackageId", tourPackageId)
                        .queryParam("seats", seats)
                        .build())
                .retrieve()
                .toBodilessEntity();
        return "Released " + seats + " seat(s) for tour package " + tourPackageId + ".";
    }

    @Tool(description = "Create a booking. The amount must match tour price times seats booked.")
    public BookingResponse createBooking(@ToolParam(description = "Booking fields.") BookingRequest request) {
        return travelApi.post()
                .uri("/api/bookings")
                .body(request)
                .retrieve()
                .body(BookingResponse.class);
    }

    @Tool(description = "Get a booking by booking id.")
    public BookingResponse getBookingById(@ToolParam(description = "Booking id.") Long bookingId) {
        return travelApi.get()
                .uri("/api/bookings/{id}", bookingId)
                .retrieve()
                .body(BookingResponse.class);
    }

    @Tool(description = "List all bookings for a user.")
    public List<BookingResponse> getBookingsByUserId(@ToolParam(description = "User id.") Long userId) {
        return travelApi.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/api/bookings")
                        .queryParam("userId", userId)
                        .build())
                .retrieve()
                .body(new ParameterizedTypeReference<>() {
                });
    }

    @Tool(description = "Cancel a booking by booking id.")
    public String cancelBooking(@ToolParam(description = "Booking id.") Long bookingId) {
        Long cancelledBookingId = travelApi.put()
                .uri("/api/bookings/{id}/cancel", bookingId)
                .retrieve()
                .body(Long.class);
        return "Cancelled booking " + cancelledBookingId + ".";
    }

    @Tool(description = "Process a payment for a booking.")
    public PaymentResponse processPayment(@ToolParam(description = "Payment fields.") PaymentRequest request) {
        return travelApi.post()
                .uri("/api/payments")
                .body(request)
                .retrieve()
                .body(PaymentResponse.class);
    }

    @Tool(description = "Get payment status and details by booking id.")
    public PaymentResponse getPaymentByBookingId(@ToolParam(description = "Booking id.") Long bookingId) {
        return travelApi.get()
                .uri("/api/payments/{bookingId}", bookingId)
                .retrieve()
                .body(PaymentResponse.class);
    }

    @Tool(description = "Refund the payment for a booking.")
    public String refundPayment(@ToolParam(description = "Booking id.") Long bookingId) {
        return travelApi.put()
                .uri("/api/payments/{bookingId}/refund", bookingId)
                .retrieve()
                .body(String.class);
    }
}
