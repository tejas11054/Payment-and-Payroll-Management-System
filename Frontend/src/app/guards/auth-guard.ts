import { CanActivateFn, Router } from '@angular/router';
import { LoginAuthentication } from '../services/login/login-authentication';
import { inject } from '@angular/core';
import { ToastService } from '../services/notification/toast-service';

export const authGuard: CanActivateFn = (route, state) => {

const authService = inject(LoginAuthentication);
  const router = inject(Router);

  // 1. Check if the user is logged in
  if (!authService.isLoggedIn()) {
    router.navigate(['/login']);
    return false;
  }

  // 2. Check for required roles
  const requiredRoles = route.data['roles'] as string[];
  const userRole = authService.getRoleFromToken();

  if (requiredRoles && userRole && requiredRoles.includes(userRole)) {
    // User has the required role, allow access
    return true;
  } else {
    // Role not authorized, redirect to a default/error page or their own dashboard
    console.error(`Authorization failed. User role '${userRole}' does not have access.`);
    // You could redirect to an "unauthorized" page or back to login
    router.navigate(['/login']); 
    return false;
  }

};
