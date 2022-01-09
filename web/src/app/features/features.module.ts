import { NgxSliderModule } from '@angular-slider/ngx-slider';
import { CommonModule } from '@angular/common';
import { HttpClientModule } from '@angular/common/http';
import { NgModule } from '@angular/core';
import { FlexLayoutModule } from '@angular/flex-layout';
import { AngularSplitModule } from 'angular-split';
import { LongPressDirective } from '../shared/long-press.directive';

import { MaterialModule } from '../shared/material/material.module';
import { SearchComponent } from './search/search.component';
import { StatisticComponent } from './statistic/statistic.component';

@NgModule({
  declarations: [
    SearchComponent,
    StatisticComponent,
    LongPressDirective,
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
