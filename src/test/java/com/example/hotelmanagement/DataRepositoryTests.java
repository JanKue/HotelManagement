package com.example.hotelmanagement;

import com.example.hotelmanagement.jpa.*;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
public class DataRepositoryTests {

    @Autowired
    private HotelRepository hotelRepository;
    @Autowired
    private GuestRepository guestRepository;

    @Test
    public void saveHotelTest() {
        Hotel hotel = new Hotel("Hobbiton Grand Hotel");
        hotelRepository.save(hotel);

        assertTrue(hotelRepository.existsById(hotel.getId()));
        assertEquals("Hobbiton Grand Hotel", hotelRepository.findById(hotel.getId()).orElseThrow().getName());
    }

    @Test
    public void saveGuestsTest() {
        Guest guest1 = new Guest("Bilbo Baggins", LocalDate.now());
        Guest guest2 = new Guest("Frodo Baggins", LocalDate.now());
        guestRepository.save(guest1);
        guestRepository.save(guest2);

        Guest found_guest = guestRepository.findById(guest1.getId()).orElseThrow();
        assertEquals("Bilbo Baggins", found_guest.getName());
        assertNotEquals("Frodo Baggins", found_guest.getName());
    }

}
