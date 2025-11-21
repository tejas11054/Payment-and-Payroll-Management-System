import { Injectable } from '@angular/core';
import { BehaviorSubject } from 'rxjs';

const STORAGE_KEY = 'dark-mode';

@Injectable({ providedIn: 'root' })
export class DarkModeService {
   private subject = new BehaviorSubject<boolean>(this.readInitial());
  isDarkMode$ = this.subject.asObservable();

  private readInitial(): boolean {
    // This check prevents errors during server-side rendering
    if (typeof window === 'undefined' || typeof localStorage === 'undefined') {
      return false;
    }
    const saved = localStorage.getItem(STORAGE_KEY);
    if (saved !== null) return saved === 'true';
    return window.matchMedia?.('(prefers-color-scheme: dark)').matches ?? false;
  }

  set(value: boolean) {
    this.subject.next(value);
    if (typeof document !== 'undefined') {
      localStorage.setItem(STORAGE_KEY, String(value));
      // FIX: Applying class to <html> tag instead of <body>
      document.documentElement.classList.toggle('dark-mode', value);
    }
  }

  toggle() {
    this.set(!this.subject.value);
  }

  get value() {
    return this.subject.value;
  }
}
