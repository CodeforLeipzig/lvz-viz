import { BreakpointObserver, Breakpoints } from '@angular/cdk/layout';
import { DatePipe } from '@angular/common';
import { AfterViewInit, Component, effect, ElementRef, inject, OnDestroy, signal, viewChild } from '@angular/core';
import { toSignal } from '@angular/core/rxjs-interop';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatPaginator, MatPaginatorModule } from '@angular/material/paginator';
import { MatTableDataSource, MatTableModule } from '@angular/material/table';
import { SplitAreaComponent, SplitComponent } from 'angular-split';
import * as L from 'leaflet';
import { debounceTime, distinctUntilChanged, fromEvent, map, merge, startWith, Subject, switchMap, takeUntil, tap } from 'rxjs';
import { Content } from './content.model';
import { SearchService } from './search.service';

@Component({
  selector: 'app-search',
  templateUrl: './search.html',
  styleUrl: './search.css',
  imports: [SplitComponent, SplitAreaComponent, MatFormFieldModule, MatInputModule, MatTableModule, MatPaginatorModule, DatePipe],
  providers: [
    // { provide: SearchService, useClass: SearchServiceMock }
  ],
})
export class Search implements AfterViewInit, OnDestroy {
  private breakpointObserver = inject(BreakpointObserver);
  private searchService = inject(SearchService);

  // Signals for reactive state
  displayedColumns = signal<string[]>(['title', 'publication']);
  dataSource = signal(new MatTableDataSource<Content>());
  isSmallSize = signal(false);
  expandedElement = signal<Content | null>(null);
  length = signal(0);
  author = signal('');

  private destroyed = new Subject<void>();
  private map!: L.Map;
  private markers!: L.FeatureGroup;

  // Signal-based ViewChild references
  split = viewChild.required<SplitComponent>('split');
  input = viewChild.required<ElementRef>('input');
  paginator = viewChild.required<MatPaginator>(MatPaginator);

  constructor() {
    // Observe breakpoints and update isSmallSize signal
    const breakpointState = toSignal(
      this.breakpointObserver.observe([Breakpoints.XSmall, Breakpoints.Small, Breakpoints.Medium, Breakpoints.Large, Breakpoints.XLarge]),
      { requireSync: true }
    );

    effect(() => {
      const result = breakpointState();
      for (const query of Object.keys(result.breakpoints)) {
        if (result.breakpoints[query]) {
          this.isSmallSize.set(query === Breakpoints.XSmall || query === Breakpoints.Small);
        }
      }
    });

    // Initialize Leaflet icon path
    effect(() => {
      /**
       * workaround:
       * Images not loaded correctly: marker-shadow.png 404.
       * So images are copied from node_modules leaflet folder into assets folder.
       */
      L.Icon.Default.imagePath = 'leaflet/';
    }, { allowSignalWrites: false });
  }

  ngAfterViewInit(): void {
    this.initMap();
    this.initSplit();
    this.loadContent();
    // workaround: invalidate map size after view init to fix display issues with map in split area
    setTimeout(() => this.map.invalidateSize(), 0);
  }

  ngOnDestroy() {
    this.destroyed.next();
    this.destroyed.complete();
  }

  /**
   * Initialize split view.
   */
  private initSplit(): void {
    const splitComponent = this.split();
    splitComponent.dragProgress$.subscribe(() => {
      this.map.invalidateSize();
    });
  }

  /**
   * Loads content from backend with parameters which contains much more information and extract the content of articles.
   */
  private loadContent(): void {
    const inputElement = this.input();
    const paginatorElement = this.paginator();

    const filter = fromEvent(inputElement.nativeElement, 'keyup').pipe(
      debounceTime(500),
      distinctUntilChanged(),
      tap(() => paginatorElement.firstPage())
    );

    merge(filter, paginatorElement.page)
      .pipe(
        takeUntil(this.destroyed),
        startWith({}),
        switchMap(() => {
          return this.searchService
            .fetch(paginatorElement.pageIndex, paginatorElement.pageSize, 'datePublished,desc', inputElement.nativeElement.value)
            .pipe(
              map((data) => {
                this.length.set(data.length);
                return data;
              })
            );
        })
      )
      .subscribe((content) => {
        this.addToMap(content);
        this.dataSource.set(new MatTableDataSource(content));
      });
  }

  /**
   * Returns the query from the input field or undefined if the value is empty.
   *
   * @returns string | undefined
   */
  query(): string | undefined {
    const inputElement = this.input();
    const input = (inputElement.nativeElement.value as string).trim();
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
    const author = article.substring(article.lastIndexOf('.') + 2, article.length);
    this.author.set(author);
    return article.substring(0, article.lastIndexOf('.') + 1);
  }
}
