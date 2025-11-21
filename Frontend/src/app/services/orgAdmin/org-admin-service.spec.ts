import { TestBed } from '@angular/core/testing';

import { OrgAdminService } from './org-admin-service';

describe('OrgAdminService', () => {
  let service: OrgAdminService;

  beforeEach(() => {
    TestBed.configureTestingModule({});
    service = TestBed.inject(OrgAdminService);
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });
});
