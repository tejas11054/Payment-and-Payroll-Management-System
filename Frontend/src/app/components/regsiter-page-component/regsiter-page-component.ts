import { Component, ChangeDetectorRef, inject } from '@angular/core';
import {
  FormBuilder,
  FormGroup,
  Validators,
  ReactiveFormsModule,
  FormsModule,
} from '@angular/forms';
import { CommonModule } from '@angular/common';
import { Router, RouterLink } from '@angular/router';
import { DarkModeService } from '../../services/darkMode/dark-mode';
import { RegisterationAuthorization, ApiError } from '../../services/register/registeration-authorization';
import { finalize } from 'rxjs';
import { ToastService } from '../../services/notification/toast-service';

@Component({
  selector: 'app-regsiter-page-component',
  imports: [CommonModule, ReactiveFormsModule, RouterLink, FormsModule],
  templateUrl: './regsiter-page-component.html',
  styleUrl: './regsiter-page-component.css',
})
export class RegsiterPageComponent {
  
  // --- Services Injection ---
  private fb = inject(FormBuilder);
  public darkModeService = inject(DarkModeService);
  private authService = inject(RegisterationAuthorization);
  private router = inject(Router);
  private cdr = inject(ChangeDetectorRef);
  public toastService = inject(ToastService);

  // --- Component State ---
  registrationForm: FormGroup;
  currentStep = 1;
  uploadedFiles: File[] = [];
  isLoading = false;
  errorMessage = '';
  
  // ✅ NEW: Field-specific errors from backend
  fieldErrors: { [key: string]: string } = {};

  constructor() {
    this.registrationForm = this.fb.group({
      orgName: ['', [Validators.required, Validators.minLength(3)]],
      email: ['', [Validators.required, Validators.email]],
      phone: ['', [Validators.required, Validators.pattern('^[0-9]{10}$')]],
      address: ['', Validators.required],
      employeeCount: [1, [Validators.required, Validators.min(1)]],
      bankAccountNo: ['', [Validators.required, Validators.pattern('^[0-9]{9,18}$')]],
      ifscCode: ['', [Validators.required, Validators.pattern('^[A-Z]{4}0[A-Z0-9]{6}$')]],
      bankName: ['', Validators.required],
      verificationDocs: [null],
    });
  }

  // --- Multi-Step Navigation ---
  nextStep() {
    if (this.currentStep < 3) this.currentStep++;
  }
  
  prevStep() {
    if (this.currentStep > 1) this.currentStep--;
  }

  // --- File Input Handling ---
  onFileChange(event: any) {
    this.uploadedFiles = Array.from(event.target.files);
  }

  // ✅ NEW: Helper method to get field-specific backend errors
  getFieldError(fieldName: string): string | null {
    return this.fieldErrors[fieldName] || null;
  }

  // ✅ NEW: Clear field error when user starts typing
  clearFieldError(fieldName: string) {
    delete this.fieldErrors[fieldName];
  }

  // --- Form Submission with Field-Specific Error Handling ---
  onSubmit() {
    
    // Clear previous backend errors
    this.fieldErrors = {};
    this.errorMessage = '';
    
    // Frontend validation check
    if (this.registrationForm.invalid) {
      this.registrationForm.markAllAsTouched();
      this.toastService.show('Please fill all required fields correctly.', 'error');
      return;
    }

    this.isLoading = true;

    // Prepare FormData
    const formData = new FormData();

    const orgDto = {
      orgName: this.f['orgName'].value,
      email: this.f['email'].value,
      phone: this.f['phone'].value,
      address: this.f['address'].value,
      employeeCount: this.f['employeeCount'].value,
      bankAccountNo: this.f['bankAccountNo'].value,
      ifscCode: this.f['ifscCode'].value,
      bankName: this.f['bankName'].value,
    };

    formData.append('dto', new Blob([JSON.stringify(orgDto)], { type: 'application/json' }));

    this.uploadedFiles.forEach((file) => {
      formData.append('verificationDocs', file, file.name);
    });

    // ✅ Call backend service
    this.authService.register(formData)
      .pipe(
        finalize(() => {
          this.isLoading = false;
          this.cdr.detectChanges();
        })
      )
      .subscribe({
        
        // ✅ Success Handler
        next: (response: any) => {
          console.log('✅ Registration successful!', response);
          this.toastService.show('Registration successful! Check your email for details.', 'success');
          this.router.navigate(['/login']);
        },
        
        // ✅ Enhanced Error Handler with Field-Specific Messages
        error: (apiError: ApiError) => {
          console.log('❌ Registration error:', apiError);
          
          // ✅ Check if it's a reactivation scenario
          if (apiError.message?.includes('Confirm reactivation')) {
            this.handleReactivationPrompt(formData);
            return;
          }
          
          // ✅ Handle field-specific errors
          if (apiError.field) {
            
            // Store error for the specific field
            this.fieldErrors[apiError.field] = apiError.message;
            
            // Show toast with field name for clarity
            const fieldDisplayNames: { [key: string]: string } = {
              'orgName': 'Organization Name',
              'email': 'Email',
              'phone': 'Phone Number',
              'bankAccountNo': 'Bank Account Number'
            };
            
            const fieldName = fieldDisplayNames[apiError.field] || apiError.field;
            
            // ✅ Different toast messages based on field
            switch(apiError.field) {
              case 'orgName':
                this.toastService.show(`❌ ${apiError.message}`, 'error');
                // Optionally scroll to the field or focus it
                this.scrollToStep(1); // Assuming org name is in step 1
                break;
                
              case 'email':
                this.toastService.show(`❌ ${apiError.message}`, 'error');
                this.scrollToStep(1);
                break;
                
              case 'phone':
                this.toastService.show(`❌ ${apiError.message}`, 'error');
                this.scrollToStep(1);
                break;
                
              case 'bankAccountNo':
                this.toastService.show(`❌ ${apiError.message}`, 'error');
                this.scrollToStep(2); // Assuming bank details are in step 2
                break;
                
              default:
                this.toastService.show(`❌ ${fieldName}: ${apiError.message}`, 'error');
            }
            
            // ✅ Mark the field as invalid in the form
            const control = this.registrationForm.get(apiError.field);
            if (control) {
              control.setErrors({ serverError: apiError.message });
              control.markAsTouched();
            }
            
          } else {
            // Generic error without specific field
            this.errorMessage = apiError.message || 'Registration failed!';
            this.toastService.show(this.errorMessage, 'error');
          }
          
          this.cdr.detectChanges();
        }
      });
  }

  // ✅ Handle reactivation prompt (existing logic)
  private handleReactivationPrompt(formData: FormData) {
    const confirmReactivate = confirm(
      'This organization was deleted earlier. Do you want to reactivate it?'
    );
    
    if (confirmReactivate) {
      // Add reactivate flag
      formData.append('reactivate', 'true');
      
      this.isLoading = true;
      
      this.authService.register(formData)
        .pipe(
          finalize(() => {
            this.isLoading = false;
            this.cdr.detectChanges();
          })
        )
        .subscribe({
          next: () => {
            this.toastService.show('Organization reactivated successfully!', 'success');
            this.router.navigate(['/login']);
          },
          error: (err: ApiError) => {
            this.toastService.show(err.message || 'Reactivation failed!', 'error');
          }
        });
    } else {
      this.isLoading = false;
      this.cdr.detectChanges();
    }
  }

  // ✅ Helper to scroll/navigate to specific step when error occurs
  private scrollToStep(step: number) {
    this.currentStep = step;
    // Optionally scroll to top of form
    window.scrollTo({ top: 0, behavior: 'smooth' });
  }

  // --- Helper getter ---
  get f() {
    return this.registrationForm.controls;
  }
}
