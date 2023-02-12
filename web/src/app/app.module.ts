import { OverlayModule } from '@angular/cdk/overlay';
import { NgModule } from '@angular/core';
import { BrowserModule } from '@angular/platform-browser';
import { BrowserAnimationsModule } from '@angular/platform-browser/animations';

import { AppComponent } from './app.component';
import { FeaturesModule } from './features/features.module';
import { MaterialModule } from './shared/material/material.module';

@NgModule({
  declarations: [
    AppComponent,
  ],
  imports: [
    BrowserAnimationsModule,
    BrowserModule,
    OverlayModule,
    MaterialModule,
    FeaturesModule,
  ],
  providers: [],
  bootstrap: [
    AppComponent,
  ],
})
export class AppModule { }
