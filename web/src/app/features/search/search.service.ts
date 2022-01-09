import { HttpClient, HttpParams } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';

import { environment } from 'src/environments/environment';

@Injectable({
  providedIn: 'root'
})
export class SearchService {

  constructor(private httpClient: HttpClient) { }

  fetch(page: number, size: number, sort: string, query?: string): Observable<any> {
    const params = new HttpParams({ fromObject: query ? { page, size, sort, query } : { page, size, sort } });
    const url = query ? 'search' : 'getx';
    return this.httpClient.get<any>(environment.api + url, { params });
  }
}
