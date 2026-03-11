package com.vyxentra.vehicle.repository;


import com.vyxentra.vehicle.entity.Skill;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface SkillRepository extends JpaRepository<Skill, Long> {

    Optional<Skill> findByName(String name);

    List<Skill> findByCategory(String category);

    @Query("SELECT s FROM Skill s WHERE s.id IN (" +
            "SELECT es.id FROM Employee e JOIN e.skills es WHERE e.id = :employeeId)")
    List<Skill> findByEmployeeId(@Param("employeeId") String employeeId);
}