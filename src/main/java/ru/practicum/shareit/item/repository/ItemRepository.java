package ru.practicum.shareit.item.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import ru.practicum.shareit.item.model.Item;

import java.util.List;

@Repository
public interface ItemRepository extends JpaRepository<Item, Long> {
    @Query("SELECT i " +
            "FROM Item i " +
            "WHERE i.available = true " +
            "  AND (" +
            "       UPPER(i.name) LIKE UPPER(CONCAT('%', :text, '%')) " +
            "    OR UPPER(i.description) LIKE UPPER(CONCAT('%', :text, '%'))" +
            "      )")
    List<Item> search(@Param("text") String text);

    List<Item> findByOwnerId(Long ownerId);
}
