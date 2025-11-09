import { provideHttpClient } from '@angular/common/http';
import { ApplicationConfig, provideBrowserGlobalErrorListeners, provideZonelessChangeDetection } from '@angular/core';

export const appConfig: ApplicationConfig = {
  providers: [provideHttpClient(), provideBrowserGlobalErrorListeners(), provideZonelessChangeDetection()],
};
