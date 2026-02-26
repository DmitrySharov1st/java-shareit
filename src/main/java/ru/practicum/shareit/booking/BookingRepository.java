package ru.practicum.shareit.booking;

import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface BookingRepository extends JpaRepository<Booking, Long> {

    List<Booking> findByBookerId(Long bookerId, Sort sort);

    List<Booking> findByBookerIdAndStartBeforeAndEndAfter(Long bookerId, LocalDateTime now1,
                                                          LocalDateTime now2, Sort sort);

    List<Booking> findByBookerIdAndEndBefore(Long bookerId, LocalDateTime now, Sort sort);

    List<Booking> findByBookerIdAndStartAfter(Long bookerId, LocalDateTime now, Sort sort);

    List<Booking> findByBookerIdAndStatus(Long bookerId, BookingStatus status, Sort sort);

    @Query("select b from Booking b where b.item.owner.id = ?1")
    List<Booking> findByOwnerId(Long ownerId, Sort sort);

    @Query("select b from Booking b where b.item.owner.id = ?1 and b.start < ?2 and b.end > ?2")
    List<Booking> findByOwnerIdCurrent(Long ownerId, LocalDateTime now, Sort sort);

    @Query("select b from Booking b where b.item.owner.id = ?1 and b.end < ?2")
    List<Booking> findByOwnerIdPast(Long ownerId, LocalDateTime now, Sort sort);

    @Query("select b from Booking b where b.item.owner.id = ?1 and b.start > ?2")
    List<Booking> findByOwnerIdFuture(Long ownerId, LocalDateTime now, Sort sort);

    @Query("select b from Booking b where b.item.owner.id = ?1 and b.status = ?2")
    List<Booking> findByOwnerIdAndStatus(Long ownerId, BookingStatus status, Sort sort);

    Optional<Booking> findFirstByItemIdAndStartBeforeAndStatusOrderByStartDesc(Long itemId, LocalDateTime now,
                                                                               BookingStatus status);

    Optional<Booking> findFirstByItemIdAndStartAfterAndStatusOrderByStartAsc(Long itemId, LocalDateTime now,
                                                                             BookingStatus status);

    boolean existsByItemIdAndBookerIdAndEndBeforeAndStatus(Long itemId, Long bookerId, LocalDateTime now,
                                                           BookingStatus status);
}