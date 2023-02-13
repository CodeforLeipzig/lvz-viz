import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';

import { AngularSplitModule } from 'angular-split';

import { SearchComponent } from './search/search.component';
import { HttpClientModule } from '@angular/common/http';
import { MaterialModule } from '../shared/material/material.module';

@NgModule({
  declarations: [
    SearchComponent,
  ],
  imports: [
    CommonModule,
    HttpClientModule,
    AngularSplitModule,
    MaterialModule,
  ],
  exports: [
    SearchComponent,
  ]
})
export class FeaturesModule { }
