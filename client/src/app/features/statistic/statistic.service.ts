import { HttpClient, HttpParams } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';

import { environment } from 'src/environments/environment';
import { DateTime } from './date-time.model';

@Injectable({
  providedIn: 'root'
})
export class StatisticService {

  constructor(private httpClient: HttpClient) { }

  fetchDates(query: string): Observable<DateTime[]> {
    return this.httpClient.get<DateTime[]>(environment.api + query);
  }

  fetch(from: string, to: string): Observable<any> {
    const params = new HttpParams({ fromObject: { from, to } });
    return this.httpClient.get<any>(environment.api + 'searchbetween', { params });
  }
}
