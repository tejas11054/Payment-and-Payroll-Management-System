  // notification.service.ts
  import { Injectable } from '@angular/core';
  import { HttpClient } from '@angular/common/http';
  import { Observable, BehaviorSubject, interval } from 'rxjs';
  import { switchMap, tap, catchError } from 'rxjs/operators';
  import { of } from 'rxjs';

  export interface AppNotification {
    notificationId: number;
    subject: string;
    bodySummary: string;
    type: string;
    status: string;
    relatedId: number;
    relatedEntityType: string;
    priority: string;
    sentAt: Date;
  }

  @Injectable({
    providedIn: 'root'
  })
  export class NotificationService {
    private apiUrl = 'http://localhost:8080/api/notifications'; // ✅ HTTP not HTTPS
    
    private unreadCountSubject = new BehaviorSubject<number>(0);
    public unreadCount$ = this.unreadCountSubject.asObservable();
    
    private notificationsSubject = new BehaviorSubject<AppNotification[]>([]);
    public notifications$ = this.notificationsSubject.asObservable();

    constructor(private http: HttpClient) {
      console.log('🔔 Notification Service Initialized');
      this.startAutoRefresh();
      
      // ✅ Load initial count
      this.refreshUnreadCount();
    }

    getNotifications(): Observable<AppNotification[]> {
      console.log('📡 Fetching notifications...');
      return this.http.get<AppNotification[]>(this.apiUrl).pipe(
        tap(notifications => {
          console.log('✅ Received notifications:', notifications);
          this.notificationsSubject.next(notifications);
        }),
        catchError(error => {
          console.error('❌ Error fetching notifications:', error);
          return of([]);
        })
      );
    }

    getUnreadNotifications(): Observable<AppNotification[]> {
      return this.http.get<AppNotification[]>(`${this.apiUrl}/unread`).pipe(
        catchError(error => {
          console.error('❌ Error fetching unread notifications:', error);
          return of([]);
        })
      );
    }

    getUnreadCount(): Observable<{ count: number }> {
      console.log('📡 Fetching unread count...');
      return this.http.get<{ count: number }>(`${this.apiUrl}/unread-count`).pipe(
        tap(response => {
          console.log('✅ Unread count:', response.count);
          this.unreadCountSubject.next(response.count);
        }),
        catchError(error => {
          console.error('❌ Error fetching unread count:', error);
          return of({ count: 0 });
        })
      );
    }

    markAsRead(notificationId: number): Observable<any> {
      return this.http.put(`${this.apiUrl}/${notificationId}/mark-read`, {}).pipe(
        tap(() => {
          console.log('✅ Marked notification as read:', notificationId);
          this.refreshUnreadCount();
          this.refreshNotifications();
        }),
        catchError(error => {
          console.error('❌ Error marking as read:', error);
          return of(null);
        })
      );
    }

    markAllAsRead(): Observable<any> {
      return this.http.put(`${this.apiUrl}/mark-all-read`, {}).pipe(
        tap(() => {
          console.log('✅ Marked all notifications as read');
          this.refreshUnreadCount();
          this.refreshNotifications();
        }),
        catchError(error => {
          console.error('❌ Error marking all as read:', error);
          return of(null);
        })
      );
    }

    deleteNotification(notificationId: number): Observable<any> {
      return this.http.delete(`${this.apiUrl}/${notificationId}`).pipe(
        tap(() => {
          this.refreshUnreadCount();
          this.refreshNotifications();
        })
      );
    }

    refreshUnreadCount(): void {
      this.getUnreadCount().subscribe();
    }

    refreshNotifications(): void {
      this.getNotifications().subscribe();
    }

    private startAutoRefresh(): void {
      console.log('🔄 Starting auto-refresh (30s interval)');
      interval(30000)
        .pipe(
          switchMap(() => this.getUnreadCount()),
          catchError(error => {
            console.error('❌ Auto-refresh error:', error);
            return of({ count: 0 });
          })
        )
        .subscribe();
    }
  }
