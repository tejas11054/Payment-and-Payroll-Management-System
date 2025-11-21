import { TestBed } from '@angular/core/testing';

import { EmpServices } from './emp-services';

describe('EmpServices', () => {
  let service: EmpServices;

  beforeEach(() => {
    TestBed.configureTestingModule({});
    service = TestBed.inject(EmpServices);
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });
});
