package com.example.hotelmanagement;

import com.example.hotelmanagement.jpa.*;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.time.LocalDate;
import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
public class StayBillManagerTests {

    @Autowired
    private HotelManager hotelManager;
    @Autowired
    private GuestManager guestManager;
    @Autowired
    private StayBillManager stayBillManager;

    @Test
    @Transactional
    public void createStayTest() {
        LocalDate today = LocalDate.now();
        Guest guest = guestManager.createGuest("Bilbo Baggins", today);
        Hotel hotel = hotelManager.createHotel("Hobbiton Public Hotel");
        Stay stay = stayBillManager.createStay(guest.getId(), hotel.getId(), today, today);

        assertNotNull(stay);
        assertEquals(guest, stay.getGuest());
        assertEquals(hotel, stay.getHotel());
        assertEquals(stay, stayBillManager.readStay(stay.getId()));
    }

    @Test
    @Transactional
    public void createBillTest() {
        LocalDate today = LocalDate.now();
        Guest guest = guestManager.createGuest("Bilbo Baggins", today);
        Hotel hotel = hotelManager.createHotel("Hobbiton Public Hotel");
        Stay stay1 = stayBillManager.createStay(guest.getId(), hotel.getId(), today, today);
        Stay stay2 = stayBillManager.createStay(guest.getId(), hotel.getId(), today, today);
        Bill bill = stayBillManager.createBill(guest.getId(), hotel.getId(), Arrays.asList(stay1.getId(), stay2.getId()));

        assertNotNull(bill);
        assertEquals(bill, stayBillManager.readBill(bill.getId()));
    }
}
