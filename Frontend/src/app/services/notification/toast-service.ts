import { Injectable, signal, effect } from '@angular/core';

@Injectable({
  providedIn: 'root'
})
export class ToastService {
  
  // ✅ Updated: Added 'warning' type
  public message = signal<string>('');
  public type = signal<'success' | 'error' | 'warning'| 'info'>('success');
  public isVisible = signal<boolean>(false);

  private toastTimer: any;

  constructor() {
    effect(() => {
      if (this.isVisible()) {
        if (this.toastTimer) {
          clearTimeout(this.toastTimer);
        }
        // ✅ Warning messages stay longer (7 seconds)
         const duration = (this.type() === 'warning' || this.type() === 'info') ? 7000 : 5000;
        
        this.toastTimer = setTimeout(() => {
          this.hide();
        }, duration);
      }
    });
  }

  /**
   * Shows a toast notification
   * @param message - The message to display
   * @param type - 'success', 'error', or 'warning'
   */
  show(message: string, type: 'success' | 'error' | 'warning' | 'info'= 'success') {
    this.message.set(message);
    this.type.set(type);
    this.isVisible.set(true);
  }

  /**
   * Hides the currently visible toast
   */
  hide() {
    this.isVisible.set(false);
  }
}
