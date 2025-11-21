import { TestBed } from '@angular/core/testing';

import { OrgAuthservice } from './org-authservice';

describe('OrgAuthservice', () => {
  let service: OrgAuthservice;

  beforeEach(() => {
    TestBed.configureTestingModule({});
    service = TestBed.inject(OrgAuthservice);
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });
});
