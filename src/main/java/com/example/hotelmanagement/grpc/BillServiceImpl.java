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

import com.example.hotelmanagement.jpa.Bill;
import com.example.hotelmanagement.jpa.StayBillManager;
import io.grpc.stub.StreamObserver;
import net.devh.boot.grpc.server.service.GrpcService;

import java.util.Collection;
import java.util.NoSuchElementException;

import static com.example.hotelmanagement.grpc.DataUtilities.*;

@GrpcService
public class BillServiceImpl extends BillServiceGrpc.BillServiceImplBase {

    private final StayBillManager stayBillManager;

    public BillServiceImpl(StayBillManager stayBillManager) {
        this.stayBillManager = stayBillManager;
    }

    @Override
    public void createBill(CreateBillRequest request, StreamObserver<BillReply> responseObserver) {
        long guestId = request.getGuestId();
        long hotelId = request.getHotelId();
        Collection<Long> stayIds = request.getStayIdsList();
        try {
            Bill bill = stayBillManager.createBill(guestId, hotelId, stayIds);
            BillReply response = replyFromBill(bill);
            responseObserver.onNext(response);
            responseObserver.onCompleted();
        } catch (NoSuchElementException e) {
            responseObserver.onError(handleNoSuchElementException(e));
        } catch (IllegalArgumentException e) {
            responseObserver.onError(handleIllegalArgumentException(e));
        }
    }

    @Override
    public void readBill(IdBillRequest request, StreamObserver<BillReply> responseObserver) {
        long id = request.getId();
        try {
            Bill bill = stayBillManager.readBill(id);
            BillReply response = replyFromBill(bill);
            responseObserver.onNext(response);
            responseObserver.onCompleted();
        } catch (NoSuchElementException e) {
            responseObserver.onError(handleNoSuchElementException(e));
        }
    }

    @Override
    public void deleteBill(IdBillRequest request, StreamObserver<DeleteBillReply> responseObserver) {
        long id = request.getId();
        stayBillManager.deleteBill(id);
        DeleteBillReply response = DeleteBillReply.newBuilder().build();
        responseObserver.onNext(response);
        responseObserver.onCompleted();
    }
}
