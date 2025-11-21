import { HttpClient, HttpErrorResponse } from '@angular/common/http';
import { inject, Injectable } from '@angular/core';
import { catchError, Observable, throwError } from 'rxjs';

// ✅ Define custom error interface for better typing
export interface ApiError {
  message: string;
  field?: string;
  status?: number;
  timestamp?: string;
  error?: string;
}

@Injectable({
  providedIn: 'root'
})
export class RegisterationAuthorization {
  
  // Inject HttpClient to make API calls
  private http = inject(HttpClient);
  
  private backendUrl = 'http://localhost:8080/api/organizations';
  
  constructor() { }
  
  register(formData: FormData): Observable<any> {
    return this.http.post<any>(`${this.backendUrl}/register`, formData).pipe(
      catchError((error) => this.handleError(error))
    );
  }
  
  /**
   * ✅ Enhanced error handler that extracts backend error details
   * Returns structured error object with field and message
   */
  private handleError(error: HttpErrorResponse): Observable<never> {
    
    console.error('❌ Full Error Object:', error);
    
    let apiError: ApiError = {
      message: 'An unknown error occurred!',
      status: error.status
    };
    
    // ✅ Client-side or network error
    if (error.error instanceof ErrorEvent) {
      apiError.message = `Network Error: ${error.error.message}`;
      console.error('🌐 Client-side error:', error.error.message);
    } 
    // ✅ Backend returned an error response (4xx, 5xx)
    else {
      
      // Check if backend sent structured error object
      if (error.error && typeof error.error === 'object') {
        
        // Extract message from backend
        apiError.message = error.error.message || error.error.error || error.statusText;
        
        // ✅ Extract specific field that caused the error (from your GlobalExceptionHandler)
        apiError.field = error.error.field;
        
        // Extract timestamp if available
        apiError.timestamp = error.error.timestamp;
        
        // Extract error type if available
        apiError.error = error.error.error;
        
        console.log('📋 Backend Error Details:', {
          field: apiError.field,
          message: apiError.message,
          status: error.status
        });
      } 
      // Backend sent plain text error
      else if (typeof error.error === 'string') {
        apiError.message = error.error;
      } 
      // Fallback to status text
      else {
        apiError.message = `Error ${error.status}: ${error.statusText}`;
      }
      
      console.error(`🔴 Server Error (${error.status}):`, apiError.message);
    }
    
    // ✅ Return the error as an observable that emits the error object
    // This allows component to access field and message
    return throwError(() => apiError);
  }
}
