import { CommonModule } from '@angular/common';
import { HttpClientModule } from '@angular/common/http';
import { NgModule } from '@angular/core';

import { NgxSliderModule } from '@angular-slider/ngx-slider';
import { AngularSplitModule } from 'angular-split';

import { LongPressDirective } from '../shared/material/long-press.directive';
import { MaterialModule } from '../shared/material/material.module';
import { SearchComponent } from './search/search.component';
import { StatisticComponent } from './statistic/statistic.component';

@NgModule({
  declarations: [SearchComponent, StatisticComponent, LongPressDirective],
  imports: [CommonModule, HttpClientModule, NgxSliderModule, AngularSplitModule, MaterialModule],
  exports: [SearchComponent, StatisticComponent],
  providers: [
    // { provide: SearchService, useClass: SearchServiceMock },
  ],
})
export class FeaturesModule {}
