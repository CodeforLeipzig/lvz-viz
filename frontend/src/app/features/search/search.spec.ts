import { provideHttpClient, withInterceptorsFromDi } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { ComponentFixture, TestBed } from '@angular/core/testing';
import { MatPaginator } from '@angular/material/paginator';
import { By } from '@angular/platform-browser';
import { MAX_RESULT_WINDOW, Search } from './search';

describe('Search', () => {
  let component: Search;
  let fixture: ComponentFixture<Search>;

  beforeEach(() => {
    TestBed.configureTestingModule({
      imports: [Search],
      providers: [provideHttpClient(withInterceptorsFromDi()), provideHttpClientTesting()],
    }).compileComponents();

    fixture = TestBed.createComponent(Search);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('should sort by newest publication by default', () => {
    expect(component.sort()).toBe('datePublished,desc');
  });

  it('should sort by oldest publication and reset to first page', () => {
    component.page.set(3);
    component.onSortChange({ active: 'publication', direction: 'asc' });
    expect(component.sort()).toBe('datePublished,asc');
    expect(component.page()).toBe(0);
  });

  it('should cap paginator length at the result window', async () => {
    TestBed.inject(HttpTestingController)
      .expectOne((req) => req.url.endsWith('getx'))
      .flush({ content: [], totalElements: 20178 });
    await fixture.whenStable();
    fixture.detectChanges();

    const paginator = fixture.debugElement.query(By.directive(MatPaginator)).componentInstance as MatPaginator;
    expect(component.totalElements()).toBe(20178);
    expect(paginator.length).toBe(MAX_RESULT_WINDOW);
  });
});
