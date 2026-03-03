package com.vestshop.Repository;

import com.vestshop.Entity.ClientPasswordResetOtp;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ClientPasswordResetOtpRepository extends JpaRepository<ClientPasswordResetOtp,Long> {
    Optional<ClientPasswordResetOtp> findTopByEmailOrderByIdDesc(String email);
}
