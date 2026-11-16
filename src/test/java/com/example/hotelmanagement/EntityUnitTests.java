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

import java.time.LocalDate;
import java.time.Year;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

public class EntityUnitTests {

    @Test
    public void createHotelTest() {
        Hotel hotel = new Hotel("Hobbiton Public Hotel");

        assertNotNull(hotel);
        assertEquals("Hobbiton Public Hotel", hotel.getName());
    }

    @Test
    public void createGuestTest() {
        int currentYear = Year.now().getValue();
        LocalDate dateOfBirth = LocalDate.of(currentYear - 111, 1, 1);
        Guest guest = new Guest("Bilbo Baggins", dateOfBirth, "Bag End, Hobbiton, The Shire");

        assertNotNull(guest);
        assertEquals("Bilbo Baggins", guest.getName());
        assertEquals(111, guest.getAge());
        assertEquals("Bag End, Hobbiton, The Shire", guest.getAddress());
    }

    @Test
    public void registerGuestTest() {
        // Setup entities
        Hotel hotel = new Hotel("Hobbiton Public Hotel");
        Guest guest1 = new Guest("Bilbo Baggins", LocalDate.now());
        Guest guest2 = new Guest("Frodo Baggins", LocalDate.now());

        // Register guest 1 and verify registration
        hotel.registerGuest(guest1);
        assertTrue(hotel.getRegisteredGuests().contains(guest1));
        assertTrue(guest1.getHotelsRegisteredAt().contains(hotel));
        // Check that guest 2 is not registered yet
        assertFalse(hotel.getRegisteredGuests().contains(guest2));
        assertFalse(guest2.getHotelsRegisteredAt().contains(hotel));
        // Register guest 2 and verify
        hotel.registerGuest(guest2);
        assertTrue(hotel.getRegisteredGuests().contains(guest2));
        assertTrue(guest2.getHotelsRegisteredAt().contains(hotel));
    }

    @Test
    public void createStayTest() {
        Guest guest = new Guest("Bilbo Baggins");
        Hotel hotel = new Hotel("Hobbiton Public Hotel");
        LocalDate today = LocalDate.now();
        Stay stay = new Stay(guest, hotel, today);
        assertNotNull(stay);
        assertEquals(guest, stay.getGuest());
        assertEquals(hotel, stay.getHotel());
        assertEquals(stay.getBeginDate(), today);
        assertNull(stay.getEndDate());
        assertTrue(guest.getStays().contains(stay));
        assertTrue(hotel.getStays().contains(stay));
    }

    @Test
    public void invalidStayDatesTest() {
        Guest guest = new Guest("Bilbo Baggins");
        Hotel hotel = new Hotel("Bilbo Baggins");
        LocalDate today = LocalDate.now();
        LocalDate oneWeekAgo = today.minusDays(7);
        LocalDate twoWeeksAgo =  today.minusDays(14);
        // Test various configurations
        assertThrows(IllegalArgumentException.class, () -> new Stay(guest, hotel, today, oneWeekAgo)); // constructor, endDate before beginDate
        Stay validStay = new Stay(guest, hotel, oneWeekAgo, today);
        assertThrows(IllegalArgumentException.class, () -> validStay.setEndDate(twoWeeksAgo)); // setEndDate value before beginDate
        assertDoesNotThrow(() -> validStay.setEndDate(oneWeekAgo)); // endDate equal to beginDate is valid however
        assertThrows(IllegalArgumentException.class, () -> validStay.setBeginDate(today)); // setBeginDate value after endDate
    }

    @Test
    public void stayDurationTest() {
        Guest guest = new Guest("Bilbo Baggins");
        Hotel hotel = new Hotel("Hobbiton Public Hotel");
        LocalDate today = LocalDate.now();
        // for the start date, subtract 7 days from today
        LocalDate oneWeekAgo = today.minusDays(7);
        Stay stay = new Stay(guest, hotel, oneWeekAgo, today);
        // check that the duration is 7 days
        assertEquals(7, stay.getStayDuration());
    }

    @Test
    public void createBillTest() {
        Guest guest =  new Guest("Bilbo Baggins");
        Hotel hotel =  new Hotel("Hobbiton Public Hotel");
        Set<Stay> stays = new HashSet<>();
        stays.add(new Stay(guest, hotel));
        stays.add(new Stay(guest, hotel));
        Bill bill = new Bill(guest, hotel, stays);
        assertNotNull(bill);
        assertEquals(stays, bill.getStays());
        assertTrue(guest.getBills().contains(bill));
    }

    @Test
    public void billingAmountTest() {
        Guest guest = new Guest("Bilbo Baggins");
        Hotel hotel = new Hotel("Hobbiton Public Hotel");
        LocalDate oneWeekAgo = LocalDate.now().minusDays(7);
        Set<Stay> stays = new HashSet<>();
        stays.add(new Stay(guest, hotel, oneWeekAgo));

        Bill bill = new Bill(guest, hotel, new HashSet<>(stays));
        assertEquals(700.0, bill.getTotalBilledAmount());
    }

    @Test
    public void createInvalidBillTest() {
        Guest guest1 =  new Guest("Bilbo Baggins");
        Guest guest2 =  new Guest("Frodo Baggins");
        Hotel hotel =  new Hotel("Hobbiton Public Hotel");
        Set<Stay> stays = new HashSet<>();
        stays.add(new Stay(guest1, hotel));
        stays.add(new Stay(guest2, hotel));
        assertThrows(IllegalArgumentException.class, () -> new Bill(guest1, hotel, stays));
    }

    @Test
    public void getStayDurationAndBillAmountByQuarterTest() {
        Guest guest = new Guest("Bilbo Baggins");
        Hotel hotel = new Hotel("Hobbiton Public Hotel");
        // Construct dates that are offset by fixed amounts
        LocalDate baseDate = LocalDate.of(2026, 2, 10); // in 2026Q1
        // // 10 days after base, still in 2026Q1
        LocalDate offsetDate1 = baseDate.plusDays(10);
        // // 90 days before base, in the previous quarter 2025Q4
        LocalDate offsetDate2 = baseDate.minusDays(90);
        // // 90 days after base, in the next quarter 2026Q2
        LocalDate offsetDate3 = baseDate.plusDays(90);
        // Check a stay that's wholly inside a quarter
        Stay stay1 = new Stay(guest, hotel, baseDate, offsetDate1);
        assertEquals(10, stay1.getStayDurationInQuarter(2026, 1));
        assertEquals(0,  stay1.getStayDurationInQuarter(2026, 2));
        assertEquals(0,  stay1.getStayDurationInQuarter(2025, 4));
        // check a stay that begins before and ends after a quarter
        Stay stay2 = new Stay(guest, hotel, offsetDate2, offsetDate3);
        assertEquals(90, stay2.getStayDurationInQuarter(2026, 1));
        // check a stay that begins before and ends during a quarter
        Stay stay3 = new Stay(guest, hotel, offsetDate2, offsetDate1);
        assertEquals(50, stay3.getStayDurationInQuarter(2026, 1));
        // check a stay that begins during and ends after a quarter
        Stay stay4 = new Stay(guest, hotel, baseDate, offsetDate3);
        assertEquals(50, stay4.getStayDurationInQuarter(2026, 1));
        // Also check the billed amounts
        assertEquals(1000.0, stay1.getBilledAmountInQuarter(2026, 1));
        assertEquals(9000.0, stay2.getBilledAmountInQuarter(2026, 1));
        assertEquals(5000.0, stay3.getBilledAmountInQuarter(2026, 1));
        assertEquals(5000.0, stay4.getBilledAmountInQuarter(2026, 1));
        // Check that stay durations across quarter boundaries add up correctly
        assertEquals(stay2.getStayDuration(), stay2.getStayDurationInQuarter(2025, 4)
                + stay2.getStayDurationInQuarter(2026, 1) + stay2.getStayDurationInQuarter(2026,2)
        );
        assertEquals(stay3.getStayDuration(), stay3.getStayDurationInQuarter(2025, 4) + stay3.getStayDurationInQuarter(2026, 1));
        assertEquals(stay4.getStayDuration(), stay4.getStayDurationInQuarter(2026, 1) + stay4.getStayDurationInQuarter(2026, 2));
    }

    @Test
    public void queryInvalidQuarterTest() {
        Guest guest = new Guest("Bilbo Baggins");
        Hotel hotel = new Hotel("Hobbiton Public Hotel");
        Stay stay = new Stay(guest, hotel);
        // query for durations and amounts in nonexistent quarters
        assertThrows(IllegalArgumentException.class, () -> stay.getStayDurationInQuarter(2026, 0));
        assertThrows(IllegalArgumentException.class, () -> stay.getStayDurationInQuarter(2026, 5));
        assertThrows(IllegalArgumentException.class, () -> stay.getBilledAmountInQuarter(2026, -1));
    }
}
