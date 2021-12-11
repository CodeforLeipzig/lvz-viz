import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { HttpClientModule } from '@angular/common/http';
import { FlexLayoutModule } from '@angular/flex-layout';

import { NgxSliderModule } from '@angular-slider/ngx-slider';
import { AngularSplitModule } from 'angular-split';

import { MaterialModule } from '../shared/material/material.module';
import { SearchComponent } from './search/search.component';
import { StatisticComponent } from './statistic/statistic.component';

@NgModule({
  declarations: [
    SearchComponent,
    StatisticComponent,
  ],
  exports: [
    SearchComponent,
    StatisticComponent,
  ],
  imports: [
    CommonModule,
    HttpClientModule,
    FlexLayoutModule,
    AngularSplitModule,
    MaterialModule,
    NgxSliderModule,
  ],
})
export class FeaturesModule { }
