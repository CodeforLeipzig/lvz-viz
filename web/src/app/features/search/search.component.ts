import { animate, state, style, transition, trigger } from '@angular/animations';
import { BreakpointObserver, Breakpoints } from '@angular/cdk/layout';
import { AfterViewInit, Component, ElementRef, OnDestroy, OnInit, ViewChild } from '@angular/core';
import { MatPaginator } from '@angular/material/paginator';
import { MatTableDataSource } from '@angular/material/table';
import { SplitComponent } from 'angular-split';

import * as L from 'leaflet';
import { debounceTime, distinctUntilChanged, fromEvent, map, merge, startWith, Subject, switchMap, takeUntil, tap } from 'rxjs';

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
export class SearchComponent implements AfterViewInit, OnInit, OnDestroy {
  displayedColumns: string[] = ['title', 'publication'];
  dataSource = new MatTableDataSource<Content>();

  destroyed = new Subject<void>();
  smallSize = false;

  expandedElement: any;

  length = 0;
  author = '';

  private map!: L.Map;
  private markers!: L.FeatureGroup;

  @ViewChild('split') split!: SplitComponent;
  @ViewChild('input') input!: ElementRef;
  @ViewChild(MatPaginator) paginator!: MatPaginator;

  constructor(private breakpointObserver: BreakpointObserver, private searchService: SearchService) {
    this.breakpointObserver
      .observe([Breakpoints.XSmall, Breakpoints.Small, Breakpoints.Medium, Breakpoints.Large, Breakpoints.XLarge])
      .pipe(takeUntil(this.destroyed))
      .subscribe((result) => {
        for (const query of Object.keys(result.breakpoints)) {
          if (result.breakpoints[query]) {
            this.smallSize = query === Breakpoints.XSmall || query === Breakpoints.Small ? true : false;
          }
        }
      });
  }

  ngOnInit(): void {
    /**
     * workaround:
     * Images not loaded correctly: marker-shadow.png 404.
     * So images are copied from node_modules leaflet folder into assets folder.
     */
    L.Icon.Default.imagePath = 'assets/leaflet/';
  }

  ngAfterViewInit(): void {
    this.initMap();
    this.initSplit();
    this.loadContent();
  }

  ngOnDestroy() {
    this.destroyed.next();
    this.destroyed.complete();
  }

  /**
   * Initialize split view.
   */
  private initSplit(): void {
    this.split.dragProgress$.subscribe(() => {
      this.map.invalidateSize();
    });
  }

  /**
   * Loads content from backend with parameters which contains much more information and extract the content of articles.
   */
  private loadContent(): void {
    const filter = fromEvent(this.input.nativeElement, 'keyup').pipe(
      debounceTime(500),
      distinctUntilChanged(),
      tap(() => this.paginator.firstPage())
    );

    merge(filter, this.paginator.page)
      .pipe(
        takeUntil(this.destroyed),
        startWith({}),
        switchMap(() => {
          return this.searchService
            .fetch(this.paginator.pageIndex, this.paginator.pageSize, 'datePublished,desc', this.input.nativeElement.value)
            .pipe(
              map((data) => {
                this.length = data.totalElements;
                return data.content;
              })
            );
        })
      )
      .subscribe((content) => {
        this.addToMap(content);
        this.dataSource = new MatTableDataSource(content);
      });
  }

  /**
   * Returns the query from the input field or undefined if the value is empty.
   *
   * @returns string | undefined
   */
  query(): string | undefined {
    const input = (this.input.nativeElement.value as string).trim();
    return input !== '' ? input : undefined;
  }

  /**
   * Initialize map.
   */
  initMap(): void {
    this.map = L.map('map-search').setView([51.339695, 12.373075], 11);

    L.tileLayer('https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png', {
      attribution: '&copy; <a href="http://www.openstreetmap.org/copyright">OpenStreetMap</a>',
    }).addTo(this.map);
  }

  /**
   * Integrates content as marker and popup information returned from the server into the initialized map.
   *
   * @param content
   */
  private addToMap(content: Content[]): void {
    if (this.markers) {
      this.map.removeLayer(this.markers);
    }
    this.markers = new L.FeatureGroup();
    content.forEach((c) => {
      if (c.coords) {
        var marker = L.marker([c.coords.lat, c.coords.lon]).bindPopup(`<a href="${c.url}">${c.title}</a><br>${c.snippet}`);
        this.markers.addLayer(marker);
      }
    });
    this.map.addLayer(this.markers);
  }

  /**
   * Returns the snippet if the content is LVZ+ or the full article if the content is not LVZ+.
   *
   * @param element
   * @returns string
   */
  displayContent(element: Content): string {
    let article = element.article ? element.article : element.snippet;
    this.author = article.substring(article.lastIndexOf('.') + 2, article.length);
    return article.substring(0, article.lastIndexOf('.') + 1);
  }
}
