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
