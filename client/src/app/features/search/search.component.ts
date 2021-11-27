import { animate, state, style, transition, trigger } from '@angular/animations';
import { AfterViewInit, Component, OnInit, ViewChild } from '@angular/core';
import { MatPaginator } from '@angular/material/paginator';
import { MatTableDataSource } from '@angular/material/table';

import { SplitComponent } from 'angular-split';
import * as L from 'leaflet';

import { Content } from './content.model';
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
export class SearchComponent implements OnInit, AfterViewInit {
  displayedColumns: string[] = ['title', 'publication'];
  dataSource = new MatTableDataSource();

  expandedElement: any;

  private map!: L.Map;
  private markers!: L.FeatureGroup;

  @ViewChild('split') split!: SplitComponent;
  @ViewChild(MatPaginator) paginator!: MatPaginator;

  constructor(private searchService: SearchService) { }

  ngOnInit(): void {
    /** workaround: images not loaded correctly so images are copied from node_modules leaflet folder into assets folder */
    L.Icon.Default.imagePath = "assets/leaflet/";
  }

  ngAfterViewInit(): void {
    this.searchService.getx().subscribe((response: any) => {
      this.dataSource = new MatTableDataSource(response.content);
      this.dataSource.paginator = this.paginator;
      this.initMap();
      this.addToMap(response.content);
    });

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

  private addToMap(content: Content[]): void {
    if (this.markers) {
      this.map.removeLayer(this.markers);
    }
    this.markers = new L.FeatureGroup();
    content.forEach((c) => {
      if (c.coords) {
        var marker = L.marker([c.coords.lat, c.coords.lon]).bindPopup('<a href=' + c.url + '>' + c.title + '</a><br>' + c.snippet);
        this.markers.addLayer(marker);
      }
    });
    this.map.addLayer(this.markers);
  }
}
