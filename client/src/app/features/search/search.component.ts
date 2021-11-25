import { animate, state, style, transition, trigger } from '@angular/animations';
import { AfterViewInit, Component, ViewChild } from '@angular/core';
import { MatTableDataSource } from '@angular/material/table';

import { SplitComponent } from 'angular-split';
import * as L from 'leaflet';

import { SearchService } from './search.service';

@Component({
  selector: 'lvzviz-search',
  templateUrl: './search.component.html',
  styleUrls: ['./search.component.css'],
  animations: [
    trigger('detailExpand', [
      state('collapsed', style({ height: '0px' })),
      state('expanded', style({ height: '*' })),
      transition('expanded <=> collapsed', animate('225ms cubic-bezier(0.4, 0.0, 0.2, 1)')),
    ]),
  ],
})
export class SearchComponent implements AfterViewInit {
  displayedColumns: string[] = ['title', 'publication'];
  dataSource = new MatTableDataSource();
  expandedElement: any;
  private map: any;

  @ViewChild('split') split!: SplitComponent;

  constructor(private searchService: SearchService) {}

  ngAfterViewInit(): void {
    this.searchService.getx().subscribe((response: any) => {
      this.dataSource = new MatTableDataSource(response.content);
    });

    this.initMap();
    this.split.dragProgress$.subscribe(() => {
      this.map.invalidateSize();
    });
  }

  private initMap(): void {
    this.map = L.map('map').setView([51.339695, 12.373075], 11);

    L.tileLayer('https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png', {
      attribution: '&copy; <a href="http://www.openstreetmap.org/copyright">OpenStreetMap</a>',
    }).addTo(this.map);
  }
}
