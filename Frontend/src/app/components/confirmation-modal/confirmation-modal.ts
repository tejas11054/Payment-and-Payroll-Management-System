import { Component, inject } from '@angular/core';
import { ConfirmationConfigModal } from '../../services/ConfirmationConfig/confirmation-config-modal';
import { FormsModule } from '@angular/forms';
import { CommonModule } from '@angular/common';

@Component({
  selector: 'app-confirmation-modal',
  imports: [FormsModule,CommonModule],
  templateUrl: './confirmation-modal.html',
  styleUrl: './confirmation-modal.css'
})
export class ConfirmationModal {
confirmService = inject(ConfirmationConfigModal);
}
