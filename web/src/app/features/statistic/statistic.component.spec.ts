import { HttpClientTestingModule } from '@angular/common/http/testing';
import { ComponentFixture, TestBed } from '@angular/core/testing';

/**
 * Imports in this order needed
 */
import * as L from 'leaflet';
import 'leaflet.heat/dist/leaflet-heat.js';

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
    /**
     * workaround:
     * 
     * Import both leaflet and leaflet-heat to avoid error
     * ReferenceError: L is not defined
     * 
     * Mock heatLayer to avoid error
     * TypeError: L.heatLayer is not a function
     */
    (L as any).heatLayer = jest.fn();
    expect(component).toBeTruthy();
  });
});
