import { NgModule } from '@angular/core';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatPaginatorModule } from '@angular/material/paginator';
import { MatTableModule } from '@angular/material/table';
import { MatTabsModule } from '@angular/material/tabs';
import { MatToolbarModule } from '@angular/material/toolbar';

@NgModule({
  imports: [
    MatFormFieldModule,
    MatInputModule,
    MatPaginatorModule,
    MatTableModule,
    MatTabsModule,
    MatToolbarModule,
  ],
  exports: [
    MatFormFieldModule,
    MatInputModule,
    MatPaginatorModule,
    MatTableModule,
    MatTabsModule,
    MatToolbarModule,
  ]
})
export class MaterialModule { }
