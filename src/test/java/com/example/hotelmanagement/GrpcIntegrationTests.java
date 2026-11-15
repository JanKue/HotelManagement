package com.example.hotelmanagement;

import com.example.hotelmanagement.grpc.*;
import net.devh.boot.grpc.client.inject.GrpcClient;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;

import java.util.Collection;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(properties = {
        "grpc.server.inProcessName=test",
        "grpc.server.port=-1",
        "grpc.client.inProcess.address=in-process:test"
})
@DirtiesContext
public class GrpcIntegrationTests {

    @GrpcClient("inProcess")
    private HotelServiceGrpc.HotelServiceBlockingStub hotelServiceStub;

    @GrpcClient("inProcess")
    private GuestServiceGrpc.GuestServiceBlockingStub guestServiceStub;

    @GrpcClient("inProcess")
    private StayServiceGrpc.StayServiceBlockingStub stayServiceStub;

    @GrpcClient("inProcess")
    private BillServiceGrpc.BillServiceBlockingStub billServiceStub;

    @Test
    @DirtiesContext
    public void createAndUpdateHotelTest() {
        // Create a hotel
        CreateHotelRequest createRequest = CreateHotelRequest.newBuilder()
                .setName("Hobbiton Grand Hotel")
                .build();
        HotelReply createReply = hotelServiceStub.createHotel(createRequest);
        assertNotNull(createReply);
        assertEquals("Hobbiton Grand Hotel", createReply.getName());

        // Update the hotel name
        long hotelId = createReply.getId();
        UpdateHotelRequest updateRequest = UpdateHotelRequest.newBuilder()
                .setId(hotelId)
                .setName("Rivendell Central Hotel")
                .build();
        HotelReply updateReply = hotelServiceStub.updateHotel(updateRequest);

        assertNotNull(updateReply);
        assertEquals("Rivendell Central Hotel", updateReply.getName());
    }

    @Test
    @DirtiesContext
    public void updateMissingHotelTest() {
        UpdateHotelRequest request = UpdateHotelRequest.newBuilder()
                .setId(1L)
                .setName("Hobbiton Grand Hotel")
                .build();

        assertThrows(RuntimeException.class, () -> hotelServiceStub.updateHotel(request));
    }

    @Test
    @DirtiesContext
    public void createAndDeleteGuestTest() {
        // Create a guest
        CalendarDate testDOB = CalendarDate.newBuilder()
                .setYear(1980).setMonth(2).setDay(10).build();
        CreateGuestRequest createRequest = CreateGuestRequest.newBuilder()
                .setName("Bilbo Baggins")
                .setDateOfBirth(testDOB)
                .build();
        GuestReply createReply = guestServiceStub.createGuest(createRequest);

        assertNotNull(createReply);
        assertEquals("Bilbo Baggins", createReply.getName());
        assertEquals(testDOB, createReply.getDateOfBirth());

        // Delete the guest
        IdGuestRequest idRequest = IdGuestRequest.newBuilder()
                .setId(createReply.getId()).build();
        DeleteGuestReply deleteReply = guestServiceStub.deleteGuest(idRequest);
        assertNotNull(deleteReply);

        // Check that the guest no longer exists
        assertThrows(RuntimeException.class, () -> guestServiceStub.readGuest(idRequest));
    }

    @Test
    @DirtiesContext
    public void registerGuestToHotelTest() {
        // Create a hotel
        CreateHotelRequest createHotelRequest = CreateHotelRequest.newBuilder()
                .setName("Hobbiton Grand Hotel")
                .build();
        HotelReply createHotelReply = hotelServiceStub.createHotel(createHotelRequest);
        long hotelId = createHotelReply.getId();
        // Create a guest
        CalendarDate testDOB = CalendarDate.newBuilder()
                .setYear(1980).setMonth(2).setDay(10).build();
        CreateGuestRequest createGuestRequest = CreateGuestRequest.newBuilder()
                .setName("Bilbo Baggins")
                .setDateOfBirth(testDOB)
                .build();
        GuestReply guestReply = guestServiceStub.createGuest(createGuestRequest);
        long guestId = guestReply.getId();

        // Register guest at hotel
        RegisterGuestRequest registerGuestRequest = RegisterGuestRequest.newBuilder()
                .setHotelId(hotelId)
                .setGuestId(guestId)
                .build();
        assertDoesNotThrow(() -> hotelServiceStub.registerGuest(registerGuestRequest));
        // Verify that the guest is registered
        IdHotelRequest idHotelRequest = IdHotelRequest.newBuilder()
                .setId(hotelId).build();
        ListGuestsReply listGuestsReply = hotelServiceStub.getRegisteredGuests(idHotelRequest);
        assertNotNull(listGuestsReply);
        assertTrue(listGuestsReply.getGuestsList().contains(guestReply));
        // Verify that the registration is also reflected in the guest
        IdGuestRequest idGuestRequest = IdGuestRequest.newBuilder()
                .setId(guestId).build();
        ListHotelsReply listHotelsReply = guestServiceStub.getHotelsRegisteredAt(idGuestRequest);
        assertNotNull(listHotelsReply);
        assertTrue(listHotelsReply.getHotelsList().contains(createHotelReply));
    }

    @Test
    @DirtiesContext
    public void registerMissingGuestTest() {
        // Create a hotel
        CreateHotelRequest createHotelRequest = CreateHotelRequest.newBuilder()
                .setName("Hobbiton Grand Hotel")
                .build();
        HotelReply createHotelReply = hotelServiceStub.createHotel(createHotelRequest);
        long hotelId = createHotelReply.getId();
        long guestId = -1L; // non-existent guest

        // Attempt to register guest
        RegisterGuestRequest registerGuestRequest = RegisterGuestRequest.newBuilder()
                .setHotelId(hotelId)
                .setGuestId(guestId)
                .build();
        assertThrows(RuntimeException.class, () -> hotelServiceStub.registerGuest(registerGuestRequest));
    }

    @Test
    @DirtiesContext
    public void createAndQueryStayTest() {
        // Create a hotel
        HotelReply hotel = hotelServiceStub.createHotel(
                CreateHotelRequest.newBuilder()
                        .setName("Hobbiton Grand Hotel")
                        .build()
        );
        long hotelId = hotel.getId();
        // Create a guest
        CalendarDate testDOB = CalendarDate.newBuilder()
                .setYear(1980).setMonth(2).setDay(10).build();
        GuestReply guest = guestServiceStub.createGuest(
                CreateGuestRequest.newBuilder()
                        .setName("Bilbo Baggins")
                        .setDateOfBirth(testDOB)
                        .build()
        );
        long guestId = guest.getId();
        // Create stay
        CalendarDate beginDate = CalendarDate.newBuilder()
                .setYear(2020).setMonth(3).setDay(10).build();
        CalendarDate endDate = CalendarDate.newBuilder()
                .setYear(2020).setMonth(3).setDay(23).build(); // 13 days later
        StayReply stay = stayServiceStub.createStay(
                CreateStayRequest.newBuilder()
                        .setGuestId(guestId).setHotelId(hotelId)
                        .setBeginDate(beginDate).setEndDate(endDate)
                        .build()
        );

        assertNotNull(stay);
        assertEquals(beginDate, stay.getBeginDate());
        assertEquals(endDate, stay.getEndDate());
        assertEquals(13, stay.getDuration());

        // Query stay via the guest
        ListStaysReply listStaysReply = guestServiceStub.getStays(
                IdGuestRequest.newBuilder().setId(guestId).build()
        );
        assertEquals(guestId, listStaysReply.getId());
        Collection<StayReply> guestStays = listStaysReply.getStaysList();
        assertNotNull(guestStays);
        assertTrue(guestStays.contains(stay));
    }

    @Test
    @DirtiesContext
    public void createAndQueryBillTest() {
        // Create a hotel
        HotelReply hotel = hotelServiceStub.createHotel(
                CreateHotelRequest.newBuilder()
                        .setName("Hobbiton Grand Hotel")
                        .build()
        );
        long hotelId = hotel.getId();
        // Create a guest
        CalendarDate testDOB = CalendarDate.newBuilder()
                .setYear(1980).setMonth(2).setDay(10).build();
        GuestReply guest = guestServiceStub.createGuest(
                CreateGuestRequest.newBuilder()
                        .setName("Bilbo Baggins")
                        .setDateOfBirth(testDOB)
                        .build()
        );
        long guestId = guest.getId();
        // Create multiple stays
        CalendarDate beginDate1 = CalendarDate.newBuilder()
                .setYear(2020).setMonth(4).setDay(10).build();
        CalendarDate endDate1 = CalendarDate.newBuilder()
                .setYear(2020).setMonth(4).setDay(23).build(); // 13 days later
        StayReply stay1 = stayServiceStub.createStay(
                CreateStayRequest.newBuilder()
                        .setGuestId(guestId).setHotelId(hotelId)
                        .setBeginDate(beginDate1).setEndDate(endDate1)
                        .build()
        );
        CalendarDate beginDate2 = CalendarDate.newBuilder()
                .setYear(2022).setMonth(10).setDay(30).build();
        CalendarDate endDate2 = CalendarDate.newBuilder()
                .setYear(2022).setMonth(11).setDay(5).build(); // 6 days later
        StayReply stay2 = stayServiceStub.createStay(
                CreateStayRequest.newBuilder()
                        .setGuestId(guestId).setHotelId(hotelId)
                        .setBeginDate(beginDate2).setEndDate(endDate2)
                        .build()
        );
        // Create bill
        BillReply bill = billServiceStub.createBill(
                CreateBillRequest.newBuilder()
                        .setGuestId(guestId).setHotelId(hotelId)
                        .addStayIds(stay1.getId())
                        .addStayIds(stay2.getId())
                        .build()
        );
        assertNotNull(bill);
        assertEquals(1900.0, bill.getBillAmount());
        // Query stays and bills via the guest and hotel
        IdGuestHotelRequest idGuestHotelRequest = IdGuestHotelRequest.newBuilder()
                .setGuestId(guestId).setHotelId(hotelId)
                .build();
        ListStaysReply listStays = guestServiceStub.getStaysAtHotel(idGuestHotelRequest);
        assertTrue(listStays.getStaysList().contains(stay1));
        assertTrue(listStays.getStaysList().contains(stay2));
        ListBillsReply listBillsReply = guestServiceStub.getBillsAtHotel(idGuestHotelRequest);
        assertTrue(listBillsReply.getBillsList().contains(bill));
    }

    @Test
    @DirtiesContext
    public void queryGuestBalanceTest() {
        // Create guest
        CalendarDate testDOB = CalendarDate.newBuilder()
                .setYear(1980).setMonth(3).setDay(10).build();
        GuestReply guest = guestServiceStub.createGuest(
                CreateGuestRequest.newBuilder()
                        .setName("Bilbo Baggins")
                        .setDateOfBirth(testDOB)
                        .build()
        );
        long guestId = guest.getId();
        // Create two hotels
        HotelReply hotel1 = hotelServiceStub.createHotel(
                CreateHotelRequest.newBuilder()
                        .setName("Hobbiton Grand Hotel")
                        .build()
        );
        long hotelId1 = hotel1.getId();
        HotelReply hotel2 = hotelServiceStub.createHotel(
                CreateHotelRequest.newBuilder()
                        .setName("Rivendell Motel")
                        .build()
        );
        long hotelId2 = hotel2.getId();
        // Create one stay at each hotel
        CalendarDate beginDate1 = CalendarDate.newBuilder()
                .setYear(2020).setMonth(4).setDay(10).build();
        CalendarDate endDate1 = CalendarDate.newBuilder()
                .setYear(2020).setMonth(4).setDay(23).build(); // 13 days later
        StayReply stay1 = stayServiceStub.createStay(
                CreateStayRequest.newBuilder()
                        .setGuestId(guestId).setHotelId(hotelId1)
                        .setBeginDate(beginDate1).setEndDate(endDate1)
                        .build()
        );
        CalendarDate beginDate2 = CalendarDate.newBuilder()
                .setYear(2022).setMonth(10).setDay(30).build();
        CalendarDate endDate2 = CalendarDate.newBuilder()
                .setYear(2022).setMonth(11).setDay(5).build(); // 6 days later
        StayReply stay2 = stayServiceStub.createStay(
                CreateStayRequest.newBuilder()
                        .setGuestId(guestId).setHotelId(hotelId2)
                        .setBeginDate(beginDate2).setEndDate(endDate2)
                        .build()
        );
        // Create separate bills for each stay
        BillReply bill1 = billServiceStub.createBill(
                CreateBillRequest.newBuilder()
                        .setGuestId(guestId).setHotelId(hotelId1)
                        .addStayIds(stay1.getId())
                        .build()
        );
        BillReply bill2 = billServiceStub.createBill(
                CreateBillRequest.newBuilder()
                        .setGuestId(guestId).setHotelId(hotelId2)
                        .addStayIds(stay2.getId())
                        .build()
        );
        // Verify the guest's total balance and hotel-specific balances
        GuestBalanceReply totalBalance = guestServiceStub.getBalance(
                IdGuestRequest.newBuilder().setId(guestId).build()
        );
        assertEquals(1900.0, totalBalance.getBalance());
        GuestBalanceReply balance1 = guestServiceStub.getBalanceAtHotel(
                IdGuestHotelRequest.newBuilder()
                        .setGuestId(guestId).setHotelId(hotelId1)
                        .build()
        );
        assertEquals(1300.0, balance1.getBalance());
        GuestBalanceReply balance2 = guestServiceStub.getBalanceAtHotel(
                IdGuestHotelRequest.newBuilder()
                        .setGuestId(guestId).setHotelId(hotelId2)
                        .build()
        );
        assertEquals(600.0, balance2.getBalance());
    }

    @Test
    @DirtiesContext
    public void createEmptyBillTest() {
        // Create a hotel
        HotelReply hotel = hotelServiceStub.createHotel(
                CreateHotelRequest.newBuilder()
                        .setName("Hobbiton Grand Hotel")
                        .build()
        );
        long hotelId = hotel.getId();
        // Create a guest
        CalendarDate testDOB = CalendarDate.newBuilder()
                .setYear(1980).setMonth(2).setDay(10).build();
        GuestReply guest = guestServiceStub.createGuest(
                CreateGuestRequest.newBuilder()
                        .setName("Bilbo Baggins")
                        .setDateOfBirth(testDOB)
                        .build()
        );
        long guestId = guest.getId();
        // Create a bill with no stays
        BillReply bill = billServiceStub.createBill(
                CreateBillRequest.newBuilder()
                        .setGuestId(guestId).setHotelId(hotelId)
                        .build()
        );
        assertNotNull(bill);
        assertEquals(0.0, bill.getBillAmount());
    }

    @Test
    @DirtiesContext
    public void deleteStayTest() {
        // Create a hotel
        HotelReply hotel = hotelServiceStub.createHotel(
                CreateHotelRequest.newBuilder()
                        .setName("Hobbiton Grand Hotel")
                        .build()
        );
        long hotelId = hotel.getId();
        // Create a guest
        CalendarDate testDOB = CalendarDate.newBuilder()
                .setYear(1980).setMonth(2).setDay(10).build();
        GuestReply guest = guestServiceStub.createGuest(
                CreateGuestRequest.newBuilder()
                        .setName("Bilbo Baggins")
                        .setDateOfBirth(testDOB)
                        .build()
        );
        long guestId = guest.getId();
        // Create stay
        CalendarDate beginDate = CalendarDate.newBuilder()
                .setYear(2020).setMonth(3).setDay(10).build();
        CalendarDate endDate = CalendarDate.newBuilder()
                .setYear(2020).setMonth(3).setDay(23).build();
        StayReply stay = stayServiceStub.createStay(
                CreateStayRequest.newBuilder()
                        .setGuestId(guestId).setHotelId(hotelId)
                        .setBeginDate(beginDate).setEndDate(endDate)
                        .build()
        );
        long stayId = stay.getId();
        // Create bill that references the stay
        BillReply bill = billServiceStub.createBill(
                CreateBillRequest.newBuilder()
                        .setGuestId(guestId).setHotelId(hotelId)
                        .addStayIds(stay.getId())
                        .build()
        );
        long billId = bill.getId();
        // Delete stay
        IdStayRequest idStayRequest = IdStayRequest.newBuilder().setId(stayId).build();
        stayServiceStub.deleteStay(idStayRequest);
        // Verify the stay is gone, but guest and hotel still exist
        assertThrows(RuntimeException.class, () -> stayServiceStub.readStay(idStayRequest));
        assertDoesNotThrow(() -> guestServiceStub.readGuest(
                IdGuestRequest.newBuilder().setId(guestId).build()
        ));
        assertDoesNotThrow(() -> hotelServiceStub.readHotel(
                IdHotelRequest.newBuilder().setId(hotelId).build()
        ));
    }

    @Test
    @DirtiesContext
    public void deleteBillTest() {
        // Create a hotel
        HotelReply hotel = hotelServiceStub.createHotel(
                CreateHotelRequest.newBuilder()
                        .setName("Hobbiton Grand Hotel")
                        .build()
        );
        long hotelId = hotel.getId();
        // Create a guest
        CalendarDate testDOB = CalendarDate.newBuilder()
                .setYear(1980).setMonth(2).setDay(10).build();
        GuestReply guest = guestServiceStub.createGuest(
                CreateGuestRequest.newBuilder()
                        .setName("Bilbo Baggins")
                        .setDateOfBirth(testDOB)
                        .build()
        );
        long guestId = guest.getId();
        // Create multiple stays
        CalendarDate beginDate1 = CalendarDate.newBuilder()
                .setYear(2020).setMonth(3).setDay(10).build();
        CalendarDate endDate1 = CalendarDate.newBuilder()
                .setYear(2020).setMonth(3).setDay(23).build();
        StayReply stay1 = stayServiceStub.createStay(
                CreateStayRequest.newBuilder()
                        .setGuestId(guestId).setHotelId(hotelId)
                        .setBeginDate(beginDate1).setEndDate(endDate1)
                        .build()
        );
        long stayId1 = stay1.getId();
        CalendarDate beginDate2 = CalendarDate.newBuilder()
                .setYear(2022).setMonth(9).setDay(30).build();
        CalendarDate endDate2 = CalendarDate.newBuilder()
                .setYear(2022).setMonth(10).setDay(5).build();
        StayReply stay2 = stayServiceStub.createStay(
                CreateStayRequest.newBuilder()
                        .setGuestId(guestId).setHotelId(hotelId)
                        .setBeginDate(beginDate2).setEndDate(endDate2)
                        .build()
        );
        long stayId2 = stay2.getId();
        // Create bill
        BillReply bill = billServiceStub.createBill(
                CreateBillRequest.newBuilder()
                        .setGuestId(guestId).setHotelId(hotelId)
                        .addStayIds(stay1.getId())
                        .addStayIds(stay2.getId())
                        .build()
        );
        long billId = bill.getId();
        // Delete bill
        IdBillRequest idBillRequest = IdBillRequest.newBuilder().setId(billId).build();
        billServiceStub.deleteBill(idBillRequest);
        // Verify that the bill is deleted, but stays still exist
        assertThrows(RuntimeException.class, () -> billServiceStub.readBill(idBillRequest));
        assertDoesNotThrow(() -> stayServiceStub.readStay(IdStayRequest.newBuilder().setId(stayId1).build()));
        assertDoesNotThrow(() -> stayServiceStub.readStay(IdStayRequest.newBuilder().setId(stayId2).build()));
    }

    @Test
    @DirtiesContext
    public void deleteStayWithBillTest() {
        // Create a hotel
        HotelReply hotel = hotelServiceStub.createHotel(
                CreateHotelRequest.newBuilder()
                        .setName("Hobbiton Grand Hotel")
                        .build()
        );
        long hotelId = hotel.getId();
        // Create a guest
        CalendarDate testDOB = CalendarDate.newBuilder()
                .setYear(1980).setMonth(2).setDay(10).build();
        GuestReply guest = guestServiceStub.createGuest(
                CreateGuestRequest.newBuilder()
                        .setName("Bilbo Baggins")
                        .setDateOfBirth(testDOB)
                        .build()
        );
        long guestId = guest.getId();
        // Create multiple stays
        CalendarDate beginDate1 = CalendarDate.newBuilder()
                .setYear(2020).setMonth(3).setDay(10).build();
        CalendarDate endDate1 = CalendarDate.newBuilder()
                .setYear(2020).setMonth(3).setDay(23).build();
        StayReply stay1 = stayServiceStub.createStay(
                CreateStayRequest.newBuilder()
                        .setGuestId(guestId).setHotelId(hotelId)
                        .setBeginDate(beginDate1).setEndDate(endDate1)
                        .build()
        );
        long stayId1 = stay1.getId();
        CalendarDate beginDate2 = CalendarDate.newBuilder()
                .setYear(2022).setMonth(9).setDay(30).build();
        CalendarDate endDate2 = CalendarDate.newBuilder()
                .setYear(2022).setMonth(10).setDay(5).build();
        StayReply stay2 = stayServiceStub.createStay(
                CreateStayRequest.newBuilder()
                        .setGuestId(guestId).setHotelId(hotelId)
                        .setBeginDate(beginDate2).setEndDate(endDate2)
                        .build()
        );
        long stayId2 = stay2.getId();
        // Create bill that references the stay
        BillReply bill = billServiceStub.createBill(
                CreateBillRequest.newBuilder()
                        .setGuestId(guestId).setHotelId(hotelId)
                        .addStayIds(stayId1)
                        .build()
        );
        long billId = bill.getId();
        // Delete first stay
        IdStayRequest idStayRequest1 = IdStayRequest.newBuilder().setId(stayId1).build();
        stayServiceStub.deleteStay(idStayRequest1);
        // Verify that the first stay and associated bill are both deleted, but second stay still exists
        assertThrows(RuntimeException.class, () -> stayServiceStub.readStay(idStayRequest1));
        assertThrows(RuntimeException.class, () -> billServiceStub.readBill(
                IdBillRequest.newBuilder().setId(billId).build()
        ));
        assertDoesNotThrow(() -> stayServiceStub.readStay(
                IdStayRequest.newBuilder().setId(stayId2).build()
        ));
        // Check that there are now 1 stay and 0 bills linked to the guest
        IdGuestRequest idGuestRequest = IdGuestRequest.newBuilder().setId(guestId).build();
        ListStaysReply guestStays = guestServiceStub.getStays(idGuestRequest);
        assertEquals(1, guestStays.getStaysCount());
        ListBillsReply guestBills = guestServiceStub.getBills(idGuestRequest);
        assertEquals(0, guestBills.getBillsCount());
    }

    @Test
    @DirtiesContext
    public void queryGuestByQuartersTest() {
        // Create a hotel
        HotelReply hotel = hotelServiceStub.createHotel(
                CreateHotelRequest.newBuilder()
                        .setName("Hobbiton Grand Hotel")
                        .build()
        );
        long hotelId = hotel.getId();
        // Create a guest
        CalendarDate testDOB = CalendarDate.newBuilder()
                .setYear(1980).setMonth(3).setDay(10).build();
        GuestReply guest = guestServiceStub.createGuest(
                CreateGuestRequest.newBuilder()
                        .setName("Bilbo Baggins")
                        .setDateOfBirth(testDOB)
                        .build()
        );
        long guestId = guest.getId();
        // Create multiple stays
        CalendarDate beginDate1 = CalendarDate.newBuilder()
                .setYear(2020).setMonth(4).setDay(10).build();
        CalendarDate endDate1 = CalendarDate.newBuilder()
                .setYear(2020).setMonth(4).setDay(23).build();
        // // this stay has 13 days, all in 2020Q2
        StayReply stay1 = stayServiceStub.createStay(
                CreateStayRequest.newBuilder()
                        .setGuestId(guestId).setHotelId(hotelId)
                        .setBeginDate(beginDate1).setEndDate(endDate1)
                        .build()
        );
        CalendarDate beginDate2 = CalendarDate.newBuilder()
                .setYear(2022).setMonth(9).setDay(28).build();
        CalendarDate endDate2 = CalendarDate.newBuilder()
                .setYear(2022).setMonth(10).setDay(6).build();
        // // this stay has 3 days in 2022Q3 (inclusive of the final day) and 5 days in 2022Q4
        StayReply stay2 = stayServiceStub.createStay(
                CreateStayRequest.newBuilder()
                        .setGuestId(guestId).setHotelId(hotelId)
                        .setBeginDate(beginDate2).setEndDate(endDate2)
                        .build()
        );
        // Query the guest for stay durations
        GuestDurationReply quarterDuration1 = guestServiceStub.getStayDurationInQuarter(
                GuestQuarterRequest.newBuilder()
                        .setId(guestId).setYear(2020).setQuarter(2).build()
        );
        assertEquals(13, quarterDuration1.getDuration());
        GuestDurationReply quarterDuration2 = guestServiceStub.getStayDurationInQuarter(
                GuestQuarterRequest.newBuilder()
                        .setId(guestId).setYear(2022).setQuarter(3).build()
        );
        assertEquals(3, quarterDuration2.getDuration());
        GuestDurationReply quarterDuration3 = guestServiceStub.getStayDurationInQuarter(
                GuestQuarterRequest.newBuilder()
                        .setId(guestId).setYear(2022).setQuarter(4).build()
        );
        assertEquals(5, quarterDuration3.getDuration());
    }
}
