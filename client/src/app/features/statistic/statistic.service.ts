import { HttpClient, HttpParams } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { addDays, differenceInDays, toDate } from 'date-fns';
import { map, Observable } from 'rxjs';

import { environment } from 'src/environments/environment';
import { Content } from '../search/content.model';
import { DateTime } from './date-time.model';

@Injectable({
  providedIn: 'root'
})
export class StatisticService {

  constructor(private httpClient: HttpClient) { }

  fetchDates(query: string): Observable<DateTime[]> {
    return this.httpClient.get<DateTime[]>(environment.api + query);
  }

  fetch(from: number, to: number): Observable<Content[]> {
    const params = new HttpParams({ fromObject: { from: toDate(from).toISOString(), to: toDate(to).toISOString() } });
    return this.httpClient.get<any>(environment.api + 'searchbetween', { params }).pipe(
      map((data: any) => data.content),
    );
  }

  determineDateValue(date: DateTime): Date {
    return new Date(date.year, date.monthOfYear - 1, date.dayOfMonth);
  }

  generateSliderSteps(minValue: number, maxValue: number): number[] {
    const dateDiff = differenceInDays(maxValue, minValue) + 1;
    return [...Array(dateDiff).keys()].map(x => addDays(minValue, x).getTime());
  }
}
