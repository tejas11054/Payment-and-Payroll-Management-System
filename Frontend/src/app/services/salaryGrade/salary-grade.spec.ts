import { TestBed } from '@angular/core/testing';

import { SalaryGrade } from './salary-grade';

describe('SalaryGrade', () => {
  let service: SalaryGrade;

  beforeEach(() => {
    TestBed.configureTestingModule({});
    service = TestBed.inject(SalaryGrade);
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });
});
