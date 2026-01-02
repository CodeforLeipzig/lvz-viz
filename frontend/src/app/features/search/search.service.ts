import { HttpClient, HttpParams } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';
import { Content } from './content.model';
import { PagedResponse } from './paged-response.model';

@Injectable({
  providedIn: 'root',
})
export class SearchService {
  private httpClient = inject(HttpClient);

  fetch(page: number, size: number, sort: string, query?: string): Observable<PagedResponse<Content>> {
    const collected = { page, size, sort };
    const params = new HttpParams({ fromObject: query ? { ...collected, query } : collected });
    const url = query ? 'search' : 'getx';
    return this.httpClient.get<PagedResponse<Content>>(environment.api + url, { params });
  }
}
