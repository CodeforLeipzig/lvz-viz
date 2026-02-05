import { HttpParams, HttpResourceRequest } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { environment } from '../../../environments/environment';

@Injectable({
  providedIn: 'root',
})
export class SearchService {
  fetch(page: number, size: number, sort: string, query?: string): HttpResourceRequest {
    const collected = { page, size, sort };
    const params = new HttpParams({ fromObject: query ? { ...collected, query } : collected });
    const url = query ? 'search' : 'getx';
    return {
      url: environment.api + url,
      method: 'GET',
      params,
    };
  }
}
