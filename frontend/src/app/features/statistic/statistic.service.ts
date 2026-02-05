import { HttpClient, HttpParams } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { addDays, differenceInDays, toDate } from 'date-fns';
import { Observable, map } from 'rxjs';
import { environment } from '../../../environments/environment';
import { Content } from '../search/models/content.model';
import { DateTime } from './date-time.model';

@Injectable({
  providedIn: 'root',
})
export class StatisticService {
  private httpClient = inject(HttpClient);

  fetchDates(query: string): Observable<DateTime[]> {
    return this.httpClient.get<DateTime[]>(environment.api + query);
  }

  fetch(from: number, to: number): Observable<Content[]> {
    const params = new HttpParams({ fromObject: { from: toDate(from).toISOString(), to: toDate(to).toISOString() } });
    // eslint-disable-next-line @typescript-eslint/no-explicit-any
    return this.httpClient.get<any>(environment.api + 'searchbetween', { params }).pipe(map((data: any) => data.content));
  }

  /**
   * Returns a date created from a DateTime object.
   *
   * @param date
   * @returns Date
   */
  determineDateValue(date: DateTime): Date {
    return new Date(date.year, date.monthOfYear - 1, date.dayOfMonth);
  }

  /**
   * Returns the steps for the slider from dates as number as an array of number.
   *
   * @param minValue
   * @param maxValue
   * @returns number[]
   */
  generateSliderSteps(minValue: number, maxValue: number): number[] {
    const dateDiff = differenceInDays(maxValue, minValue) + 1;
    return [...Array(dateDiff).keys()].map((x) => addDays(minValue, x).getTime());
  }
}
