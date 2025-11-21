import { TestBed } from '@angular/core/testing';

import { DarkModeService } from './dark-mode';

describe('DarkMode', () => {
  let service: DarkModeService;

  beforeEach(() => {
    TestBed.configureTestingModule({});
    service = TestBed.inject(DarkModeService);
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });
});
