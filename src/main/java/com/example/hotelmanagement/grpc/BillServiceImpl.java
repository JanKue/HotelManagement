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
