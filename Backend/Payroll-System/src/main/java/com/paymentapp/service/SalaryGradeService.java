package com.paymentapp.service;

import java.util.List;

import com.paymentapp.dto.SalaryGradeRequestDTO;
import com.paymentapp.dto.SalaryGradeResponseDTO;

public interface SalaryGradeService {

	SalaryGradeResponseDTO createSalaryGrade(Long orgId, SalaryGradeRequestDTO requestDTO);

	SalaryGradeResponseDTO updateSalaryGrade(Long orgId, Long gradeId, SalaryGradeRequestDTO requestDTO);

	SalaryGradeResponseDTO getSalaryGradeById(Long orgId, Long gradeId);

	List<SalaryGradeResponseDTO> getAllSalaryGrades(Long orgId);

	void deleteSalaryGrade(Long orgId, Long gradeId);
}
