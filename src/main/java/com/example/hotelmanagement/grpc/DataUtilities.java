package com.example.hotelmanagement.grpc;

import com.example.hotelmanagement.jpa.Bill;
import com.example.hotelmanagement.jpa.Hotel;
import com.example.hotelmanagement.jpa.Guest;
import com.example.hotelmanagement.jpa.Stay;
import io.grpc.Status;
import io.grpc.StatusRuntimeException;

import java.time.LocalDate;
import java.util.NoSuchElementException;

public final class DataUtilities {

    /**
     * Converts from the CalendarDate format defined for gRPC to a Java LocalDate object.
     */
    static LocalDate convertFromCalendarDate(CalendarDate calDate) {
        return LocalDate.of(calDate.getYear(), calDate.getMonth(), calDate.getDay());
    }

    /**
     * Converts from a Java LocalDate object to the CalendarDate format used for gRPC.
     */
    static CalendarDate convertToCalendarDate(LocalDate localDate) {
        return CalendarDate.newBuilder()
                .setYear(localDate.getYear())
                .setMonth(localDate.getMonthValue())
                .setDay(localDate.getDayOfMonth())
                .build();
    }

    /**
     * Encapsulates the process of building a HotelReply object from a Hotel.
     */
    static HotelReply replyFromHotel(Hotel hotel) {
        return HotelReply.newBuilder()
                .setId(hotel.getId())
                .setName(hotel.getName())
                .build();
    }

    /**
     * Encapsulates the process of building a GuestReply object from a Guest.
     */
    static GuestReply replyFromGuest(Guest guest) {
        return GuestReply.newBuilder()
                .setId(guest.getId())
                .setName(guest.getName())
                .setDateOfBirth(convertToCalendarDate(guest.getDateOfBirth()))
                .build();
    }

    /**
     * Encapsulates the process of building a StayReply object from a Stay.
     */
    static StayReply replyFromStay(Stay stay) {
        return StayReply.newBuilder()
                .setId(stay.getId())
                .setGuestId(stay.getGuest().getId())
                .setHotelId(stay.getHotel().getId())
                .setBeginDate(convertToCalendarDate(stay.getBeginDate()))
                .setEndDate(convertToCalendarDate(stay.getEndDate()))
                .setDuration(stay.getStayDuration())
                .build();
    }

    /**
     * Encapsulates the process of building a BillReply object from a Bill.
     */
    static BillReply replyFromBill(Bill bill) {
        return BillReply.newBuilder()
                .setId(bill.getId())
                .setGuestId(bill.getGuest().getId())
                .setHotelId(bill.getHotel().getId())
                .setBillAmount(bill.getTotalBilledAmount())
                .build();
    }

    /**
     * Transforms NoSuchElementException into a StatusRuntimeException that can be passed to gRPC StreamObserver.
     */
    static StatusRuntimeException handleNoSuchElementException(NoSuchElementException e) {
        return Status.NOT_FOUND
                .withDescription(e.getMessage())
                .asRuntimeException();
    }

    /**
     * Transforms IllegalArgumentException into a StatusRuntimeException that can be passed to gRPC StreamObserver.
     */
    static StatusRuntimeException handleIllegalArgumentException(IllegalArgumentException e) {
        return Status.INVALID_ARGUMENT
                .withDescription(e.getMessage())
                .asRuntimeException();
    }
}
