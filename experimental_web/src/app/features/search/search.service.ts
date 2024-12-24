import { HttpClient, HttpParams } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';

import { environment } from '../../../environments/environment';

@Injectable({
  providedIn: 'root',
})
export class SearchService {
  private httpClient = inject(HttpClient);


  // eslint-disable-next-line @typescript-eslint/no-explicit-any
  fetch(page: number, size: number, sort: string, query?: string): Observable<any> {
    const collected = { page, size, sort };
    const params = new HttpParams({ fromObject: query ? { ...collected, query } : collected });
    const url = query ? 'search' : 'getx';
    // eslint-disable-next-line @typescript-eslint/no-explicit-any
    return this.httpClient.get<any>(environment.api + url, { params });
  }
}
