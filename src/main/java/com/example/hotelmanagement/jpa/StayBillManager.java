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

import java.time.LocalDate;
import java.util.Collection;
import java.util.NoSuchElementException;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class StayBillManager {

    private final HotelManager hotelManager;
    private final GuestManager guestManager;
    private final StayRepository stayRepository;
    private final BillRepository billRepository;

    public StayBillManager(HotelManager hotelManager, GuestManager guestManager,
                           StayRepository stayRepository, BillRepository billRepository) {
        this.hotelManager = hotelManager;
        this.guestManager = guestManager;
        this.stayRepository = stayRepository;
        this.billRepository = billRepository;
    }

    @Transactional
    public Stay createStay(Long guestId, Long hotelId, LocalDate beginDate, LocalDate endDate) throws NoSuchElementException {
        Guest guest = guestManager.readGuest(guestId);
        Hotel hotel = hotelManager.readHotel(hotelId);
        Stay stay = new Stay(guest, hotel, beginDate, endDate);
        return stayRepository.save(stay);
    }

    @Transactional
    public Stay readStay(Long id) throws NoSuchElementException {
        return stayRepository.findById(id).orElseThrow();
    }

    @Transactional
    public Stay updateStay(Long id, LocalDate beginDate, LocalDate endDate) throws NoSuchElementException {
        Stay stay = readStay(id);
        stay.setBeginDate(beginDate);
        stay.setEndDate(endDate);
        return stay;
    }

    @Transactional
    public void deleteStay(Long id) {
        stayRepository.deleteById(id);
    }

    @Transactional
    public Bill createBill(Long guestId, Long hotelId, Collection<Long> stayIds) throws NoSuchElementException, IllegalArgumentException {
        Guest guest = guestManager.readGuest(guestId);
        Hotel hotel = hotelManager.readHotel(hotelId);
        Set<Stay> stays = stayIds.stream()
                .map(this::readStay)
                .collect(Collectors.toSet());
        Bill bill = new Bill(guest, hotel, stays);
        return billRepository.save(bill);
    }

    @Transactional
    public Bill readBill(Long billId) throws NoSuchElementException {
        return billRepository.findById(billId).orElseThrow();
    }

    @Transactional
    public void deleteBill(Long billId)  throws NoSuchElementException {
        // first separate the bill from all its stays so it can safely be deleted
        Bill bill = readBill(billId);
        bill.getStays().forEach(stay -> stay.setBill(null));
        billRepository.deleteById(billId);
    }


}
