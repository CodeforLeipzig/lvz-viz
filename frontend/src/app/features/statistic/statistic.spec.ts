import { provideHttpClient, withInterceptorsFromDi } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { ComponentFixture, TestBed } from '@angular/core/testing';
import { Statistic } from './statistic';

describe('Statistic', () => {
  let component: Statistic;
  let fixture: ComponentFixture<Statistic>;
  let httpTestingController: HttpTestingController;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [Statistic],
      providers: [provideHttpClient(withInterceptorsFromDi()), provideHttpClientTesting()],
    }).compileComponents();

    httpTestingController = TestBed.inject(HttpTestingController);
    fixture = TestBed.createComponent(Statistic);
    component = fixture.componentInstance;
    fixture.detectChanges();
    await fixture.whenStable();

    // ngAfterViewInit loads leaflet.heat asynchronously before requesting the dates,
    // so wait for the requests and flush them before the test module is torn down
    const minmaxdate = await vi.waitFor(() => httpTestingController.expectOne('./api/minmaxdate'));
    const last7days = httpTestingController.expectOne('./api/last7days');
    minmaxdate.flush([
      { year: 2021, monthOfYear: 12, dayOfMonth: 1 },
      { year: 2021, monthOfYear: 12, dayOfMonth: 31 },
    ]);
    last7days.flush([
      { year: 2021, monthOfYear: 12, dayOfMonth: 25 },
      { year: 2021, monthOfYear: 12, dayOfMonth: 31 },
    ]);
    httpTestingController.expectOne((request) => request.url === './api/searchbetween').flush({ content: [] });
  });

  afterEach(() => {
    httpTestingController.verify();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
    expect(component.dataLoaded).toBe(true);
  });
});
