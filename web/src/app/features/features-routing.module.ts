import { NgModule } from '@angular/core';
import { RouterModule, Routes } from '@angular/router';

import { SearchComponent } from './search/search.component';
import { environment } from '../../environments/environment';

const routes: Routes = [{
  component: SearchComponent,
  path: environment.defaultRoute,
}];

@NgModule({
  imports: [
    RouterModule.forChild(routes),
  ],
  exports: [
    RouterModule,
  ],
})
export class FeaturesRoutingModule {

  static ROUTES = routes;
}
