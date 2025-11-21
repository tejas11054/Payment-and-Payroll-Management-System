import { ComponentFixture, TestBed } from '@angular/core/testing';

import { RegsiterPageComponent } from './regsiter-page-component';

describe('RegsiterPageComponent', () => {
  let component: RegsiterPageComponent;
  let fixture: ComponentFixture<RegsiterPageComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [RegsiterPageComponent]
    })
    .compileComponents();

    fixture = TestBed.createComponent(RegsiterPageComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
