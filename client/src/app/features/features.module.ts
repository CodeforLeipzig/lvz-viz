import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { HttpClientModule } from '@angular/common/http';
import { FlexLayoutModule } from '@angular/flex-layout';

import { AngularSplitModule } from 'angular-split';

import { SearchComponent } from './search/search.component';
import { MaterialModule } from '../shared/material/material.module';

@NgModule({
  declarations: [
    SearchComponent,
  ],
  exports: [
    SearchComponent,
  ],
  imports: [
    CommonModule,
    HttpClientModule,
    FlexLayoutModule,
    AngularSplitModule,
    MaterialModule,
  ],
})
export class FeaturesModule { }
