// notification.component.ts
import { Component, OnInit, ChangeDetectorRef } from '@angular/core';

import { CommonModule } from '@angular/common';
import { AppNotification, NotificationService } from '../../services/notification/notification-service';
import { FormsModule, ReactiveFormsModule } from '@angular/forms';

@Component({
  selector: 'app-notification',
  standalone: true,
  imports: [CommonModule,ReactiveFormsModule,FormsModule],
  templateUrl: './notification-component.html',
  styleUrls: ['./notification-component.css']
})
export class NotificationComponent implements OnInit {
  notifications: AppNotification[] = []; // ✅ Changed type
  unreadCount: number = 0;
  isDropdownOpen: boolean = false;
  isLoading: boolean = false;

  constructor(
    private notificationService: NotificationService,
    private cdr: ChangeDetectorRef
  ) {}

  ngOnInit(): void {
    this.loadNotifications();
    this.loadUnreadCount();
    
    this.notificationService.unreadCount$.subscribe(count => {
      this.unreadCount = count;
      this.cdr.markForCheck();
    });
  }

  loadNotifications(): void {
    this.isLoading = true;
    this.notificationService.getNotifications().subscribe({
      next: (notifications) => {
        this.notifications = notifications;
        this.isLoading = false;
        this.cdr.markForCheck();
      },
      error: (error) => {
        console.error('Error loading notifications:', error);
        this.isLoading = false;
      }
    });
  }

  loadUnreadCount(): void {
    this.notificationService.getUnreadCount().subscribe();
  }

  toggleDropdown(): void {
    this.isDropdownOpen = !this.isDropdownOpen;
    if (this.isDropdownOpen) {
      this.loadNotifications();
    }
  }

  markAsRead(notification: AppNotification, event: Event): void {
    event.stopPropagation();
    
    if (notification.status === 'UNREAD') {
      this.notificationService.markAsRead(notification.notificationId).subscribe({
        next: () => {
          notification.status = 'READ';
          this.cdr.markForCheck();
        },
        error: (error) => {
          console.error('Error marking notification as read:', error);
        }
      });
    }
  }

  markAllAsRead(): void {
    this.notificationService.markAllAsRead().subscribe({
      next: () => {
        this.notifications.forEach(n => n.status = 'READ');
        this.cdr.markForCheck();
      },
      error: (error) => {
        console.error('Error marking all as read:', error);
      }
    });
  }

  getNotificationIcon(type: string): string {
    switch(type) {
      case 'SALARY_DISBURSAL': return '💰';
      case 'SALARY_APPROVED': return '✅';
      case 'SALARY_REJECTED': return '❌';
      default: return '🔔';
    }
  }

  getPriorityClass(priority: string): string {
    return `priority-${priority?.toLowerCase() || 'medium'}`;
  }

  getTimeAgo(sentAt: Date): string {
    const now = new Date();
    const notifDate = new Date(sentAt);
    const diffMs = now.getTime() - notifDate.getTime();
    const diffMins = Math.floor(diffMs / 60000);
    
    if (diffMins < 1) return 'Just now';
    if (diffMins < 60) return `${diffMins}m ago`;
    
    const diffHours = Math.floor(diffMins / 60);
    if (diffHours < 24) return `${diffHours}h ago`;
    
    const diffDays = Math.floor(diffHours / 24);
    if (diffDays < 7) return `${diffDays}d ago`;
    
    return notifDate.toLocaleDateString();
  }

  closeDropdown(): void {
    this.isDropdownOpen = false;
  }
  selectedNotification: AppNotification | null = null;

openNotificationDetail(notification: AppNotification, event: Event): void {
  event.stopPropagation();
  this.selectedNotification = notification;
  
  // Mark as read
  if (notification.status === 'UNREAD') {
    this.markAsRead(notification, event);
  }
}

closeNotificationDetail(): void {
  this.selectedNotification = null;
}
}
