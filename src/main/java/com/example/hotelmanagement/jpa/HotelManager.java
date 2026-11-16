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

package com.example.hotelmanagement.jpa;

import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.NoSuchElementException;

@Service
public class HotelManager {

    private final HotelRepository hotelRepository;
    private final GuestRepository guestRepository;

    public HotelManager(HotelRepository hotelRepository, GuestRepository guestRepository) {
        this.hotelRepository = hotelRepository;
        this.guestRepository = guestRepository;
    }

    @Transactional
    public Hotel createHotel(String name) {
        Hotel hotel = new Hotel(name);
        return hotelRepository.save(hotel);
    }

    @Transactional
    public Hotel readHotel(Long id) throws NoSuchElementException {
        return hotelRepository.findById(id).orElseThrow();
    }

    @Transactional
    public Hotel updateHotel(Long id, String name ) throws NoSuchElementException {
        Hotel hotel = readHotel(id);
        hotel.setName(name);
        return hotel;
    }

    @Transactional
    public void deleteHotel(Long id) {
        hotelRepository.deleteById(id);
    }

    @Transactional
    public Hotel registerGuest(Long hotelId, Long guestId) throws NoSuchElementException {
        Hotel hotel = readHotel(hotelId);
        Guest guest = guestRepository.findById(guestId).orElseThrow();
        hotel.registerGuest(guest);
        return hotel;
    }

    @Transactional
    public Collection<Guest> getRegisteredGuests(Long hotelId) throws NoSuchElementException{
        Hotel hotel = readHotel(hotelId);
        return hotel.getRegisteredGuests();
    }

}
