import { NgModule } from '@angular/core';
import { MatTabsModule } from '@angular/material/tabs';
import { MatToolbarModule } from '@angular/material/toolbar';

@NgModule({
  imports: [
    MatTabsModule,
    MatToolbarModule,
  ],
  exports: [
    MatTabsModule,
    MatToolbarModule,
  ]
})
export class MaterialModule { }
