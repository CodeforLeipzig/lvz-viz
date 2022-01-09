import { NgxSliderModule } from '@angular-slider/ngx-slider';
import { TestBed } from '@angular/core/testing';

import { FeaturesModule } from './features.module';

describe('FeaturesModule', () => {
  let featuresModule: FeaturesModule;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [NgxSliderModule],
    }).compileComponents();
  });

  beforeEach(() => {
    featuresModule = new FeaturesModule();
  });

  it('should create an instance', () => {
    expect(featuresModule).toBeTruthy();
  });
});
