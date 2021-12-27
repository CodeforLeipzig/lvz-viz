import { HttpClientTestingModule, HttpTestingController } from '@angular/common/http/testing';
import { TestBed } from '@angular/core/testing';
import { Content } from '../search/content.model';
import { DateTime } from './date-time.model';

import { StatisticService } from './statistic.service';

describe('StatisticService', () => {
  let service: StatisticService;
  let httpMock: HttpTestingController;

  beforeEach(() => {
    TestBed.configureTestingModule({
      imports: [HttpClientTestingModule],
    });
    service = TestBed.inject(StatisticService);
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });

  it('should fetch dates with query minmaxdate', (done) => {
    httpMock = TestBed.inject(HttpTestingController);
    const mockResponse: DateTime[] = [
      {
        year: 2021,
        monthOfYear: 1,
        dayOfMonth: 1,
      },
      {
        year: 2021,
        monthOfYear: 12,
        dayOfMonth: 31,
      },
    ];
    service.fetchDates('minmaxdate').subscribe((response) => {
      expect(response).toEqual(mockResponse);
      done();
    });
    const mockRequest = httpMock.expectOne('./minmaxdate');
    mockRequest.flush(mockResponse);
  });

  it('should fetch dates with query last7days', (done) => {
    httpMock = TestBed.inject(HttpTestingController);
    const mockResponse: DateTime[] = [
      {
        year: 2021,
        monthOfYear: 12,
        dayOfMonth: 25,
      },
      {
        year: 2021,
        monthOfYear: 12,
        dayOfMonth: 31,
      },
    ];
    service.fetchDates('last7days').subscribe((response) => {
      expect(response).toEqual(mockResponse);
      done();
    });
    const mockRequest = httpMock.expectOne('./last7days');
    mockRequest.flush(mockResponse);
  });

  it('should fetch content', (done) => {
    httpMock = TestBed.inject(HttpTestingController);
    const mockResponse: Content[] = [
      {
        id: '5260376b8960373336f93f38b7a56d1877e0b974',
        url: 'https://www.lvz.de/Leipzig/Polizeiticker/Polizeiticker-Leipzig/Juwelier-Einbruch-in-Leipzig-Acht-Monate-spaeter-bleibt-ein-Teil-der-Beute-verschwunden',
        title: 'Juweliereinbruch in der City: Was aus dem Leipziger Millionencoup wurde',
        article:
          'Es war ein Einbruch wie in einem Actionthriller: Mit einem Auto hatten Profigangster in der Nacht zum 20. April dieses Jahres den gesicherten Eingang des Juweliergeschäftes in der Leipziger Petersstraße durchbrochen und einen Millionencoup gelandet.',
        snippet:
          'Was wurde eigentlich aus dem spektakulären Juweliereinbruch in der Leipziger Innenstadt im April 2021? Die LVZ hat herausgefunden, wie nach dem Millionencoup in der Petersstraße weiterging und wie es um die Beute steht.',
        copyright: '© Leipziger Verlags- und Druckereigesellschaft mbH & Co. KG',
        datePublished: '2021-12-26T16:58:16.000+0000',
        coords: {
          lat: 52.3644031,
          lon: 9.7379697,
          geohash: 'u1qcvsduz0hr',
          fragment: true,
        },
      },
    ];
    service.fetch(new Date(2021, 11, 20).getTime(), new Date(2021, 11, 27).getTime()).subscribe((response) => {
     expect(response).toEqual(mockResponse);
      done();
    });
    const mockRequest = httpMock.expectOne('./searchbetween?from=2021-12-19T23:00:00.000Z&to=2021-12-26T23:00:00.000Z');
    mockRequest.flush({ content: mockResponse});
  });

  it('should determine date from dateTime object', () => {
    // DateTime object with just needed properties, be aware of monthOfYear, it is not zero based
    const dateTime = {
      year: 2021,
      monthOfYear: 12,
      dayOfMonth: 14,
    };
    expect(service.determineDateValue(dateTime)).toEqual(new Date(2021, 11, 14));
  });

  it('should generate slidersteps from min and max value', () => {
    const minDate = new Date(2021, 11, 1).getTime();
    const maxDate = new Date(2021, 11, 5).getTime();
    const steps = [1638313200000, 1638399600000, 1638486000000, 1638572400000, 1638658800000];
    expect(service.generateSliderSteps(minDate, maxDate)).toEqual(steps);
  });
});
