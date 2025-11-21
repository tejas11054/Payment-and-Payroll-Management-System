import { TestBed } from '@angular/core/testing';

import { BankadminService } from './bankadmin-service';

describe('BankadminService', () => {
  let service: BankadminService;

  beforeEach(() => {
    TestBed.configureTestingModule({});
    service = TestBed.inject(BankadminService);
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });
});
