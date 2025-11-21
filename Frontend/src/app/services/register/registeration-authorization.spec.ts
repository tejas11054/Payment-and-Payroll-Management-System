import { TestBed } from '@angular/core/testing';

import { RegisterationAuthorization } from './registeration-authorization';

describe('RegisterationAuthorization', () => {
  let service: RegisterationAuthorization;

  beforeEach(() => {
    TestBed.configureTestingModule({});
    service = TestBed.inject(RegisterationAuthorization);
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });
});
