import { HttpClientTestingModule } from '@angular/common/http/testing';
import { ComponentFixture, TestBed } from '@angular/core/testing';

import { StatisticComponent } from './statistic.component';
import { MaterialModule } from 'src/app/shared/material/material.module';

describe('StatisticComponent', () => {
  let component: StatisticComponent;
  let fixture: ComponentFixture<StatisticComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [StatisticComponent],
      imports: [HttpClientTestingModule, MaterialModule],
    }).compileComponents();
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(StatisticComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
