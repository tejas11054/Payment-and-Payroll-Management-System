import { Component, inject, ChangeDetectorRef, OnInit } from '@angular/core';
import { FormBuilder, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { ToastService } from '../../services/notification/toast-service';
import { LoginAuthentication } from '../../services/login/login-authentication';
import { finalize } from 'rxjs';
import { CommonModule } from '@angular/common';
import { Router, RouterLink } from '@angular/router';
import { DarkModeService } from '../../services/darkMode/dark-mode';

@Component({
  selector: 'app-forgot-password',
  standalone: true,
  imports: [ReactiveFormsModule, CommonModule, RouterLink],
  templateUrl: './forgot-password.html',
  styleUrl: './forgot-password.css'
})
export class ForgotPassword implements OnInit {

  private fb = inject(FormBuilder);
  private authService = inject(LoginAuthentication);
  private toastService = inject(ToastService);
  private router = inject(Router);
  private cdr = inject(ChangeDetectorRef);
  private darkModeService = inject(DarkModeService);

  currentStep: 'email' | 'otp' | 'password' = 'email';
  emailForm: FormGroup;
  otpForm: FormGroup;
  passwordForm: FormGroup;
  isLoading = false;
  userEmail = '';
  isDarkMode = false;

  constructor() {
    this.emailForm = this.fb.group({
      email: ['', [Validators.required, Validators.email]]
    });

    this.otpForm = this.fb.group({
      otp1: ['', [Validators.required, Validators.pattern(/^\d$/)]],
      otp2: ['', [Validators.required, Validators.pattern(/^\d$/)]],
      otp3: ['', [Validators.required, Validators.pattern(/^\d$/)]],
      otp4: ['', [Validators.required, Validators.pattern(/^\d$/)]],
      otp5: ['', [Validators.required, Validators.pattern(/^\d$/)]],
    });

    this.passwordForm = this.fb.group({
      newPassword: ['', [Validators.required, Validators.minLength(6)]],
      confirmPassword: ['', [Validators.required]]
    }, { validators: this.passwordMatchValidator });
  }

  ngOnInit() {
    this.darkModeService.isDarkMode$.subscribe(isDark => {
      this.isDarkMode = isDark;
      this.cdr.markForCheck();
    });
  }

  onRequestOTP() {
    if (this.emailForm.invalid) {
      this.toastService.show('Please enter a valid email address.', 'error');
      return;
    }

    this.isLoading = true;
    const email = this.emailForm.value.email.trim().toLowerCase();
    this.userEmail = email;

    this.authService.requestPasswordReset(email).pipe(
      finalize(() => {
        this.isLoading = false;
        this.cdr.detectChanges();
      })
    ).subscribe({
      next: (message) => {
        this.toastService.show('OTP sent to your email!', 'success');
        this.currentStep = 'otp';
        this.cdr.detectChanges();
      },
      error: (err) => {
        this.toastService.show(err.error?.message || err.error || 'Failed to send OTP. Please try again.', 'error');
      }
    });
  }

  onVerifyOTP() {
    if (this.otpForm.invalid) {
      this.toastService.show('Please enter a valid 5-digit OTP.', 'error');
      return;
    }

    this.isLoading = true;
    const otpValues = this.otpForm.value;
    const otp = `${otpValues.otp1}${otpValues.otp2}${otpValues.otp3}${otpValues.otp4}${otpValues.otp5}`;

    this.authService.verifyOTP(this.userEmail, otp).pipe(
      finalize(() => {
        this.isLoading = false;
        this.cdr.detectChanges();
      })
    ).subscribe({
      next: (message) => {
        this.toastService.show('OTP verified successfully!', 'success');
        this.currentStep = 'password';
        this.cdr.detectChanges();
      },
      error: (err) => {
        console.error('OTP Verification Error:', err);
        this.toastService.show('Invalid or expired OTP', 'error');
      }
    });
  }
  
  onResetPassword() {
    if (this.passwordForm.invalid) {
      if (this.passwordForm.errors?.['passwordMismatch']) {
        this.toastService.show('Passwords do not match!', 'error');
      } else {
        this.toastService.show('Please fill all fields correctly.', 'error');
      }
      return;
    }

    this.isLoading = true;
    const otpValues = this.otpForm.value;
    const otp = `${otpValues.otp1}${otpValues.otp2}${otpValues.otp3}${otpValues.otp4}${otpValues.otp5}`;
    const newPassword = this.passwordForm.value.newPassword;

    this.authService.resetPasswordWithOTP(this.userEmail, otp, newPassword).pipe(
      finalize(() => {
        this.isLoading = false;
        this.cdr.detectChanges();
      })
    ).subscribe({
      next: (message) => {
        this.toastService.show('Password reset successfully! Redirecting to login...', 'success');
        setTimeout(() => {
          this.router.navigate(['/login']);
        }, 2000);
      },
      error: (err) => {
        this.toastService.show(err.error?.message || err.error || 'Failed to reset password. Please try again.', 'error');
      }
    });
  }

  passwordMatchValidator(group: FormGroup): { [key: string]: boolean } | null {
    const password = group.get('newPassword')?.value;
    const confirmPassword = group.get('confirmPassword')?.value;
    return password === confirmPassword ? null : { passwordMismatch: true };
  }

  goBackToEmail() {
    this.currentStep = 'email';
    this.otpForm.reset();
    this.cdr.detectChanges();
  }

  goBackToOTP() {
    this.currentStep = 'otp';
    this.passwordForm.reset();
    this.cdr.detectChanges();
  }

  resendOTP() {
    // Clear the OTP form before requesting a new one
    this.otpForm.reset();
    this.onRequestOTP();
  }

  // OTP input handlers
  onKeyUp(event: KeyboardEvent, nextInputId?: string, prevInputId?: string) {
    const input = event.target as HTMLInputElement;
    const key = event.key;

    if (key === 'Backspace' || key === 'Delete') {
      if (prevInputId) {
        const prevInput = document.getElementById(prevInputId);
        if (prevInput) {
          (prevInput as HTMLInputElement).focus();
        }
      }
    } else if (/\d/.test(key) && input.value && nextInputId) {
      const nextInput = document.getElementById(nextInputId);
      if (nextInput) {
        (nextInput as HTMLInputElement).focus();
      }
    }
  }
}

