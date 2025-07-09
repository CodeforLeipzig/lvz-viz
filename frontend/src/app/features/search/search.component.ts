import { animate, state, style, transition, trigger } from '@angular/animations';
import { BreakpointObserver, Breakpoints } from '@angular/cdk/layout';
import { DatePipe, NgIf } from '@angular/common';
import { AfterViewInit, Component, DestroyRef, ElementRef, inject, OnInit, ViewChild } from '@angular/core';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatPaginator, MatPaginatorModule } from '@angular/material/paginator';
import { MatTableDataSource, MatTableModule } from '@angular/material/table';
import { AngularSplitModule, SplitComponent } from 'angular-split';
import * as L from 'leaflet';
import { debounceTime, distinctUntilChanged, fromEvent } from 'rxjs';
import { Content } from './content.model';
import { SearchService } from './search.service';

@Component({
  selector: 'lvzviz-search',
  templateUrl: './search.component.html',
  styleUrl: './search.component.css',
  animations: [
    trigger('detailExpand', [
      state('collapsed', style({ height: '0px' })),
      state('expanded', style({ height: '*' })),
      transition('expanded <=> collapsed', animate('225ms cubic-bezier(0.4, 0.0, 0.2, 1)')),
    ]),
  ],
  imports: [AngularSplitModule, MatFormFieldModule, MatInputModule, NgIf, MatTableModule, MatPaginatorModule, DatePipe],
  providers: [
    // { provide: SearchService, useClass: SearchServiceMock }
  ],
})
export class SearchComponent implements AfterViewInit, OnInit {
  private breakpointObserver = inject(BreakpointObserver);
  private searchService = inject(SearchService);

  displayedColumns: string[] = ['title', 'publication'];
  dataSource = new MatTableDataSource<Content>();

  isSmallSize = false;

  // eslint-disable-next-line @typescript-eslint/no-explicit-any
  expandedElement: any;

  totalElements = 0;
  author = '';
  searchTerm = '';

  destroyRef = inject(DestroyRef);

  private map!: L.Map;
  private markers!: L.FeatureGroup;

  @ViewChild('split') split!: SplitComponent;
  @ViewChild('input') input!: ElementRef;
  @ViewChild(MatPaginator) paginator!: MatPaginator;

  constructor() {
    this.breakpointObserver
      .observe([Breakpoints.XSmall, Breakpoints.Small, Breakpoints.Medium, Breakpoints.Large, Breakpoints.XLarge])
      .pipe(takeUntilDestroyed())
      .subscribe((result) => {
        for (const query of Object.keys(result.breakpoints)) {
          if (result.breakpoints[query]) {
            this.isSmallSize = query === Breakpoints.XSmall || query === Breakpoints.Small ? true : false;
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
    L.Icon.Default.imagePath = 'leaflet/';
  }

  ngAfterViewInit(): void {
    this.initMap();
    this.initSplit();
    this.initInput();

    this.loadPage(0, 5);
    this.paginator.page.subscribe(() => {
      this.loadPage(this.paginator.pageIndex, this.paginator.pageSize);
    });
  }

  /**
   * Loads content from backend with parameters which contains much more information and extract the content of articles.
   */
  loadPage(pageIndex: number, pageSize: number) {
    this.searchService.fetch(pageIndex, pageSize, 'datePublished,desc', this.searchTerm).subscribe((response) => {
      this.totalElements = response.totalElements;
      this.dataSource = new MatTableDataSource(response.content);
      this.addToMap(response.content);
    });
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
   * Initialize search field.
   */
  initInput(): void {
    fromEvent(this.input.nativeElement, 'keyup')
      .pipe(takeUntilDestroyed(this.destroyRef), debounceTime(500), distinctUntilChanged())
      .subscribe(() => {
        this.searchTerm = this.input.nativeElement.value;
        this.paginator.firstPage();
        this.loadPage(0, this.paginator.pageSize);
      });
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
        const marker = L.marker([c.coords.lat, c.coords.lon]).bindPopup(`<a href="${c.url}">${c.title}</a><br>${c.snippet}`);
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
    const article = element.article ? element.article : element.snippet;
    this.author = article.substring(article.lastIndexOf('.') + 2, article.length);
    return article.substring(0, article.lastIndexOf('.') + 1);
  }
}
