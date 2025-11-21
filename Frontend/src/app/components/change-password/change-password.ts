import { CommonModule } from '@angular/common';
import { Component, inject } from '@angular/core';
import { AbstractControl, FormBuilder, FormGroup, ReactiveFormsModule, ValidationErrors, ValidatorFn, Validators } from '@angular/forms';
import { Router, RouterLink } from '@angular/router';
import { LoginAuthentication } from '../../services/login/login-authentication';
import { ToastService } from '../../services/notification/toast-service';
import { finalize } from 'rxjs';

export const passwordMatchValidator: ValidatorFn = (control: AbstractControl): ValidationErrors | null => {
  const newPassword = control.get('newPassword');
  const confirmPassword = control.get('confirmPassword');
  return newPassword && confirmPassword && newPassword.value !== confirmPassword.value ? { passwordMismatch: true } : null;
};

export interface ChangePasswordDTO {
  oldPassword: string;
  newPassword: string;
}


@Component({
  selector: 'app-change-password',
  imports: [CommonModule,ReactiveFormsModule,RouterLink],
  templateUrl: './change-password.html',
  styleUrl: './change-password.css'
})
export class ChangePassword {
  
  private fb = inject(FormBuilder);
  private authService = inject(LoginAuthentication);
  private toastService = inject(ToastService);
  private router = inject(Router);

  changePasswordForm: FormGroup;
  isLoading = false;

  constructor() {
    this.changePasswordForm = this.fb.group({
      currentPassword: ['', [Validators.required]],
      newPassword: ['', [Validators.required, Validators.minLength(6)]],
      confirmPassword: ['', [Validators.required]]
    }, { validators: passwordMatchValidator }); // Apply the custom validator
  }

  // onSubmit() {
  //   if (this.changePasswordForm.invalid) {
  //     this.toastService.show('Please fill all fields correctly.', 'error');
  //     return;
  //   }

  //   this.isLoading = true;
  //   const { currentPassword, newPassword } = this.changePasswordForm.value;

  //   this.authService.changePassword(currentPassword, newPassword).pipe(
  //     finalize(() => this.isLoading = false)
  //   ).subscribe({
  //     next: (message) => {
  //       this.toastService.show(message, 'success');
  //       // Optionally, navigate away after successful change
  //       this.router.navigate(['/']); // Or to the user's dashboard
  //     },
  //     error: (err) => {
  //       this.toastService.show(err.message, 'error');
  //     }
  //   });
  // }

    onSubmit() {
    if (this.changePasswordForm.invalid) {
      this.toastService.show('Please fill all fields correctly.', 'error');
      return;
    }

    this.isLoading = true;
    const { currentPassword, newPassword } = this.changePasswordForm.value;

    this.authService.changePassword(currentPassword, newPassword).pipe(
      finalize(() => this.isLoading = false)
    ).subscribe({
      next: (message) => {
        this.toastService.show(message, 'success');
        // LOGIC TO LOGOUT AND LOGIN AGAIN FOR A NEW TOKEN
        alert("Password changed successfully! Please log in again with your new password.");
        this.authService.logout();
      },
      error: (err) => {
        this.toastService.show(err.message, 'error');
      }
    });
  }
  
  // Helper for template errors
  get f() {
    return this.changePasswordForm.controls;
  }

}
