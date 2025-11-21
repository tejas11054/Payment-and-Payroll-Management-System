import { Component, HostBinding, inject, signal } from '@angular/core';
import { Router, RouterLink, RouterModule, RouterOutlet } from '@angular/router';
import { ToastComponent } from './components/notification/toast/toast';
import { LoginAuthentication } from './services/login/login-authentication';
import { DarkModeService } from './services/darkMode/dark-mode';
import { FormsModule, ReactiveFormsModule } from '@angular/forms';
import { CommonModule } from '@angular/common';
import { ConfirmationModal } from './components/confirmation-modal/confirmation-modal';


@Component({
  selector: 'app-root',
  standalone:true,
  imports: [RouterOutlet, FormsModule, ToastComponent, RouterLink, ReactiveFormsModule, CommonModule,ConfirmationModal], 
  templateUrl: './app.html',
  styleUrl: './app.css',
})
export class App {
  protected readonly title = signal('PayRoll_System');
  public authService = inject(LoginAuthentication);
  router = inject(Router);
  constructor(public dark: DarkModeService) {
    // init body class and host binding
    this.dark.set(this.dark.value);
    this.dark.isDarkMode$.subscribe(v => this.darkHost = v);
  }



  logout(): void {
    this.authService.logout();
    this.router.navigate(['/']); // redirect landing page after logout
  }

  // Helper to redirect user to their dashboard from the logo
  navigateToDashboard(): void {
    const role = this.authService.getRoleFromToken();
    let dashboardPath = '/';
    switch (role) {
      case 'ROLE_BANK_ADMIN': dashboardPath = '/bank-admin/dashboard'; break;
      case 'ROLE_ORGANIZATION': dashboardPath = '/organization/dashboard'; break;
      case 'ROLE_EMPLOYEE': dashboardPath = '/employee/dashboard'; break;
      case 'ROLE_ORG_ADMIN': dashboardPath = '/organization/dashboard'; break; 
      case 'ROLE_VENDOR': dashboardPath = '/vendor/dashboard'; break;
      default: dashboardPath = '/'; break;
    }
    this.router.navigate([dashboardPath]);
  }

  @HostBinding('class.dark-mode') darkHost = false;
  get isDarkMode(): boolean { return this.dark.value; }
}
