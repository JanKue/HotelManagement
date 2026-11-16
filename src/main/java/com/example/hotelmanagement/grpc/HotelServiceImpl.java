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

import com.example.hotelmanagement.jpa.Hotel;
import com.example.hotelmanagement.jpa.HotelManager;
import com.example.hotelmanagement.jpa.Guest;
import io.grpc.stub.StreamObserver;
import net.devh.boot.grpc.server.service.GrpcService;

import java.util.Collection;
import java.util.List;
import java.util.NoSuchElementException;

import static com.example.hotelmanagement.grpc.DataUtilities.*;

@GrpcService
public class HotelServiceImpl extends HotelServiceGrpc.HotelServiceImplBase {

    private final HotelManager hotelManager;

    public HotelServiceImpl(HotelManager hotelManager) {
        this.hotelManager = hotelManager;
    }

    @Override
    public void createHotel(CreateHotelRequest request, StreamObserver<HotelReply> responseObserver) {
        String name = request.getName();
        Hotel Hotel = hotelManager.createHotel(name);
        HotelReply response = replyFromHotel(Hotel);
        responseObserver.onNext(response);
        responseObserver.onCompleted();
    }

    @Override
    public void readHotel(IdHotelRequest request, StreamObserver<HotelReply> responseObserver) {
        long HotelId = request.getId();
        Hotel Hotel = hotelManager.readHotel(HotelId);
        try {
            HotelReply response = replyFromHotel(Hotel);
            responseObserver.onNext(response);
            responseObserver.onCompleted();
        } catch (NoSuchElementException e) {
            responseObserver.onError(handleNoSuchElementException(e));
        }
    }

    @Override
    public void updateHotel(UpdateHotelRequest request, StreamObserver<HotelReply> responseObserver) {
        long HotelId = request.getId();
        String name = request.getName();
        try {
            Hotel Hotel = hotelManager.updateHotel(HotelId, name);
            HotelReply response = replyFromHotel(Hotel);
            responseObserver.onNext(response);
            responseObserver.onCompleted();
        } catch (NoSuchElementException e) {
            responseObserver.onError(handleNoSuchElementException(e));
        }
    }

    @Override
    public void deleteHotel(IdHotelRequest request, StreamObserver<DeleteHotelReply> responseObserver) {
        long HotelId = request.getId();
        hotelManager.deleteHotel(HotelId);
        DeleteHotelReply response = DeleteHotelReply.newBuilder().build();
        responseObserver.onNext(response);
        responseObserver.onCompleted();
    }

    @Override
    public void registerGuest(RegisterGuestRequest request, StreamObserver<HotelReply> responseObserver) {
        long HotelId = request.getHotelId();
        long guestId = request.getGuestId();
        try {
            Hotel Hotel = hotelManager.registerGuest(HotelId, guestId);
            HotelReply response = replyFromHotel(Hotel);
            responseObserver.onNext(response);
            responseObserver.onCompleted();
        } catch (NoSuchElementException e) {
            responseObserver.onError(handleNoSuchElementException(e));
        }
    }

    @Override
    public void getRegisteredGuests(IdHotelRequest request, StreamObserver<ListGuestsReply> responseObserver) {
        long HotelId = request.getId();
        try {
            Collection<Guest> guests = hotelManager.getRegisteredGuests(HotelId);
            List<GuestReply> guestReplies = guests.stream()
                    .map(DataUtilities::replyFromGuest)
                    .toList();

            ListGuestsReply response = ListGuestsReply.newBuilder()
                    .setId(HotelId)
                    .addAllGuests(guestReplies)
                    .build();
            responseObserver.onNext(response);
            responseObserver.onCompleted();
        } catch (NoSuchElementException e) {
            responseObserver.onError(handleNoSuchElementException(e));
        }
    }
}
