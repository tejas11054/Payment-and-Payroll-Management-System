import { Injectable, signal } from '@angular/core';

export interface ConfirmationConfig {
  title: string;
  message: string;
  confirmText?: string;
  cancelText?: string;
  confirmCallback?: () => void;
  cancelCallback?: () => void;
}
@Injectable({
  providedIn: 'root'
})
export class ConfirmationConfigModal {
   
  public isVisible = signal<boolean>(false);
  public config = signal<ConfirmationConfig>({
    title: '',
    message: '',
    confirmText: 'Confirm',
    cancelText: 'Cancel'
  });

  private confirmCallback?: () => void;
  private cancelCallback?: () => void;

  /**
   * Opens a confirmation modal
   */
  confirm(config: ConfirmationConfig): Promise<boolean> {
    return new Promise((resolve) => {
      this.config.set({
        ...config,
        confirmText: config.confirmText || 'Confirm',
        cancelText: config.cancelText || 'Cancel'
      });
      
      this.confirmCallback = () => {
        config.confirmCallback?.();
        resolve(true);
        this.hide();
      };

      this.cancelCallback = () => {
        config.cancelCallback?.();
        resolve(false);
        this.hide();
      };

      this.isVisible.set(true);
    });
  }

  /**
   * User clicked confirm
   */
  onConfirm() {
    this.confirmCallback?.();
  }

  /**
   * User clicked cancel
   */
  onCancel() {
    this.cancelCallback?.();
  }

  /**
   * Hides the modal
   */
  hide() {
    this.isVisible.set(false);
  }
}
