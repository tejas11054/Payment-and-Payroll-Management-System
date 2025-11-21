import { HttpClient, HttpParams } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';

@Injectable({
  providedIn: 'root'
})
export class EmpServices {
  
  private baseUrl = 'http://localhost:8080/api'; // Adjust based on your backend URL

  constructor(private http: HttpClient) { }


  getEmployeeProfile(empId: number): Observable<any> {
    return this.http.get(`${this.baseUrl}/employees/${empId}`);
  }

  getDashboardStats(empId: number): Observable<any> {
    return this.http.get(`${this.baseUrl}/employees/${empId}/stats`);
  }
getEmployeeById(empId: number, departmentId: number): Observable<any> {
  return this.http.get(`${this.baseUrl}/employees/${empId}/${departmentId}`);
}


  getSalarySlips(empId: number, month?: string, year?: number): Observable<any[]> {
    let params = new HttpParams();
    
    if (month) {
      params = params.set('month', month);
    }
    if (year) {
      params = params.set('year', year.toString());
    }
    
    return this.http.get<any[]>(`${this.baseUrl}/employees/${empId}/salary-slips`, { params });
  }

  getSalarySlipDetails(slipId: number): Observable<any> {
    return this.http.get(`${this.baseUrl}/salary-slip/${slipId}`);
  }

  downloadSalarySlip(slipId: number): Observable<Blob> {
    return this.http.get(`${this.baseUrl}/salary-slip/${slipId}/download`, {
      responseType: 'blob'
    });
  }


  raiseConcern(concernData: any, file: File | null): Observable<any> {
    const formData = new FormData();
    

    const dataBlob = new Blob([JSON.stringify(concernData)], {
      type: 'application/json'
    });
    formData.append('data', dataBlob);
    

    if (file) {
      formData.append('file', file);
    }
    
    return this.http.post(`${this.baseUrl}/concerns`, formData);
  }

  getEmployeeConcerns(empId: number): Observable<any[]> {
    return this.http.get<any[]>(`${this.baseUrl}/employees/${empId}/concerns`);
  }
}
