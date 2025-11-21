import { ComponentFixture, TestBed } from '@angular/core/testing';

import { OrganizationAdminComponent } from './organization-admin-component';

describe('OrganizationAdminComponent', () => {
  let component: OrganizationAdminComponent;
  let fixture: ComponentFixture<OrganizationAdminComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [OrganizationAdminComponent]
    })
    .compileComponents();

    fixture = TestBed.createComponent(OrganizationAdminComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
