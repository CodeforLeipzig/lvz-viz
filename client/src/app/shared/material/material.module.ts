import { NgModule } from '@angular/core';
import { MatPaginatorModule } from '@angular/material/paginator';
import { MatTableModule } from '@angular/material/table';
import { MatTabsModule } from '@angular/material/tabs';

@NgModule({
  imports: [
    MatPaginatorModule,
    MatTableModule,
    MatTabsModule,
  ],
  exports: [
    MatPaginatorModule,
    MatTableModule,
    MatTabsModule,
  ]
})
export class MaterialModule { }
