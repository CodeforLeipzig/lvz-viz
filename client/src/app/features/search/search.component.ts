import { animate, state, style, transition, trigger } from '@angular/animations';
import { AfterViewInit, Component, ViewChild } from '@angular/core';
import { MatPaginator } from '@angular/material/paginator';
import { MatTableDataSource } from '@angular/material/table';

import { SplitComponent } from 'angular-split';
import * as L from 'leaflet';
import { catchError, map, of, startWith, switchMap } from 'rxjs';

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
export class SearchComponent implements AfterViewInit {
  displayedColumns: string[] = ['title', 'publication'];
  dataSource = new MatTableDataSource();

  expandedElement: any;
  length = 0;

  private map!: L.Map;
  private markers!: L.FeatureGroup;

  @ViewChild('split') split!: SplitComponent;
  @ViewChild(MatPaginator) paginator!: MatPaginator;

  constructor(private searchService: SearchService) { }

  ngAfterViewInit(): void {
    this.initMap();

    this.split.dragProgress$.subscribe(() => {
      this.map.invalidateSize();
    });

    this.paginator.page.pipe(
      startWith({}),
      switchMap(() => {
        return this.searchService.fetch(this.paginator.pageIndex, this.paginator.pageSize, 'datePublished,desc').pipe(
          catchError(() => of(null))
        );
      }),
      map(data => {
        if (data === null) {
          return [];
        }
        this.length = data.totalElements;
        return data.content;
      }),
    ).subscribe(data => {
      this.addToMap(data);
      this.dataSource = new MatTableDataSource(data);
    });
  }

  initMap(): void {
    /** workaround: images not loaded correctly so images are copied from node_modules leaflet folder into assets folder */
    L.Icon.Default.imagePath = "assets/leaflet/";

    this.map = L.map('mapSearch').setView([51.339695, 12.373075], 11);

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

  displayContent(element: Content): string {
    return element.article.endsWith('...') ? element.snippet : element.article;
  }
}
