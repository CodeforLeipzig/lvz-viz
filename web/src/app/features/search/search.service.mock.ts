import { HttpClient, HttpParams } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { map, Observable } from 'rxjs';

import { environment } from 'src/environments/environment';

@Injectable({
  providedIn: 'root',
})
export class SearchServiceMock {
  constructor(private httpClient: HttpClient) {}

  fetch(_page: number, _limit: number, _sort: string, q?: string): Observable<any> {
    const collected = { _page: _page + 1, _limit, _sort, _order: _sort.slice(_sort.indexOf(',') + 1) };
    const params = new HttpParams({ fromObject: q ? { ...collected, q } : collected });

    return this.httpClient.get<any>(`${environment.api}getx`, { params }).pipe(
      map((result) => {
        const content = { content: result };
        const totalElements = q ? content.content.length : 25;
        return { ...content, totalElements };
      })
    );
  }
}
