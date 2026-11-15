package com.example.hotelmanagement;

import com.example.hotelmanagement.jpa.*;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
public class HotelAndGuestManagerTests {

    @Autowired
    private HotelManager hotelManager;
    @Autowired
    private GuestManager guestManager;

    @Test
    @Transactional
    public void registerGuestsTest() {
        // create hotel and guests
        Hotel hotel = hotelManager.createHotel("Hobbiton Public Hotel");
        Guest guest1 = guestManager.createGuest("Bilbo Baggins", LocalDate.now());
        Guest guest2 = guestManager.createGuest("Frodo Baggins", LocalDate.now());

        // register guest1 and test for presence
        hotelManager.registerGuest(hotel.getId(), guest1.getId());
        assertTrue(hotelManager.getRegisteredGuests(hotel.getId()).contains(guest1));
        assertTrue(guestManager.getHotelsRegisteredAt(guest1.getId()).contains(hotel));
        // verify that guest2 is not registered yet
        assertFalse(hotelManager.getRegisteredGuests(hotel.getId()).contains(guest2));
        assertFalse(guestManager.getHotelsRegisteredAt(guest2.getId()).contains(hotel));

        // register guest2 and verify presence
        hotelManager.registerGuest(hotel.getId(), guest2.getId());
        assertTrue(hotelManager.getRegisteredGuests(hotel.getId()).contains(guest2));
        assertTrue(guestManager.getHotelsRegisteredAt(guest2.getId()).contains(hotel));
    }

    @Test
    @Transactional
    public void deleteHotelWithGuestTest() {
        // create hotel and register guest
        Hotel hotel = hotelManager.createHotel("Hobbiton Public Hotel");
        Guest guest = guestManager.createGuest("Bilbo Baggins", LocalDate.now());
        hotelManager.registerGuest(hotel.getId(), guest.getId());
        assertTrue(hotelManager.getRegisteredGuests(hotel.getId()).contains(guest));

        // delete hotel and verify that guest is NOT deleted
        hotelManager.deleteHotel(hotel.getId());
        assertDoesNotThrow(() -> guestManager.readGuest(guest.getId()));
    }
}
