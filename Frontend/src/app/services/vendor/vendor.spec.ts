import { TestBed } from '@angular/core/testing';

import { VendorService } from './vendor';

describe('Vendor', () => {
  let service: VendorService;

  beforeEach(() => {
    TestBed.configureTestingModule({});
    service = TestBed.inject(VendorService);
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });
});
