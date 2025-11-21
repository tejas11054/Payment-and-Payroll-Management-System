// add-vendor-modal.component.ts

import { Component, EventEmitter, inject, Input, Output } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormBuilder, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';

import { ToastService } from '../../services/notification/toast-service';
import { VendorRequest, VendorService } from '../../services/vendor/vendor';

@Component({
  selector: 'add-vendor-modal',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule],
  templateUrl: './add-vendor-modal.html',
  styleUrl: './add-vendor-modal.css'
})
export class AddVendorModal{
  
  @Input() isOpen = false;
  @Input() orgId!: number;
  @Output() closed = new EventEmitter<void>();
  @Output() vendorCreated = new EventEmitter<void>();

  private fb = inject(FormBuilder);
  private vendorService = inject(VendorService);
  private toastService = inject(ToastService);

  vendorForm!: FormGroup;
  isLoading = false;
  selectedFile: File | null = null;

  vendorTypes = [
    'Service Provider',
    'Product Supplier',
    'Consultant',
    'Contractor',
    'Freelancer',
    'Other'
  ];
constructor() {
    console.log('AddVendorModal Constructor - isOpen:', this.isOpen); // Should be false
  }

  ngOnInit(): void {
    this.initializeForm();
  }

  initializeForm(): void {
    this.vendorForm = this.fb.group({
      name: ['', [Validators.required, Validators.minLength(2)]],
      vendorType: ['', Validators.required],
      bankName: ['', Validators.required],
      bankAccountNo: ['', [Validators.required, Validators.pattern(/^\d{9,18}$/)]],
      ifscCode: ['', [Validators.required, Validators.pattern(/^[A-Z]{4}0[A-Z0-9]{6}$/)]],
      contactEmail: ['', [Validators.required, Validators.email]],
      phone: ['', [Validators.required, Validators.pattern(/^[6-9]\d{9}$/)]]
    });
  }

  onFileSelected(event: any): void {
    const file = event.target.files[0];
    if (file) {
      // Validate file type
      const allowedTypes = ['application/pdf', 'image/jpeg', 'image/png'];
      if (!allowedTypes.includes(file.type)) {
        this.toastService.show('Only PDF, JPEG, PNG files allowed', 'error');
        return;
      }

      // Validate file size (max 5MB)
      if (file.size > 5 * 1024 * 1024) {
        this.toastService.show('File size must be less than 5MB', 'error');
        return;
      }

      this.selectedFile = file;
    }
  }

  onSubmit(): void {
    if (this.vendorForm.invalid) {
      Object.keys(this.vendorForm.controls).forEach(key => {
        this.vendorForm.get(key)?.markAsTouched();
      });
      return;
    }

    this.isLoading = true;
    const vendorData: VendorRequest = this.vendorForm.value;

    this.vendorService.createVendor(this.orgId, vendorData, this.selectedFile || undefined).subscribe({
      next: (response) => {
        this.isLoading = false;
        this.toastService.show(`Vendor "${response.name}" created successfully!`, 'success');
        this.vendorCreated.emit();
        this.closeModal();
      },
      error: (err) => {
        this.isLoading = false;
        console.error('Error creating vendor:', err);
        const errorMessage = err?.error?.message || 'Failed to create vendor';
        this.toastService.show(errorMessage, 'error');
      }
    });
  }

  closeModal(): void {
    this.vendorForm.reset();
    this.selectedFile = null;
    this.closed.emit();
  }

  isFieldInvalid(fieldName: string): boolean {
    const field = this.vendorForm.get(fieldName);
    return !!(field && field.invalid && (field.dirty || field.touched));
  }

  getErrorMessage(fieldName: string): string {
    const field = this.vendorForm.get(fieldName);
    if (field?.errors) {
      if (field.errors['required']) return `${fieldName} is required`;
      if (field.errors['email']) return 'Invalid email format';
      if (field.errors['pattern']) {
        if (fieldName === 'bankAccountNo') return 'Account number must be 9-18 digits';
        if (fieldName === 'ifscCode') return 'Invalid IFSC code format';
        if (fieldName === 'phone') return 'Invalid phone number';
      }
      if (field.errors['minlength']) return `Minimum length is ${field.errors['minlength'].requiredLength}`;
    }
    return '';
  }
}
