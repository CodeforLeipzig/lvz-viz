import { Component, inject, DOCUMENT } from '@angular/core';
import { MatTabsModule } from '@angular/material/tabs';
import { MatToolbarModule } from '@angular/material/toolbar';
import { Title } from '@angular/platform-browser';
import { environment } from '../environments/environment';
import { SearchComponent } from './features/search/search.component';
import { StatisticComponent } from './features/statistic/statistic.component';

@Component({
  selector: 'lvzviz-root',
  templateUrl: './app.component.html',
  styleUrl: './app.component.css',
  imports: [MatTabsModule, MatToolbarModule, SearchComponent, StatisticComponent],
})
export class AppComponent {
  private titleService = inject(Title);
  private document = inject<Document>(DOCUMENT);

  appname: string;

  public constructor() {
    this.appname = environment.appname;
    this.titleService.setTitle(this.appname);
    this.document.body.classList.add(`${environment.theme}-theme`);
  }
}
