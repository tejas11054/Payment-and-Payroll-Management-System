import { inject } from '@angular/core';
import { CanActivateFn, Router } from '@angular/router';
import { LoginAuthentication } from '../services/login/login-authentication'; 

export const publicGuard: CanActivateFn = (route, state) => {
  const authService = inject(LoginAuthentication);
  const router = inject(Router);

  if (authService.isLoggedIn()) {
    // User is logged in, so they shouldn't be on a public page.
    // Redirect them to their proper dashboard.
    const role = authService.getRoleFromToken();
    let dashboardPath = '/';

    switch (role) {
      case 'ROLE_BANK_ADMIN':
        dashboardPath = '/bank-admin/dashboard';
        break;
      case 'ROLE_ORGANIZATION':
      case 'ROLE_ORG_ADMIN':
        dashboardPath = '/organization/dashboard';
        break;
      case 'ROLE_EMPLOYEE':
        dashboardPath = '/employee/dashboard';
        break;
          case 'ROLE_VENDOR':
        dashboardPath = '/vendor/dashboard';
        break;
    }
    
    router.navigate([dashboardPath]);
    return false; // Prevent access to the public page
  }

  // User is not logged in, allow them to access the public page
  return true;
};
