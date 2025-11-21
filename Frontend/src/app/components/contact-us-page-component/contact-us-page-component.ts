// contact-us-page-component.ts
import { CommonModule } from '@angular/common';
import { Component, HostBinding } from '@angular/core';
import { FormBuilder, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { RouterLink } from '@angular/router';
import { DarkModeService } from '../../services/darkMode/dark-mode';
import { ToastService } from '../../services/notification/toast-service';



@Component({
  selector: 'app-contact-us-page-component',
  standalone: true,
  imports: [CommonModule, RouterLink, ReactiveFormsModule],
  templateUrl: './contact-us-page-component.html',
  styleUrl: './contact-us-page-component.css'
})
export class ContactUsPageComponent {
  contactForm!: FormGroup;
  isLoading = false;

  @HostBinding('class.dark-mode') darkHost = false;

  constructor(private fb: FormBuilder, private dark: DarkModeService,private toastservice:ToastService) {
    this.dark.set(this.dark.value);
    this.dark.isDarkMode$.subscribe(v => this.darkHost = v);
  }

  ngOnInit(): void {
    this.contactForm = this.fb.group({
      name: ['', Validators.required],
      email: ['', [Validators.required, Validators.email]],
      subject: ['', Validators.required],
      message: ['', Validators.required],
    });
  }

  get isDarkMode(): boolean { return this.dark.value; }
  toggleTheme() { this.dark.toggle(); }

  onSubmit(): void {
    if (this.contactForm.invalid) {
      this.contactForm.markAllAsTouched();
      return;
    }
    this.isLoading = true;
    setTimeout(() => {
      this.isLoading = false;
      this.toastservice.show('Thank you for your message! We will get back to you shortly.','success');
      this.contactForm.reset();
    }, 1500);
  }
}
