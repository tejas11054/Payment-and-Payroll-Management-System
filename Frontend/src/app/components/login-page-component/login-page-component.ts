import { Component, inject } from '@angular/core';
import { FormBuilder, FormGroup, FormsModule, ReactiveFormsModule, Validators } from '@angular/forms';
import { DarkModeService } from '../../services/darkMode/dark-mode';
import { LoginAuthentication } from '../../services/login/login-authentication';
import { ToastService } from '../../services/notification/toast-service';
import { Router, RouterLink } from '@angular/router';
import { finalize } from 'rxjs';
import { CommonModule } from '@angular/common';

import { NgxCaptchaModule } from 'ngx-captcha';
import { loginRequestDTO } from '../../LoginDTO\'s/loginRequestDTO';

@Component({
  selector: 'app-login-page-component',
  standalone: true, // Added for standalone component
  imports: [CommonModule, ReactiveFormsModule, RouterLink, NgxCaptchaModule, FormsModule],
  templateUrl: './login-page-component.html',
  styleUrl: './login-page-component.css'
})
export class LoginPageComponent {

  private fb = inject(FormBuilder);
  public darkModeService = inject(DarkModeService);
  private authService = inject(LoginAuthentication);
  private toastService = inject(ToastService);
  private router = inject(Router); 

  loginForm: FormGroup;
  isLoading = false;
  passwordFieldType: 'password' | 'text' = 'password';
  protected siteKey = '6LdWgeQrAAAAAByCHFwHypGaB6GVMw4PemKKC54c';

  constructor() {
    this.loginForm = this.fb.group({
      email: ['', [Validators.required, Validators.email]],
      password: ['', [Validators.required, Validators.minLength(6)]],
      recaptcha: ['', Validators.required]
    });
  }

  togglePasswordVisibility(): void {
    this.passwordFieldType = this.passwordFieldType === 'password' ? 'text' : 'password';
  }

  onSubmit() {
    if (this.loginForm.invalid) {
      this.loginForm.markAllAsTouched();
      this.toastService.show('Please enter a valid email and password.', 'error');
      return;
    }

    this.isLoading = true;
    
    const loginData: loginRequestDTO = {
      email: this.f['email'].value,
      password: this.f['password'].value,
    };

    this.authService.login(loginData).pipe(
      finalize(() => {
        this.isLoading = false;
      })
    ).subscribe({      
      next: (response: any) => {
        this.toastService.show('Login Successful! Redirecting...', 'success');
        
        localStorage.setItem('token', response.token);
        localStorage.setItem('empId', String(response.empId)); 
        localStorage.setItem('orgId', String(response.orgId)); 
        localStorage.setItem('userRole', response.userRole);

        this.router.navigate(['/']); 
      },
      error: (err) => {
        const errorMessage = err.error?.message || 'Login failed. Please check your credentials.';
        this.toastService.show(errorMessage, 'error');
      }
    });
  }

  get f() {
    return this.loginForm.controls;
  }
}

