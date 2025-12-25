import { DOCUMENT } from '@angular/common';
import { Component, inject } from '@angular/core';
import { MatTabsModule } from '@angular/material/tabs';
import { MatToolbarModule } from '@angular/material/toolbar';
import { Title } from '@angular/platform-browser';
import { environment } from '../environments/environment';
import { Search } from './features/search/search';
import { Statistic } from './features/statistic/statistic';

@Component({
  selector: 'app-root',
  templateUrl: './app.html',
  styleUrls: ['./app.css'],
  imports: [Search, Statistic, MatTabsModule, MatToolbarModule],
})
export class App {
  readonly #titleService = inject(Title);
  readonly #document = inject<Document>(DOCUMENT);

  appname: string;

  constructor() {
    this.appname = environment.appname;
    this.#titleService.setTitle(this.appname);
    this.#document.body.classList.add(`${environment.theme}-theme`);
  }
}
