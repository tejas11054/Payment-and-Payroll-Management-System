package com.paymentapp.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.paymentapp.entity.BankAdmin;

public interface BankAdminRepository extends JpaRepository<BankAdmin, Long> {

	  @Query("SELECT b FROM BankAdmin b JOIN FETCH b.user u WHERE u.email = :email AND b.deleted = false")
	    Optional<BankAdmin> findByUserEmail(@Param("email") String email);
}
