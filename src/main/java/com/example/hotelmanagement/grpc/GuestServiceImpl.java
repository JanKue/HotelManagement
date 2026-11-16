/*
    Copyright (C) 2026 Jan Küblbeck

    This program is free software: you can redistribute it and/or modify
    it under the terms of the GNU Affero General Public License as published
    by the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    This program is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU Affero General Public License for more details.

    You should have received a copy of the GNU Affero General Public License
    along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package com.example.hotelmanagement.grpc;

import com.example.hotelmanagement.jpa.*;
import io.grpc.stub.StreamObserver;
import net.devh.boot.grpc.server.service.GrpcService;

import java.time.LocalDate;
import java.util.*;

import static com.example.hotelmanagement.grpc.DataUtilities.*;

@GrpcService
public class GuestServiceImpl extends GuestServiceGrpc.GuestServiceImplBase {

    private final GuestManager guestManager;

    public GuestServiceImpl(GuestManager guestManager) {
        this.guestManager = guestManager;
    }

    @Override
    public void createGuest(CreateGuestRequest request, StreamObserver<GuestReply> responseObserver) {
        String name = request.getName();
        LocalDate dateOfBirth = convertFromCalendarDate(request.getDateOfBirth());
        Guest guest = guestManager.createGuest(name, dateOfBirth);
        GuestReply response = replyFromGuest(guest);
        responseObserver.onNext(response);
        responseObserver.onCompleted();
    }

    @Override
    public void readGuest(IdGuestRequest request, StreamObserver<GuestReply> responseObserver) {
        long guestId = request.getId();
        Guest guest = guestManager.readGuest(guestId);
        try {
            GuestReply response = replyFromGuest(guest);
            responseObserver.onNext(response);
            responseObserver.onCompleted();
        } catch (NoSuchElementException e) {
            responseObserver.onError(handleNoSuchElementException(e));
        }
    }

    @Override
    public void updateGuest(UpdateGuestRequest request, StreamObserver<GuestReply> responseObserver) {
        long guestId = request.getId();
        String name = request.getName();
        LocalDate dateOfBirth = convertFromCalendarDate(request.getDateOfBirth());
        try {
            Guest guest = guestManager.updateGuest(guestId, name, dateOfBirth);
            GuestReply response = replyFromGuest(guest);
            responseObserver.onNext(response);
            responseObserver.onCompleted();
        } catch (NoSuchElementException e) {
            responseObserver.onError(handleNoSuchElementException(e));
        }
    }

    @Override
    public void deleteGuest(IdGuestRequest request, StreamObserver<DeleteGuestReply> responseObserver) {
        long guestId = request.getId();
        guestManager.deleteGuest(guestId);
        DeleteGuestReply response = DeleteGuestReply.newBuilder().build();
        responseObserver.onNext(response);
        responseObserver.onCompleted();
    }

    @Override
    public void getHotelsRegisteredAt(IdGuestRequest request, StreamObserver<ListHotelsReply> responseObserver) {
        long guestId = request.getId();
        try {
            Collection<Hotel> hotels = guestManager.getHotelsRegisteredAt(guestId);
            List<HotelReply> hotelReplies = hotels.stream()
                    .map(DataUtilities::replyFromHotel)
                    .toList();

            ListHotelsReply response = ListHotelsReply.newBuilder()
                    .setId(guestId)
                    .addAllHotels(hotelReplies)
                    .build();
            responseObserver.onNext(response);
            responseObserver.onCompleted();
        } catch (NoSuchElementException e) {
            responseObserver.onError(handleNoSuchElementException(e));
        }
    }

    @Override
    public void getStays(IdGuestRequest request, StreamObserver<ListStaysReply> responseObserver) {
        long guestId = request.getId();
        try {
            Collection<Stay> stays = guestManager.getGuestStays(guestId);
            List<StayReply> stayReplies = stays.stream()
                    .map(DataUtilities::replyFromStay)
                    .toList();
            ListStaysReply response = ListStaysReply.newBuilder()
                    .setId(guestId)
                    .addAllStays(stayReplies)
                    .build();
            responseObserver.onNext(response);
            responseObserver.onCompleted();
        } catch (NoSuchElementException e) {
            responseObserver.onError(handleNoSuchElementException(e));
        }
    }

    @Override
    public void getStaysAtHotel(IdGuestHotelRequest request, StreamObserver<ListStaysReply> responseObserver) {
        long guestId = request.getGuestId();
        long hotelId = request.getHotelId();
        try {
            Collection<Stay> stays = guestManager.getGuestStaysAtHotel(guestId, hotelId);
            List<StayReply> stayReplies = stays.stream()
                    .map(DataUtilities::replyFromStay)
                    .toList();
            ListStaysReply response = ListStaysReply.newBuilder()
                    .setId(guestId)
                    .addAllStays(stayReplies)
                    .build();
            responseObserver.onNext(response);
            responseObserver.onCompleted();
        } catch (NoSuchElementException e) {
            responseObserver.onError(handleNoSuchElementException(e));
        }
    }

    @Override
    public void getBills(IdGuestRequest request, StreamObserver<ListBillsReply> responseObserver) {
        long guestId = request.getId();
        try {
            Collection<Bill> bills = guestManager.getGuestBills(guestId);
            List<BillReply> billReplies =  bills.stream()
                    .map(DataUtilities::replyFromBill)
                    .toList();
            ListBillsReply response = ListBillsReply.newBuilder()
                    .setId(guestId)
                    .addAllBills(billReplies)
                    .build();
            responseObserver.onNext(response);
            responseObserver.onCompleted();
        } catch (NoSuchElementException e) {
            responseObserver.onError(handleNoSuchElementException(e));
        }
    }

    @Override
    public void getBillsAtHotel(IdGuestHotelRequest request, StreamObserver<ListBillsReply> responseObserver) {
        long guestId = request.getGuestId();
        long hotelId = request.getHotelId();
        try {
            Collection<Bill> bills = guestManager.getGuestBillsAtHotel(guestId, hotelId);
            List<BillReply> billReplies = bills.stream()
                    .map(DataUtilities::replyFromBill)
                    .toList();
            ListBillsReply response = ListBillsReply.newBuilder()
                    .setId(guestId)
                    .addAllBills(billReplies)
                    .build();
            responseObserver.onNext(response);
            responseObserver.onCompleted();
        } catch (NoSuchElementException e) {
            responseObserver.onError(handleNoSuchElementException(e));
        }
    }

    @Override
    public void getBalance(IdGuestRequest request, StreamObserver<GuestBalanceReply> responseObserver) {
        long guestId = request.getId();
        try {
            double balance = guestManager.getGuestBalance(guestId);
            GuestBalanceReply response = GuestBalanceReply.newBuilder()
                    .setId(guestId)
                    .setBalance(balance)
                    .build();
            responseObserver.onNext(response);
            responseObserver.onCompleted();
        } catch (NoSuchElementException e) {
            responseObserver.onError(handleNoSuchElementException(e));
        }
    }

    @Override
    public void getBalanceAtHotel(IdGuestHotelRequest request, StreamObserver<GuestBalanceReply> responseObserver) {
        long guestId = request.getGuestId();
        long hotelId = request.getHotelId();
        try {
            double balance = guestManager.getGuestBalanceAtHotel(guestId,  hotelId);
            GuestBalanceReply response = GuestBalanceReply.newBuilder()
                    .setId(guestId)
                    .setBalance(balance)
                    .build();
            responseObserver.onNext(response);
            responseObserver.onCompleted();
        } catch (NoSuchElementException e) {
            responseObserver.onError(handleNoSuchElementException(e));
        }
    }

    @Override
    public void getBalanceInQuarter(GuestQuarterRequest request, StreamObserver<GuestBalanceReply> responseObserver) {
        long guestId = request.getId();
        int year = request.getYear();
        int quarter = request.getQuarter();
        try {
            double balance = guestManager.getGuestBalanceInQuarter(guestId, year, quarter);
            GuestBalanceReply response = GuestBalanceReply.newBuilder()
                    .setId(guestId)
                    .setBalance(balance)
                    .build();
            responseObserver.onNext(response);
            responseObserver.onCompleted();
        } catch (NoSuchElementException e) {
            responseObserver.onError(handleNoSuchElementException(e));
        } catch (IllegalArgumentException e) {
            responseObserver.onError(handleIllegalArgumentException(e));
        }
    }

    @Override
    public void getStayDurationInQuarter(GuestQuarterRequest request, StreamObserver<GuestDurationReply> responseObserver) {
        long guestId = request.getId();
        int year = request.getYear();
        int quarter = request.getQuarter();
        try {
            long duration = guestManager.getGuestStayDurationInQuarter(guestId, year, quarter);
            GuestDurationReply response = GuestDurationReply.newBuilder()
                    .setId(guestId)
                    .setDuration(duration)
                    .build();
            responseObserver.onNext(response);
            responseObserver.onCompleted();
        } catch (NoSuchElementException e) {
            responseObserver.onError(handleNoSuchElementException(e));
        } catch (IllegalArgumentException e) {
            responseObserver.onError(handleIllegalArgumentException(e));
        }
    }
}
