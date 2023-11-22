import { HttpClient, HttpParams } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { addDays, differenceInDays, toDate } from 'date-fns';
import { Content } from 'leaflet';
import { Observable, map } from 'rxjs';

import { environment } from '../../../environments/environment';
import { DateTime } from '../statistic/date-time.model';

@Injectable({
  providedIn: 'root',
})
export class StatisticServiceMock {
  constructor(private httpClient: HttpClient) {}

  fetchDates(query: string): Observable<DateTime[]> {
    return this.httpClient.get<DateTime[]>(environment.api + query);
  }

  /**
   * This mock is used to override some parameters to get json server working.
   */
  fetch(from: number, to: number): Observable<Content[]> {
    const params = new HttpParams({
      fromObject: {
        datePublished_gte: toDate(from).toISOString().substring(0, 10),
        datePublished_lte: toDate(to).toISOString().substring(0, 10),
      },
    });
    // the correct URL is searchbetween instead of getx but in db.json this is the same object
    return this.httpClient.get<any>(`${environment.api}getx`, { params }).pipe(map((data: any) => data));
  }

  determineDateValue(date: DateTime): Date {
    return new Date(date.year, date.monthOfYear - 1, date.dayOfMonth);
  }

  generateSliderSteps(minValue: number, maxValue: number): number[] {
    const dateDiff = differenceInDays(maxValue, minValue) + 1;
    return [...Array(dateDiff).keys()].map((x) => addDays(minValue, x).getTime());
  }
}
