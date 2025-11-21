// about-us-page-component.ts
import { CommonModule } from '@angular/common';
import { Component, HostBinding, inject } from '@angular/core';
import { RouterLink } from '@angular/router';
import { DarkModeService } from '../../services/darkMode/dark-mode';



@Component({
  selector: 'app-about-us-page-component',
  standalone: true,
  imports: [CommonModule,RouterLink],
  templateUrl: './about-us-page-component.html',
  styleUrl: './about-us-page-component.css'
})
export class AboutUsPageComponent {
  @HostBinding('class.dark-mode') darkHost = false;
  // public darkModeService = inject(DarkModeService);
  constructor(private dark: DarkModeService) {
    this.dark.set(this.dark.value);
    this.dark.isDarkMode$.subscribe(v => this.darkHost = v);
  }

  get isDarkMode(): boolean { return this.dark.value; }
  toggleTheme() { this.dark.toggle(); }
}
