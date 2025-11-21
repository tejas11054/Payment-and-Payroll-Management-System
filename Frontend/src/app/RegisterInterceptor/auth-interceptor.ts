import { HttpInterceptorFn } from '@angular/common/http';

export const authInterceptor: HttpInterceptorFn = (req, next) => {
    // Get JWT token from localStorage
  const token = localStorage.getItem('authToken');

  // If token exists, clone the request and add the Authorization header
  if (token) {
    const clonedReq = req.clone({
      setHeaders: {
        Authorization: `Bearer ${token}`
      }
    });
    return next(clonedReq);
  }

  // If no token, just forward the original request
  return next(req);

};
