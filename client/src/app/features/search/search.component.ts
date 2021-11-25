import { AfterViewInit, Component, OnInit, ViewChild } from '@angular/core';
import { MatTableDataSource } from '@angular/material/table';

import { SplitComponent } from 'angular-split';
import * as L from 'leaflet';

import { SearchService } from './search.service';

@Component({
  selector: 'lvzviz-search',
  templateUrl: './search.component.html',
  styleUrls: ['./search.component.css'],
})
export class SearchComponent implements OnInit, AfterViewInit {

  displayedColumns: string[] = ['title', 'publication'];
  dataSource = new MatTableDataSource();
  expandedElement!: null;
  private map: any;

  @ViewChild("split") split!: SplitComponent;

  constructor(private searchService: SearchService) { }

  ngOnInit(): void {
    this.searchService.getx().subscribe((response: any) => {
      this.dataSource = new MatTableDataSource(response.content);
    });
  }

  ngAfterViewInit(): void {
    this.initMap();
    this.split.dragProgress$.subscribe(() => {
      this.map.invalidateSize();
    });
  }

  private initMap(): void {
    this.map = L.map('map').setView([51.339695, 12.373075], 11);

    L.tileLayer('https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png', {
      attribution: '&copy; <a href="http://www.openstreetmap.org/copyright">OpenStreetMap</a>'
    }).addTo(this.map);
  }
}
