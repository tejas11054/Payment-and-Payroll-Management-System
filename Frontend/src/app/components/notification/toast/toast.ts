import { Component, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ToastService } from '../../../services/notification/toast-service';

@Component({
  selector: 'app-toast',
  standalone: true,
  imports: [CommonModule],
  template: `
    <div 
      class="toast-container" 
      [class.show]="toastService.isVisible()" 
      [class.success]="toastService.type() === 'success'" 
      [class.error]="toastService.type() === 'error'"
      [class.warning]="toastService.type() === 'warning'"
      [class.info]="toastService.type() === 'info'">
      
      <!-- Icon -->
      <div class="toast-icon">
        <!-- Success Icon -->
        <svg *ngIf="toastService.type() === 'success'" 
             xmlns="http://www.w3.org/2000/svg" 
             fill="none" 
             viewBox="0 0 24 24" 
             stroke-width="1.5" 
             stroke="currentColor">
          <path stroke-linecap="round" 
                stroke-linejoin="round" 
                d="M9 12.75L11.25 15 15 9.75M21 12a9 9 0 11-18 0 9 9 0 0118 0z" />
        </svg>
        
        <!-- Error Icon -->
        <svg *ngIf="toastService.type() === 'error'" 
             xmlns="http://www.w3.org/2000/svg" 
             fill="none" 
             viewBox="0 0 24 24" 
             stroke-width="1.5" 
             stroke="currentColor">
          <path stroke-linecap="round" 
                stroke-linejoin="round" 
                d="M12 9v3.75m-9.303 3.376c-.866 1.5.217 3.374 1.948 3.374h14.71c1.73 0 2.813-1.874 1.948-3.374L13.949 3.378c-.866-1.5-3.032-1.5-3.898 0L2.697 16.126zM12 15.75h.007v.008H12v-.008z" />
        </svg>
        
        <!-- Warning Icon -->
        <svg *ngIf="toastService.type() === 'warning'" 
             xmlns="http://www.w3.org/2000/svg" 
             fill="none" 
             viewBox="0 0 24 24" 
             stroke-width="1.5" 
             stroke="currentColor">
          <path stroke-linecap="round" 
                stroke-linejoin="round" 
                d="M12 9v3.75m9-.75a9 9 0 11-18 0 9 9 0 0118 0zm-9 3.75h.008v.008H12v-.008z" />
        </svg>
        
        <!-- ✅ NEW: Info Icon -->
        <svg *ngIf="toastService.type() === 'info'" 
             xmlns="http://www.w3.org/2000/svg" 
             fill="none" 
             viewBox="0 0 24 24" 
             stroke-width="1.5" 
             stroke="currentColor">
          <path stroke-linecap="round" 
                stroke-linejoin="round" 
                d="M11.25 11.25l.041-.02a.75.75 0 011.063.852l-.708 2.836a.75.75 0 001.063.853l.041-.021M21 12a9 9 0 11-18 0 9 9 0 0118 0zm-9-3.75h.008v.008H12V8.25z" />
        </svg>
      </div>
      
      <!-- Message -->
      <p class="toast-message">{{ toastService.message() }}</p>
      
      <!-- Close Button -->
      <button class="toast-close-btn" (click)="toastService.hide()">
        <svg xmlns="http://www.w3.org/2000/svg" 
             fill="none" 
             viewBox="0 0 24 24" 
             stroke-width="1.5" 
             stroke="currentColor">
          <path stroke-linecap="round" 
                stroke-linejoin="round" 
                d="M6 18L18 6M6 6l12 12" />
        </svg>
      </button>
    </div>
  `,
  styles: [`
    .toast-container {
      position: fixed;
      top: 20px;
      right: 20px;
      display: flex;
      align-items: center;
      padding: 1rem 1.5rem;
      border-radius: 8px;
      box-shadow: 0 4px 12px rgba(0, 0, 0, 0.15);
      min-width: 300px;
      max-width: 450px;
      z-index: 9999;
      transform: translateX(calc(100% + 20px));
      transition: transform 0.5s cubic-bezier(0.25, 1, 0.5, 1);
      color: white;
      font-size: 0.9375rem;
      line-height: 1.5;
    }

    .toast-container.show {
      transform: translateX(0);
      animation: slideIn 0.5s cubic-bezier(0.25, 1, 0.5, 1);
    }

    /* Success - Green */
    .toast-container.success { 
      background: linear-gradient(135deg, #22c55e 0%, #16a34a 100%);
    }

    /* Error - Red */
    .toast-container.error { 
      background: linear-gradient(135deg, #ef4444 0%, #dc2626 100%);
    }

    /* Warning - Orange/Yellow */
    .toast-container.warning { 
      background: linear-gradient(135deg, #f59e0b 0%, #d97706 100%);
    }

    /* ✅ NEW: Info - Blue */
    .toast-container.info { 
      background: linear-gradient(135deg, #3b82f6 0%, #2563eb 100%);
    }

    .toast-icon { 
      margin-right: 1rem;
      flex-shrink: 0;
    }

    .toast-icon svg { 
      width: 24px; 
      height: 24px;
      filter: drop-shadow(0 2px 4px rgba(0, 0, 0, 0.1));
    }

    .toast-message { 
      flex-grow: 1; 
      margin: 0; 
      font-weight: 500;
      word-wrap: break-word;
      overflow-wrap: break-word;
    }

    .toast-close-btn { 
      background: none; 
      border: none; 
      color: white; 
      cursor: pointer; 
      margin-left: 1rem; 
      padding: 0; 
      opacity: 0.7;
      flex-shrink: 0;
      transition: opacity 0.2s ease, transform 0.2s ease;
    }

    .toast-close-btn:hover { 
      opacity: 1;
      transform: scale(1.1);
    }

    .toast-close-btn svg { 
      width: 20px; 
      height: 20px; 
    }

    /* Animation */
    @keyframes slideIn {
      from {
        transform: translateX(calc(100% + 20px));
        opacity: 0;
      }
      to {
        transform: translateX(0);
        opacity: 1;
      }
    }

    /* Responsive */
    @media (max-width: 640px) {
      .toast-container {
        left: 20px;
        right: 20px;
        min-width: auto;
        max-width: none;
        transform: translateY(-100px);
        top: auto;
        bottom: 20px;
      }

      .toast-container.show {
        transform: translateY(0);
      }

      @keyframes slideIn {
        from {
          transform: translateY(-100px);
          opacity: 0;
        }
        to {
          transform: translateY(0);
          opacity: 1;
        }
      }
    }
  `]
})
export class ToastComponent {
  public toastService = inject(ToastService);
}
