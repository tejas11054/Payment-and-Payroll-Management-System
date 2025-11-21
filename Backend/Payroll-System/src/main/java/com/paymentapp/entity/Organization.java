package com.paymentapp.entity;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "organization")
@Getter
@Setter
@NoArgsConstructor
public class Organization {

	@Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long orgId;

    @NotBlank(message = "Organization name is required")
    @Column(nullable = false,unique = true)
    private String orgName;

    @NotBlank(message = "Email is required")
    @Email(message = "Invalid email format")
    @Column(nullable = false, unique = true, length = 100)
    private String email;

    @NotBlank(message = "Phone number is required")
    @Column(nullable = false, length = 15,unique = true)
    private String phone;

    @NotBlank(message = "Address is required")
    @Column(nullable = false, length = 255)
    private String address;

    @NotBlank(message = "Bank account number is required")
    @Column(nullable = false,unique = true)
    private String bankAccountNo;

    @NotBlank(message = "IFSC Code is required")
    @Column(nullable = false, length = 11)
    private String ifscCode;

    private String bankName;

    @Column(nullable = false)
    private String status = "PENDING";

    @CreationTimestamp
    private Instant createdAt;

    @UpdateTimestamp
    private Instant updatedAt;
    
    @Column(nullable = false)
    private boolean deleted  = false; 
    
    @Column(nullable = false)
    private int employeeCount = 0;
    

    private BigDecimal accountBalance = BigDecimal.ZERO; 
    
    @OneToMany(mappedBy = "organization", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<User> users = new ArrayList<>();
    
    @OneToMany(mappedBy = "organization", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<OrgAdmin> orgAdmins = new ArrayList<>();
    
    @OneToMany(mappedBy = "organization", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Department> departments = new ArrayList<>();

    @OneToMany(mappedBy = "organization", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Employee> employees = new ArrayList<>();

    @OneToMany(mappedBy = "organization", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Vendor> vendors = new ArrayList<>();

}
