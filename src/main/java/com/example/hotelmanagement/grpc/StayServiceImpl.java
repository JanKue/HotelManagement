package com.example.hotelmanagement.grpc;

import com.example.hotelmanagement.jpa.Stay;
import com.example.hotelmanagement.jpa.StayBillManager;
import io.grpc.stub.StreamObserver;
import net.devh.boot.grpc.server.service.GrpcService;

import java.time.LocalDate;
import java.util.NoSuchElementException;

import static com.example.hotelmanagement.grpc.DataUtilities.*;

@GrpcService
public class StayServiceImpl extends StayServiceGrpc.StayServiceImplBase {

    private final StayBillManager stayBillManager;

    public StayServiceImpl(StayBillManager stayBillManager) {
        this.stayBillManager = stayBillManager;
    }

    @Override
    public void createStay(CreateStayRequest request, StreamObserver<StayReply> responseObserver) {
        long guestId = request.getGuestId();
        long hotelId = request.getHotelId();
        LocalDate beginDate = convertFromCalendarDate(request.getBeginDate());
        LocalDate endDate = convertFromCalendarDate(request.getEndDate());
        try {
            Stay stay = stayBillManager.createStay(guestId, hotelId, beginDate, endDate);
            StayReply response = replyFromStay(stay);
            responseObserver.onNext(response);
            responseObserver.onCompleted();
        } catch (NoSuchElementException e) {
            responseObserver.onError(handleNoSuchElementException(e));
        }
    }

    @Override
    public void readStay(IdStayRequest request, StreamObserver<StayReply> responseObserver) {
        long id = request.getId();
        try {
            Stay stay = stayBillManager.readStay(id);
            StayReply response = replyFromStay(stay);
            responseObserver.onNext(response);
            responseObserver.onCompleted();
        } catch (NoSuchElementException e) {
            responseObserver.onError(handleNoSuchElementException(e));
        }
    }

    @Override
    public void updateStay(UpdateStayRequest request, StreamObserver<StayReply> responseObserver) {
        long id = request.getId();
        LocalDate beginDate = convertFromCalendarDate(request.getBeginDate());
        LocalDate endDate = convertFromCalendarDate(request.getEndDate());
        try {
            Stay stay = stayBillManager.updateStay(id, beginDate, endDate);
            StayReply response = replyFromStay(stay);
            responseObserver.onNext(response);
            responseObserver.onCompleted();
        }  catch (NoSuchElementException e) {
            responseObserver.onError(handleNoSuchElementException(e));
        }
    }

    @Override
    public void deleteStay(IdStayRequest request, StreamObserver<DeleteStayReply> responseObserver) {
        long id = request.getId();
        stayBillManager.deleteStay(id);
        DeleteStayReply response = DeleteStayReply.newBuilder().build();
        responseObserver.onNext(response);
        responseObserver.onCompleted();
    }
}
