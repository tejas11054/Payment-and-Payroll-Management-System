import { TestBed } from '@angular/core/testing';

import { BankAdmin } from './bank-admin';

describe('BankAdmin', () => {
  let service: BankAdmin;

  beforeEach(() => {
    TestBed.configureTestingModule({});
    service = TestBed.inject(BankAdmin);
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });
});
