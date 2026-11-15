package com.example.hotelmanagement.jpa;

import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.Collection;
import java.util.NoSuchElementException;

@Service
public class GuestManager {

    private final GuestRepository guestRepository;

    public GuestManager(GuestRepository guestRepository) {
        this.guestRepository = guestRepository;
    }

    @Transactional
    public Guest createGuest(String name, LocalDate dateOfBirth) {
        Guest guest = new Guest(name, dateOfBirth);
        return guestRepository.save(guest);
    }

    @Transactional
    public Guest readGuest(Long id) throws NoSuchElementException {
        return guestRepository.findById(id).orElseThrow();
    }

    @Transactional
    public Guest updateGuest(Long id, String name, LocalDate dateOfBirth) throws NoSuchElementException {
        Guest guest = readGuest(id);
        guest.setName(name);
        guest.setDateOfBirth(dateOfBirth);
        return guest;
    }

    @Transactional
    public void deleteGuest(Long id) {
        guestRepository.deleteById(id);
    }

    @Transactional
    public Collection<Hotel> getHotelsRegisteredAt(Long guestId) throws NoSuchElementException {
        Guest guest = readGuest(guestId);
        return guest.getHotelsRegisteredAt();
    }

    @Transactional
    public Collection<Stay> getGuestStays(Long guestId) throws NoSuchElementException {
        return readGuest(guestId).getStays();
    }

    @Transactional
    public Collection<Stay> getGuestStaysAtHotel(Long guestId, Long hotelId) throws NoSuchElementException {
        return readGuest(guestId).getStays().stream()
                .filter(stay -> stay.getHotel().getId().equals(hotelId))
                .toList();
    }

    @Transactional
    public Collection<Bill> getGuestBills(Long guestId) throws NoSuchElementException {
        return readGuest(guestId).getBills();
    }

    @Transactional
    public Collection<Bill> getGuestBillsAtHotel(Long guestId, Long hotelId) throws NoSuchElementException {
        return readGuest(guestId).getBills().stream()
                .filter(bill -> bill.getHotel().getId().equals(hotelId))
                .toList();
    }

    @Transactional
    public Double getGuestBalance(Long guestId) throws NoSuchElementException {
        Collection<Bill> bills = getGuestBills(guestId);
        return bills.stream()
                .map(Bill::getTotalBilledAmount)
                .mapToDouble(Double::doubleValue)
                .sum();
    }

    @Transactional
    public Double getGuestBalanceAtHotel(Long guestId, Long hotelId) throws NoSuchElementException {
        Collection<Bill> bills = getGuestBillsAtHotel(guestId, hotelId);
        return bills.stream()
                .map(Bill::getTotalBilledAmount)
                .mapToDouble(Double::doubleValue)
                .sum();
    }

    @Transactional
    public Long getGuestStayDurationInQuarter(Long guestId, Integer year, Integer quarter) throws NoSuchElementException, IllegalArgumentException {
        Collection<Stay> stays = getGuestStays(guestId);
        return stays.stream()
                .map(stay -> stay.getStayDurationInQuarter(year, quarter))
                .mapToLong(Long::longValue)
                .sum();
    }

    @Transactional
    public Double getGuestBalanceInQuarter(Long guestId, Integer year, Integer quarter) throws NoSuchElementException, IllegalArgumentException {
        Collection<Stay> stays = getGuestStays(guestId);
        return stays.stream()
                .map(stay -> stay.getBilledAmountInQuarter(year, quarter))
                .mapToDouble(Double::doubleValue)
                .sum();
    }


}
