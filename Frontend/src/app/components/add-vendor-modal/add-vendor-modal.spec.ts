import { ComponentFixture, TestBed } from '@angular/core/testing';

import { AddVendorModal } from './add-vendor-modal';

describe('AddVendorModal', () => {
  let component: AddVendorModal;
  let fixture: ComponentFixture<AddVendorModal>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [AddVendorModal]
    })
    .compileComponents();

    fixture = TestBed.createComponent(AddVendorModal);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
