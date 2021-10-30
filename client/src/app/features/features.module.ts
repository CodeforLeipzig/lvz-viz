import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { HttpClientModule } from '@angular/common/http';

import { SearchComponent } from './search/search.component';


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
  ],
})
export class FeaturesModule { }
