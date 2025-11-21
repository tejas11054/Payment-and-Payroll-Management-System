import { ComponentFixture, TestBed } from '@angular/core/testing';

import { BankAdminAuditLog } from './bank-admin-audit-log';

describe('BankAdminAuditLog', () => {
  let component: BankAdminAuditLog;
  let fixture: ComponentFixture<BankAdminAuditLog>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [BankAdminAuditLog]
    })
    .compileComponents();

    fixture = TestBed.createComponent(BankAdminAuditLog);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
