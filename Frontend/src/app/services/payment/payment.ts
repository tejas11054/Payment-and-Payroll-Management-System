import { HttpClient, HttpParams } from '@angular/common/http';
import { inject, Injectable } from '@angular/core';
import { Observable } from 'rxjs';

@Injectable({
  providedIn: 'root'
})
export class Payment {
    private http = inject(HttpClient);
  private baseUrl = 'http://localhost:8080/api/payments';

  /**
   * Mock payment - Add balance directly
   */
  mockPayment(orgId: number, amount: number): Observable<any> {
    const params = new HttpParams()
      .set('orgId', orgId.toString())
      .set('amount', amount.toString());

    return this.http.post(`${this.baseUrl}/mock-payment`, null, { params });
  }

  /**
   * Get current balance
   */
  getBalance(orgId: number): Observable<any> {
    return this.http.get(`${this.baseUrl}/balance/${orgId}`);
  }

  /**
   * Health check
   */
  healthCheck(): Observable<any> {
    return this.http.get(`${this.baseUrl}/health`);
  }
}
