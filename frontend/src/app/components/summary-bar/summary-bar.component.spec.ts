import { ComponentFixture, TestBed } from '@angular/core/testing';

import { SummaryBarComponent } from './summary-bar.component';

describe('SummaryBarComponent', () => {
  let component: SummaryBarComponent;
  let fixture: ComponentFixture<SummaryBarComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ SummaryBarComponent ]
    })
    .compileComponents();

    fixture = TestBed.createComponent(SummaryBarComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
