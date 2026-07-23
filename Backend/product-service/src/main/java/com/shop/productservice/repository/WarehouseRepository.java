package com.shop.productservice.repository;

import com.shop.productservice.entity.Warehouse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface WarehouseRepository extends JpaRepository<Warehouse, Long> {

    Optional<Warehouse> findByWarehouseCode(String warehouseCode);

    Optional<Warehouse> findByWarehouseCodeAndIsDeletedFalse(String warehouseCode);

    List<Warehouse> findAllByIsDeletedFalse();
    Page<Warehouse> findAllByIsDeletedFalse(Pageable pageable);

    List<Warehouse> findAllByIsActiveTrueAndIsDeletedFalse();
    Page<Warehouse> findAllByIsActiveTrueAndIsDeletedFalse(Pageable pageable);

    Optional<Warehouse> findByIsDefaultTrueAndIsDeletedFalse();

    List<Warehouse> findByCityAndIsDeletedFalse(String city);

    List<Warehouse> findByStateAndIsDeletedFalse(String state);

    boolean existsByWarehouseCode(String warehouseCode);

    @Query(value = """
    SELECT CASE
        WHEN COUNT(*) > 0 THEN TRUE
        ELSE FALSE
    END
    FROM warehouses
    WHERE UPPER(TRIM(warehouse_code)) = UPPER(TRIM(:warehouseCode))
    """, nativeQuery = true)
    Long existsByWarehouseCodeIncludingDeleted(@Param("warehouseCode") String warehouseCode);

    @Query(value = "SELECT * FROM warehouses WHERE id = :id AND is_deleted = true", nativeQuery = true)
    Optional<Warehouse> findDeletedById(@Param("id") Long id);
}
