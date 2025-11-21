import { CommonModule } from '@angular/common';
import { Component, HostBinding, signal } from '@angular/core';
import { RouterLink } from '@angular/router';
import { DarkModeService } from '../../services/darkMode/dark-mode';

@Component({
  selector: 'app-landing-page-component',
  imports: [CommonModule, RouterLink],
  templateUrl: './landing-page-component.html',
  styleUrl: './landing-page-component.css'
})
export class LandingPageComponent {
 // --- ✅ MOBILE MENU LOGIC ---
  isMenuOpen = signal(false);

  toggleMenu(): void {
    this.isMenuOpen.update((value) => !value);
  }

  // // --- ✨ DARK MODE LOGIC ---
  // isDarkMode = false;

  // @HostBinding('class.dark-mode') get darkMode() {
  //   return this.isDarkMode;
  // }

  // toggleTheme() {
  //   this.isDarkMode = !this.isDarkMode;
  // }
   @HostBinding('class.dark-mode') darkHost = false;

  constructor(private dark: DarkModeService) {
    // init body class and host binding
    this.dark.set(this.dark.value);
    this.dark.isDarkMode$.subscribe(v => this.darkHost = v);
  }

  get isDarkMode(): boolean { return this.dark.value; }
  toggleTheme() { this.dark.toggle(); }
}
