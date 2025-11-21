package com.paymentapp.exception;

public class DuplicatePayrollException extends RuntimeException {
 /**
	 * 
	 */
	private static final long serialVersionUID = 1L;
 private String period;
 private String status;
 
 public DuplicatePayrollException(String period, String status) {
     super(String.format(
         "Salary disbursal request already exists for period %s with status: %s. " +
         "Please wait for approval or contact administrator.",
         period,
         status
     ));
     this.period = period;
     this.status = status;
 }
 
 // Getters
 public String getPeriod() { return period; }
 public String getStatus() { return status; }
}

//In Service


