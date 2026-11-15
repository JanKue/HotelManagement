package com.example.hotelmanagement.jpa;

import jakarta.persistence.*;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

@Entity
public class Stay {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private LocalDate beginDate;
    private LocalDate endDate;

    @ManyToOne(fetch = FetchType.EAGER, cascade = CascadeType.ALL)
    private Bill bill;
    @ManyToOne(fetch = FetchType.EAGER)
    private Guest guest;
    @ManyToOne(fetch = FetchType.EAGER)
    private Hotel hotel;

    private final Double billingMultiplier = 100.0;

    protected Stay() {}

    public Stay(Guest guest, Hotel hotel) {
        this.guest = guest;
        guest.getStays().add(this);
        this.hotel = hotel;
        hotel.getStays().add(this);
        // register the guest at the hotel, if not already registered
        if (!hotel.getRegisteredGuests().contains(guest)) {
            hotel.registerGuest(guest);
        }
        // if no beginDate is provided, default to beginning today
        this.beginDate = LocalDate.now();
    }

    public Stay(Guest guest, Hotel hotel, LocalDate beginDate) {
        this(guest, hotel);
        this.beginDate = beginDate;
    }

    public Stay(Guest guest, Hotel hotel, LocalDate beginDate, LocalDate endDate) throws IllegalArgumentException {
        this(guest, hotel, beginDate);
        if (endDate.isBefore(beginDate)) {
            throw new IllegalArgumentException("endDate cannot be before beginDate");
        }
        this.endDate = endDate;
    }

    /**
     * Calculates the duration of the stay in days.
     * If no end date is defined (i.e. the Stay is ongoing), the duration is calculated from the beginning date until the current day.
     * In any case, the duration is exclusive of the last day.
     */
    public Long getStayDuration() {
        if (endDate != null) {
            return ChronoUnit.DAYS.between(beginDate, endDate);
        } else {
            return ChronoUnit.DAYS.between(beginDate, LocalDate.now());
        }
    }

    public Double getBilledAmount() {
        return billingMultiplier * getStayDuration();
    }

    /**
     * Calculates the number of days of the stay's duration that overlap with the given quarter.
     * If the stay is cut off by the end of the quarter, the duration will be calculated inclusive of that final day.
     * This ensures that the durations across quarters add up correctly.
     *
     * @return Long in interval [0, max(total stay duration, length of the quarter)]
     */
    public Long getStayDurationInQuarter(Integer year, Integer quarter) throws IllegalArgumentException {
        if (quarter < 1 || quarter > 4) {
            throw new IllegalArgumentException("quarter must be between 1 and 4");
        }

        int endMonth = quarter * 3; // LocalDate months begin at 1
        int startMonth = endMonth - 2;
        int endMonthLength = (quarter == 1 || quarter == 4) ? 31 : 30; // Q1 and Q4 end on the 31st, Q2 and Q3 on the 30th
        LocalDate quarterStartDate = LocalDate.of(year, startMonth, 1);
        LocalDate quarterEndDate = LocalDate.of(year, endMonth, endMonthLength);

        // if no endDate is set, use today as the temporary end date for this calculation
        LocalDate tempEndDate = (endDate != null) ? endDate : LocalDate.now();

        if (beginDate.isAfter(quarterEndDate) || tempEndDate.isBefore(quarterStartDate)) {
            // stay begins after the quarter or ends before it -> no overlap
            return 0L;
        }
        if (!beginDate.isBefore(quarterStartDate)) {
            // stay begins within the quarter
            if (!tempEndDate.isAfter(quarterEndDate)) {
                // stay also ends within the quarter -> whole stay duration
                return getStayDuration();
            }
            // else: stay ends outside the quarter -> duration from stay beginDate to quarterEndDate
            return ChronoUnit.DAYS.between(beginDate, quarterEndDate.plusDays(1));
        }
        // else: stay begins before the quarter
        if (!tempEndDate.isAfter(quarterEndDate)) {
            // stay ends within the quarter -> duration from quarterStartDate to stay endDate
            return ChronoUnit.DAYS.between(quarterStartDate, tempEndDate);
        }
        // else: stay ends after the quarter -> duration from quarterStartDate to quarterEndDate
        return ChronoUnit.DAYS.between(quarterStartDate, quarterEndDate.plusDays(1));

    }

    /**
     * @return Billed amount associated with the stay's duration within the given quarter.
     */
    public Double getBilledAmountInQuarter(Integer year, Integer quarter) throws IllegalArgumentException {
        return billingMultiplier * getStayDurationInQuarter(year, quarter);
    }

    public Long getId() {
        return id;
    }

    public LocalDate getBeginDate() {
        return beginDate;
    }

    public void setBeginDate(LocalDate beginDate) {
        if (endDate != null && beginDate.isAfter(endDate)) {
            throw new IllegalArgumentException("beginDate cannot be after endDate");
        }
        this.beginDate = beginDate;
    }

    public LocalDate getEndDate() {
        return endDate;
    }

    public void setEndDate(LocalDate endDate) {
        if (endDate.isBefore(beginDate)) {
            throw new IllegalArgumentException("endDate cannot be before beginDate");
        }
        this.endDate = endDate;
    }

    public Guest getGuest() {
        return guest;
    }

    public Hotel getHotel() {
        return hotel;
    }

    public Bill getBill() {
        return bill;
    }

    protected void setBill(Bill bill) {
        this.bill = bill;
    }
}
