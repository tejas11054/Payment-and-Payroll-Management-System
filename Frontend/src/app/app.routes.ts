import { Routes } from '@angular/router';
import { LandingPageComponent } from './components/landing-page-component/landing-page-component';
import { RegsiterPageComponent } from './components/regsiter-page-component/regsiter-page-component';
import { LoginPageComponent } from './components/login-page-component/login-page-component';
import { AboutUsPageComponent } from './components/about-us-page-component/about-us-page-component';
import { ContactUsPageComponent } from './components/contact-us-page-component/contact-us-page-component';
import { ErrorPageComponent } from './errorPagecomponents/error-page-component/error-page-component';
import { OrganizationAdminComponent } from './dashboards/organization-admin-component/organization-admin-component';
import { BankAdminComponent } from './dashboards/bank-admin-component/bank-admin-component';
import { EmployeeDashboardComponent } from './dashboards/employee-dashboard-component/employee-dashboard-component';
import { authGuard } from './guards/auth-guard';
import { publicGuard } from './guards/public-guard';
import { PayrollComponent } from './components/payroll-component/payroll-component';
import { VendorDashboardComponent } from './dashboards/vendor-dashboard-component/vendor-dashboard-component';
import { ForgotPassword } from './components/forgot-password/forgot-password';
// import { ForgotPassword } from './components/forgot-password/forgot-password';

export const routes: Routes = [
  { path: 'register', component: RegsiterPageComponent, canActivate: [publicGuard] },
  { path: 'login', component: LoginPageComponent, canActivate: [publicGuard] },
 
  { path: 'about', component: AboutUsPageComponent, canActivate: [publicGuard] },
  { path: 'contact', component: ContactUsPageComponent, canActivate: [publicGuard] },
  { path: '', component: LandingPageComponent,canActivate: [publicGuard]},

  {
    path: 'organization/dashboard',
    component: OrganizationAdminComponent,
    canActivate: [authGuard],
    data: { roles: ['ROLE_ORGANIZATION','ROLE_ORG_ADMIN'] },
  },
  {
    path: 'bank-admin/dashboard',
    component: BankAdminComponent,
    canActivate: [authGuard],
    data: { roles: ['ROLE_BANK_ADMIN'] },
  },
  {
    path: 'employee/dashboard',
    component: EmployeeDashboardComponent,
    canActivate: [authGuard],
    data: { roles: ['ROLE_EMPLOYEE'] },
  },
   {
    path: 'vendor/dashboard',
    component: VendorDashboardComponent,
    canActivate: [authGuard],
    data: { roles: ['ROLE_VENDOR'] },
  },
{path:'payroll' , component:PayrollComponent},
 {
    path: 'forgot-password',
    component: ForgotPassword
  },
  { path: '**', component: ErrorPageComponent },
];
