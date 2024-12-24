import { Component, Inject } from '@angular/core';
import { Title } from '@angular/platform-browser';

import { DOCUMENT } from '@angular/common';
import { MatTabsModule } from '@angular/material/tabs';
import { MatToolbarModule } from '@angular/material/toolbar';
import { environment } from '../environments/environment';
import { SearchComponent } from './features/search/search.component';
import { StatisticComponent } from './features/statistic/statistic.component';

@Component({
  selector: 'lvzviz-root',
  templateUrl: './app.component.html',
  styleUrls: ['./app.component.css'],
  imports: [MatTabsModule, MatToolbarModule, SearchComponent, StatisticComponent],
})
export class AppComponent {
  appname: string;

  public constructor(private titleService: Title, @Inject(DOCUMENT) private document: Document) {
    this.appname = environment.appname;
    this.titleService.setTitle(this.appname);
    this.document.body.classList.add(`${environment.theme}-theme`);
  }
}
