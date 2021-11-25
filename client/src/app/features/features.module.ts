import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { HttpClientModule } from '@angular/common/http';

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
    AngularSplitModule,
    MaterialModule,
  ],
})
export class FeaturesModule { }
