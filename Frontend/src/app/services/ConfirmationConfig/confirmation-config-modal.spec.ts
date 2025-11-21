import { TestBed } from '@angular/core/testing';

import { ConfirmationConfigModal } from './confirmation-config-modal';

describe('ConfirmationConfigModal', () => {
  let service: ConfirmationConfigModal;

  beforeEach(() => {
    TestBed.configureTestingModule({});
    service = TestBed.inject(ConfirmationConfigModal);
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });
});
