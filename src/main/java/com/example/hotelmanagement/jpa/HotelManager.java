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
