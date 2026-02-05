import { BreakpointObserver, Breakpoints } from '@angular/cdk/layout';
import { DatePipe } from '@angular/common';
import { httpResource } from '@angular/common/http';
import { AfterViewInit, Component, computed, effect, inject, OnInit, signal, viewChild } from "@angular/core";
import { toSignal } from '@angular/core/rxjs-interop';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatPaginatorModule, PageEvent } from '@angular/material/paginator';
import { MatTableModule } from '@angular/material/table';
import { SplitAreaComponent, SplitComponent } from 'angular-split';
import * as L from 'leaflet';
import { debounceTime, distinctUntilChanged, Subject } from 'rxjs';
import { Content } from './models/content.model';
import { PagedResponse } from './models/paged-response.model';
import { SearchService } from "./search.service";

@Component({
  selector: 'app-search',
  templateUrl: './search.html',
  styleUrl: './search.css',
  imports: [MatFormFieldModule, MatInputModule, MatPaginatorModule, MatTableModule, SplitAreaComponent, SplitComponent, DatePipe],
  providers: [
    // { provide: SearchService, useClass: SearchServiceMock }
  ],
})
export class Search implements OnInit, AfterViewInit {

  #searchService = inject(SearchService);
  #breakpointObserver = inject(BreakpointObserver);

  #map!: L.Map;
  #markers!: L.FeatureGroup;
  #search$ = new Subject<string>();
  // eslint-disable-next-line @typescript-eslint/no-explicit-any
  expandedElement: any;
  author = '';

  // Signal-based ViewChild references
  split = viewChild.required<SplitComponent>('split');

  // Signals for reactive state
  isSmallSize = signal(false);

  displayedColumns = signal<string[]>(['title', 'publication']);
  readonly page = signal(0);
  readonly size = signal(5);
  readonly sort = signal('');
  readonly query = signal<string | undefined>(undefined);
  #tempContent = signal<Content[]>([]);

  readonly searchResource = httpResource<PagedResponse<Content[]>>(() =>
    this.#searchService.fetch(
      this.page(),
      this.size(),
      this.sort(),
      this.query()
    )
  );

  readonly dataSource = computed(() => {
    const page = this.searchResource.value();
    const content = this.searchResource.isLoading() ? this.#tempContent() : page?.content.flat() ?? [];
    // if content is available and map is initialized, add markers to map
    if (content && this.#map) {
      this.#addToMap(content);
    }
    return content;
  });

  readonly totalElements = computed(
    () => this.searchResource.isLoading() ? this.#tempContent().length : this.searchResource.value()?.totalElements ?? 0
  );

  constructor() {
    // Observe breakpoints and update isSmallSize signal
    const breakpointState = toSignal(
      this.#breakpointObserver.observe([Breakpoints.XSmall, Breakpoints.Small, Breakpoints.Medium, Breakpoints.Large, Breakpoints.XLarge]),
      { requireSync: true }
    );

    effect((): void => {
      const result = breakpointState();
      for (const query of Object.keys(result.breakpoints)) {
        if (result.breakpoints[query]) {
          break; // Exit after first match to avoid unnecessary overwrites
        }
      }
    });

    effect((): void => {
      const data = this.searchResource.value();
      if (data) {
        this.#tempContent.set(data.content.flat())
      };
    });

    this.#search$
      .pipe(debounceTime(500), distinctUntilChanged())
      .subscribe((value) => {
        this.page.set(0);
        this.query.set(value || undefined);
      });
  }

  ngOnInit(): void {
    // Workaround:
    // Images not loaded correctly: marker-shadow.png 404.
    // So images are copied from node_modules leaflet folder into assets folder.
    L.Icon.Default.imagePath = 'leaflet/';
  }

  ngAfterViewInit(): void {
    this.#initMap();
    this.#initSplit();

    // Workaround:
    // Invalidate map size after map initialization to fix display issues with map in split area.
    setTimeout(() => this.#map.invalidateSize(), 0);
  }

  /**
   * Initialize map.
   */
  #initMap(): void {
    this.#map = L.map('map-search').setView([51.339695, 12.373075], 11);

    L.tileLayer('https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png', {
      attribution: '&copy; <a href="http://www.openstreetmap.org/copyright">OpenStreetMap</a>',
    }).addTo(this.#map);
  }

  /**
   * Initialize split view.
   */
  #initSplit(): void {
    this.split().dragProgress$.subscribe(() => {
      this.#map.invalidateSize();
    });
  }

  /**
   * Integrates content as marker and popup information returned from the server into the initialized map.
   *
   * @param content
   */
  #addToMap(content: Content[]): void {
    if (this.#markers) {
      this.#map.removeLayer(this.#markers);
    }
    this.#markers = new L.FeatureGroup();
    content.forEach((c) => {
      if (c.coords) {
        const marker = L.marker([c.coords.lat, c.coords.lon]).bindPopup(`<a href="${c.url}">${c.title}</a><br>${c.snippet}`);
        this.#markers.addLayer(marker);
      }
    });
    this.#map.addLayer(this.#markers);
  }

  /**
   * UI Handlers.
   */
  onSearchInput(value: string) {
    this.#search$.next(value);
  }

  onPageChange(event: PageEvent) {
    this.page.set(event.pageIndex);
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
